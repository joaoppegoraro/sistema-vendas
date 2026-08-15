package com.salessystem.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/** Exposes app.limited-scope to every template, so layout.html can hide the extra nav links. */
@ControllerAdvice
public class LimitedScopeModelAttributes {

    @Value("${app.limited-scope:false}")
    private boolean limitedScope;

    @ModelAttribute("limitedScope")
    public boolean limitedScope() {
        return limitedScope;
    }
}
