package com.qpa.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

@Service
public class CloudinaryService {

    @Value("${cloudinary.cloud-name}")
    private String cloudName;

    @Value("${cloudinary.api-key}")
    private String apiKey;

    @Value("${cloudinary.api-secret}")
    private String apiSecret;

    private Cloudinary cloudinary;

    @PostConstruct
    public void init() {
        cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "secure", true
        ));
    }

    public String uploadSpotImage(MultipartFile file, Long spotId, String existingImageUrl) {
        try {
            validateSpotImage(file);

            if (existingImageUrl != null && !existingImageUrl.isEmpty()) {
                deleteCloudinaryImage(extractPublicIdFromUrl(existingImageUrl));
            }

            String publicId = "spots/" + spotId + "_spot_" + generateUniqueId();
            return uploadToCloudinary(file, publicId, "spots");
        } catch (IOException e) {
            throw new RuntimeException("Spot image upload failed", e);
        }
    }

    public void deleteSpotImage(String imageUrl) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            try {
                deleteCloudinaryImage(extractPublicIdFromUrl(imageUrl));
            } catch (IOException e) {
                throw new RuntimeException("Spot image deletion failed", e);
            }
        }
    }

    private String uploadToCloudinary(MultipartFile file, String publicId, String folder) throws IOException {
        Map<String, Object> params = ObjectUtils.asMap(
                "public_id", publicId,
                "folder", folder,
                "overwrite", true,
                "resource_type", "auto"
        );

        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), params);
        return (String) uploadResult.get("secure_url");
    }

    private void deleteCloudinaryImage(String publicId) throws IOException {
        cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
    }

    private String extractPublicIdFromUrl(String url) {
        String[] urlParts = url.split("/");
        String fileNameWithExtension = urlParts[urlParts.length - 1];
        return fileNameWithExtension.substring(0, fileNameWithExtension.lastIndexOf("."));
    }

    private String generateUniqueId() {
        return UUID.randomUUID().toString();
    }

    private void validateSpotImage(MultipartFile file) {
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new IllegalArgumentException("Spot image must be less than 10MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !Arrays.asList("image/jpeg", "image/png", "image/webp").contains(contentType)) {
            throw new IllegalArgumentException("Invalid file type. Only JPEG, PNG, and WebP are allowed");
        }
    }
}
