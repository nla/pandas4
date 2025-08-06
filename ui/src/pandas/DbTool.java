package pandas;

import com.grack.nanojson.JsonAppendableWriter;
import com.grack.nanojson.JsonParserException;
import com.grack.nanojson.JsonReader;
import com.grack.nanojson.JsonWriter;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Pattern;

public class DbTool {
    private static Pattern SANE_TABLE_NAME = Pattern.compile("[a-zA-Z0-9_]+");

    public static void main(String args[]) throws SQLException, IOException, JsonParserException {
        String dbUrl = System.getenv("PANDAS_DB_URL");
        if (args.length < 2 || dbUrl == null) {
            usage();
        }
        Properties properties = new Properties();
        properties.put("user", System.getenv("PANDAS_DB_USER"));
        properties.put("password", System.getenv("PANDAS_DB_PASSWORD"));
        properties.put("defaultRowPrefetch", 500);
        try (Connection connection = DriverManager.getConnection(dbUrl, properties)) {
            switch (args[0]) {
                case "dump":
                    dump(connection, Paths.get(args[1]));
                    break;
                case "load":
                    if (!dbUrl.startsWith("jdbc:h2:") && !dbUrl.startsWith("jdbc:postgresql:")) {
                        System.err.println("Only loading into h2 or postgresql allowed to prevent mishaps");
                        System.exit(1);
                    }
                    load(connection, Paths.get(args[1]));
                    break;
                default:
                    usage();
            }
        }
    }

    private static void usage() {
        System.err.println("Usage: DbTool dump <outfile>");
        System.err.println("   or: DbTool load <infile>");
        System.err.println("\nSet env vars PANDAS_DB_URL, PANDAS_DB_USER, PANDAS_DB_PASSWORD");
        System.exit(1);
    }

    private static void load(Connection connection, Path infile) throws IOException, JsonParserException, SQLException {
        boolean lowercase = connection.getMetaData().getURL().startsWith("jdbc:postgresql:");
        int batchSize = 5000;
        connection.setAutoCommit(false);
        connection.prepareStatement("SET session_replication_role = replica").execute();

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
                            String columnsWithCommas = String.join(", ", columns);
                            String placeholders = String.join(",", Collections.nCopies(columns.size(), "?"));
                            String sql = "INSERT INTO " + table + " (" + columnsWithCommas + ") VALUES (" + placeholders + ")";

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

                            try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM " + table)) {
                                long rows = stmt.executeLargeUpdate();
                                System.out.println("deleted " + rows);
                            }

                            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                                json.array();
                                int row;
                                for (row = 1; json.next(); row++) {
                                    json.array();
                                    for (int col = 1; json.next(); col++) {
                                        switch (columnTypes[col - 1]) {
                                            case Types.BLOB:
                                            case Types.BINARY:
                                                stmt.setBytes(col, Base64.getDecoder().decode(json.string()));
                                                break;
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
                                                        stmt.setObject(col, LocalDateTime.parse(value.replace(' ', 'T')));
                                                    } catch (DateTimeParseException e) {
                                                        System.err.println("Bogus date: " + value);
                                                        stmt.setNull(col, columnTypes[col - 1]);
                                                    }
                                                }
                                                //stmt.setString(col, value);
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

                                    if (row % batchSize == 0) {
                                        int[] result = stmt.executeBatch();
                                        System.err.println("Inserted " + result.length + " rows into " + table);
                                    }
                                }
                                if (row % batchSize != 0) {
                                    int[] result = stmt.executeBatch();
                                    System.err.println("Inserted " + result.length + " rows into " + table);
                                }
                            }
                            break;
                    }
                }
            }
        }
        try (PreparedStatement stmt = connection.prepareStatement("update TITLE_GATHER set NEXT_GATHER_DATE = null")) {
            stmt.execute();
        }
        connection.commit();
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
            }
        }
        if (!ok) return null;
        return columnTypes;
    }

    private static void dump(Connection connection, Path outfile) throws IOException, SQLException {
        Set<String> excludedTables = new HashSet<>(Arrays.asList(System.getenv().getOrDefault("IGNORE_TABLES", "THUMBNAIL,STATUS_HISTORY,schema_version").split(",")));
        JsonAppendableWriter json = JsonWriter.indent("  ").on(Files.newBufferedWriter(outfile, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE))
                .array();

        List<String> tableNames = new ArrayList<>();
        try (ResultSet tables = connection.getMetaData().getTables(null, "PANDAS3", null, new String[]{"TABLE"})) {
            while (tables.next()) {
                tableNames.add(tables.getString("TABLE_NAME"));
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
            json.object();
            json.value("table", tableName);
            try (PreparedStatement stmt = connection.prepareStatement("select * from " + tableName)) {
                stmt.setFetchSize(10000);
                ResultSet rs = stmt.executeQuery();
                ResultSetMetaData metadata = rs.getMetaData();
                json.array("columns");
                int columnCount = metadata.getColumnCount();
                for (int col = 1; col <= columnCount; col++) {
                    json.value(metadata.getColumnName(col));
                }
                json.end();

                json.array("rows");
                while (rs.next()) {
                    json.array();
                    for (int col = 1; col <= columnCount; col++) {
                        if (metadata.getColumnType(col) == Types.BLOB) {
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
                }
                json.end();
            }
            json.end();
        }
        json.end().done();
    }

    private static void sanityCheckTableName(String tableName) throws SQLException {
        if (!SANE_TABLE_NAME.matcher(tableName).matches()) {
            throw new SQLException("Bogus table name: " + tableName);
        }
    }
}
