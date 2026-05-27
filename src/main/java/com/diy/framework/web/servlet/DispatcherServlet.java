package com.diy.framework.web.servlet;

import com.diy.framework.context.ApplicationContext;
import com.diy.framework.web.mvc.view.JspViewResolver;
import com.diy.framework.web.mvc.view.ModelAndView;
import com.diy.framework.web.mvc.view.UrlBasedViewResolver;
import com.diy.framework.web.mvc.view.View;
import com.diy.framework.web.mvc.view.ViewResolver;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.Nullable;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DispatcherServlet extends HttpServlet {

    private final List<HandlerMapping> handlerMappings = ApplicationContext.handlerMappings;
    private final List<HandlerAdapter> handlerAdapters = new ArrayList<>();
    private final List<ViewResolver> viewResolvers = new ArrayList<>();

    public DispatcherServlet() {
        this.handlerAdapters.add(new SimpleControllerHandlerAdapter());
        this.handlerAdapters.add(new HandlerMethodHandlerAdapter());

        this.viewResolvers.add(new UrlBasedViewResolver());
        this.viewResolvers.add(new JspViewResolver());
    }

    @Override
    protected void service(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        final Object handler = getHandler(req);

        if (handler == null) {
            return;
        }

        try {
            final HandlerAdapter handlerAdapter = getHandlerAdapter(handler);
            final ModelAndView mav = handlerAdapter.handle(req, resp, handler);
            render(mav, req, resp);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Nullable
    private Object getHandler(final HttpServletRequest req) {
        for (final HandlerMapping handlerMapping : this.handlerMappings) {
            final Object handler = handlerMapping.getHandler(req);
            if (handler != null) {
                return handler;
            }
        }

        return null;
    }

    private HandlerAdapter getHandlerAdapter(final Object handler) {
        for (final HandlerAdapter handlerAdapter : this.handlerAdapters) {
            if (handlerAdapter.supports(handler)) {
                return handlerAdapter;
            }
        }

        throw new RuntimeException("No adapter for handler: " + handler);
    }

    private void render(final ModelAndView mav, final HttpServletRequest req, final HttpServletResponse resp) throws Exception {
        final String viewName = mav.getViewName();

        final View view = resolveViewName(viewName);

        if (view == null) {
            throw new RuntimeException("View not found: " + viewName);
        }

        view.render(mav.getModel(), req, resp);
    }

    private View resolveViewName(final String viewName) {
        for (final ViewResolver viewResolver : this.viewResolvers) {
            final View view = viewResolver.resolveViewName(viewName);
            if (view != null) {
                return view;
            }
        }

        return null;
    }

    private Map<String, ?> parseParams(final HttpServletRequest req) throws IOException {
        if ("application/json".equals(req.getHeader("Content-Type"))) {
            final byte[] bodyBytes = req.getInputStream().readAllBytes();
            final String body = new String(bodyBytes, StandardCharsets.UTF_8);

            return new ObjectMapper().readValue(body, new TypeReference<Map<String, Object>>() {
            });
        } else {
            return req.getParameterMap();
        }
    }
}
