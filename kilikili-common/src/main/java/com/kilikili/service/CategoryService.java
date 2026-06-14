package com.kilikili.service;

import com.kilikili.entity.po.Category;
import com.kilikili.entity.query.CategoryQuery;

import java.util.List;

public interface CategoryService {
    List<Category> loadAllCategory();
    void saveCategory(Integer categoryId, Integer pCategoryId, String categoryCode, String categoryName, String icon, String background);
    void deleteCategory(Integer categoryId);
    void changeSort(Integer categoryId, Integer sort);
    Object getCategoryList(CategoryQuery query);
}
