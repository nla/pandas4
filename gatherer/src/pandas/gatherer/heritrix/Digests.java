package pandas.gatherer.heritrix;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static java.nio.charset.StandardCharsets.UTF_8;

class Digests {
    static String sha256(Path path) throws IOException, NoSuchAlgorithmException {
        byte[] buf = new byte[8192];
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream stream = Files.newInputStream(path)) {
            while (true) {
                int n = stream.read(buf);
                if (n < 0) break;
                digest.update(buf, 0, n);
            }
        }
        return hexEncode(digest.digest());
    }

    static String md5(String s) {
        try {
            return hexEncode(MessageDigest.getInstance("md5").digest(s.getBytes(UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private static String hexEncode(byte[] bytes) {
        String alphabet = "0123456789abcdef";
        char[] chars = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            chars[i * 2] = alphabet.charAt((bytes[i] & 0xFF) >>> 4);
            chars[i * 2 + 1] = alphabet.charAt(bytes[i] & 0xf);
        }
        return new String(chars);
    }

}
