package com.qysoft.zelin_codez.langgraph4j.model;

import com.qysoft.zelin_codez.langgraph4j.model.enums.ImageCategoryEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 图片资源对象
 *
 * @author qysoft
 * @version 1.0
 * @since 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImageResource implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 图片类型
     */
    private ImageCategoryEnum imageCategoryEnum;

    /**
     * 图片描述
     */
    private String desc;

    /**
     * 图片地址
     */
    private String url;
}
