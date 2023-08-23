package com.ksyun.start.camp.bean;

import lombok.Data;

@Data
public class ServiceInstanceInfo {

    String serviceName;
    String serviceId;
    String ipAddress;
    int port;

    public ServiceInstanceInfo(String serviceName, String serviceId, String ipAddress, int port) {
        this.serviceName = serviceName;
        this.serviceId = serviceId;
        this.ipAddress = ipAddress;
        this.port = port;
    }

    public ServiceInstanceInfo() {
    }

    // Constructors, getters, and setters
}
