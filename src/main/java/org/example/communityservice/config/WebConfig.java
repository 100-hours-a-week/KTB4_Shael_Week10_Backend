package org.example.communityservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final String postUploadDir;
    private final String profileUploadDir;

    public WebConfig(@Value("${file.post-upload-dir}") String postUploadDir, @Value("${file.profile-upload-dir}") String profileUploadDir){
        this.postUploadDir = postUploadDir;
        this.profileUploadDir = profileUploadDir;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry){
        String postLocation = Paths.get(postUploadDir).toAbsolutePath().normalize().toUri().toString();
        String profileLocation = Paths.get(profileUploadDir).toAbsolutePath().normalize().toUri().toString();

        registry.addResourceHandler("/images/posts/**").addResourceLocations(postLocation);
        registry.addResourceHandler("/images/profiles/**").addResourceLocations(profileLocation);
    }
}
