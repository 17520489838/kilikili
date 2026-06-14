package com.kilikili.mappers;

import com.kilikili.entity.po.SysSetting;
import com.kilikili.entity.query.SysSettingQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysSettingMapper {
    Integer insert(SysSetting sysSetting);
    Integer updateBySettingKey(SysSetting sysSetting);
    SysSetting selectBySettingKey(@Param("settingKey") String settingKey);
    List<SysSetting> selectListByCondition(@Param("query") SysSettingQuery query);
    Long selectCountByCondition(@Param("query") SysSettingQuery query);
}
