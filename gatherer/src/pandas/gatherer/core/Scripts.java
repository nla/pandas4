package pandas.gatherer.core;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.*;

@Component
public class Scripts {
    private final Map<String, String> environment;
    private final Path postGatherScript;
    private final Path archiveMoveScript;
    private final Path archivePreserveScript;
    private final Path globalReplaceScript;
    private final Path uploadProcessScript;

    public Scripts(Config config) {
        this.environment = buildEnvMap(config);
        Path scriptsDir = config.getScriptsDir();
        postGatherScript = checkExists(scriptsDir.resolve("post_gather"));
        archiveMoveScript = checkExists(scriptsDir.resolve("archive_move"));
        archivePreserveScript = checkExists(scriptsDir.resolve("archive_preserve"));
        globalReplaceScript = checkExists(scriptsDir.resolve("global_replace"));
        uploadProcessScript = checkExists(scriptsDir.resolve("upload_process"));
    }

    private Path checkExists(Path path) {
        if (!Files.exists(path)) {
            throw new UncheckedIOException(new NoSuchFileException(path.toString()));
        }
        return path;
    }

    public void postGather(long pi, String dateString) throws IOException, InterruptedException {
        runScript(postGatherScript, pi, dateString);
    }

    public void archiveMove(long pi, String dateString) throws IOException, InterruptedException {
        runScript(archiveMoveScript, pi, dateString);
    }

    public void archivePreserve(long pi, String dateString) throws IOException, InterruptedException {
        runScript(archivePreserveScript, pi, dateString);
    }

    public void globalReplace(long pi, String dateString, String... args) throws IOException, InterruptedException {
        runScript(globalReplaceScript, pi, dateString, args);
    }

    public void uploadProcess(long pi, String dateString) throws IOException, InterruptedException {
        runScript(uploadProcessScript, pi, dateString);
    }

    private void runScript(Path script, long pi, String dateString, String... args) throws IOException, InterruptedException {
        List<String> command = new ArrayList<>();
        command.add(script.toString());
        command.add(String.valueOf(pi));
        command.add(dateString);
        Collections.addAll(command, args);

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.environment().putAll(environment);
        pb.redirectErrorStream(true);
        pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        Process p = pb.start();
        int exitCode = p.waitFor();
        if (exitCode != 0) {
            throw new IOException(script + " returned " + exitCode);
        }
    }

    private static Map<String, String> buildEnvMap(Config config) {
        Map<String, String> map = new HashMap<>();
        map.put("PANDAS_SCRIPTS", config.getScriptsDir().toAbsolutePath().toString());
        map.put("PANDAS_WORKING", config.getWorkingDir().toAbsolutePath().toString());
        map.put("PANDAS_MASTER", config.getMastersDir().toAbsolutePath().toString());
        map.put("PANDAS_REPO1", config.getRepo1Dir().toAbsolutePath().toString());
        map.put("PANDAS_REPO2", config.getRepo2Dir().toAbsolutePath().toString());
        map.put("CLASSPATH", System.getProperty("java.class.path"));
        return Collections.unmodifiableMap(map);
    }
}
