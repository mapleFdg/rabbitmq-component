package com.maple.rabbit.api;

/**
 * 接口描述：发送消息回调函数
 *
 * @author hzc
 * @date 2020/11/22 11:20 上午
 */
public interface SendCallback {

    /**
     * 成功回调
     */
    void onSuccess();

    /**
     * 失败回调
     */
    void onFailure();

}
