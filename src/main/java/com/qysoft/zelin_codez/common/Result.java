package com.qysoft.zelin_codez.common;

import com.qysoft.zelin_codez.exception.ErrorCode;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @Description R对象
 * @Author wudi
 * @Date 2026/4/3 15:21
 **/
@Data
public class Result<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Integer code;

    private T data;

    private String message;

    public Result(Integer code, T data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
    }

    public static <T> Result<T> success(T data) {
        return new Result<T>(200, data, "success");
    }

    public static <T> Result<T> success(T data, String message) {
        return new Result<T>(200, data, message);
    }

    public static <T> Result<T> success() {
        return new Result<>(200, null, "success");
    }

    public static <T> Result<T> error(ErrorCode errorCode) {
        return new Result<T>(errorCode.getCode(), null, errorCode.getMessage());
    }

    public static <T> Result<T> error(ErrorCode errorCode, String message) {
        return new Result<T>(errorCode.getCode(), null, message);
    }

    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, null, message);
    }

    public static <T> Result<T> error(Integer code) {
        return new Result<>(code, null, "fail");
    }

    public static <T> Result<T> error() {
        return new Result<>(50000, null, "fail");
    }
}
