package com.example.realpilot.exceptionList;

public class JsonAndObjectMappingException extends RuntimeException {
    public JsonAndObjectMappingException() {
        super("Json과 Object 간 데이터 Mapping이 올바르지 않습니다.");
    }
}
