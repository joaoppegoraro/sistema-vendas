package com.salessystem.config;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

/** Exposes app.visible-modules to every template, so layout.html can show/hide each nav link. */
@ControllerAdvice
public class LimitedScopeModelAttributes {

    private final VisibleModules visibleModules;

    public LimitedScopeModelAttributes(VisibleModules visibleModules) {
        this.visibleModules = visibleModules;
    }

    @ModelAttribute("visibleModules")
    public List<String> visibleModules() {
        return visibleModules.asList();
    }
}
