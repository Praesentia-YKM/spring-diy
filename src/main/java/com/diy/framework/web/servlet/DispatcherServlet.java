package com.diy.framework.web.servlet;

import com.diy.framework.context.ApplicationContext;
import com.diy.framework.web.mvc.Controller;
import com.diy.framework.web.mvc.annotation.RequestMapping;
import com.diy.framework.web.mvc.annotation.RequestMethod;
import com.diy.framework.web.mvc.view.JspViewResolver;
import com.diy.framework.web.mvc.view.ModelAndView;
import com.diy.framework.web.mvc.view.UrlBasedViewResolver;
import com.diy.framework.web.mvc.view.View;
import com.diy.framework.web.mvc.view.ViewResolver;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// @WebServlet 제거: 톰캣 자동 스캔이 no-arg 생성자로 인스턴스화 시도 -> NoSuchMethodException 에러 나서
public class DispatcherServlet extends HttpServlet {

    // Map(String, Controller) — URL 1개 = 핸들러 1개 라서 같은 URL내 HTTP메서드별로 핸들러 등록을 못했음
    // 따라서, 라우팅 테이블 개념을 도입해야겠다고 판단.
    // 라우팅 테이블: (URL, HTTP method) -> 핸들러.
    private final Map<HandlerKey, Object> handlers = new HashMap<>();

    // 뷰 이름을 실제 View 구현체로 해석하기 위함
    private final List<ViewResolver> viewResolvers = new ArrayList<>();

    public DispatcherServlet(final ApplicationContext applicationContext) {
        // "redirect:/" 처럼 prefix 가 있는 뷰 이름 우선 처리하려고
        this.viewResolvers.add(new UrlBasedViewResolver());
        this.viewResolvers.add(new JspViewResolver());
        // 시작 시 한 번만 라우팅 테이블 정보 세팅함
        registerHandlers(applicationContext);
    }

    private void registerHandlers(final ApplicationContext ctx) {
        // 컨테이너의 모든 (이름, 인스턴스) 를 순회
        for (final Map.Entry<String, Object> entry : ctx.getBeans().entrySet()) {
            final String beanName = entry.getKey();
            final Object bean = entry.getValue();
            final Class<?> clazz = bean.getClass();

            // isAnnotationPresent은 커스텀 메타 어노테이션을 못찾더라..
            if (clazz.isAnnotationPresent(com.diy.framework.web.mvc.annotation.Controller.class)) {
                registerAnnotationController(bean, clazz);
                continue;
            }

            // Controller 인스턴스이면서 빈 이름이 URL 형태("/")로 시작할 때만 핸들러에 등록시킴
            // 왜 핸들러라고 변수명 지었을까?: 어떤 사건/메시지가 발생했을 때 그것을 받아 처리하는 코드 단위라는 의미에서 핸들러(handler)라고 부름.
            if (bean instanceof Controller && beanName.startsWith("/")) {
                for (final RequestMethod httpMethod : RequestMethod.values()) {
                    handlers.put(new HandlerKey(beanName, httpMethod), bean);
                }
            }
        }
    }

    /**
     * 어노테이션 클래스 1개 -> 메서드 N개로 분해 등록.
     */
    private void registerAnnotationController(final Object bean, final Class<?> clazz) {
        // 클래스에 @RequestMapping 이 있으면 그 value 가 URL prefix, 없으면 빈 문자열
        final String classPrefix = clazz.isAnnotationPresent(RequestMapping.class)
                ? clazz.getAnnotation(RequestMapping.class).value()
                : "";

        // 클래스의 모든 선언된 메서드를 순회하면서 @requestMapping의 정보를 추출
        for (final Method m : clazz.getDeclaredMethods()) {
            if (!m.isAnnotationPresent(RequestMapping.class)) continue;

            final RequestMapping rm = m.getAnnotation(RequestMapping.class);
            final String url = classPrefix + rm.value();
            final RequestMethod[] httpMethods = rm.methods().length == 0
                    ? RequestMethod.values()
                    : rm.methods();

            // 매핑할 각 HTTP method 마다 (URL, method) → (bean, method) 등록
            for (final RequestMethod httpMethod : httpMethods) {
                handlers.put(new HandlerKey(url, httpMethod), new Object[]{bean, m});
            }
        }
    }

    /**
     * 매 요청의 진입점. 톰캣이 호출
     */
    @Override
    protected void service(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        final String uri = req.getRequestURI();
        final RequestMethod method = RequestMethod.valueOf(req.getMethod());

        // 키로 핸들러 조회
        final Object handler = handlers.get(new HandlerKey(uri, method));
        if (handler == null) {
            return;
        }

        try {
            final ModelAndView mav;
            if (handler instanceof Controller) {
                // 인터페이스 방식
                mav = ((Controller) handler).handleRequest(req, resp);
            } else {
                // 어노테이션 방식
                final Object[] bm = (Object[]) handler;
                mav = (ModelAndView) ((Method) bm[1]).invoke(bm[0], req, resp);
            }
            render(mav, req, resp);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
            return new ObjectMapper().readValue(body, new TypeReference<Map<String, Object>>() {});
        } else {
            return req.getParameterMap();
        }
    }
}
