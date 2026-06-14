package com.kilikili.web.controller;

import com.kilikili.entity.vo.ResponseVO;
import com.kilikili.service.VideoSeriesService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;

@RestController("webSeriesController")
@RequestMapping("/series")
@Validated
public class SeriesController extends ABaseController {

    @Resource
    private VideoSeriesService videoSeriesService;

    @RequestMapping("/getVideoSeriesDetail")
    public ResponseVO getVideoSeriesDetail(@NotEmpty String seriesId) {
        return getSuccessResponseVO(videoSeriesService.getVideoSeriesDetail(seriesId));
    }
}
