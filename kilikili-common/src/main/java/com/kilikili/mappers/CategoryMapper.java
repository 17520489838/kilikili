package com.kilikili.mappers;

import com.kilikili.entity.po.Category;
import com.kilikili.entity.query.CategoryQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CategoryMapper {
    Integer insert(Category category);
    Integer deleteByCategoryId(@Param("categoryId") Integer categoryId);
    Integer deleteByPCategoryId(@Param("pCategoryId") Integer pCategoryId);
    Integer updateByCategoryId(Category category);
    Category selectByCategoryId(@Param("categoryId") Integer categoryId);
    List<Category> selectList();
    List<Category> selectListByCondition(@Param("query") CategoryQuery query);
    Long selectCountByCondition(@Param("query") CategoryQuery query);
}
