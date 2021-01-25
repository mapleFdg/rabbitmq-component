package com.imooc.controller;
/**
 * @author hzc
 * @date 2020-06-26 20:46
 */

import com.imooc.enums.YesOrNo;
import com.imooc.pojo.Carousel;
import com.imooc.pojo.Category;
import com.imooc.service.CarouselService;
import com.imooc.service.CategoryService;
import com.imooc.utils.JSONResult;
import com.imooc.utils.JsonUtils;
import com.imooc.utils.RedisOperator;
import com.imooc.vo.CategoryVO;
import com.imooc.vo.NewItemsVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author hzc
 * @date 2020-06-26 20:46
 */
@Api(value = "首页接口", tags = {"首页展示的相关接口"})
@RestController
@RequestMapping("index")
public class IndexController {

    @Autowired
    private CarouselService carouselService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private RedisOperator redisOperator;

    @ApiOperation(value = "获取首页轮播图列表", notes = "获取首页轮播图列表")
    @GetMapping("/carousel")
    public JSONResult carousel(){
        List<Carousel> carousels;
        String carouselStr = redisOperator.get("carousels");
        if(StringUtils.isBlank(carouselStr)){
            carousels= carouselService.queryAll(YesOrNo.YES.type);
            redisOperator.set("carousels", JsonUtils.objectToJson(carousels));
        }else{
            carousels = JsonUtils.jsonToList(carouselStr,Carousel.class);
        }
        return JSONResult.ok(carousels);
    }

    @ApiOperation(value = "获取商品分类（一级分类）", notes = "获取商品分类（一级分类）")
    @GetMapping("/cats")
    public JSONResult cats(){
        List<Category> categorys;
        String categoryStr = redisOperator.get("categorys");
        if(StringUtils.isBlank(categoryStr)){
            categorys= categoryService.queryAllRootLevelCat();
            redisOperator.set("categorys", JsonUtils.objectToJson(categorys));
        }else{
            categorys = JsonUtils.jsonToList(categoryStr,Category.class);
        }
        return JSONResult.ok(categorys);
    }

    @ApiOperation(value = "获取商品子分类", notes = "获取商品子分类")
    @GetMapping("/subCat/{rootCatId}")
    public JSONResult subCat(
            @ApiParam(name = "rootCatId",value = "一级分类ID",required = true)
            @PathVariable Integer rootCatId){
        if(rootCatId == null){
            return JSONResult.errorMsg("分类不存在");
        }

        List<CategoryVO> subCatList;
        String subCatListStr = redisOperator.get("subCat:"+rootCatId);
        if(StringUtils.isBlank(subCatListStr)){
            subCatList = categoryService.getSubCatList(rootCatId);
            redisOperator.set("subCat:"+rootCatId, JsonUtils.objectToJson(subCatList));
        }else{
            subCatList = JsonUtils.jsonToList(subCatListStr, CategoryVO.class);
        }
        return JSONResult.ok(subCatList);
    }

    @ApiOperation(value = "查询每一个一级分类下的最新6条商品数据", notes = "查询每一个一级分类下的最新6条商品数据")
    @GetMapping("/sixNewItems/{rootCatId}")
    public JSONResult sixNewItems(
            @ApiParam(name = "rootCatId",value = "一级分类ID",required = true)
            @PathVariable Integer rootCatId){
        if(rootCatId == null){
            return JSONResult.errorMsg("分类不存在");
        }

        List<NewItemsVO> sixNewItemsLazy;
        String sixNewItemsLazyStr = redisOperator.get("sixNewItemsLazy:"+rootCatId);
        if(StringUtils.isBlank(sixNewItemsLazyStr)){
            sixNewItemsLazy = categoryService.getSixNewItemsLazy(rootCatId);
            redisOperator.set("sixNewItemsLazy:"+rootCatId, JsonUtils.objectToJson(sixNewItemsLazy));
        }else{
            sixNewItemsLazy = JsonUtils.jsonToList(sixNewItemsLazyStr,NewItemsVO.class);
        }


        return JSONResult.ok(sixNewItemsLazy);
    }
}
