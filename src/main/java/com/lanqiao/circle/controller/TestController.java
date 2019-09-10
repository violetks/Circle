package com.lanqiao.circle.controller;

import com.lanqiao.circle.config.PathConfig;
import com.lanqiao.circle.mapper.UsersMapper;
import com.lanqiao.circle.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * @author 刘佳昇
 * @Date 2019/9/10 11:24
 */

@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    PathConfig pathConfig;


    @Autowired
    UsersMapper usersMapper;

    @GetMapping("/testGetPath")
    public Result testGetPath(){
        return Result.createSuccessResult(pathConfig.getChatImgPath());
    }

    @GetMapping("/testMapGet")
    public Result testMapGet(){
        List<HashMap> allUser = usersMapper.getAllUser();
        for (HashMap hashMap : allUser) {
            System.out.println(hashMap.toString());
        }
        return Result.createSuccessResult();
}
}
