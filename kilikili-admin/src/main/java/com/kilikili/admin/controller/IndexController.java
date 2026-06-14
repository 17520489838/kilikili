package com.kilikili.admin.controller;

import com.kilikili.entity.vo.ResponseVO;
import com.kilikili.service.StatisticsService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController("adminIndexController")
@RequestMapping("/index")
@Validated
public class IndexController extends ABaseController {

    @Resource
    private StatisticsService statisticsService;

    @RequestMapping("/getActualTimeStatisticsInfo")
    public ResponseVO getActualTimeStatisticsInfo() {
        return getSuccessResponseVO(statisticsService.getActualTimeStatisticsInfo());
    }

    @RequestMapping("/getWeekStatisticsInfo")
    public ResponseVO getWeekStatisticsInfo() {
        return getSuccessResponseVO(statisticsService.getWeekStatisticsInfo());
    }
}
