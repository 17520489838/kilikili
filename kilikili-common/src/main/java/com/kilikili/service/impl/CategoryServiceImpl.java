package com.kilikili.service.impl;

import com.kilikili.entity.constants.Constants;
import com.kilikili.entity.po.Category;
import com.kilikili.entity.query.CategoryQuery;
import com.kilikili.entity.query.SimplePage;
import com.kilikili.entity.vo.CategoryVO;
import com.kilikili.entity.vo.PaginationResultVO;
import com.kilikili.mappers.CategoryMapper;
import com.kilikili.redis.RedisUtils;
import com.kilikili.service.CategoryService;
import com.kilikili.utils.CopyTools;
import com.kilikili.utils.StringTools;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
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
    private static final String REDIS_KEY_PARENT_CATEGORY_LIST = Constants.REDIS_KEY_PREFIX + "category:parentList";

    @Override
    public List<CategoryVO> loadAllCategory() {
        // Try cache first
        @SuppressWarnings("unchecked")
        List<CategoryVO> cachedList = (List<CategoryVO>) redisUtils.get(REDIS_KEY_CATEGORY_LIST);
        if (cachedList != null && !cachedList.isEmpty()) {
            return cachedList;
        }
        // Query from DB (selectList already filters is_deleted=0)
        List<Category> list = categoryMapper.selectList();
        // Build tree structure
        List<CategoryVO> treeList = buildCategoryTree(list);
        // Cache in Redis for 1 hour
        if (treeList != null && !treeList.isEmpty()) {
            redisUtils.setex(REDIS_KEY_CATEGORY_LIST, (Object) treeList, 3600000L);
        }
        return treeList;
    }

    @Override
    public List<Category> loadAllCategoryFlat() {
        return categoryMapper.selectList();
    }

    @Override
    public List<Category> loadAllParentCategory() {
        // Try cache first
        @SuppressWarnings("unchecked")
        List<Category> cachedList = (List<Category>) redisUtils.get(REDIS_KEY_PARENT_CATEGORY_LIST);
        if (cachedList != null && !cachedList.isEmpty()) {
            return cachedList;
        }
        // Query parent categories (pCategoryId=0) with is_deleted=0 filter
        CategoryQuery query = new CategoryQuery();
        query.setPCategoryId(0);
        List<Category> parentList = categoryMapper.selectListByCondition(query);
        // Cache in Redis for 1 hour
        if (parentList != null && !parentList.isEmpty()) {
            redisUtils.setex(REDIS_KEY_PARENT_CATEGORY_LIST, (Object) parentList, 3600000L);
        }
        return parentList;
    }

    /**
     * 构建分类树形结构
     */
    private List<CategoryVO> buildCategoryTree(List<Category> list) {
        List<CategoryVO> resultList = new ArrayList<>();
        if (list == null || list.isEmpty()) {
            return resultList;
        }
        // Convert all categories to VOs
        List<CategoryVO> allVOList = CopyTools.copyList(list, CategoryVO.class);
        // Separate parents and children
        List<CategoryVO> parentList = new ArrayList<>();
        List<CategoryVO> childList = new ArrayList<>();
        for (CategoryVO vo : allVOList) {
            if (vo.getPCategoryId() != null && vo.getPCategoryId() == 0) {
                parentList.add(vo);
            } else {
                childList.add(vo);
            }
        }
        // For each parent category, find its children
        for (CategoryVO parent : parentList) {
            List<CategoryVO> children = new ArrayList<>();
            for (CategoryVO child : childList) {
                if (child.getPCategoryId() != null && child.getPCategoryId().equals(parent.getCategoryId())) {
                    children.add(child);
                }
            }
            children.sort(Comparator.comparingInt(c -> c.getSort() != null ? c.getSort() : 0));
            parent.setChildren(children);
        }
        // Sort parent categories by sort
        parentList.sort(Comparator.comparingInt(c -> c.getSort() != null ? c.getSort() : 0));
        return parentList;
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
        redisUtils.delete(REDIS_KEY_CATEGORY_LIST, REDIS_KEY_PARENT_CATEGORY_LIST);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCategory(Integer categoryId) {
        Category category = categoryMapper.selectByCategoryId(categoryId);
        if (category == null) {
            return;
        }
        // 软删除自身
        categoryMapper.deleteByCategoryId(categoryId);
        // 如果是父分类，级联软删除其子分类
        if (category.getPCategoryId() != null && category.getPCategoryId() == 0) {
            categoryMapper.deleteByPCategoryId(categoryId);
        }
        // Clear cache
        redisUtils.delete(REDIS_KEY_CATEGORY_LIST, REDIS_KEY_PARENT_CATEGORY_LIST);
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
        redisUtils.delete(REDIS_KEY_CATEGORY_LIST, REDIS_KEY_PARENT_CATEGORY_LIST);
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
