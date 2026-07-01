package com.alper.worldcup.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class RulesController {

    @GetMapping("/rules")
    public String rules(@RequestParam(value = "lang", defaultValue = "tr") String lang) {
        if ("en".equalsIgnoreCase(lang)) {
            return "rules-en";
        }
        return "rules";
    }
}
