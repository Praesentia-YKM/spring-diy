package com.diy.framework.web.servlet;

import com.diy.framework.web.method.HandlerMethod;
import com.diy.framework.web.mvc.view.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class HandlerMethodHandlerAdapter implements HandlerAdapter {

    @Override
    public boolean supports(final Object handler) {
        return handler instanceof HandlerMethod;
    }

    @Override
    public ModelAndView handle(final HttpServletRequest request, final HttpServletResponse response, final Object handler) throws Exception {
        return ((HandlerMethod) handler).handle(request, response);
    }
}
