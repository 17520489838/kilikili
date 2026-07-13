package com.kilikili.web.controller;

import com.kilikili.component.RedisComponent;
import com.kilikili.entity.dto.TokenUserInfoDto;
import com.kilikili.entity.dto.VideoMetaOptimizeResult;
import com.kilikili.entity.enums.ResponseCodeEnum;
import com.kilikili.entity.vo.ResponseVO;
import com.kilikili.exception.BusinessException;
import com.kilikili.service.AIVideoService;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;

@RestController("webAIController")
@RequestMapping("/ai")
@Validated
public class AIController extends ABaseController {

    private static final Logger logger = LoggerFactory.getLogger(AIController.class);

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private AIVideoService aiVideoService;

    @Resource
    private RedissonClient redissonClient;

    private static final String RATE_LIMIT_KEY_PREFIX = "kilikili:ai:ratelimit:optimize:";

    @RequestMapping("/optimizeVideoMeta")
    public ResponseVO optimizeVideoMeta(@NotEmpty String title, String description) {
        String token = getTokenFromCookie();
        TokenUserInfoDto tokenUserInfoDto = redisComponent.getTokenUserInfo(token);
        if (tokenUserInfoDto == null) {
            throw new BusinessException(ResponseCodeEnum.UNAUTHORIZED);
        }

        // Rate limit: 3 req/min per user
        if (!checkRateLimit(tokenUserInfoDto.getUserId())) {
            throw new BusinessException(429, "请求过于频繁，请稍后再试");
        }

        VideoMetaOptimizeResult result = aiVideoService.optimizeVideoMeta(title, description);
        return getSuccessResponseVO(result);
    }

    private boolean checkRateLimit(String userId) {
        try {
            String key = RATE_LIMIT_KEY_PREFIX + userId;
            RRateLimiter rateLimiter = redissonClient.getRateLimiter(key);
            rateLimiter.trySetRate(RateType.OVERALL, 3, 1, RateIntervalUnit.MINUTES);
            if (!rateLimiter.tryAcquire()) {
                // Fallback: try IP-based limit
                String ipKey = RATE_LIMIT_KEY_PREFIX + "ip:" + getIpAddr();
                RRateLimiter ipLimiter = redissonClient.getRateLimiter(ipKey);
                ipLimiter.trySetRate(RateType.OVERALL, 3, 1, RateIntervalUnit.MINUTES);
                return ipLimiter.tryAcquire();
            }
            return true;
        } catch (Exception e) {
            logger.warn("限流检查异常，放行请求", e);
            return true;
        }
    }
}
