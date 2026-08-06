package com.dzy.goodsprovider8090.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoryRequest {

    /** 父分类，0 表示顶级 */
    private Long parentId = 0L;

    @NotBlank(message = "分类名称不能为空")
    private String name;

    private Integer sortOrder = 0;

    /** 1启用 0禁用 */
    private Integer status = 1;
}
