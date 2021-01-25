package com.imooc.service;

import com.imooc.pojo.Items;
import com.imooc.pojo.ItemsImg;
import com.imooc.pojo.ItemsParam;
import com.imooc.pojo.ItemsSpec;
import com.imooc.utils.PagedGridResult;
import com.imooc.vo.CommentLevelCountsVO;
import com.imooc.vo.ShopcartVO;

import java.util.List;

/**
 * @author hzc
 * @date 2020-06-27 15:16
 */
public interface ItemService {

    public Items queryItemById(String itemId);

    public List<ItemsImg> queryItemImgList(String itemId);

    public List<ItemsSpec> queryItemSpecList(String itemId);

    public ItemsParam queryItemParam(String itemId);

    public CommentLevelCountsVO queryCommentCounts(String itemId);

    public PagedGridResult queryPagedComments(String itemId,Integer level,Integer page,Integer pageSize);

    public PagedGridResult searhItems(String keyword,String sort,Integer page,Integer pageSize);

    public PagedGridResult searhItems(Integer catId,String sort,Integer page,Integer pageSize);

    public List<ShopcartVO> queryItemsBySpecIds(String specIds);

    public ItemsSpec queryItemSpecById(String itemSpecId);

    /**
     * 获取主图地址
     *
     * @param itemId
     * @return
     */
    public String queryItemMainImgById(String itemId);

    public void decreaseItemSpecStock(String specId,int buyCount);

}
