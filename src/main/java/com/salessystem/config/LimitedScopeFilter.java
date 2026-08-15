package com.salessystem.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * College project delivery gate: while app.limited-scope=true, only Clients and Suppliers
 * are reachable, even by typing another module's URL directly — everything else redirects
 * to /clients. Controlled by a single property so it's a one-line revert once graded.
 */
@Component
public class LimitedScopeFilter implements Filter {

    @Value("${app.limited-scope:false}")
    private boolean limitedScope;

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        if (limitedScope && !isAllowed(request.getRequestURI())) {
            response.sendRedirect(request.getContextPath() + "/clients");
            return;
        }
        chain.doFilter(req, res);
    }

    private boolean isAllowed(String path) {
        return path.startsWith("/clients") || path.startsWith("/suppliers")
                || path.startsWith("/css") || path.startsWith("/js")
                || path.startsWith("/h2-console") || path.equals("/favicon.ico");
    }
}
