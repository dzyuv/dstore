package com.dzy.goodsprovider8090.service.impl;

import com.dzy.common.exception.BusinessException;
import com.dzy.goodsprovider8090.dto.CategoryRequest;
import com.dzy.goodsprovider8090.entity.Category;
import com.dzy.goodsprovider8090.entity.CategoryTreeNode;
import com.dzy.goodsprovider8090.mapper.CategoryMapper;
import com.dzy.goodsprovider8090.mapper.ProductMapper;
import com.dzy.goodsprovider8090.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private ProductMapper productMapper;

    @Override
    public List<CategoryTreeNode> tree(boolean onlyEnabled) {
        List<Category> all = onlyEnabled ? categoryMapper.selectEnabled() : categoryMapper.selectAll();
        return buildTree(all, 0L);
    }

    @Override
    public Category getById(Long id) {
        Category c = categoryMapper.selectById(id);
        if (c == null) {
            throw new BusinessException("分类不存在");
        }
        return c;
    }

    @Override
    @Transactional
    public Category create(CategoryRequest request) {
        Long parentId = request.getParentId() == null ? 0L : request.getParentId();
        int level = 1;
        if (parentId != 0) {
            Category parent = categoryMapper.selectById(parentId);
            if (parent == null) {
                throw new BusinessException("父分类不存在");
            }
            if (parent.getStatus() != null && parent.getStatus() == 0) {
                throw new BusinessException("父分类已禁用");
            }
            level = parent.getLevel() + 1;
            if (level > 3) {
                throw new BusinessException("分类最多支持三级");
            }
        }
        Category c = new Category();
        c.setParentId(parentId);
        c.setName(request.getName().trim());
        c.setLevel(level);
        c.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
        c.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        categoryMapper.insert(c);
        return c;
    }

    @Override
    @Transactional
    public Category update(Long id, CategoryRequest request) {
        Category c = getById(id);
        if (request.getName() != null && !request.getName().isBlank()) {
            c.setName(request.getName().trim());
        }
        if (request.getSortOrder() != null) {
            c.setSortOrder(request.getSortOrder());
        }
        if (request.getStatus() != null) {
            c.setStatus(request.getStatus());
        }
        categoryMapper.update(c);
        return c;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        getById(id);
        if (categoryMapper.countChildren(id) > 0) {
            throw new BusinessException("请先删除子分类");
        }
        if (productMapper.countByCategory(id) > 0) {
            throw new BusinessException("该分类下存在商品，无法删除");
        }
        categoryMapper.delete(id);
    }

    private List<CategoryTreeNode> buildTree(List<Category> all, Long parentId) {
        return all.stream()
                .filter(c -> parentId.equals(c.getParentId() == null ? 0L : c.getParentId()))
                .map(c -> {
                    CategoryTreeNode node = new CategoryTreeNode();
                    node.setId(c.getId());
                    node.setParentId(c.getParentId());
                    node.setName(c.getName());
                    node.setLevel(c.getLevel());
                    node.setSortOrder(c.getSortOrder());
                    node.setStatus(c.getStatus() == null ? null : String.valueOf(c.getStatus()));
                    node.setChildren(buildTree(all, c.getId()));
                    return node;
                })
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
