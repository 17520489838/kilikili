package com.kilikili.admin.controller;

import com.kilikili.entity.vo.ResponseVO;
import com.kilikili.service.CategoryService;
import com.kilikili.service.VideoFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

@RestController("adminCategoryController")
@RequestMapping("/category")
@Validated
public class CategoryController extends ABaseController {

    @Resource
    private CategoryService categoryService;

    @Autowired
    private VideoFileService videoFileService;

    /**
     * 加载分类列表（不分页，树形结构）
     * - categoryList: 父分类列表（每个父分类包含 children 子分类列表），前端据此区分父子
     * - parentList: 所有父分类平铺列表（用于前端父分类选择器）
     */
    @RequestMapping("/loadCategory")
    public ResponseVO loadCategory() {
        Map<String, Object> result = new HashMap<>();
        result.put("categoryList", categoryService.loadAllCategory());
        result.put("parentList", categoryService.loadAllParentCategory());
        return getSuccessResponseVO(result);
    }

// 保存分类
    @RequestMapping("/saveCategory")
    public ResponseVO saveCategory(Integer pCategoryId,// 父级分类ID
                                   Integer categoryId,// 分类ID
                                   @NotEmpty String categoryCode,// 分类编码
                                   @NotEmpty String categoryName,
                                   String icon,// 图标
                                   MultipartFile background // 背景图片文件
                                   ) throws Exception {
        String backgroundPath = null;
        if (background != null && !background.isEmpty()) {
            File tempFile = File.createTempFile("category_", background.getOriginalFilename());
            background.transferTo(tempFile);
            backgroundPath = videoFileService.uploadImage(tempFile.getAbsolutePath(), false);
        }
        categoryService.saveCategory(categoryId, pCategoryId, categoryCode, categoryName, icon, backgroundPath);
        return getSuccessResponseVO(null);
    }
// 删除分类
    @PostMapping("/delCategory")
    public ResponseVO delCategory(@NotNull Integer categoryId) {
        categoryService.deleteCategory(categoryId);
        return getSuccessResponseVO(null);
    }

    // 修改分类排序
    @RequestMapping("/changeSort")
    public ResponseVO changeSort(@NotNull Integer categoryId, @NotNull Integer sort) {
        categoryService.changeSort(categoryId, sort);
        return getSuccessResponseVO(null);
    }
}
