package com.qysoft.zelin_codez.exception;

/**
 * @Description 抛异常工具类
 * @Author wudi
 * @Date 2026/4/3 14:57
 **/
public class ThrowUtils {

    public static void throwIf(boolean condition, String message){
        if (condition)
            throw new BusinessException(message);
    }
    public static void throwIf(boolean condition,Integer code,String message){
        if (condition)
            throw new BusinessException(code,message);
    }

    public static void throwIf(boolean condition,ErrorCode errorCode){
        throwIf(condition, errorCode.getCode(), errorCode.getMessage());
    }

    public static void throwIf(boolean condition,ErrorCode errorCode,String message){
        throwIf(condition, errorCode.getCode(), message);
    }
}
