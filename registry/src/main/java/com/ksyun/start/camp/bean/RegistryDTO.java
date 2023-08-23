package com.ksyun.start.camp.bean;

import lombok.Data;

@Data
public class RegistryDTO {
    String serviceName;
    String serviceId;
    String ipAddress;
    int port;
}
