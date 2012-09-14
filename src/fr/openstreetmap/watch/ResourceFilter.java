package fr.openstreetmap.watch;
import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

public class ResourceFilter implements Filter {
    RequestDispatcher defaultDispatcher;

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;

        if (request.getServletPath() != null && request.getServletPath().startsWith("/resources/")) {
            this.defaultDispatcher.forward(req, resp);
        } else {
            chain.doFilter(req, resp);
        }
    }

    @Override
    public void init(FilterConfig config) throws ServletException {
        defaultDispatcher = config.getServletContext().getNamedDispatcher("default");
    }
}