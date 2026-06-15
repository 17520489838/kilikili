package com.kilikili.service.impl;

import com.kilikili.component.RedisComponent;
import com.kilikili.entity.constants.Constants;
import com.kilikili.entity.po.Category;
import com.kilikili.entity.query.CategoryQuery;
import com.kilikili.entity.query.SimplePage;
import com.kilikili.entity.vo.PaginationResultVO;
import com.kilikili.mappers.CategoryMapper;
import com.kilikili.redis.RedisUtils;
import com.kilikili.service.CategoryService;
import com.kilikili.utils.StringTools;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

@Service("categoryService")
public class CategoryServiceImpl implements CategoryService {

    @Resource
    private CategoryMapper categoryMapper;
    @Resource
    private RedisUtils<Object> redisUtils;

    private static final String REDIS_KEY_CATEGORY_LIST = Constants.REDIS_KEY_PREFIX + "category:list";

    @Override
    public List<Category> loadAllCategory() {
        // Try cache first
        @SuppressWarnings("unchecked")
        List<Category> cachedList = (List<Category>) redisUtils.get(REDIS_KEY_CATEGORY_LIST);
        if (cachedList != null && !cachedList.isEmpty()) {
            return cachedList;
        }
        // Query from DB (selectList already filters is_deleted=0)
        List<Category> list = categoryMapper.selectList();
        // Sort: parent categories (pCategoryId=0) first, then by sort ASC
        if (list != null && !list.isEmpty()) {
            list.sort(Comparator.comparingInt((Category c) ->
                            c.getPCategoryId() != null && c.getPCategoryId() == 0 ? 0 : 1)
                    .thenComparingInt(c -> c.getSort() != null ? c.getSort() : 0));
        }
        // Cache in Redis for 1 hour
        if (list != null && !list.isEmpty()) {
            redisUtils.setex(REDIS_KEY_CATEGORY_LIST, (Object) list, 3600000L);
        }
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveCategory(Integer categoryId, Integer pCategoryId, String categoryCode,
                             String categoryName, String icon, String background) {
        if (categoryId == null) {
            // Insert new category
            Category category = new Category();
            // Generate a new categoryId
            category.setCategoryId(Integer.valueOf(StringTools.getRandomNumber(6)));
            category.setPCategoryId(pCategoryId);
            category.setCategoryCode(categoryCode);
            category.setCategoryName(categoryName);
            category.setIcon(icon);
            category.setBackground(background);
            category.setSort(0);
            category.setCreateTime(new Date());
            categoryMapper.insert(category);
        } else {
            // Update existing category
            Category category = categoryMapper.selectByCategoryId(categoryId);
            if (category != null) {
                category.setPCategoryId(pCategoryId);
                category.setCategoryCode(categoryCode);
                category.setCategoryName(categoryName);
                category.setIcon(icon);
                category.setBackground(background);
                categoryMapper.updateByCategoryId(category);
            }
        }
        // Clear cache
        redisUtils.delete(REDIS_KEY_CATEGORY_LIST);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCategory(Integer categoryId) {
        categoryMapper.deleteByCategoryId(categoryId);
        // Clear cache
        redisUtils.delete(REDIS_KEY_CATEGORY_LIST);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeSort(Integer categoryId, Integer sort) {
        Category category = categoryMapper.selectByCategoryId(categoryId);
        if (category != null) {
            category.setSort(sort);
            categoryMapper.updateByCategoryId(category);
        }
        // Clear cache
        redisUtils.delete(REDIS_KEY_CATEGORY_LIST);
    }

    @Override
    public Object getCategoryList(CategoryQuery query) {
        Integer pageNo = query.getPageNo() != null ? query.getPageNo() : 1;
        Integer pageSize = query.getPageSize() != null ? query.getPageSize() : 10;

        Long totalCount = categoryMapper.selectCountByCondition(query);
        SimplePage simplePage = new SimplePage(pageNo, pageSize, totalCount);

        query.setPageNo(pageNo);
        query.setPageSize(pageSize);

        List<Category> list = categoryMapper.selectListByCondition(query);
        return new PaginationResultVO<>(simplePage, list);
    }
}
