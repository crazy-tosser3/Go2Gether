package com.example.STOLOTO.DTO.common;

import java.util.Map;

public record ErrorResponse(ErrorBody error) {

    public record ErrorBody(String code, String message, Map<String, String> fields) {
    }

    public static ErrorResponse of(String code, String message, Map<String, String> fields) {
        return new ErrorResponse(new ErrorBody(code, message, fields == null || fields.isEmpty() ? null : fields));
    }
}
