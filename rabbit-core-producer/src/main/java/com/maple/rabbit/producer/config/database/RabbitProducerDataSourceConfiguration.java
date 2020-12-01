package com.maple.rabbit.producer.config.database;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * 类描述：读取配置文件，生成数据源
 *
 * @author hzc
 * @date 2020/11/22 8:47 下午
 */
@Slf4j
@Configuration
@PropertySource({"classpath:rabbit-producer-message.properties"})
public class RabbitProducerDataSourceConfiguration {

    @Value("${rabbit.producer.druid.type}")
    private Class<? extends DataSource> dataSourceType;

    /**
     * 注解解析：
     * #@Primary 当有相同的bean名时，优先注入此bean
     * #@ConfigurationProperties 向Bean中注入配置文件的属性
     *
     * @return
     * @throws SQLException
     */
    @Bean(name = "rabbitProducerDataSource")
    @Primary
    @ConfigurationProperties(prefix = "rabbit.producer.druid.jdbc")
    public DataSource rabbitProducerDataSource() throws SQLException {
        DataSource rabbitProducerDataSource = DataSourceBuilder.create().type(dataSourceType).build();
        log.info("============= rabbitProducerDataSource : {} ================", rabbitProducerDataSource);
        return rabbitProducerDataSource;
    }

    public DataSourceProperties primaryDataSourceProperties(){
        return new DataSourceProperties();
    }

    public DataSource primaryDataSource(){
        return primaryDataSourceProperties().initializeDataSourceBuilder().build();
    }

}
