package net.risesoft.config;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public class CheckUserLoginFilter4log implements Filter {

    public void destroy() {}

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException {
        // 在risenet-y9boot-3rd-cas-client工程的AssertionThreadLocalFilter类中，已经往session中保存了以下对象：
        // tenantName、loginName、loginPerson、tenantId、y9User等
        // 同时Y9LoginUserHolder也设置了tenantName、tenantId、person、Map<String, Object>属性。
        chain.doFilter(request, response);

    }

    public void init(FilterConfig filterConfig) throws ServletException {}
}
