package com.example.eventreserve.service;

/** 画面にそのまま表示してよいエラー（満席・登録済みなど） */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
