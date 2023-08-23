package com.ksyun.start.camp.controller;


//import com.ksyun.cloud.cloudinitlitar.bean.ServiceInstanceInfo;
import com.ksyun.start.camp.ApiResponse;
import com.ksyun.start.camp.bean.RegistryDTO;
import com.ksyun.start.camp.bean.ServiceInstanceInfo;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.ArrayList;

//import com.ksyun.cloud.cloudinitlitar.bean.ServiceInstanceInfo;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
public class RegistryController {

    private final Map<String, List<ServiceInstanceInfo>> serviceRegistry = new HashMap<>();
    private final Map<String, LocalDateTime> lastHeartbeatMap = new ConcurrentHashMap<>();
    private static final int HEARTBEAT_INTERVAL = 60; // 心跳间隔，单位：秒
    //private static final int EXPIRATION_TIME = 2 * HEARTBEAT_INTERVAL; // 超过2倍心跳间隔没有心跳，则服务过期
    private static final int EXPIRATION_TIME = HEARTBEAT_INTERVAL;// 超过1倍心跳间隔没有心跳，则服务过期
    private final Map<String, ScheduledExecutorService> heartbeatTimers = new ConcurrentHashMap<>();

    //轮询算法的数字 从1开始  每次需要判断服务是否还在，判断服务的数量
    private int pollingNum = 0;

    private ApiResponse apiResponse = new ApiResponse();


    @RequestMapping(value = "/api/register", produces = "application/json")
    public ApiResponse register(@RequestBody ServiceInstanceInfo param, HttpServletRequest request) {
        List<ServiceInstanceInfo> allInstances = new ArrayList<>();
        serviceRegistry.forEach((serviceName, instances) -> allInstances.addAll(instances));

        // 去重处理，保留每个instanceId的第一个元素
        List<ServiceInstanceInfo> distinctInstances = allInstances.stream()
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(ServiceInstanceInfo::getServiceId, Function.identity(), (existing, replacement) -> existing),
                        map -> new ArrayList<>(map.values())
                ));
        String sameServiceId = null;
        for (ServiceInstanceInfo s: distinctInstances
             ) {
            if (s.getServiceId().equals(param.getServiceId())){
                sameServiceId = s.getServiceId();
            }
        }
        if (sameServiceId!=null){
            apiResponse.setCode(502);
            apiResponse.setData("This service existed.");
            return apiResponse;
        }
        String instanceAddress = param.getIpAddress() + ":" +param.getPort();
        //String instanceAddress = instanceInfo.getHost() + ":" + instanceInfo.getPort();
        serviceRegistry.computeIfAbsent(param.getServiceName(), k -> new ArrayList<>()).add(param);
        lastHeartbeatMap.put(instanceAddress, LocalDateTime.now());

        // 检查服务是否已经存在计时器，如果存在则取消旧计时器
        ScheduledExecutorService existingTimer = heartbeatTimers.get(instanceAddress);
        if (existingTimer != null) {
            existingTimer.shutdown();
        }

        // 创建并启动新的心跳计时器
        ScheduledExecutorService heartbeatTimer = Executors.newSingleThreadScheduledExecutor();
        heartbeatTimer.scheduleAtFixedRate(() -> {
            // 检查心跳时间戳，如果超过一定时间没有收到心跳消息，则将服务注销
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime lastHeartbeatTime = lastHeartbeatMap.get(instanceAddress);
            if (lastHeartbeatTime.plusSeconds(EXPIRATION_TIME).isBefore(now)) {
                System.out.println("服务 " + instanceAddress + " 已过期，正在注销...");
                unregister(param,request);
            }
        }, 0, HEARTBEAT_INTERVAL, TimeUnit.SECONDS);

        // 将新的计时器存储到心跳计时器Map中
        heartbeatTimers.put(instanceAddress, heartbeatTimer);
        apiResponse.setCode(200);
        apiResponse.setData(param.getServiceName()+" on "+instanceAddress+" registry.");
        return apiResponse;
    }


    @RequestMapping(value = "/api/unregister", produces = "application/json")
    public ApiResponse unregister(@RequestBody ServiceInstanceInfo param, HttpServletRequest request) {
        String serviceNameToRemove = param.getServiceName();
        String instanceAddress = param.getIpAddress() + ":" + param.getPort();
        List<ServiceInstanceInfo> instances = serviceRegistry.get(serviceNameToRemove);
        System.out.println("instances:"+instances);
        if (instances==null){
            apiResponse.setCode(404);
            apiResponse.setData("not found such instance");
            return apiResponse;
        }
        int count = 0;
        List<ServiceInstanceInfo> suitInstances = getSuitInstances(serviceNameToRemove);
        for (ServiceInstanceInfo s:suitInstances
             ) {
            if(param.getServiceId().equals(s.getServiceId())&&param.getIpAddress().equals(s.getIpAddress())&&param.getPort()==s.getPort()){
                System.out.println(1);
                count++;
                break;
            }
        }
        if (count==0){
                apiResponse.setCode(503);
                apiResponse.setData("error unregister");
                return apiResponse;
        }
        if (instances != null) {
            instances.removeIf(instance -> (instance.getIpAddress() + ":" + instance.getPort()).equals(instanceAddress));
            if (instances.isEmpty()) {
                serviceRegistry.remove(serviceNameToRemove);
            }
            lastHeartbeatMap.remove(instanceAddress);
            System.out.println("Service at " + instanceAddress + " expired. Unregistering...");
        }

        // 关闭心跳计时器
        ScheduledExecutorService heartbeatTimer = heartbeatTimers.get(instanceAddress);
        if (heartbeatTimer != null) {
            heartbeatTimer.shutdown();
            heartbeatTimers.remove(instanceAddress);
        }
        apiResponse.setCode(200);
        apiResponse.setData(param.getServiceName() + " on " + instanceAddress + " unregistered.");
        return apiResponse;
    }

    public List<ServiceInstanceInfo> getSuitInstances(String name) {
        List<ServiceInstanceInfo> allInstances = new ArrayList<>();
        serviceRegistry.forEach((serviceName, instances) -> allInstances.addAll(instances));

        // 去重处理，保留每个instanceId的第一个元素
        List<ServiceInstanceInfo> distinctInstances = allInstances.stream()
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(ServiceInstanceInfo::getServiceId, Function.identity(), (existing, replacement) -> existing),
                        map -> new ArrayList<>(map.values())
                ));
        if(name==null){
            apiResponse.setCode(200);
            apiResponse.setData(distinctInstances.toString());
            return distinctInstances;
        }
        List<ServiceInstanceInfo> selectInstances = new ArrayList<>();
        System.out.println(distinctInstances);
        for (ServiceInstanceInfo s:distinctInstances
        ) {
            System.out.println(s.getServiceName()+name);
            if (s.getServiceName().equals(name)){
                //System.out.println(1);
                selectInstances.add(s);
            }
        }
        return selectInstances;
    }

    @GetMapping("/api/discovery")
    public ApiResponse getAll(String name) {
        List<ServiceInstanceInfo> allInstances = new ArrayList<>();
        serviceRegistry.forEach((serviceName, instances) -> allInstances.addAll(instances));

        // 去重处理，保留每个instanceId的第一个元素
        List<ServiceInstanceInfo> distinctInstances = allInstances.stream()
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(ServiceInstanceInfo::getServiceId, Function.identity(), (existing, replacement) -> existing),
                        map -> new ArrayList<>(map.values())
                ));
        if(name==null){
            apiResponse.setCode(200);
            apiResponse.setData(distinctInstances);
            return apiResponse;
        }
//        List<ServiceInstanceInfo> selectInstances = new ArrayList<>();
//        System.out.println(distinctInstances);
//        for (ServiceInstanceInfo s:distinctInstances
//             ) {
//            System.out.println(s.getServiceName()+name);
//            if (s.getServiceName().equals(name)){
//                //System.out.println(1);
//                selectInstances.add(s);
//            }
//        }
//        System.out.println(selectInstances);
        ServiceInstanceInfo pollingService = pollingService(name);
        apiResponse.setCode(200);
        apiResponse.setData(pollingService);
        return apiResponse;
    }


    @GetMapping("/api/polling")
    public ServiceInstanceInfo pollingService(String name) {
        List<ServiceInstanceInfo> allInstances = new ArrayList<>();
        serviceRegistry.forEach((serviceName, instances) -> allInstances.addAll(instances));

        // 去重处理，保留每个instanceId的第一个元素
        List<ServiceInstanceInfo> distinctInstances = allInstances.stream()
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(ServiceInstanceInfo::getServiceId, Function.identity(), (existing, replacement) -> existing),
                        map -> new ArrayList<>(map.values())
                ));

        List<ServiceInstanceInfo> selectInstances = new ArrayList<>();
        for (ServiceInstanceInfo s : distinctInstances) {
            if (s.getServiceName().equals(name)) {
                selectInstances.add(s);
            }
        }

        if (!selectInstances.isEmpty()) {
            // 只有在给定名称存在实例时才执行轮询
            int serviceNum = selectInstances.size();

            if (pollingNum >= serviceNum) {
                pollingNum = 0; // 当pollingNum超过实例数时重置为0
            }

            ServiceInstanceInfo selectService = selectInstances.get(pollingNum);
            pollingNum++;
            return selectService;
        } else {
            // 当给定名称没有实例时，返回一些默认的ServiceInstanceInfo对象
            return null;
        }
    }

    @PostMapping(value = "/api/heartbeat", produces = "application/json")
    public ApiResponse heartbeat(@RequestBody ServiceInstanceInfo param, HttpServletRequest request){
        String instanceAddress = param.getIpAddress() + ":" +param.getPort();
        //String instanceAddress = instanceInfo.getHost() + ":" + instanceInfo.getPort();
        //serviceRegistry.computeIfAbsent(param.getServiceName(), k -> new ArrayList<>()).add(param);
        List<ServiceInstanceInfo> allInstances = new ArrayList<>();
        serviceRegistry.forEach((serviceName, instances) -> allInstances.addAll(instances));

        // 去重处理，保留每个instanceId的第一个元素
        List<ServiceInstanceInfo> distinctInstances = allInstances.stream()
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(ServiceInstanceInfo::getServiceId, Function.identity(), (existing, replacement) -> existing),
                        map -> new ArrayList<>(map.values())
                ));
        ServiceInstanceInfo heartService = null;
        for (ServiceInstanceInfo s: distinctInstances
             ) {
            if(s.getServiceId().equals(param.getServiceId())&&s.getIpAddress().equals(param.getIpAddress())&&s.getPort()==param.getPort()){
                heartService = s;
                //param =heartService;
            }
        }
        System.out.println(heartService);
        lastHeartbeatMap.put(instanceAddress, LocalDateTime.now());
        if(heartService==null){
            apiResponse.setCode(502);
            apiResponse.setData("No such service in registry"+ "on "+instanceAddress+" heatbeat.");
            return apiResponse;
        }
        // 检查服务是否已经存在计时器，如果存在则取消旧计时器
        ScheduledExecutorService existingTimer = heartbeatTimers.get(instanceAddress);
        if (existingTimer != null) {
            existingTimer.shutdown();
        }

        // 创建并启动新的心跳计时器
        ScheduledExecutorService heartbeatTimer = Executors.newSingleThreadScheduledExecutor();
        heartbeatTimer.scheduleAtFixedRate(() -> { }, 0, HEARTBEAT_INTERVAL, TimeUnit.SECONDS);

        // 将新的计时器存储到心跳计时器Map中
        heartbeatTimers.put(instanceAddress, heartbeatTimer);
        apiResponse.setCode(200);
        apiResponse.setData(heartService.getServiceName()+" on "+instanceAddress+" heatbeat.");
        return apiResponse;
    }


    private void unregisterExpiredService(String instanceAddress) {
        // Find the serviceName corresponding to the instanceAddress
        String serviceNameToRemove = null;
        for (Map.Entry<String, List<ServiceInstanceInfo>> entry : serviceRegistry.entrySet()) {
            List<ServiceInstanceInfo> instances = entry.getValue();
            for (ServiceInstanceInfo instance : instances) {
                String address = instance.getIpAddress() + ":" + instance.getPort();
                if (address.equals(instanceAddress)) {
                    serviceNameToRemove = entry.getKey();
                    break;
                }
            }
            if (serviceNameToRemove != null) {
                break;
            }
        }

        if (serviceNameToRemove != null) {
            List<ServiceInstanceInfo> instances = serviceRegistry.get(serviceNameToRemove);
            instances.removeIf(instance -> (instance.getIpAddress() + ":" + instance.getPort()).equals(instanceAddress));
            if (instances.isEmpty()) {
                serviceRegistry.remove(serviceNameToRemove);
            }
            lastHeartbeatMap.remove(instanceAddress);
            System.out.println("Service at " + instanceAddress + " expired. Unregistering...");
        }
    }


    @DeleteMapping("/deregister/{serviceName}/{instanceId}")
    public void deregisterService(@PathVariable String serviceName, @PathVariable String instanceId) {
        List<ServiceInstanceInfo> instances = serviceRegistry.get(serviceName);
        if (instances != null) {
            instances.removeIf(instance -> instanceId.equals(instance.getServiceId()));
            if (instances.isEmpty()) {
                serviceRegistry.remove(serviceName);
            }
        }
    }

    @GetMapping("/discover/{serviceName}")
    public List<ServiceInstanceInfo> discoverService(@PathVariable String serviceName) {
        return serviceRegistry.getOrDefault(serviceName, new ArrayList<>());
    }

    @GetMapping("/all")
    public List<ServiceInstanceInfo> getAllServices() {
//        List<ServiceInstanceInfo> allInstances = new ArrayList<>();
//        serviceRegistry.forEach((serviceName, instances) -> allInstances.addAll(instances));
//        return allInstances;
        List<ServiceInstanceInfo> allInstances = new ArrayList<>();
        serviceRegistry.forEach((serviceName, instances) -> allInstances.addAll(instances));

        // 去重处理，保留每个instanceId的第一个元素
        List<ServiceInstanceInfo> distinctInstances = allInstances.stream()
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(ServiceInstanceInfo::getServiceId, Function.identity(), (existing, replacement) -> existing),
                        map -> new ArrayList<>(map.values())
                ));

        return distinctInstances;
    }

    // 在注册中心启动后，定时检查服务的心跳时间戳
    @PostConstruct
    public void startHeartbeatChecker() {
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(this::checkHeartbeats, 0, HEARTBEAT_INTERVAL, TimeUnit.SECONDS);
    }

    // 检查服务的心跳时间戳，如果超过一定时间没有收到心跳消息，则将服务注销
//    private void checkHeartbeats() {
//        LocalDateTime now = LocalDateTime.now();
//        lastHeartbeatMap.forEach((instanceAddress, lastHeartbeatTime) -> {
//            if (lastHeartbeatTime.plusSeconds(EXPIRATION_TIME).isBefore(now)) {
//                System.out.println("Service at " + instanceAddress + " expired. Unregistering...");
//                unregisterExpiredService(instanceAddress);
//            }
//        });
//    }
// 检查服务的心跳时间戳，如果超过一定时间没有收到心跳消息，则将服务注销
    private synchronized void checkHeartbeats() {
        LocalDateTime now = LocalDateTime.now();
        List<String> expiredAddresses = new ArrayList<>();

        lastHeartbeatMap.forEach((instanceAddress, lastHeartbeatTime) -> {
            if (lastHeartbeatTime.plusSeconds(EXPIRATION_TIME).isBefore(now)) {
                System.out.println("Service at " + instanceAddress + " expired. Unregistering...");
                expiredAddresses.add(instanceAddress);
            }
        });

        // 注销过期服务
        expiredAddresses.forEach(this::unregisterExpiredService);
    }

}
