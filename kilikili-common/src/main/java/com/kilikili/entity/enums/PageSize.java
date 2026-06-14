package com.kilikili.entity.enums;

public enum PageSize {
    SIZE15(15),
    SIZE20(20),
    SIZE30(30),
    SIZE40(40),
    SIZE50(50);

    private final Integer size;

    PageSize(Integer size) {
        this.size = size;
    }

    public Integer getSize() {
        return size;
    }
}
