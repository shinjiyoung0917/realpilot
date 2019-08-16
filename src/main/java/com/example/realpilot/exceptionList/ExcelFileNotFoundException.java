package com.example.realpilot.exceptionList;

public class ExcelFileNotFoundException extends RuntimeException {
    public ExcelFileNotFoundException() {
        super("엑셀 파일을 찾을 수 없습니다.");
    }
}
