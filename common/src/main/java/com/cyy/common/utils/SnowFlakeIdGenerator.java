package com.cyy.common.utils;

/**
 * Snowflake Algorithm Generator Tool
 */
public class SnowFlakeIdGenerator {

    private static final SnowFlakeIdWorker ID_WORKER;
    
    static {
        ID_WORKER = new SnowFlakeIdWorker();
    }

    public static long generateId() {
        return ID_WORKER.nextId();
    }
}