package com.kpjunaid.util;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Component
public class FileUploadUtil {
    public void saveNewFile(String uploadDir,
                            String fileName,
                            MultipartFile multipartFile) throws IOException {
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        try (InputStream inputStream = multipartFile.getInputStream()) {
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IOException("Could not save file");
        }
    }

    public void updateFile(String uploadDir,
                           String oldFileName,
                           String newFileName,
                           MultipartFile multipartFile) throws IOException {
        Path uploadPath = Paths.get(uploadDir);

        try (InputStream inputStream = multipartFile.getInputStream()) {
            Path oldFilePath = uploadPath.resolve(oldFileName);
            Path newFilePath = uploadPath.resolve(newFileName);
            Files.deleteIfExists(oldFilePath);
            Files.copy(inputStream, newFilePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IOException("Could not update file");
        }
    }

    public void deleteFile(String uploadDir, String fileName) throws IOException {
        Path uploadPath = Paths.get(uploadDir);
        Path filePath = uploadPath.resolve(fileName);
        Files.deleteIfExists(filePath);
    }
}
