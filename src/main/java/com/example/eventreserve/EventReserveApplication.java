package com.example.eventreserve;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class EventReserveApplication {

    /** このアプリで使う時間帯（日本時間） */
    public static final String TIME_ZONE = "Asia/Tokyo";

    public static void main(String[] args) {
        // Render のサーバーは世界標準時（UTC）で動いているので、日本時間に固定する。
        // これがないと LocalDateTime.now() が9時間ずれて、予約期限の判定がおかしくなる。
        TimeZone.setDefault(TimeZone.getTimeZone(TIME_ZONE));
        SpringApplication.run(EventReserveApplication.class, args);
    }
}
