package com.example.realpilot.exceptionList;

public class DgraphQueryException extends RuntimeException {
    public DgraphQueryException() {
        super("쿼리 이상입니다.");
    }
}
