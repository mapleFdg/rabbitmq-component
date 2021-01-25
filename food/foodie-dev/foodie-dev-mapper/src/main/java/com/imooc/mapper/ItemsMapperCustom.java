package com.imooc.mapper;

import com.imooc.vo.ItemCommentVO;
import com.imooc.vo.SearchItemsVO;
import com.imooc.vo.ShopcartVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface ItemsMapperCustom {

    public List<ItemCommentVO> queryItemComments(@Param("paramsMap") Map<String,Object> paramsMap);

    public List<SearchItemsVO> searchItems(@Param("paramsMap")Map<String,Object> paramsMap);

    public List<SearchItemsVO> searchItemsByThirdCat(@Param("paramsMap")Map<String,Object> paramsMap);

    public List<ShopcartVO>  queryItemsBySpecIds(@Param("params")List<String> params);

    public Integer decreaseItemSpecStock(@Param("specId")String itemId,@Param("panddingCount") Integer panddingCount);
}