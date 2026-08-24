package com.salessystem.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * Parses app.visible-modules once, shared by LimitedScopeFilter (blocks direct URLs to modules
 * not yet delivered) and LimitedScopeModelAttributes (hides their nav links).
 */
@Component
public class VisibleModules {

    private final List<String> modules;

    public VisibleModules(@Value("${app.visible-modules:clients,suppliers}") String raw) {
        this.modules = Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(module -> !module.isEmpty())
                .toList();
    }

    public boolean contains(String module) {
        return modules.contains(module);
    }

    public List<String> asList() {
        return modules;
    }
}
