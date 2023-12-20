package com.roger.redistemppipeline.config;

import com.alibaba.fastjson2.JSONObject;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableMBeanExport;
import org.springframework.jmx.support.RegistrationPolicy;
import org.springframework.util.StringUtils;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Configuration
@Slf4j
@EnableMBeanExport(registration= RegistrationPolicy.IGNORE_EXISTING)
public class JedisConfig {

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private Integer redisPort;

    @Value("${spring.data.redis.password}")
    private String redisPassword;

    @Value("${spring.data.redis.database}")
    private Integer database;

    @Value("${spring.data.redis.jedis.pool.max-idle}")
    private Integer maxIdle;

    @Value("${spring.data.redis.jedis.pool.min-idle}")
    private Integer minIdle;

    @Value("${spring.data.redis.jedis.pool.max-active}")
    private Integer maxTotal;

    @Value("${spring.data.redis.jedis.pool.max-wait}")
    private Long maxWaitMillis;

/*    @Value("${spring.datasource.dbcp2.test-on-return}")
    private Boolean testOnReturn;

    @Value("${spring.datasource.dbcp2.test-on-borrow}")
    private Boolean testOnBorrow;*/


    @Bean
    public JedisPool jedisPool() {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxIdle(maxIdle);
        jedisPoolConfig.setMinIdle(minIdle);
        jedisPoolConfig.setMaxTotal(maxTotal);
        jedisPoolConfig.setMaxWaitMillis(maxWaitMillis);
//        jedisPoolConfig.setTestOnBorrow(testOnBorrow);
//        jedisPoolConfig.setTestOnReturn(testOnReturn);
        if (StringUtil.isNullOrEmpty(redisPassword)) {
            redisPassword = null;
        }
        log.info("redis properties {}, jedisPoolConfig={}", this.toString(), JSONObject.toJSONString(jedisPoolConfig));
        JedisPool jedisPool = new JedisPool(jedisPoolConfig, redisHost, redisPort, 0,redisPassword , database);
        return jedisPool;
    }


}
