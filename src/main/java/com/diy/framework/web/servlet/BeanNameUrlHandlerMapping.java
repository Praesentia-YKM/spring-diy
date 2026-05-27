package com.diy.framework.web.servlet;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

public class BeanNameUrlHandlerMapping implements HandlerMapping {

    private final Map<String, Object> handlers = new HashMap<>();

    public void register(final String url, final Object handler) {
        this.handlers.put(url, handler);
    }

    @Override
    public Object getHandler(final HttpServletRequest request) {
        return this.handlers.get(request.getRequestURI());
    }
}
