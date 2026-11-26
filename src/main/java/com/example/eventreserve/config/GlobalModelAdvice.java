package com.example.eventreserve.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * どの画面でもヘッダーに「〇〇さん」「ログアウト」を出せるよう、
 * ログイン中の会員を loginUser という名前で画面に渡す。
 */
@ControllerAdvice
public class GlobalModelAdvice {

    /**
     * 今開いているページのパス（?以降もふくむ）。
     * お気に入りボタンを押したあと、同じページ（検索条件もそのまま）に戻るために使う。
     */
    @ModelAttribute("currentPath")
    public String currentPath(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        String query = request.getQueryString();
        return (query == null) ? path : path + "?" + query;
    }

    @ModelAttribute("loginUser")
    public LoginUser loginUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return (session == null) ? null : (LoginUser) session.getAttribute(LoginUser.SESSION_KEY);
    }
}
