package com.diy.framework.web.mvc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * TYPE+METHOD 관리:
 *   - 클래스 레벨: 기본 URL prefix
 *   - 메서드 레벨: prefix 에 덧붙는 path + HTTP method 매핑
**/
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestMapping {
    String value() default "";
    RequestMethod[] methods() default {};
}
