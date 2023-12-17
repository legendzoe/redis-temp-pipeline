package com.roger.redistemppipeline;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.roger.redistemppipeline.mapper")
public class RedisTempPipelineApplication {

    public static void main(String[] args) {
        SpringApplication.run(RedisTempPipelineApplication.class, args);
    }

}
