package com.diy.framework.web.servlet;

import com.diy.framework.web.mvc.view.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface HandlerAdapter {
    boolean supports(final Object handler);

    ModelAndView handle(final HttpServletRequest request, final HttpServletResponse response, final Object handler) throws Exception;
}
