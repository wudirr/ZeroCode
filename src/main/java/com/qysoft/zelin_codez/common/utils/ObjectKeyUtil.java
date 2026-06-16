package com.qysoft.zelin_codez.common.utils;

import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONUtil;
import com.qysoft.zelin_codez.common.constant.RedisConstant;
import org.apache.commons.lang3.StringUtils;

/**
 * 构建对象缓存键的工具类
 *
 * @author qysoft
 * @version 1.0
 * @since 1.0
 */
public class ObjectKeyUtil {

    /**
     * 构建对象缓存键[使用md5进行构建]
     *
     * @param obj 构建对象
     * @return 缓存键
     */
    public static String buildKey(Object obj) {
        if (obj == null) {
            //防止缓存穿透
            return "null";
        }
        //先转化成JSON
        String jsonStr = JSONUtil.toJsonStr(obj);
        if (StringUtils.isBlank(jsonStr)) {
            return "null";
        }
        String md5Value = DigestUtil.md5Hex(jsonStr);
        return RedisConstant.PAGE_QUERY_KEY_PREFIX + md5Value;
    }
}
