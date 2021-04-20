package com.maple.rabbit.common.mybatis;

import com.maple.rabbit.api.Message;
import com.maple.rabbit.common.utils.FastJsonConvertUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 类描述：
 *
 * @author hzc
 * @date 2021/4/20 2:25 下午
 */
public class MessageJsonTypeHandler extends BaseTypeHandler<Message> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Message parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, FastJsonConvertUtil.convertObjectToJSON(parameter));
    }

    @Override
    public Message getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        if(null != value && !StringUtils.isBlank(value)) {
            return FastJsonConvertUtil.convertJSONToObject(rs.getString(columnName), Message.class);
        }
        return null;
    }

    @Override
    public Message getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        if(null != value && !StringUtils.isBlank(value)) {
            return FastJsonConvertUtil.convertJSONToObject(rs.getString(columnIndex), Message.class);
        }
        return null;
    }

    @Override
    public Message getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String value = cs.getString(columnIndex);
        if(null != value && !StringUtils.isBlank(value)) {
            return FastJsonConvertUtil.convertJSONToObject(cs.getString(columnIndex), Message.class);
        }
        return null;
    }
}
