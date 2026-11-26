package com.example.eventreserve.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * ログインしていない人をログイン画面へ戻す。
 * /admin で始まるページは企業アカウントだけが開ける。
 */
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        HttpSession session = request.getSession(false);
        LoginUser user = (session == null) ? null : (LoginUser) session.getAttribute(LoginUser.SESSION_KEY);

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }

        String path = request.getRequestURI().substring(request.getContextPath().length());
        if (path.startsWith("/admin") && !user.isCompany()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }
        return true;
    }
}
