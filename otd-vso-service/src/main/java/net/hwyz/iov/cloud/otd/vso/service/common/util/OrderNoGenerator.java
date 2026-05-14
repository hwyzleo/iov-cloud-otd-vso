package net.hwyz.iov.cloud.otd.vso.service.common.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

public final class OrderNoGenerator {

    private static final String PREFIX = "VSO";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyMMdd");
    private static final AtomicInteger SEQUENCE = new AtomicInteger(1);
    private static final int MAX_SEQUENCE = 9999;
    private static String lastDate = "";

    private OrderNoGenerator() {
    }

    public static String generate() {
        LocalDateTime now = LocalDateTime.now();
        String currentDate = now.format(DATE_FORMAT);
        
        synchronized (OrderNoGenerator.class) {
            if (!currentDate.equals(lastDate)) {
                lastDate = currentDate;
                SEQUENCE.set(1);
            }
            
            int seq = SEQUENCE.getAndIncrement();
            if (seq > MAX_SEQUENCE) {
                SEQUENCE.set(1);
                seq = 1;
            }
            
            return PREFIX + currentDate + String.format("%04d", seq);
        }
    }
}