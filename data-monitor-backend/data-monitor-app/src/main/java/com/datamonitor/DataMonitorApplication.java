package com.datamonitor;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 数智企业服务平台 - 启动类
 *
 * @author zhoulu
 * @since 2026-04-22
 */
@SpringBootApplication
@MapperScan("com.datamonitor.mapper")
public class DataMonitorApplication {

    public static void main(String[] args) {
        SpringApplication.run(DataMonitorApplication.class, args);
    }
}
