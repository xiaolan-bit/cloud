package com.ksyun.start.camp;

import com.ksyun.start.camp.bean.ClientLog;
import com.ksyun.start.camp.service.LoggingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 实现日志服务 API
 */
@RestController
@RequestMapping("/api")
public class ServiceController {

    // TODO: 实现日志服务 API
    @Autowired
    LoggingService loggingService;
    @PostMapping(value = "/logging")
    public ApiResponse setLog(@RequestBody ClientLog clientLog){
        return loggingService.setClientLog(clientLog);
    }

    @GetMapping("/list")
    public ApiResponse getLogList(String service){
        return loggingService.getLogList(service);
    }

}
