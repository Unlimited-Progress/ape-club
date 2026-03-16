package com.jingdianjichi.subject.application.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

/**
 * Feign请求拦截器
 * 两个微服务之间的调用
 */
@Component
public class FeignRequstInterceptor implements RequestInterceptor {


    @Override
    public void apply(RequestTemplate requestTemplate) {
//        用RequestContextHolder.getRequestAttributes()方法，
//        我们可以获取到当前线程绑定的ServletRequestAttributes对象。
//        这段代码的作用是在Spring MVC应用程序中，当你需要访问当前HTTP请求的信息时，
//        可以使用这个对象来获取请求的各种属性。例如，你可以使用requestAttributes.getRequest()
//        来获取当前的HttpServletRequest对象，从而访问请求的URL、参数等信息。

//        ServletRequestAttributes是RequestAttributes接口的一个实现类，专门用于封装基于Servlet的HTTP请求的相关属性。
//        它提供了获取HttpServletRequest和HttpServletResponse对象的方法，
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder
                .getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();
        if (Objects.nonNull(request)){
            String loginId = request.getHeader("loginId");
            if (StringUtils.isNotBlank(loginId)){
                requestTemplate.header("loginId",loginId);
            }
        }

    }
}
