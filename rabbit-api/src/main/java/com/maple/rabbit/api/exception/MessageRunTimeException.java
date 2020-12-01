package com.maple.rabbit.api.exception;

/**
 * 类描述：消息运行时异常
 *
 * @author hzc
 * @date 2020/11/22 11:15 上午
 */
public class MessageRunTimeException extends RuntimeException {
    private static final long serialVersionUID = -2758343703977529714L;

    public MessageRunTimeException() {
        super();
    }

    public MessageRunTimeException(String message) {
        super(message);
    }

    public MessageRunTimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public MessageRunTimeException(Throwable cause) {
        super(cause);
    }
}
