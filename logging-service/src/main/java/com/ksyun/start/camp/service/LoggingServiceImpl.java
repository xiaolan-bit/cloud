package com.ksyun.start.camp.service;

import com.ksyun.start.camp.ApiResponse;
import com.ksyun.start.camp.bean.ClientLog;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 日志服务的实现
 */
@Component
public class LoggingServiceImpl implements LoggingService {

    List<ClientLog> clientLogList = new ArrayList<>();
    //private List<ClientLog> clientLogList = new ArrayList<>();
    private Set<ClientLog> clientLogSet = new HashSet<>();


    @Override
    public ApiResponse setClientLog(ClientLog clientLog) {
        // 检查日志内容是否已存在于List中
        if (!isLogAlreadyExists(clientLog)) {
            int logId = clientLogList.size() + 1;
            clientLog.setLogId(logId);
            clientLogList.add(clientLog);
        }

        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setCode(200);
        apiResponse.setData("成功记录日志");
        return apiResponse;
    }

    // 检查List中是否已存在相同内容的日志
    private boolean isLogAlreadyExists(ClientLog clientLog) {
        for (ClientLog log : clientLogList) {
            if (log.getServiceName().equals(clientLog.getServiceName())
                    && log.getServiceId().equals(clientLog.getServiceId())
                    && log.getDatetime().equals(clientLog.getDatetime())
                    && log.getLevel().equals(clientLog.getLevel())
                    && log.getMessage().equals(clientLog.getMessage())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ApiResponse getLogList(String serviceId) {
        ApiResponse apiResponse = new ApiResponse();
        System.out.println(serviceId);
        if (serviceId == null) {
            // Return the last 5 log records in descending order when serviceId is empty
            int startIndex = Math.max(clientLogList.size() - 5, 0);
            List<ClientLog> lastFiveLogs = new ArrayList<>(clientLogList.subList(startIndex, clientLogList.size()));
            Collections.reverse(lastFiveLogs);

            apiResponse.setCode(200);
            apiResponse.setData(lastFiveLogs);
        } else {
            // Return logs for the specified serviceId
            List<ClientLog> logListForService = new ArrayList<>();
            for (int i = clientLogList.size() - 1; i >= 0 && logListForService.size() < 5; i--) {
                ClientLog log = clientLogList.get(i);
                if (log.getServiceId().equals(serviceId)) {
                    logListForService.add(log);
                }
            }

            apiResponse.setCode(200);
            apiResponse.setData(logListForService);
        }

        return apiResponse;
    }


}
