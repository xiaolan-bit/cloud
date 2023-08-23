package com.ksyun.start.camp;

import com.alibaba.fastjson.JSON;
import com.ksyun.start.camp.bean.ApiResponsePlus;
import com.ksyun.start.camp.bean.GetTimeDTO;
import com.ksyun.start.camp.service.SimpleTimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.fastjson.JSONObject;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ServiceController {

    @Autowired
    private SimpleTimeService simpleTimeService;
    @Value("${server.serviceId}")
    private String serviceId;

    @GetMapping("/getDateTime")
    public ApiResponsePlus getDateTime(String style) {
        ApiResponsePlus apiResponsePlus = new ApiResponsePlus();
        apiResponsePlus.setCode(200);
        ApiResponsePlus.Data data = new ApiResponsePlus.Data();

        data.setServiceId(serviceId);
        data.setResult(simpleTimeService.getDateTime(style));

        apiResponsePlus.setData(data);
        return apiResponsePlus;
    }

}
