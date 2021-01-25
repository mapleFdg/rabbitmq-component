package com.imooc.controller;

import com.imooc.service.StuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

/**
 * @author hzc
 * @date 2020-06-23 23:06
 */
@RestController
@ApiIgnore
public class StuFooController {

    @Autowired
    private StuService stuService;

    @GetMapping("/getStu")
    public Object getStu(int id){
        return stuService.getStuInfo(id);
    }

    @PostMapping("/saveStu")
    public void saveStu(){
        stuService.saveStu();
    }

    @PostMapping("/updateStu")
    public void updateStu(int id){
        stuService.updateStu(id);
    }

    @PostMapping("/deleteStu")
    public void deleteStu(int id){
        stuService.deleteStu(id);
    }



}
