package com.imooc.resource;
/**
 * @author hzc
 * @date 2020-07-10 23:31
 */

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 * @author hzc
 * @date 2020-07-10 23:31
 */
@Component
@ConfigurationProperties(prefix = "file")
@PropertySource("classpath:file-upload-dev.properties")
@Data
public class FileUpload {

    private String imageUserFaceLocation;

    private String imageServerUrl;

}
