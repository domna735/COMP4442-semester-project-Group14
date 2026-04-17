package hk.polyu.comp4442.cloudcompute.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

@Service
public class FileScanService {

    private final boolean scanEnabled;
    private final String dockerImage;
    private final int timeoutSeconds;

    public FileScanService(@Value("${file.scan.enabled:false}") boolean scanEnabled,
                           @Value("${file.scan.docker-image:clamav/clamav:stable}") String dockerImage,
                           @Value("${file.scan.timeout-seconds:30}") int timeoutSeconds) {
        this.scanEnabled = scanEnabled;
        this.dockerImage = dockerImage;
        this.timeoutSeconds = timeoutSeconds;
    }

    public void scanOrThrow(Path stagedFile) {
        if (!scanEnabled) {
            return;
        }

        String volumeBinding = stagedFile.toAbsolutePath() + ":/scan/file:ro";
        ProcessBuilder processBuilder = new ProcessBuilder(
                "docker",
                "run",
                "--rm",
                "--network",
                "none",
                "-v",
                volumeBinding,
                dockerImage,
                "clamscan",
                "--no-summary",
                "/scan/file");
        processBuilder.redirectErrorStream(true);

        try {
            Process process = processBuilder.start();
            boolean completed = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
            if (!completed) {
                process.destroyForcibly();
                throw new IllegalStateException("File scan timed out.");
            }

            int exitCode = process.exitValue();
            if (exitCode != 0) {
                throw new IllegalArgumentException("Uploaded file failed security scan.");
            }
        } catch (IOException ex) {
            throw new IllegalStateException("File scan command failed to start.", ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("File scan interrupted.", ex);
        }
    }
}
