package com.honyicare.urp.manager.task.support;

/**
 * @author hzc
 * @date 2020-09-15 18:17
 */

public class TaskException extends RuntimeException{

    private String message;

    public TaskException(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
