package com.diy.framework.web.servlet;

import com.diy.framework.web.method.HandlerMethod;
import com.diy.framework.web.method.RequestMappingInfo;
import com.diy.framework.web.mvc.anotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

public class RequestMappingHandlerMapping implements HandlerMapping {

    private final Map<RequestMappingInfo, HandlerMethod> handlers = new HashMap<>();

    public void register(final RequestMappingInfo mappingInfo, final HandlerMethod handlerMethod) {
        this.handlers.put(mappingInfo, handlerMethod);
    }

    @Override
    public Object getHandler(final HttpServletRequest request) {
        final String uri = request.getRequestURI();
        final RequestMethod requestMethod = RequestMethod.valueOf(request.getMethod().toUpperCase());

        return this.handlers.entrySet().stream()
                .filter(entry -> entry.getKey().isMatch(uri, requestMethod))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }
}
