package pandas;

import com.grack.nanojson.JsonAppendableWriter;
import com.grack.nanojson.JsonWriter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.*;
import java.util.*;

public class DbTool {
    public static void main(String args[]) throws SQLException, IOException {
        if (args.length == 0 || System.getenv("PANDAS_DB_URL") == null) {
            usage();
        }
        Properties properties = new Properties();
        properties.put("user", System.getenv("PANDAS_DB_USER"));
        properties.put("password", System.getenv("PANDAS_DB_PASSWORD"));
        properties.put("defaultRowPrefetch", 500);
        try (Connection connection = DriverManager.getConnection(System.getenv("PANDAS_DB_URL"), properties)) {
            switch (args[1]) {
                case "dump":
                    dump(connection);
                    break;
                default:
                    usage();
            }
        }
    }

    private static void usage() {
        System.err.println("Usage: DbTool dump");
        System.err.println("\nSet env vars PANDAS_DB_URL, PANDAS_DB_USER, PANDAS_DB_PASSWORD");
        System.exit(1);
    }

    private static void dump(Connection connection) throws IOException, SQLException {
        Set<String> excludedTables = Set.of("THUMBNAIL", "schema_version");
        JsonAppendableWriter json = JsonWriter.indent("  ").on(Files.newBufferedWriter(Paths.get("/tmp/pandas.json"), StandardOpenOption.TRUNCATE_EXISTING))
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
