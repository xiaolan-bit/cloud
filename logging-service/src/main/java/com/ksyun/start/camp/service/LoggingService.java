package com.ksyun.start.camp.service;

import com.ksyun.start.camp.ApiResponse;
import com.ksyun.start.camp.bean.ClientLog;

/**
 * 日志服务实现接口
 */
public interface LoggingService {

    // TODO: 实现日志服务接口
    // 此处不再重复提示骨架代码，可参考其他 Service 接口的定义
    ApiResponse setClientLog(ClientLog clientLog);

    ApiResponse getLogList(String serviceId);
}
