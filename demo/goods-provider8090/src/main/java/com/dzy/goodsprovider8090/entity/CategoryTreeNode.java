package com.dzy.goodsprovider8090.entity;

import lombok.Data;
import java.util.List;

@Data
public class CategoryTreeNode {
    private Long id;
    private Long parentId;
    private String name;
    private Integer level;
    private Integer sortOrder;
    private String status;
    private List<CategoryTreeNode> children;
}