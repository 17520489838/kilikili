package com.kilikili.entity.query;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CategoryQuery extends BaseParma {
    private Integer categoryId;
    private Integer pCategoryId;
    private String categoryNameFuzzy;
    private String categoryCode;
}
