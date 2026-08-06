package com.dzy.goodsprovider8090.service;

import com.dzy.goodsprovider8090.dto.CategoryRequest;
import com.dzy.goodsprovider8090.entity.Category;
import com.dzy.goodsprovider8090.entity.CategoryTreeNode;

import java.util.List;

public interface CategoryService {

    List<CategoryTreeNode> tree(boolean onlyEnabled);

    Category getById(Long id);

    Category create(CategoryRequest request);

    Category update(Long id, CategoryRequest request);

    void delete(Long id);
}
