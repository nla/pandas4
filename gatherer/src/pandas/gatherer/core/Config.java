package pandas.gatherer.core;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "gatherer")
@EnableConfigurationProperties
public class Config {

    private final List<String> errors = new ArrayList<>();
    private Path workingDir;
    private Path scriptsDir;
    private Path repo1Dir;
    private Path repo2Dir;
    private Path mastersDir;
    private Path uploadDir;
    private Path pywbDir;
    private Path pywbDataDir;
    private int pywbPort = 10910;
    private int uploadWorkers = 2;
    private int scriptWorkers = 2;
    private final int gatherStatsPollSeconds = 60;
    private Integer webPort = null;
    private String webBindAddress = "0.0.0.0";
    private String contextPath = "/";
    private String bambooDbUrl;
    private String bambooDbUser;
    private String bambooDbPassword;
    private String dossUrl;
    private String oidcUrl;
    private String oidcClientId;
    private String oidcClientSecret;
    private Path legacyScripts;

    public Config() {
    }

    public void loadFromEnvironment(Map<String,String> env) {
        Path pandasHome = Paths.get(env.getOrDefault("PANDAS_HOME", "data")).toAbsolutePath();
        setWorkingDir(pandasHome.resolve("working"));
        setScriptsDir(pandasHome.resolve("scripts"));
        setMastersDir(pandasHome.resolve("master"));
        setRepo1Dir(pandasHome.resolve("repo1"));
        setRepo2Dir(pandasHome.resolve("repo2"));
        setPywbDir(pandasHome.resolve("pywb"));
        setPywbDataDir(getPywbDir().resolve("data"));
        setUploadDir(pandasHome.resolve("upload"));

        // to allow running from a source checkout with no options
        if (!Files.exists(getScriptsDir()) && Files.exists(Paths.get("PandasPerlScripts"))) {
            setScriptsDir(Paths.get("PandasPerlScripts"));
        }

        setWorkingDir(Paths.get(env.getOrDefault("PANDAS_WORKING", getWorkingDir().toString())));
        setScriptsDir(Paths.get(env.getOrDefault("PANDAS_SCRIPTS", getScriptsDir().toString())));
        setMastersDir(Paths.get(env.getOrDefault("PANDAS_MASTERS", getMastersDir().toString())));
        setRepo1Dir(Paths.get(env.getOrDefault("PANDAS_REPO1", getRepo1Dir().toString())));
        setRepo2Dir(Paths.get(env.getOrDefault("PANDAS_REPO2", getRepo2Dir().toString())));
        setPywbDir(Paths.get(env.getOrDefault("PYWB", getPywbDir().toString())));
        setPywbDataDir(Paths.get(env.getOrDefault("PYWB_DATA", getPywbDataDir().toString())));
        setPywbPort(Integer.parseInt(env.getOrDefault("PYWB_PORT", String.valueOf(getPywbPort()))));

        if (env.containsKey("PORT")) {
            setWebPort(Integer.parseInt(env.get("PORT")));
        }
        setWebBindAddress(env.getOrDefault("BIND_ADDRESS", getWebBindAddress()));
        setContextPath(env.getOrDefault("CONTEXT_PATH", getContextPath()));

        setUploadWorkers(Integer.parseInt(env.getOrDefault("UPLOAD_WORKERS", Integer.toString(getUploadWorkers()))));
        setScriptWorkers(Integer.parseInt(env.getOrDefault("SCRIPT_WORKERS", Integer.toString(getScriptWorkers()))));

        setBambooDbUrl(env.get("BAMBOO_DB_URL"));
        setBambooDbUser(env.get("BAMBOO_DB_USER"));
        setBambooDbPassword(env.get("BAMBOO_DB_PASSWORD"));
        setDossUrl(env.getOrDefault("DOSS_URL", "trivial:" + pandasHome.resolve("blobs")));
        setOidcUrl(env.get("OIDC_URL"));
        setOidcClientId(env.get("OIDC_CLIENT_ID"));
        setOidcClientSecret(env.get("OIDC_CLIENT_SECRET"));
    }

    public void validate() {
        getErrors().clear();

        checkExists("PANDAS_WORKING", getWorkingDir());
        checkExists("PANDAS_SCRIPTS", getScriptsDir());
        checkExists("PANDAS_MASTERS", getMastersDir());
        checkExists("PANDAS_REPO1", getRepo1Dir());
        checkExists("PANDAS_REPO2", getRepo2Dir());

        if (!getErrors().isEmpty()) {
            throw new RuntimeException("Invalid configuration: " + String.join("\n", getErrors()));
        }
    }

    private void checkExists(String name, Path path) {
        if (!Files.exists(path)) {
            getErrors().add(name + " does not exist: " + path);
        }
    }

    public List<String> getErrors() {
        return errors;
    }

    public Path getWorkingDir() {
        return workingDir;
    }

    public void setWorkingDir(Path workingDir) {
        this.workingDir = workingDir;
    }

    public Path getScriptsDir() {
        return scriptsDir;
    }

    public void setScriptsDir(Path scriptsDir) {
        this.scriptsDir = scriptsDir;
    }

    public Path getRepo1Dir() {
        return repo1Dir;
    }

    public void setRepo1Dir(Path repo1Dir) {
        this.repo1Dir = repo1Dir;
    }

    public Path getRepo2Dir() {
        return repo2Dir;
    }

    public void setRepo2Dir(Path repo2Dir) {
        this.repo2Dir = repo2Dir;
    }

    public Path getMastersDir() {
        return mastersDir;
    }

    public void setMastersDir(Path mastersDir) {
        this.mastersDir = mastersDir;
    }

    public Path getPywbDir() {
        return pywbDir;
    }

    public void setPywbDir(Path pywbDir) {
        this.pywbDir = pywbDir;
    }

    public Path getPywbDataDir() {
        return pywbDataDir;
    }

    public void setPywbDataDir(Path pywbDataDir) {
        this.pywbDataDir = pywbDataDir;
    }

    public int getUploadWorkers() {
        return uploadWorkers;
    }

    public void setUploadWorkers(int uploadWorkers) {
        this.uploadWorkers = uploadWorkers;
    }

    public int getScriptWorkers() {
        return scriptWorkers;
    }

    public void setScriptWorkers(int scriptWorkers) {
        this.scriptWorkers = scriptWorkers;
    }

    public int getGatherStatsPollSeconds() {
        return gatherStatsPollSeconds;
    }

    public Integer getWebPort() {
        return webPort;
    }

    public void setWebPort(Integer webPort) {
        this.webPort = webPort;
    }

    public String getWebBindAddress() {
        return webBindAddress;
    }

    public void setWebBindAddress(String webBindAddress) {
        this.webBindAddress = webBindAddress;
    }

    public String getContextPath() {
        return contextPath;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    public String getBambooDbUrl() {
        return bambooDbUrl;
    }

    public void setBambooDbUrl(String bambooDbUrl) {
        this.bambooDbUrl = bambooDbUrl;
    }

    public String getBambooDbUser() {
        return bambooDbUser;
    }

    public void setBambooDbUser(String bambooDbUser) {
        this.bambooDbUser = bambooDbUser;
    }

    public String getBambooDbPassword() {
        return bambooDbPassword;
    }

    public void setBambooDbPassword(String bambooDbPassword) {
        this.bambooDbPassword = bambooDbPassword;
    }

    public String getDossUrl() {
        return dossUrl;
    }

    public void setDossUrl(String dossUrl) {
        this.dossUrl = dossUrl;
    }

    public String getOidcUrl() {
        return oidcUrl;
    }

    public void setOidcUrl(String oidcUrl) {
        this.oidcUrl = oidcUrl;
    }

    public String getOidcClientId() {
        return oidcClientId;
    }

    public void setOidcClientId(String oidcClientId) {
        this.oidcClientId = oidcClientId;
    }

    public String getOidcClientSecret() {
        return oidcClientSecret;
    }

    public void setOidcClientSecret(String oidcClientSecret) {
        this.oidcClientSecret = oidcClientSecret;
    }

    public Path getUploadDir() {
        return uploadDir;
    }

    public void setUploadDir(Path uploadDir) {
        this.uploadDir = uploadDir;
    }

    public Path getMimeDir() {
        return getWorkingDir().getParent().resolve("mime");
    }

    public Path getLegacyScripts() {
        return legacyScripts;
    }

    public void setLegacyScripts(Path legacyScripts) {
        this.legacyScripts = legacyScripts;
    }

    public int getPywbPort() {
        return pywbPort;
    }

    public void setPywbPort(int pywbPort) {
        this.pywbPort = pywbPort;
    }
}
