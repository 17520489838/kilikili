package com.kilikili.service;

import com.kilikili.entity.dto.TokenUserInfoDto;
import com.kilikili.entity.po.Danmu;

import java.util.List;

public interface DanmuService {
    void postDanmu(TokenUserInfoDto token, String videoId, String fileId, String text, Integer mode, String color, Integer time);
    List<Danmu> loadDanmu(String fileId, String videoId);
    List<Danmu> loadDanmuByPage(Integer pageNo, Integer pageSize);
    void delDanmu(String danmuId);
}
