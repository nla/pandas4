package pandas.util;

import org.springframework.security.crypto.codec.Hex;

import java.util.Arrays;
import java.util.List;

public class MimeTypeSniffer {
    private final static List<MimeTypeSniffer> imageSniffers = List.of(
            new MimeTypeSniffer("image/gif", "47 49 46 38 39 61"),
            new MimeTypeSniffer("image/gif", "47 49 46 38 37 61"),
            new MimeTypeSniffer("image/png", "89 50 4E 47 0D 0A 1A 0A"),
            new MimeTypeSniffer("image/jpeg", "FF D8 FF"),
            new MimeTypeSniffer("image/webp", "52 49 46 46 00 00 00 00 57 45 42 50 56 50", "FF FF FF FF 00 00 00 00 FF FF FF FF FF FF"),
            new MimeTypeSniffer("image/svg+xml", "3C 73 76 67"),
            new MimeTypeSniffer("image/svg+xml", "3C 3F 78 6D 6C"));

    private final String mimeType;
    private final byte[] signature;
    private final byte[] mask;

    public MimeTypeSniffer(String mimeType, String signature, String mask) {
        this.mimeType = mimeType;
        this.signature = Hex.decode(signature.replace(" ", ""));
        this.mask = Hex.decode(mask.replace(" ", ""));
    }

    public MimeTypeSniffer(String mimeType, String signature) {
        this.mimeType = mimeType;
        this.signature = Hex.decode(signature.replace(" ", ""));
        this.mask = new byte[this.signature.length];
        Arrays.fill(mask, (byte) 0xff);
    }

    public boolean matches(byte[] buffer) {
        if (buffer.length < signature.length) return false;
        for (int i = 0; i < signature.length; i++) {
            if ((buffer[i] & mask[i]) != signature[i]) return false;
        }
        return true;
    }

    public static String sniffImageType(byte[] buffer) {
        for (var sniffer : imageSniffers) {
            if (sniffer.matches(buffer)) {
                return sniffer.mimeType;
            }
        }
        return null;
    }
}
