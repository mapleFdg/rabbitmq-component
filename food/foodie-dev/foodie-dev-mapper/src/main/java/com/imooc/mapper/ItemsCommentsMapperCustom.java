package com.imooc.mapper;

import com.imooc.vo.MyCommentVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface ItemsCommentsMapperCustom {

    public int saveComments(@Param("paramMap")Map<String,Object> paramMap);

    public List<MyCommentVO> queryMyComments(String userId);

}