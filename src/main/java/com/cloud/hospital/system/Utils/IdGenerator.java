package com.cloud.hospital.system.Utils;

import org.springframework.stereotype.Component;


@Component
public class IdGenerator {

    // 起始时间戳 (2026-01-01)，一旦确定不可更改
    private final long twepoch = 1735689600000L;

    // 各部分占用的位数
    private final long workerIdBits = 5L;
    private final long datacenterIdBits = 5L;
    private final long sequenceBits = 12L;

    // 各部分最大的值
    private final long maxWorkerId = -1L ^ (-1L << workerIdBits);
    private final long maxDatacenterId = -1L ^ (-1L << datacenterIdBits);

    // 各部分向左移位的位数
    private final long workerIdShift = sequenceBits;
    private final long datacenterIdShift = sequenceBits + workerIdBits;
    private final long timestampLeftShift = sequenceBits + workerIdBits + datacenterIdBits;

    private final long sequenceMask = -1L ^ (-1L << sequenceBits);

    private long workerId = 1;     // 实际开发中通过配置文件读取
    private long datacenterId = 1; // 实际开发中通过配置文件读取
    private long sequence = 0L;
    private long lastTimestamp = -1L;

    /**
     * 核心方法：生成下一个唯一ID (线程安全)
     */
    public synchronized long nextId() {
        long timestamp = timeGen();

        // 1. 处理时钟回拨问题 (大厂面试必问)
        if (timestamp < lastTimestamp) {
            throw new RuntimeException("时钟回拨，拒绝生成ID");
        }

        // 2. 如果是同一毫秒生成的，则自增序列号
        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & sequenceMask;
            // 毫秒内序列溢出
            if (sequence == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            // 时间戳改变，序列号重置
            sequence = 0L;
        }

        lastTimestamp = timestamp;

        // 3. 利用位运算拼接 64 位 ID
        return ((timestamp - twepoch) << timestampLeftShift) 
                | (datacenterId << datacenterIdShift) 
                | (workerId << workerIdShift) 
                | sequence;
    }

    private long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    private long timeGen() {
        return System.currentTimeMillis();
    }
}