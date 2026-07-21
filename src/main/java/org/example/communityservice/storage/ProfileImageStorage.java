package org.example.communityservice.storage;

import org.example.communityservice.common.exception.FileStorageException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

@Component
public class ProfileImageStorage {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".webp");
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/webp");
    private final Path uploadPath;

    public ProfileImageStorage(@Value("${file.profile-upload-dir}") String profileUploadDir){
        this.uploadPath = Paths.get(profileUploadDir).toAbsolutePath().normalize();

        try{
            Files.createDirectories(uploadPath);
        }
        catch (IOException e){
            throw new FileStorageException();
        }
    }

    public String store(MultipartFile image){
        validateImage(image);

        String extension = extractExtension(image.getOriginalFilename());
        String profileStoredFilename = UUID.randomUUID() + extension;

        Path targetPath = uploadPath.resolve(profileStoredFilename).normalize();
        validatePath(targetPath);

        try(InputStream inputStream = image.getInputStream()) {
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
        }
        catch (IOException e){
            throw new FileStorageException();
        }

        return profileStoredFilename;
    }

    public void delete(String profileStoredFilename){
        Path targetPath = uploadPath.resolve(profileStoredFilename).normalize();
        validatePath(targetPath);

        try{
            Files.deleteIfExists(targetPath);
        }
        catch (IOException e){
            throw new FileStorageException();
        }
    }

    private void validateImage(MultipartFile image){
        if(image == null || image.isEmpty()){
            throw new FileStorageException();
        }
        String contentType = image.getContentType();

        if(contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)){
            throw new FileStorageException();
        }
    }

    private String extractExtension(String originalFilename){
        if(originalFilename == null || originalFilename.isBlank()){
            throw new FileStorageException();
        }

        int extensionIndex = originalFilename.lastIndexOf(".");
        if(extensionIndex == -1){
            throw new FileStorageException();
        }

        String extension = originalFilename.substring(extensionIndex).toLowerCase();

        if(!ALLOWED_EXTENSIONS.contains(extension)){
            throw new FileStorageException();
        }

        return extension;
    }

    public void validatePath(Path targetPath){
        if(!targetPath.startsWith(uploadPath)){
            throw new FileStorageException();
        }
    }
}
