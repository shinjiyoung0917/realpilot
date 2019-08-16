package com.example.realpilot.exceptionList;

public class ApiCallException extends RuntimeException {
    public ApiCallException() {
        super("잘못된 API 호출입니다.");
    }
}
