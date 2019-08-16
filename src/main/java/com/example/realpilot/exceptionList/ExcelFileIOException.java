package com.example.realpilot.exceptionList;

public class ExcelFileIOException extends RuntimeException {
    public ExcelFileIOException() {
        super("엑셀 파일을 읽거나 쓸 수 없습니다.");
    }
}
