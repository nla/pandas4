package pandas.gatherer.heritrix;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DigestChallengeTest {
    @Test
    public void testDigestAuth() throws IOException {
        DigestChallenge challenge = DigestChallenge.parse("Digest realm=\"Authentication Required\", domain=\"/\", nonce=\"MTU2NjE0NTMxMzYwODo2M2U5YzNhM2ZjNzA3ODA3N2IwYzQ4OTc0NThjOWMwYw==\", algorithm=MD5, qop=\"auth\"");
        assertEquals("Authentication Required", challenge.realm);
        assertEquals("MTU2NjE0NTMxMzYwODo2M2U5YzNhM2ZjNzA3ODA3N2IwYzQ4OTc0NThjOWMwYw==", challenge.nonce);
        assertEquals("MD5", challenge.algorithm);
        assertEquals("Digest username=\"admin\", realm=\"Authentication Required\", uri=\"/\", nonce=\"MTU2NjE0NTMxMzYwODo2M2U5YzNhM2ZjNzA3ODA3N2IwYzQ4OTc0NThjOWMwYw==\", response=\"b598f7d88b3ea33a2710f32f8b7d4073\"", challenge.authorize("admin", "admin", "GET", "/"));
    }
}