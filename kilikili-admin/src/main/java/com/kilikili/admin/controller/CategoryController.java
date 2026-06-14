package com.kilikili.admin.controller;

import com.kilikili.entity.query.CategoryQuery;
import com.kilikili.entity.vo.ResponseVO;
import com.kilikili.service.CategoryService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@RestController("adminCategoryController")
@RequestMapping("/category")
@Validated
public class CategoryController extends ABaseController {

    @Resource
    private CategoryService categoryService;

    @RequestMapping("/loadCategory")
    public ResponseVO loadCategory() {
        return getSuccessResponseVO(categoryService.getCategoryList(new CategoryQuery()));
    }

    @RequestMapping("/saveCategory")
    public ResponseVO saveCategory(Integer pCategoryId,
                                   Integer categoryId,
                                   @NotEmpty String categoryCode,
                                   @NotEmpty String categoryName,
                                   String icon,
                                   String background) {
        categoryService.saveCategory(categoryId, pCategoryId, categoryCode, categoryName, icon, background);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/delCategory")
    public ResponseVO delCategory(@NotNull Integer categoryId) {
        categoryService.deleteCategory(categoryId);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/changeSort")
    public ResponseVO changeSort(@NotNull Integer categoryId, @NotNull Integer sort) {
        categoryService.changeSort(categoryId, sort);
        return getSuccessResponseVO(null);
    }
}
