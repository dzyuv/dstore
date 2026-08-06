package com.dzy.goodsprovider8090.controller;

import com.dzy.common.constants.Constants;
import com.dzy.common.entity.ResultJSON;
import com.dzy.common.exception.BusinessException;
import com.dzy.goodsprovider8090.dto.CategoryRequest;
import com.dzy.goodsprovider8090.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /** 消费者/商家：启用中的分类树 */
    @GetMapping("/tree")
    public ResultJSON tree() {
        return ResultJSON.success(categoryService.tree(true));
    }

    /** 管理员：全部分类树（含禁用） */
    @GetMapping("/admin/tree")
    public ResultJSON adminTree(@RequestHeader(value = Constants.HEADER_ROLE, required = false) String role) {
        requireAdmin(role);
        return ResultJSON.success(categoryService.tree(false));
    }

    @GetMapping("/{id}")
    public ResultJSON get(@PathVariable Long id) {
        return ResultJSON.success(categoryService.getById(id));
    }

    @PostMapping
    public ResultJSON create(@RequestHeader(value = Constants.HEADER_ROLE, required = false) String role,
                             @Valid @RequestBody CategoryRequest request) {
        requireAdmin(role);
        return ResultJSON.success(categoryService.create(request));
    }

    @PutMapping("/{id}")
    public ResultJSON update(@RequestHeader(value = Constants.HEADER_ROLE, required = false) String role,
                             @PathVariable Long id,
                             @Valid @RequestBody CategoryRequest request) {
        requireAdmin(role);
        return ResultJSON.success(categoryService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResultJSON delete(@RequestHeader(value = Constants.HEADER_ROLE, required = false) String role,
                             @PathVariable Long id) {
        requireAdmin(role);
        categoryService.delete(id);
        return ResultJSON.success();
    }

    private void requireAdmin(String role) {
        if (role == null || !Constants.ROLE_ADMIN.equals(role)) {
            throw new BusinessException("仅管理员可操作分类");
        }
    }
}
