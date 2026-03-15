package com.hmdp.hmdp_common.utils;

public interface ILock {

    /**
     * 获取锁
     * @param timeoutSec 锁持有的超时时间，过期自动释放
     * @return
     */
    boolean tryLock(long timeoutSec);

    /**
     * 释放锁
     */
    void unlock();
}
