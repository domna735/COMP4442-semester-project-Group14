package hk.polyu.comp4442.cloudcompute;

import hk.polyu.comp4442.cloudcompute.security.CustomUserDetails;
import hk.polyu.comp4442.cloudcompute.service.FileScanService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Locale;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/files")
@CrossOrigin(origins = "*")
public class FileController {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "txt", "pdf", "png", "jpg", "jpeg", "gif", "csv", "json", "md", "zip");
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "application/pdf",
            "application/json",
            "application/zip",
            "application/x-zip-compressed");

    private final Path fileStorageLocation;
    private final long maxUploadSizeBytes;
    private final FileScanService fileScanService;

    public FileController(@Value("${file.upload-dir}") String uploadDir,
                          @Value("${file.upload.max-size-bytes:5242880}") long maxUploadSizeBytes,
                          FileScanService fileScanService) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.maxUploadSizeBytes = maxUploadSizeBytes;
        this.fileScanService = fileScanService;
        try {
            Files.createDirectories(this.fileStorageLocation);
            Files.createDirectories(resolveQuarantineDirectory());
        } catch (Exception ex) {
            throw new RuntimeException("Could not create the directory to store files.", ex);
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@AuthenticationPrincipal CustomUserDetails userDetails,
                                             @RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("File is empty.");
            }

            if (file.getSize() > maxUploadSizeBytes) {
                return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                        .body("File exceeds max allowed size.");
            }

            String safeOriginalName = sanitizeFilename(file.getOriginalFilename());
            if (!isAllowedExtension(safeOriginalName)) {
                return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                        .body("File type is not allowed.");
            }

            Path userDirectory = resolveUserDirectory(userDetails);
            Path quarantineDirectory = resolveQuarantineDirectory();

            String storedName = UUID.randomUUID() + "-" + safeOriginalName;
            Path targetLocation = userDirectory.resolve(storedName).normalize();
            if (!targetLocation.startsWith(userDirectory)) {
                return ResponseEntity.badRequest().body("Invalid file path.");
            }

            Path stagedFile = quarantineDirectory.resolve(storedName).normalize();
            if (!stagedFile.startsWith(quarantineDirectory)) {
                return ResponseEntity.badRequest().body("Invalid file path.");
            }

            Files.copy(file.getInputStream(), stagedFile, StandardCopyOption.REPLACE_EXISTING);

            if (!isAllowedMimeType(stagedFile)) {
                Files.deleteIfExists(stagedFile);
                return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                        .body("File content type is not allowed.");
            }

            fileScanService.scanOrThrow(stagedFile);
            Files.move(stagedFile, targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return ResponseEntity.ok("File uploaded successfully: " + storedName);
        } catch (IOException ex) {
            return ResponseEntity.badRequest().body("Could not upload file: " + ex.getMessage());
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("File scan service unavailable.");
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(ex.getMessage());
        }
    }

    @GetMapping("/list")
    public ResponseEntity<List<String>> listFiles(@AuthenticationPrincipal CustomUserDetails userDetails) {
        List<String> filenames = new ArrayList<>();
        File folder = resolveUserDirectory(userDetails).toFile();
        File[] listOfFiles = folder.listFiles();
        
        if (listOfFiles != null) {
            for (File file : listOfFiles) {
                if (file.isFile()) {
                    filenames.add(file.getName());
                }
            }
        }
        return ResponseEntity.ok(filenames);
    }

    @GetMapping("/download/{filename:.+}")
    public ResponseEntity<Resource> downloadFile(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                 @PathVariable String filename) {
        try {
            String safeFilename = sanitizeFilename(filename);
            Path userDirectory = resolveUserDirectory(userDetails);
            Path filePath = userDirectory.resolve(safeFilename).normalize();
            if (!filePath.startsWith(userDirectory)) {
                return ResponseEntity.badRequest().build();
            }

            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException ex) {
            return ResponseEntity.badRequest().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    private Path resolveUserDirectory(CustomUserDetails userDetails) {
        if (userDetails == null || userDetails.getId() == null) {
            throw new IllegalArgumentException("Authenticated user is required.");
        }

        String userFolderName = "user-" + userDetails.getId();
        Path userDirectory = this.fileStorageLocation.resolve(userFolderName).normalize();
        try {
            Files.createDirectories(userDirectory);
        } catch (IOException ex) {
            throw new RuntimeException("Could not create user file directory.", ex);
        }
        return userDirectory;
    }

    private Path resolveQuarantineDirectory() {
        return this.fileStorageLocation.resolve(".quarantine").normalize();
    }

    private String sanitizeFilename(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new IllegalArgumentException("Invalid file name.");
        }

        String fileName = Paths.get(originalFilename).getFileName().toString();
        if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
            throw new IllegalArgumentException("Invalid file name.");
        }

        return fileName;
    }

    private boolean isAllowedExtension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        if (dot <= 0 || dot == fileName.length() - 1) {
            return false;
        }

        String ext = fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
        return ALLOWED_EXTENSIONS.contains(ext);
    }

    private boolean isAllowedMimeType(Path stagedFile) {
        try {
            String mimeType = Files.probeContentType(stagedFile);
            if (mimeType == null || mimeType.isBlank()) {
                return false;
            }

            if (mimeType.startsWith("text/") || mimeType.startsWith("image/")) {
                return true;
            }

            return ALLOWED_MIME_TYPES.contains(mimeType.toLowerCase(Locale.ROOT));
        } catch (IOException ex) {
            return false;
        }
    }
}
