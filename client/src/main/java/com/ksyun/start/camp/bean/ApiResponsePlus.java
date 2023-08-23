package com.ksyun.start.camp.bean;

import lombok.Data;

@Data
public class ApiResponsePlus {
    private int code;

    private Data data;

    @lombok.Data
    public static class Data{
        private Object result;
        private String serviceId;
    }
}
