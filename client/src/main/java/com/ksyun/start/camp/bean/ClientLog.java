package com.ksyun.start.camp.bean;

import lombok.Data;

@Data
public class ClientLog {
    String serviceName;
    String serviceId;
    String datetime;
    String level;
    String message;

    public ClientLog(String serviceName, String serviceId, String datetime, String level, String message) {
        this.serviceName = serviceName;
        this.serviceId = serviceId;
        this.datetime = datetime;
        this.level = level;
        this.message = message;
    }
}
