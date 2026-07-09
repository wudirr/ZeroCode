package com.qysoft.zelin_codez.common;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @Description 删除请求
 * @Author wudi
 * @Date 2026/4/3 15:25
 **/
@Data
public class DeleteRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
}
