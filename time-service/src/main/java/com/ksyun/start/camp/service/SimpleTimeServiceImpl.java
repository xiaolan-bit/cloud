package com.ksyun.start.camp.service;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * 代表简单时间服务实现
 */
@Component
public class SimpleTimeServiceImpl implements SimpleTimeService {

    @Override
    public String getDateTime(String style) {
        Instant now = Instant.now();
        String formattedDateTime;

        switch (style) {
            case "full":
                formattedDateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("GMT")).format(now);
                break;
            case "date":
                formattedDateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.of("GMT")).format(now);
                break;
            case "time":
                formattedDateTime = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.of("GMT")).format(now);
                break;
            case "unix":
                formattedDateTime = String.valueOf(now.toEpochMilli());
                break;
            default:
                formattedDateTime = "Invalid style. Please use one of: full, date, time, unix";
        }

        return formattedDateTime;
    }
}
