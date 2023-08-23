package com.ksyun.start.camp.service;

import com.ksyun.start.camp.bean.ApiResponsePlus;
import com.ksyun.start.camp.bean.InfoClass;
import com.ksyun.start.camp.bean.ServiceInstanceInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * 客户端服务实现
 */
@Component
public class ClientServiceImpl implements ClientService {

    @Autowired
    TimeService timeService;
    @Value("${server.serviceId}")
    String serviceId;
    RestTemplate restTemplate = new RestTemplate();

    @Value("${registry.host}")
    String registryIpAddress;

    @Value("${registry.port}")
    int registryPort;
    @Override
    public InfoClass getInfo() {


        // 开始编写你的逻辑，下面是提示
        // 1. 调用 TimeService 获取远端服务返回的时间
        // 2. 获取到自身的 serviceId 信息
        // 3. 组合相关信息返回
        //输出示例"Hello Kingsoft Clound Star Camp - [服务 ID] - 2023-07-25 12:34:56"
        //向注册中心发起请求，获取轮询得到的host+port
        InfoClass infoClass = new InfoClass();

        String registryUrl = "http://"+registryIpAddress+":"+registryPort+"/api/polling?name=time-service";
        ServiceInstanceInfo serviceInstanceInfo = restTemplate.getForObject(registryUrl,ServiceInstanceInfo.class);
        if (serviceInstanceInfo==null){
            infoClass.setError("没有可以提供的服务");
            infoClass.setResult(null);
            return infoClass;
        }

        //String url = "http://127.0.0.1:8280/api/getDateTime?style=full";
        String url = "http://"+serviceInstanceInfo.getIpAddress()+":"+serviceInstanceInfo.getPort()+"/api/getDateTime?style=full";
        ApiResponsePlus response = restTemplate.getForObject(url, ApiResponsePlus.class);
        response.getData().getResult();
        String result = "Hello Kingsoft Clound Star Camp - ["+serviceId+"] - "+response.getData().getResult();;
        //String result = "Hello Kingsoft Clound Star Camp - ["+serviceInstanceInfo.getServiceId()+"] - "+response.getData().getResult();;

        infoClass.setError(null);
        infoClass.setResult(result);
        return infoClass;
    }
}
