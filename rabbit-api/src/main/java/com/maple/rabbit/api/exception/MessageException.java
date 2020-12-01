package com.maple.rabbit.api.exception;

/**
 * 类描述：消息异常
 *
 * @author hzc
 * @date 2020/11/22 11:14 上午
 */
public class MessageException extends Exception {
    private static final long serialVersionUID = 9001300737401732420L;

    public MessageException() {
        super();
    }

    public MessageException(String message) {
        super(message);
    }

    public MessageException(String message, Throwable cause) {
        super(message, cause);
    }

    public MessageException(Throwable cause) {
        super(cause);
    }
}
