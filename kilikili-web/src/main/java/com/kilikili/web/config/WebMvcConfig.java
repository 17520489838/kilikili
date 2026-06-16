package com.kilikili.web.config;

import com.kilikili.config.Appconfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Resource
    private Appconfig appconfig;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String projectFolder = appconfig.getProjectFolder();
        if (projectFolder != null && !projectFolder.isEmpty()) {
            String transcodePath = "file:" + projectFolder + "video/transcode/";
            registry.addResourceHandler("/file/hls/**")
                    .addResourceLocations(transcodePath);
        }
    }
}
