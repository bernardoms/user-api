package com.bernardoms.user.filter;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.stream.Collectors;

@Component
public class MDCFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {
        MDC.clear();

        MDC.put("path", httpServletRequest.getRequestURI());
        MDC.put("requestMethod", httpServletRequest.getMethod());

        addHeaderToMDC(httpServletRequest);
        addParamsToMDC(httpServletRequest);

        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }

    private void addHeaderToMDC(HttpServletRequest httpServletRequest) {
        var headers = Collections.list(httpServletRequest.getHeaderNames())
                .stream()
                .collect(Collectors.toMap(h -> h, httpServletRequest::getHeader));
        headers.forEach(MDC::put);
    }

    private void addParamsToMDC(HttpServletRequest httpServletRequest) {
        var parameterMap = httpServletRequest.getParameterMap();
        parameterMap.forEach((key, value) -> MDC.put(key, value[0]));
    }
}
