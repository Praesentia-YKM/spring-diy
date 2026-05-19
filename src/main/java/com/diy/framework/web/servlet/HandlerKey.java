package com.diy.framework.web.servlet;

import com.diy.framework.web.mvc.annotation.RequestMethod;

import java.util.Objects;

/**
 * 라우팅 키 — (url, HTTP method)
 */
public final class HandlerKey {

    private final String url;
    private final RequestMethod method;

    public HandlerKey(final String url, final RequestMethod method) {
        this.url = url;
        this.method = method;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof HandlerKey)) return false;
        final HandlerKey that = (HandlerKey) o;
        return Objects.equals(url, that.url) && method == that.method;
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, method);
    }

    @Override
    public String toString() {
        return method + " " + url;
    }
}
