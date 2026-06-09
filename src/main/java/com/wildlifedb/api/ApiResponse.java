package com.wildlifedb.api;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class ApiResponse<T> {

    private final int code;
    private final String message;
    private final T data;
    private final OffsetDateTime timestamp;

    public ApiResponse(int code, String message, T data, OffsetDateTime timestamp) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = timestamp;
    }

    public static <T> ApiResponse<T> success(T data) {
        return success("Success", data);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(200, message, data, OffsetDateTime.now(ZoneOffset.UTC));
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message, null, OffsetDateTime.now(ZoneOffset.UTC));
    }

    public static <T> ApiResponse<T> error(int code, String message, T data) {
        return new ApiResponse<>(code, message, data, OffsetDateTime.now(ZoneOffset.UTC));
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }
}
