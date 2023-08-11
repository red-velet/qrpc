package icu.chiou.protection;

/**
 * Author: chiou
 * createTime: 2023/8/1
 * Description: 基于令牌桶的限流器
 */
public class TokenBucketRateLimiter implements RateLimiter {
    //令牌桶 - 令牌>0,放行
    private int tokens;

    //容量
    private int capacity;

    //补充速率
    //定时器添加
    //每次放行请求后添加
    private int rate;

    private Long lastTokenTime;

    public int getCapacity() {
        return capacity;
    }

    public int getRate() {
        return rate;
    }

    public TokenBucketRateLimiter(int capacity, int rate) {
        this.capacity = capacity;
        this.rate = rate;
        this.tokens = capacity;
        this.lastTokenTime = System.currentTimeMillis();
    }

    @Override
    public synchronized Boolean isAllowRequest() {
        //1.添加令牌
        //计算从现在到上一次的时间间隔的令牌数
        Long currTime = System.currentTimeMillis();
        Long timeInterval = currTime - lastTokenTime;
        if (timeInterval >= 1000) {
            int needAddTokens = (int) ((timeInterval * rate) / 1000);
            //给令牌桶添加令牌
            tokens = Math.min(capacity, tokens + needAddTokens);
            this.lastTokenTime = System.currentTimeMillis();
        }

        //2.分发出令牌
        if (tokens >= 0) {
            tokens--;
            return true;
        } else {
            return false;
        }
    }

    public static void main(String[] args) {
        TokenBucketRateLimiter tokenBucketRateLimiter = new TokenBucketRateLimiter(10, 10);
        for (int i = 0; i < 1000; i++) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            Boolean allowRequest = tokenBucketRateLimiter.isAllowRequest();
            System.out.println("allowRequest = " + allowRequest);
        }
    }
}
