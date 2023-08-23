package com.ksyun.start.camp;

import com.ksyun.start.camp.bean.ApiResponsePlus;
import com.ksyun.start.camp.bean.InfoClass;
import com.ksyun.start.camp.service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 默认的客户端 API Controller
 */
@RestController
public class ClientController {

    // 在这里开始编写你的相关接口实现代码
    // 返回值对象使用 ApiResponse 类

    // 提示：调用 ClientService
    @Autowired
    ClientService clientService;

    @GetMapping("/api/getInfo")
    public InfoClass getInfo() {
        return clientService.getInfo();
    }
}
