package com.imooc.controller;
/**
 * @author hzc
 * @date 2020-06-27 15:29
 */

import com.imooc.pojo.Items;
import com.imooc.pojo.ItemsImg;
import com.imooc.pojo.ItemsParam;
import com.imooc.pojo.ItemsSpec;
import com.imooc.service.ItemService;
import com.imooc.utils.JSONResult;
import com.imooc.utils.PagedGridResult;
import com.imooc.vo.CommentLevelCountsVO;
import com.imooc.vo.ItemInfoVO;
import com.imooc.vo.ShopcartVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author hzc
 * @date 2020-06-27 15:29
 */
@Api(value = "商品信息接口", tags = "商品信息接口")
@RestController
@RequestMapping("items")
public class ItemsController extends BaseController{

    @Autowired
    private ItemService itemService;

    @ApiOperation(value = "获取商品信息",notes = "获取商品信息")
    @GetMapping("/info/{itemId}")
    public JSONResult info(
            @ApiParam(name = "itemId",value = "商品ID")
            @PathVariable String itemId){
        if(StringUtils.isBlank(itemId)){
            return JSONResult.errorMsg("商品ID不能为空");
        }

        Items items = itemService.queryItemById(itemId);
        List<ItemsImg> itemsImgs = itemService.queryItemImgList(itemId);
        List<ItemsSpec> itemsSpecs = itemService.queryItemSpecList(itemId);
        ItemsParam itemsParam = itemService.queryItemParam(itemId);

        ItemInfoVO itemInfoVO = new ItemInfoVO();
        itemInfoVO.setItem(items);
        itemInfoVO.setItemImgList(itemsImgs);
        itemInfoVO.setItemSpecList(itemsSpecs);
        itemInfoVO.setItemParams(itemsParam);
        return JSONResult.ok(itemInfoVO);
    }

    @ApiOperation(value = "查询商品评价数量",notes = "查询商品评价数量")
    @GetMapping("/commentLevel")
    public JSONResult commentLevel(
            @ApiParam(name = "itemId",value = "商品ID", required = true)
            @RequestParam String itemId){
        if(StringUtils.isBlank(itemId)){
            return JSONResult.errorMsg("商品ID不能为空");
        }
        CommentLevelCountsVO commentLevelCountsVO = itemService.queryCommentCounts(itemId);

        return JSONResult.ok(commentLevelCountsVO);
    }

    @ApiOperation(value = "查询商品评价",notes = "查询商品评价")
    @GetMapping("/comments")
    public JSONResult comments(
            @ApiParam(name = "itemId",value = "商品ID", required = true)
            @RequestParam String itemId,
            @ApiParam(name = "level",value = "评价类型", required = false)
            @RequestParam Integer level,
            @ApiParam(name = "page",value = "页数", required = false)
            @RequestParam Integer page,
            @ApiParam(name = "pageSize",value = "每页数量", required = false)
            @RequestParam Integer pageSize){
        if(StringUtils.isBlank(itemId)){
            return JSONResult.errorMsg("商品ID不能为空");
        }
        if(page == null){
            page = 1;
        }
        if(pageSize == null){
            pageSize = COMMON_PAGE_SIZE;
        }

        PagedGridResult pagedGridResult = itemService.queryPagedComments(itemId, level, page, pageSize);

        return JSONResult.ok(pagedGridResult);
    }

    @ApiOperation(value = "根据关键字搜索商品" , notes = "根据关键字搜索商品")
    @GetMapping("/search")
    public JSONResult search(
            @ApiParam(name = "keywords",value = "搜索值",required = true)
            @RequestParam String keywords,
            @ApiParam(name = "sort",value = "排序条件",required = false)
            @RequestParam String sort,
            @ApiParam(name = "page",value = "页数", required = false)
            @RequestParam Integer page,
            @ApiParam(name = "pageSize",value = "每页数量", required = false)
            @RequestParam Integer pageSize){

        if(StringUtils.isBlank(keywords)){
            return JSONResult.errorMsg("");
        }

        if (page == null) {
            page = 1;
        }

        if (pageSize == null) {
            pageSize = PAGE_SIZE;
        }

        PagedGridResult pagedGridResult = itemService.searhItems(keywords, sort, page, pageSize);

        return  JSONResult.ok(pagedGridResult);
    }

    @ApiOperation(value = "通过分类id搜索商品列表" , notes = "通过分类id搜索商品列表")
    @GetMapping("/catItems")
    public JSONResult catItems(
            @ApiParam(name = "catId",value = "三级分类id",required = true)
            @RequestParam Integer catId,
            @ApiParam(name = "sort",value = "排序条件",required = false)
            @RequestParam String sort,
            @ApiParam(name = "page",value = "页数", required = false)
            @RequestParam Integer page,
            @ApiParam(name = "pageSize",value = "每页数量", required = false)
            @RequestParam Integer pageSize){

        if(catId == null){
            return JSONResult.errorMsg("");
        }

        if (page == null) {
            page = 1;
        }

        if (pageSize == null) {
            pageSize = PAGE_SIZE;
        }

        PagedGridResult pagedGridResult = itemService.searhItems(catId, sort, page, pageSize);

        return  JSONResult.ok(pagedGridResult);
    }

    // 用于用户长时间未登录网站，刷新购物车中的数据（主要是商品价格），类似京东淘宝
    @ApiOperation(value = "根据商品规格ids查找最新的商品数据", notes = "根据商品规格ids查找最新的商品数据", httpMethod = "GET")
    @GetMapping("/refresh")
    public JSONResult refresh(
            @ApiParam(name = "itemSpecIds", value = "拼接的规格ids", required = true, example = "1001,1003,1005")
            @RequestParam String itemSpecIds) {

        if (StringUtils.isBlank(itemSpecIds)) {
            return JSONResult.ok();
        }

        List<ShopcartVO> list = itemService.queryItemsBySpecIds(itemSpecIds);

        return JSONResult.ok(list);
    }

}
