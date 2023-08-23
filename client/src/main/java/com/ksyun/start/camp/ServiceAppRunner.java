package com.ksyun.start.camp;

import com.ksyun.start.camp.bean.ClientLog;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.ksyun.start.camp.bean.ServiceInstanceInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.springframework.web.client.RestTemplate;

/**
 * 服务启动运行逻辑
 */
@Component
public class ServiceAppRunner implements ApplicationRunner {

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public void run(ApplicationArguments args) throws Exception {

        // 此处代码会在 Boot 应用启动时执行

        // 开始编写你的逻辑，下面是提示
        // 1. 向 registry 服务注册当前服务
        // 2. 定期发送心跳逻辑
        registerServiceAtStartup();
        // 启动定时任务发送心跳请求
        startHeartbeatTask();
        startClientLogTask();
    }
    final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    // TODO
    @Value("${server.ipAddress}")
    String ipAddress;

    @Value("${server.port}")
    int port;

    @Value("${server.serviceId}")
    String serviceId;

    @Value("${spring.application.name}")
    String serviceName;

    @Value("${registry.host}")
    String registryIpAddress;

    @Value("${registry.port}")
    int registryPort;

//    log.host = 127.0.0.1
//    log.port = 8320
    @Value("${log.host}")
    String logHost;
    @Value("${log.port}")
    String logPort;

    final ScheduledExecutorService schedulerlog = Executors.newScheduledThreadPool(1);
    public void registerServiceAtStartup() {
        // 在服务提供者启动时自动注册到注册中心
        ServiceInstanceInfo serviceInstanceInfo = new ServiceInstanceInfo(serviceName, serviceId, ipAddress, port);
        String registryAddress = "http://"+registryIpAddress + ":" + registryPort;
        String registryUrl = registryAddress + "/api/register";
        System.out.println("Client registration successful.");
        System.out.println(registryUrl);
        restTemplate.postForObject(registryUrl, serviceInstanceInfo, String.class);
    }

    public void startHeartbeatTask() {
        String registryAddress = "http://"+registryIpAddress + ":" + registryPort;
        String heartBeatUrl = registryAddress + "/api/heartbeat";
        ServiceInstanceInfo serviceInstanceInfo = new ServiceInstanceInfo(serviceName, serviceId, ipAddress, port);

        // 每过58秒运行一次发送心跳请求的任务
        scheduler.scheduleAtFixedRate(() -> {
            restTemplate.postForObject(heartBeatUrl, serviceInstanceInfo, String.class);
        }, 58, 58, TimeUnit.SECONDS);
    }

    final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    public void startClientLogTask() {
        String logAddress = "http://" + logHost + ":" + logPort;
        String logAddressUrl = logAddress + "/api/logging";

        schedulerlog.scheduleAtFixedRate(() -> {
            LocalDateTime currentTime = LocalDateTime.now();
            String formattedDateTime = currentTime.format(formatter);

            ClientLog clientLog = new ClientLog(serviceName, serviceId, formattedDateTime, "info", "Client status is OK.");
            restTemplate.postForObject(logAddressUrl, clientLog, String.class);
        }, 1, 1, TimeUnit.SECONDS);
    }

}
