package com.imooc.service;

import com.imooc.pojo.Stu;

/**
 * @author hzc
 * @date 2020-06-23 23:01
 */
public interface StuService {

    public Stu getStuInfo(int id);

    public void saveStu();

    public void updateStu(int id);

    public void deleteStu(int id);

}
