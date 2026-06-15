package com.alper.worldcup.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ChangelogController {

    private final String changelogText;

    public ChangelogController(ResourceLoader resourceLoader) throws IOException {
        Resource resource = resourceLoader.getResource("classpath:Changelog.md");
        this.changelogText = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    }

    @GetMapping("/changelog")
    public String changelog(Model model) {
        model.addAttribute("changelogText", changelogText);
        return "changelog";
    }
}
