package com.kilikili.service;

import java.util.Map;

public interface SysSettingService {
    Map<String, String> getSetting();
    void saveSetting(Map<String, String> settings);
}
