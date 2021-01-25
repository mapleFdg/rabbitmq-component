package com.imooc.exception;
/**
 * @author hzc
 * @date 2020-07-11 23:31
 */

import com.imooc.utils.JSONResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/**
 * Controller 异常处理类
 *
 * @author hzc
 * @date 2020-07-11 23:31
 */
@Slf4j
@ControllerAdvice
public class CustomExceptionHandler {

    /**
     * 处理文件上传过大异常 MaxUploadSizeExceededException
     *
     * @param e
     * @return
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public JSONResult handleMaxUploadFile(MaxUploadSizeExceededException e){
        log.error("上传的文件过大");
        return JSONResult.errorMsg("文件上传过大，不能超过500kb");
    }


}
