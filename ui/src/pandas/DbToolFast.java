package pandas;

import com.googlecode.flyway.core.Flyway;
import com.grack.nanojson.JsonAppendableWriter;
import com.grack.nanojson.JsonParserException;
import com.grack.nanojson.JsonReader;
import com.grack.nanojson.JsonWriter;
import com.mysql.cj.jdbc.MysqlDataSource;
import pandas.core.FlywayConfig;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.*;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/*
 * Dump / load pandas tables.  Tables are dumped, one per file, with provided prefix.
 * Multithreaded load, commit per batch or table.
 */
public class DbToolFast {
    private static final Pattern SANE_TABLE_NAME = Pattern.compile("[a-zA-Z0-9_]+");
    private final String jdbcUrl;
    private final String quoteChar;
    private final Connection connection;

    public DbToolFast(Connection connection) throws SQLException {
        this.connection = connection;
        this.jdbcUrl = connection.getMetaData().getURL();
        if (jdbcUrl.startsWith("jdbc:mysql:") || jdbcUrl.startsWith("jdbc:mariadb:")) {
            quoteChar = "`";
        } else {
            quoteChar = "";
        }
    }

    private static boolean isMySQL(String jdbcUrl) {
        return jdbcUrl.startsWith("jdbc:mysql:") || jdbcUrl.startsWith("jdbc:mariadb:");
    }

    public static void main(String args[])
            throws SQLException, IOException, JsonParserException, ExecutionException, InterruptedException {

        String dbUrl = System.getenv("PANDAS_DB_URL");

        if (args.length < 2 || dbUrl == null) {
            usage();
        }
        Properties properties = new Properties();
        properties.put("user", System.getenv("PANDAS_DB_USER"));
        properties.put("password", System.getenv("PANDAS_DB_PASSWORD"));
        if (isMySQL(dbUrl)) {
            // ensure DATETIME columns are stored in UTC to avoid DST ambiguity
            properties.put("connectionTimeZone", "UTC");
            properties.put("preserveInstants", true);
            properties.put("forceConnectionTimeZoneToSession", true);
        } else {
            properties.put("defaultRowPrefetch", 500);
        }
        try (Connection connection = DriverManager.getConnection(dbUrl, properties)) {
            var dbTool = new DbToolFast(connection);
            switch (args[0]) {
                case "dump":
                    dump(connection, args[1]);
                    break;
                case "load":
                    if (!dbUrl.startsWith("jdbc:h2:") &&
                        !dbUrl.startsWith("jdbc:postgresql:") &&
                        !dbUrl.startsWith("jdbc:mysql:") &&
                        !dbUrl.startsWith("jdbc:mariadb:")) {
                        System.err.println("Only loading into h2, postgresql, mysql or mariadb allowed to prevent mishaps");
                        System.exit(1);
                    }
                    dbTool.migrate();

                    LoadOptions loadOptions = parseLoadOptions(Arrays.copyOfRange(args, 1, args.length));
                    DbToolFast.processAll(loadOptions.fileNames, loadOptions.commitEveryBatch, loadOptions.preserveNextGatherDates);
                    break;
                default:
                    usage();
            }
        }
    }

    private record LoadOptions(String[] fileNames, boolean commitEveryBatch, boolean preserveNextGatherDates) {}

    private static LoadOptions parseLoadOptions(String[] args) {
        boolean commitEveryBatch = false;
        boolean preserveNextGatherDates = false;
        List<String> fileNames = new ArrayList<>();

        for (String arg : args) {
            if ("--commit-every-batch".equals(arg)) {
                commitEveryBatch = true;
            } else if ("--preserve-next-gather-dates".equals(arg)) {
                preserveNextGatherDates = true;
            } else if (arg.startsWith("--")) {
                usage();
            } else {
                fileNames.add(arg);
            }
        }

        if (fileNames.isEmpty()) {
            usage();
        }

        return new LoadOptions(fileNames.toArray(String[]::new), commitEveryBatch, preserveNextGatherDates);
    }

    private static void processAll(String[] fileNames, boolean commitEveryBatch, boolean preserveNextGatherDates)
            throws ExecutionException, InterruptedException {

        int maxThreads = Integer.parseInt(System.getenv().getOrDefault("PANDAS_LOAD_THREADS", String.valueOf(5)));
        maxThreads = Math.min(maxThreads, fileNames.length);

        ExecutorService executor = Executors.newFixedThreadPool(maxThreads);
        List<Future<?>> futures = new ArrayList<>();

        for (String fileName : fileNames) {
            futures.add(executor.submit(() -> {
                try {
                    Properties properties = new Properties();
                    properties.put("user", System.getenv("PANDAS_DB_USER"));
                    properties.put("password", System.getenv("PANDAS_DB_PASSWORD"));
                    String dbUrl = System.getenv("PANDAS_DB_URL");
                    if (isMySQL(dbUrl)) {
                        properties.put("connectionTimeZone", "UTC");
                        properties.put("preserveInstants", true);
                        properties.put("forceConnectionTimeZoneToSession", true);
                    } else {
                        properties.put("defaultRowPrefetch", 500);
                    }
                    try (Connection conn = DriverManager.getConnection(dbUrl, properties)) {
                        DbToolFast tool = new DbToolFast(conn);
                        tool.load(Path.of(fileName), commitEveryBatch, preserveNextGatherDates);
                    }
                } catch (Exception e) {
                    System.err.println("Exception loading %s (failed)".formatted(fileName));
                    throw new RuntimeException(e);
                }
            }));
        }

        List<Throwable> failures = new ArrayList<>();
        try {
            for (java.util.concurrent.Future<?> f : futures) {
                try {
                    f.get();
                } catch (ExecutionException e) {
                    failures.add(e.getCause());
                }
            }
        } finally {
            executor.shutdown();
        }

        if (!failures.isEmpty()) {
            RuntimeException failure = new RuntimeException(failures.size() + " table load(s) failed!");
            failures.forEach(failure::addSuppressed);
            throw new ExecutionException(failure);
        }
    }

    private void migrate() throws SQLException {
        String dbType = FlywayConfig.determineDatabaseType(jdbcUrl);
        if (dbType == null) return;

        Flyway flyway = new Flyway();
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setUrl(System.getenv("PANDAS_DB_URL"));
        dataSource.setUser(System.getenv("PANDAS_DB_USER"));
        dataSource.setPassword(System.getenv("PANDAS_DB_PASSWORD"));
        flyway.setDataSource(dataSource);
        flyway.setLocations("classpath:pandas/migrations/" + dbType);
        flyway.migrate();
    }

    private static void usage() {
        System.err.println(
            "Usage: DbTool dump <outfile_prefix>\n" +
            "   or: DbTool load [--commit-every-batch] [--preserve-next-gather-dates] <infile>...\n" +
            "Set env vars PANDAS_DB_URL, PANDAS_DB_USER, PANDAS_DB_PASSWORD (optionally PANDAS_LOAD_THREADS)\n"
        );
        System.exit(1);
    }

    private String quote(String s) {
        return quoteChar + s + quoteChar;
    }

    private void load(Path infile, boolean commitEveryBatch, boolean preserveNextGatherDates)
            throws IOException, JsonParserException, SQLException {

        boolean lowercase = jdbcUrl.startsWith("jdbc:postgresql:")
                            || jdbcUrl.startsWith("jdbc:mysql:")
                            || jdbcUrl.startsWith("jdbc:mariadb:");
        int batchSize = 5000;

        connection.setAutoCommit(false);
        if (jdbcUrl.startsWith("jdbc:postgresql:")) {
            connection.prepareStatement("SET session_replication_role = replica").execute();
        } else if (jdbcUrl.startsWith("jdbc:mysql:") || jdbcUrl.startsWith("jdbc:mariadb:")) {
            connection.prepareStatement("SET foreign_key_checks = 0").execute();
            connection.prepareStatement("SET unique_checks = 0").execute();
            // Allow inserting 0 into AUTO_INCREMENT columns
            connection.prepareStatement("SET SESSION sql_mode = CONCAT(@@sql_mode, ',NO_AUTO_VALUE_ON_ZERO')").execute();
        }

        try (InputStream stream = Files.newInputStream(infile)) {
            JsonReader json = JsonReader.from(stream);
            json.array();
            while (json.next()) {
                json.object();

                String table = null;
                List<String> columns = new ArrayList<>();
                while (json.next()) {
                    switch (json.key()) {
                        case "table":
                            table = json.string();
                            if (lowercase) {
                                table = table.toLowerCase(Locale.ROOT);
                            }
                            break;
                        case "columns":
                            json.array();
                            while (json.next()) {
                                columns.add(lowercase ? json.string().toLowerCase(Locale.ROOT) : json.string());
                            }
                            break;
                        case "rows":
                            sanityCheckTableName(table);
                            String columnsWithCommas = columns.stream().map(this::quote).collect(Collectors.joining(", "));
                            String placeholders = String.join(",", Collections.nCopies(columns.size(), "?"));
                            String sql = "INSERT INTO " + quote(table) + " (" + columnsWithCommas + ") VALUES (" + placeholders + ")";

                            int[] columnTypes = lookupColumnTypes(connection, table, columns);
                            if (columnTypes == null) {
                                System.err.println("Unable to get columns for " + table + " maybe it doesn't exist. Skipping.");
                                json.array();
                                while (json.next()) {
                                    json.array();
                                    while (json.next()) {
                                    }
                                }
                                break;
                            }

                            System.out.println(table);

                            try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM " + quote(table))) {
                                long rows = stmt.executeLargeUpdate();
                                System.out.println("deleted " + rows);
                            }

                            try (PreparedStatement stmt = connection.prepareStatement(sql)) {

                                json.array();
                                int batchCount = 0;
                                int row;
                                for (row = 1; json.next(); row++) {
                                    json.array();
                                    for (int col = 1; json.next(); col++) {
                                        switch (columnTypes[col - 1]) {
                                            case Types.BLOB:
                                            case Types.BINARY:
                                            case Types.VARBINARY:
                                            case Types.LONGVARBINARY: {
                                                String value = json.string();
                                                stmt.setBytes(col, value != null ? Base64.getDecoder().decode(json.string()) : null);
                                                break;
                                            }
                                            case Types.DATE: {
                                                String value = json.string();
                                                if (value != null && value.contains(" ")) { // strip times from date fields
                                                    value = value.replaceFirst(" .*", "");
                                                }
                                                stmt.setString(col, value);
                                                break;
                                            }
                                            case Types.TIMESTAMP: {
                                                String value = json.string();
                                                if (value != null && value.startsWith("-")) { // workaround for crazy negative timestamps
                                                    value = "1987-01-01 00:00:00";
                                                }
                                                if (value == null) {
                                                    stmt.setNull(col, columnTypes[col - 1]);
                                                } else {
                                                    try {
                                                        Instant instant = Instant.parse(value);
                                                        stmt.setObject(col, Timestamp.from(instant));
                                                    } catch (DateTimeParseException e) {
                                                        System.err.println("Bogus instant: " + value + " on " + table + " row " + row);
                                                        stmt.setNull(col, columnTypes[col - 1]);
                                                    }
                                                }
                                                break;
                                            }
                                            case Types.BIT:
                                            case Types.BOOLEAN: {
                                                Object value = json.value();
                                                if (value == null) {
                                                    stmt.setNull(col, columnTypes[col - 1]);
                                                } else {
                                                    stmt.setBoolean(col, value.equals("1") || value.equals((Long) 1L));
                                                }
                                                break;
                                            }
                                            case Types.INTEGER: {
                                                Object value = json.value();
                                                if (value == null) {
                                                    stmt.setNull(col, columnTypes[col - 1]);
                                                } else if (value instanceof String) {
                                                    stmt.setInt(col, Integer.parseInt((String) value));
                                                } else {
                                                    stmt.setInt(col, (Integer) value);
                                                }
                                                break;
                                            }
                                            case Types.BIGINT: {
                                                Object value = json.value();
                                                if (value == null) {
                                                    stmt.setNull(col, columnTypes[col - 1]);
                                                } else if (value instanceof String) {
                                                    stmt.setLong(col, Long.parseLong((String) value));
                                                } else {
                                                    stmt.setLong(col, (Long) value);
                                                }
                                                break;
                                            }
                                            default:
                                                stmt.setObject(col, json.value());
                                                break;
                                        }
                                    }
                                    stmt.addBatch();
                                    batchCount++;

                                    if (batchCount >= batchSize) {
                                        int[] result = stmt.executeBatch();
                                        stmt.clearBatch();

                                        if (commitEveryBatch) {
                                            connection.commit();
                                        }

                                        batchCount = 0;
                                        System.err.println("Inserted " + result.length + " rows into " + table);
                                    }
                                }
                                if (batchCount > 0) {
                                    int[] result = stmt.executeBatch();
                                    stmt.clearBatch();
                                    System.err.println("Inserted " + result.length + " rows into " + table);
                                }
                                connection.commit();
                            }
                            if (!preserveNextGatherDates && "title_gather".equals(table)) {
                                try (PreparedStatement stmt = connection.prepareStatement("update " + quote(table) + " set next_gather_date = null")) {
                                    stmt.execute();
                                }
                                connection.commit();
                            }
                            break;
                    }
                }
            }
        }
    }

    private static int[] lookupColumnTypes(Connection connection, String table, List<String> columns) throws SQLException {
        int[] columnTypes = new int[columns.size()];
        boolean ok = false;
        try (ResultSet rs = connection.getMetaData().getColumns(null, null, table, null)) {
            while (rs.next()) {
                int i = columns.indexOf(rs.getString("COLUMN_NAME"));
                if (i < 0) continue;
                columnTypes[i] = rs.getInt("DATA_TYPE");
                ok = true;
                System.out.println("Table %s.%s data_type: %d".formatted(table, columns.get(i), columnTypes[i]));
            }
        }
        if (!ok) return null;
        return columnTypes;
    }


    private static void dump(Connection connection, String outfile) throws IOException, SQLException {
        String catalog = System.getenv().getOrDefault("CATALOG", null);
        String schema  = System.getenv().getOrDefault("SCHEMA", "PANDAS3");

        Set<String> excludedTables = Stream.concat(
                Stream.of("schema_version", "PLAN_TABLE"),
                Arrays.stream(System.getenv().getOrDefault("IGNORE_TABLES", "").split(","))
                        .filter(s -> !s.isBlank())
        ).collect(Collectors.toSet());

        List<String> tableNames = new ArrayList<>();
        if (System.getenv().containsKey("TABLES")) {
            tableNames.addAll(Arrays.asList(System.getenv("TABLES").split(",")));
        } else {
            try (ResultSet tables = connection.getMetaData().getTables(catalog, schema, null, new String[]{"TABLE"})) {
                while (tables.next()) {

                    String s = "%s\t%s\t%s".formatted(
                                    tables.getString("TABLE_CAT"),
                                    tables.getString("TABLE_SCHEM"),
                                    tables.getString("TABLE_NAME")
                    );
                    System.out.println(s);

                    tableNames.add(tables.getString("TABLE_NAME"));
                }
            }
        }
        System.err.println(tableNames);

        for (String tableName : tableNames) {
            if (excludedTables.contains(tableName)) {
                System.err.println("Skipping " + tableName);
                continue;
            }
            sanityCheckTableName(tableName);
            System.err.println("Dumping " + tableName);

            JsonAppendableWriter json = JsonWriter.indent("  ")
                    .on(Files.newBufferedWriter(Paths.get("%s_%s".formatted(outfile, tableName)), StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE))
                    .array();

            json.object();
            json.value("table", tableName);

            String qualifiedName = schema.isBlank() ? tableName : schema + "." + tableName;
            try (PreparedStatement stmt = connection.prepareStatement("select * from " + qualifiedName)) {

                stmt.setFetchSize(10000);
                ResultSet rs = stmt.executeQuery();
                ResultSetMetaData metadata = rs.getMetaData();
                json.array("columns");
                int columnCount = metadata.getColumnCount();
                for (int col = 1; col <= columnCount; col++) {
                    json.value(metadata.getColumnName(col));
                }
                json.end();

                int c = 0;
                json.array("rows");
                while (rs.next()) {
                    json.array();
                    for (int col = 1; col <= columnCount; col++) {
                        int columnType = metadata.getColumnType(col);
                        if (columnType == Types.TIMESTAMP) {
                            var timestamp = rs.getTimestamp(col);
                            if (timestamp == null) {
                                json.nul();
                            } else {
                                json.value(timestamp.toInstant().toString());
                            }
                        } else if (columnType == Types.BLOB ||
                                   columnType == Types.BINARY ||
                                   columnType == Types.VARBINARY ||
                                   columnType == Types.LONGVARBINARY) {
                            byte[] bytes = rs.getBytes(col);
                            if (bytes == null) {
                                json.nul();
                            } else {
                                json.value(Base64.getEncoder().encodeToString(bytes));
                            }
                        } else {
                            json.value(rs.getString(col));
                        }
                    }
                    json.end();

                    c++;
                    if (c % 10000 == 0) {
                        System.out.println("(%s: %d rows written)".formatted(tableName, c));
                    }
                }
                json.end();
            }
            json.end();
            json.end().done();
        }

    }

    private static void sanityCheckTableName(String tableName) throws SQLException {
        if (!SANE_TABLE_NAME.matcher(tableName).matches()) {
            throw new SQLException("Bogus table name: " + tableName);
        }
    }
}
