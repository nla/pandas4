package pandas.gatherer.heritrix;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pandas.gatherer.core.FileStats;

import javax.net.ssl.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.atomic.AtomicReference;

import static java.nio.charset.StandardCharsets.UTF_8;

public class HeritrixClient {
    private final static Logger log = LoggerFactory.getLogger(HeritrixClient.class);
    private final URI uri;
    private final XmlMapper xmlMapper = new XmlMapper();
    private final SSLSocketFactory socketFactory;
    private final AtomicReference<DigestChallenge> lastChallenge = new AtomicReference<>();
    private final String username;
    private final String password;
    private int pollDelay = 1000;
    private int timeoutMillis = 60000;

    public HeritrixClient(String url, String username, String password) {
        if (!url.endsWith("/")) {
            url += "/";
        }
        this.uri = URI.create(url);
        this.socketFactory = sslContextWhichIgnoresCertificate().getSocketFactory();
        this.username = username;
        this.password = password;
    }

    private SSLContext sslContextWhichIgnoresCertificate() {
        try {
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, new TrustManager[]{new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            }}, null);
            return sslContext;
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException(e);
        }
    }

    private <T> T sendRequest(String method, URI uri, String[] keysAndValues, Class<T> responseClass) throws IOException {
        DigestChallenge challenge = lastChallenge.get();
        for (int tries = 0; tries < 3; tries++) {
            HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
            if (connection instanceof HttpsURLConnection https) {
                https.setHostnameVerifier((hostname, session) -> true);
                https.setSSLSocketFactory(socketFactory);
            }
            connection.setInstanceFollowRedirects(false); // automatic redirect handling won't preserve auth header
            connection.setRequestMethod(method);
            connection.setRequestProperty("User-Agent", "pandas-gatherer");
            connection.setRequestProperty("Accept", "application/xml");
            if (challenge != null) {
                connection.setRequestProperty("Authorization", challenge.authorize(username, password, method, uri.getPath()));
            }

            if (keysAndValues != null) {
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream(), UTF_8))) {
                    for (int i = 0; i < keysAndValues.length; i += 2) {
                        if (i != 0) out.append('&');
                        out.append(URLEncoder.encode(keysAndValues[i], "utf-8"));
                        out.append('=');
                        out.append(URLEncoder.encode(keysAndValues[i + 1], "utf-8"));
                    }
                }
            }
            try (InputStream body = connection.getInputStream()) {
                if (connection.getResponseCode() == 303) {
                    URI location = uri.resolve(connection.getHeaderField("Location"));
                    return sendRequest("GET", location, null, responseClass);
                }
                return xmlMapper.readValue(body, responseClass);
            } catch (IOException e) {
                String authenticate = connection.getHeaderField("WWW-Authenticate");
                if (connection.getResponseCode() == 401 && authenticate != null && authenticate.startsWith("Digest ")) {
                    challenge = DigestChallenge.parse(authenticate);
                    lastChallenge.set(challenge);
                } else {
                    throw e;
                }
            }
        }
        throw new IOException("Authentication failed: " + method + " " + uri);
    }

    private <T> T sendPost(URI uri, Class<T> responseClass, String... keysAndValues) throws IOException {
        return sendRequest("POST", uri, keysAndValues, responseClass);
    }

    private <T> T sendGet(URI uri, Class<T> responseClass) throws IOException {
        return sendRequest("GET", uri, null, responseClass);
    }

    private Engine callEngine(String... keysAndValues) throws IOException {
        return sendPost(uri, Engine.class, keysAndValues);
    }

    private Job callJob(String jobName, String... keysAndValues) throws IOException {
        return sendPost(uri.resolve("job/" + jobName), Job.class, keysAndValues);
    }

    void addJobDir(Path addpath) throws IOException {
        callEngine("action", "add", "addpath", addpath.toString());
    }

    void buildJob(String jobName) throws IOException {
        log.info("HeritrixClient.buildJob(\"{}\")", jobName);
        callJob(jobName, "action", "build");
    }

    @SuppressWarnings("BusyWait")
    void launchJob(String jobName) throws IOException {
        log.info("HeritrixClient.launchJob(\"{}\")", jobName);
        int tries = 0;
        while (true) {
            try {
                callJob(jobName, "action", "launch");
                break;
            } catch (JsonParseException e) {
                if (tries >= 3) {
                    throw e;
                }
                tries++;
                long delay = 5000L * tries;
                log.warn("Failed to launch " + jobName + " waiting " + delay + "ms to retry (attempt " + tries + ")");
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    void unpauseJob(String jobName) throws IOException {
        log.info("HeritrixClient.unpauseJob(\"{}\")", jobName);
        callJob(jobName, "action", "unpause");
    }

    @SuppressWarnings("BusyWait")
    void teardownJob(String jobName) throws IOException {
        log.info("HeritrixClient.teardownJob(\"{}\")", jobName);
        for (int tries = 0; tries < 10; tries++) {
            try {
                long delay = 100;
                while (getJob(jobName).crawlControllerState == State.STOPPING) {
                    log.warn("Teardown requested but Heritrix " + jobName + " STOPPING, waiting " + delay + "ms");
                    try {
                        Thread.sleep(delay);
                        delay *= 2;
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                callJob(jobName, "action", "teardown");
                break;
            } catch (FileNotFoundException e) {
                log.warn("Job not found for teardown: {}", jobName);
                return;
            } catch (IOException e) {
                if (e.getMessage().contains("500 for URL")) {
                    try {
                        Thread.sleep((1 << tries) * 1000);
                    } catch (InterruptedException e2) {
                        throw e;
                    }
                } else {
                    throw e;
                }
            }
        }
    }

    Job getJob(String jobName) throws IOException {
        return sendGet(uri.resolve("job/" + jobName), Job.class);
    }

    public Engine getEngine() throws IOException {
        return sendGet(uri, Engine.class);
    }

    void waitForStateTransition(String jobName, State allowed, State target) throws IOException, InterruptedException {
        long start = System.currentTimeMillis();
        while (true) {
            State state = getJob(jobName).crawlControllerState;
            if (state == target) {
                break;
            } else if (state != allowed) {
                throw new IllegalStateException("Heritrix job " + jobName + " in state " + state + " but expected " + target + " or " + allowed);
            } else if (System.currentTimeMillis() - start > timeoutMillis) {
                throw new RuntimeException("Timed out waiting for Heritrix to prepapre job " + jobName);
            }
            Thread.sleep(pollDelay);
        }
    }

    public static void main(String args[]) throws Exception {
        HeritrixClient heritrix = new HeritrixClient("https://localhost:8444/engine", "admin", "admin");

        System.out.println(heritrix.getJob("bogus"));

    }



    @SuppressWarnings({"WeakerAccess", "unused"})
    public enum State {
        NASCENT, RUNNING, EMPTY, PAUSED, PAUSING, STOPPING, FINISHED, PREPARING
    }

    @SuppressWarnings({"WeakerAccess", "unused"})
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Engine {
        public String heritrixVersion;
    }

    @SuppressWarnings({"WeakerAccess", "unused"})
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Job {
        public State crawlControllerState;
        public UriTotalsReport uriTotalsReport;
        public SizeTotalsReport sizeTotalsReport;

        public FileStats fileStats() {
            return new FileStats(uriTotalsReport.downloadedUriCount, sizeTotalsReport.total);
        }
    }

    @SuppressWarnings({"WeakerAccess", "unused"})
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UriTotalsReport {
        public long downloadedUriCount;
        public long queuedUriCount;
        public long totalUriCount;
        public long futureUriCount;
    }

    @SuppressWarnings({"WeakerAccess", "unused"})
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SizeTotalsReport {
        public long total;
    }
}
