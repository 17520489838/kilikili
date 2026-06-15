package com.kilikili.service;

import com.kilikili.entity.po.Category;
import com.kilikili.entity.query.CategoryQuery;
import com.kilikili.entity.vo.CategoryVO;

import java.util.List;

public interface CategoryService {
    /**
     * 加载所有分类（树形结构，父分类包含子分类列表）
     */
    List<CategoryVO> loadAllCategory();

    /**
     * 加载所有父分类（仅pCategoryId=0的分类）
     */
    List<Category> loadAllParentCategory();

    /**
     * 加载全部分类列表（平铺，不分页）
     */
    List<Category> loadAllCategoryFlat();

    void saveCategory(Integer categoryId, Integer pCategoryId, String categoryCode, String categoryName, String icon, String background);

    void deleteCategory(Integer categoryId);

    void changeSort(Integer categoryId, Integer sort);

    Object getCategoryList(CategoryQuery query);
}
