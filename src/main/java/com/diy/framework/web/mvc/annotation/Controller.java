package com.diy.framework.web.mvc.annotation;

import com.diy.framework.context.annotation.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 컨트롤러 어노테이션
 *    - 스프링에서 제공해주는 컨트롤러랑 겹치지 않아야 함...!
 **/
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface Controller {
}
