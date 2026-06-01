package com.alper.worldcup.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttributes {

    @Value("${app.version:unknown}")
    private String appVersion;

    @ModelAttribute("appVersion")
    public String appVersion() {
        return appVersion;
    }
}
