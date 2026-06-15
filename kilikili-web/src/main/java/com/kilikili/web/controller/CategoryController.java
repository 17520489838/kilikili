package com.kilikili.web.controller;

import com.kilikili.entity.vo.ResponseVO;
import com.kilikili.service.CategoryService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController("webCategoryController")
@RequestMapping("/category")
@Validated
public class CategoryController extends ABaseController {

    @Resource
    private CategoryService categoryService;

    /**
     * 加载所有分类（树形结构，父分类包含子分类）
     */
    @RequestMapping("/loadAllCategory")
    public ResponseVO loadAllCategory() {
        return getSuccessResponseVO(categoryService.loadAllCategory());
    }

    /**
     * 加载所有父分类（仅父分类，用于前端父分类选择器）
     */
    @RequestMapping("/loadAllParentCategory")
    public ResponseVO loadAllParentCategory() {
        return getSuccessResponseVO(categoryService.loadAllParentCategory());
    }
}
