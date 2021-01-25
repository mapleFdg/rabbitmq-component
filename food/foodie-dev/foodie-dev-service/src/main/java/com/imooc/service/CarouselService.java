package com.imooc.service;
/**
 * @author hzc
 * @date 2020-06-26 20:35
 */

import com.imooc.pojo.Carousel;

import java.util.List;

/**
 * @author hzc
 * @date 2020-06-26 20:35
 */
public interface CarouselService {

   public List<Carousel> queryAll(Integer isShow);

}
