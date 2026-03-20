package com.sky.interceptor;

import com.sky.constant.JwtClaimsConstant;
import com.sky.context.BaseContext;
import com.sky.properties.JwtProperties;
import com.sky.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class JwtTokenAdminInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenAdminInterceptor.class);
    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_OPERATOR = "OPERATOR";
    private static final String[] ADMIN_OR_OPERATOR_PREFIXES = {
            "/admin/order/",
            "/admin/shop/"
    };

    @Autowired
    private JwtProperties jwtProperties;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        String token = request.getHeader(jwtProperties.getAdminTokenName());
        try {
            log.info("jwt校验:{}", token);
            Claims claims = JwtUtil.parseJWT(jwtProperties.getAdminSecretKey(), token);
            Long empId = Long.valueOf(claims.get(JwtClaimsConstant.EMP_ID).toString());
            String role = claims.get(JwtClaimsConstant.ROLE) == null
                    ? ROLE_ADMIN
                    : claims.get(JwtClaimsConstant.ROLE).toString();
            Long diningPointId = claims.get(JwtClaimsConstant.DINING_POINT_ID) == null
                    ? null
                    : Long.valueOf(claims.get(JwtClaimsConstant.DINING_POINT_ID).toString());

            log.info("当前员工id：{}，角色：{}，助餐点：{}", empId, role, diningPointId);
            BaseContext.setCurrentId(empId);
            BaseContext.setCurrentRole(role);
            BaseContext.setCurrentDiningPointId(diningPointId);

            if (!hasPermission(role, request.getRequestURI())) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
                return false;
            }

            return true;
        } catch (Exception ex) {
            BaseContext.clear();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        BaseContext.clear();
    }

    private boolean hasPermission(String role, String uri) {
        if (ROLE_ADMIN.equals(role)) {
            return true;
        }

        if (!ROLE_OPERATOR.equals(role)) {
            return false;
        }

        if ("/admin/employee/logout".equals(uri)) {
            return true;
        }

        return startsWithAny(uri, ADMIN_OR_OPERATOR_PREFIXES);
    }

    private boolean startsWithAny(String uri, String[] prefixes) {
        for (String prefix : prefixes) {
            if (uri.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }
}
