package pandas.util;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Blob;
import java.sql.SQLException;

public class ServletUtils {
    public static void sendBlobAsImage(Blob blob, HttpServletResponse response) throws SQLException, IOException {
        try (var stream = blob.getBinaryStream()) {
            byte[] buffer = new byte[(int)Math.min(8192, blob.length())];
            int n = stream.read(buffer);
            String mimeType = MimeTypeSniffer.sniffImageType(buffer);
            if (mimeType != null) {
                response.setContentType(mimeType);
            }
            response.setContentLengthLong(blob.length());
            response.addHeader("Cache-Control", "max-age=86400");
            ServletOutputStream outputStream = response.getOutputStream();
            outputStream.write(buffer, 0, n);
            stream.transferTo(outputStream);
        }
    }
}
