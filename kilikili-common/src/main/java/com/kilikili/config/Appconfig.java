package com.kilikili.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class Appconfig {
    @Value( "${project.folder:}")
    private String projectFolder;

    @Value( "${admin.account:}")
    private String  adminAccount;

    @Value( "${admin.password:}")
    private String  adminPassword;

}
