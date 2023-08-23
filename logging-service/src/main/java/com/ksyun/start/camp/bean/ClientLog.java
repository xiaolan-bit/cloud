package com.ksyun.start.camp.bean;

import lombok.Data;

@Data
public class ClientLog {
    int logId;
    String serviceName;
    String serviceId;
    String datetime;
    String level;
    String message;
}
