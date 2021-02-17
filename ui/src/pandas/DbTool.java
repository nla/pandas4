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
import java.util.*;

public class DbTool {
    public static void main(String args[]) throws SQLException, IOException, JsonParserException {
        if (args.length < 2 || System.getenv("PANDAS_DB_URL") == null) {
            usage();
        }
        Properties properties = new Properties();
        properties.put("user", System.getenv("PANDAS_DB_USER"));
        properties.put("password", System.getenv("PANDAS_DB_PASSWORD"));
        properties.put("defaultRowPrefetch", 500);
        try (Connection connection = DriverManager.getConnection(System.getenv("PANDAS_DB_URL"), properties)) {
            switch (args[0]) {
                case "dump":
                    dump(connection, Paths.get(args[1]));
                    break;
                case "load":
                    if (!System.getenv("PANDAS_DB_URL").startsWith("jdbc:h2:")) {
                        System.err.println("Only loading into h2 allowed to prevent mishaps");
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
        int batchSize = 5000;
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
                            break;
                        case "columns":
                            json.array();
                            while (json.next()) {
                                columns.add(json.string());
                            }
                            break;
                        case "rows":
                            System.out.println(table);
                            String columnsWithCommas = String.join(", ", columns);
                            String placeholders = String.join(",", Collections.nCopies(columns.size(), "?"));
                            String sql = "INSERT INTO " + table + " (" + columnsWithCommas + ") VALUES (" + placeholders + ")";
                            System.out.println(sql);

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

                            System.out.println(columnsWithCommas);

                            try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM " + table)) {
                                stmt.executeLargeUpdate();
                            }

                            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                                json.array();
                                int row;
                                for (row = 1; json.next(); row++) {
                                    json.array();
                                    for (int col = 1; json.next(); col++) {
                                        if (columnTypes[col - 1] == Types.BLOB) {
                                            stmt.setBytes(col, Base64.getDecoder().decode(json.string()));
                                        } else if (columnTypes[col - 1] == Types.DATE) {
                                            String value = json.string();
                                            if (value != null && value.contains(" ")) { // strip times from date fields
                                                value = value.replaceFirst(" .*", "");
                                            }
                                            stmt.setString(col, value);
                                        } else if (columnTypes[col - 1] == Types.TIMESTAMP) {
                                            String value = json.string();
                                            if (value != null && value.startsWith("-")) { // workaround for crazy negative timestamps
                                                value = "1987-01-01 00:00:00";
                                            }
                                            stmt.setString(col, value);
                                        } else {
                                            stmt.setString(col, json.string());
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
                            json.value(Base64.getEncoder().encodeToString(rs.getBytes(col)));
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
}
