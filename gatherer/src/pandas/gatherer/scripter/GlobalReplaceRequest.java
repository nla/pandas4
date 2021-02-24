package pandas.gatherer.scripter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Path;
import java.util.regex.Pattern;

import static java.lang.ProcessBuilder.Redirect.PIPE;
import static java.nio.charset.StandardCharsets.UTF_8;

public class GlobalReplaceRequest implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(GlobalReplaceRequest.class);

    private String email;
    long instanceId;
    private String directory;
    private String filename;
    private String find;
    private String replaceWith;
    private boolean regexMode;
    private boolean recursive;
    private boolean reportMode;
    Path root;
    Long pi;

    public void run() {
        Process p = null;
        try {
            p = new ProcessBuilder("mail", "-s", "PANDAS: Global replacement report for PI " + pi,
                    "-r", "PANDAS <noreply@nla.gov.au>", email).inheritIO().redirectInput(PIPE).start();
            Writer out = new BufferedWriter(new OutputStreamWriter(p.getOutputStream(), UTF_8));
            out.write("*** This is an automated message - please do not reply to this address ***\n\n");
            out.write("----------------------------------------------------\n");
            out.write("Title Pi: $pi\nInstance: $datestamp\nCommand: $cmdline\n");
            out.write("----------------------------------------------------\n\n");
            try {
                if (replaceWith == null) replaceWith = "";
                int maxDepth = recursive ? Integer.MAX_VALUE : 1;
                Pattern pattern = Pattern.compile(regexMode ? find : Pattern.quote(find));
                GlobalReplace.Report report = GlobalReplace.globrep(root, maxDepth, filename, pattern,
                        reportMode ? null : replaceWith, out);
                out.write(report.toString());
            } catch (Exception e) {
                out.write("An error occurred.\n\n");
                e.printStackTrace(new PrintWriter(out));
                log.error("Exception running " + this, e);
            }
            out.flush();
            out.close();
            p.waitFor();
        } catch (IOException | InterruptedException e) {
            log.error("Exception running " + this, e);
            if (p != null) p.destroyForcibly();
        }
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setInstanceId(long instanceId) {
        this.instanceId = instanceId;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public void setFind(String find) {
        this.find = find;
    }

    public void setReplaceWith(String replaceWith) {
        this.replaceWith = replaceWith;
    }

    public void setRegexMode(boolean regexMode) {
        this.regexMode = regexMode;
    }
    public void setRecursive(boolean recursive) {
        this.recursive = recursive;
    }

    public void setReportMode(boolean reportMode) {
        this.reportMode = reportMode;
    }

    @Override
    public String toString() {
        return "GlobalReplaceRequest{" +
                "email='" + email + '\'' +
                ", instanceId=" + instanceId +
                ", directory='" + directory + '\'' +
                ", filename='" + filename + '\'' +
                ", find='" + find + '\'' +
                ", replaceWith='" + replaceWith + '\'' +
                ", regexMode=" + regexMode +
                ", recursive=" + recursive +
                ", reportMode=" + reportMode +
                ", root=" + root +
                ", pi=" + pi +
                '}';
    }
}
