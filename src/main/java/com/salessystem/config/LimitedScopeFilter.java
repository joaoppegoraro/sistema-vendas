package com.salessystem.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * College project delivery gate: only the modules listed in app.visible-modules are reachable,
 * even by typing another module's URL directly — everything else redirects to /clients. The
 * delivery scope grows one property edit at a time as each stage gets graded, instead of a
 * code change per stage.
 */
@Component
public class LimitedScopeFilter implements Filter {

    private static final Map<String, String> MODULE_PATHS = Map.of(
            "clients", "/clients",
            "suppliers", "/suppliers",
            "products", "/products",
            "sales", "/sales",
            "reports", "/reports"
    );

    private final VisibleModules visibleModules;

    public LimitedScopeFilter(VisibleModules visibleModules) {
        this.visibleModules = visibleModules;
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        String path = request.getRequestURI();

        boolean dashboardAllowed = path.equals("/") && visibleModules.contains("dashboard");
        if (isAlwaysAllowed(path) || isVisibleModulePath(path) || dashboardAllowed) {
            chain.doFilter(req, res);
            return;
        }
        response.sendRedirect(request.getContextPath() + "/clients");
    }

    private boolean isVisibleModulePath(String path) {
        return MODULE_PATHS.entrySet().stream()
                .filter(entry -> visibleModules.contains(entry.getKey()))
                .anyMatch(entry -> path.startsWith(entry.getValue()));
    }

    private boolean isAlwaysAllowed(String path) {
        return path.startsWith("/css") || path.startsWith("/js")
                || path.startsWith("/h2-console") || path.equals("/favicon.ico")
                || path.equals("/login") || path.equals("/logout");
    }
}
