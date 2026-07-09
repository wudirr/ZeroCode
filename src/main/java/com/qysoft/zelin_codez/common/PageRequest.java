package com.qysoft.zelin_codez.common;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @Description 分页请求
 * @Author wudi
 * @Date 2026/4/3 15:23
 **/
@Data
public class PageRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Integer pageNum;

    private Integer pageSize;

    /**
     * 排序字段
     */
    private String sortField;

    /**
     * 排序方式,默认是降序排序
     */
    private String sortOrder = "descend";
}
