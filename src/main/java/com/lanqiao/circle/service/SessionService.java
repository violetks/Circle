package com.lanqiao.circle.service;

import com.lanqiao.circle.util.Result;
/**
 * @Author 钱琦瑛
 * @Date 2019/9/10 16:34
 */
public interface SessionService {


    //根据userId查询出当前登录用户的信息：userId,userName,avatarUrl
    public Result selectUserInfo(Integer userId);

    //根据userId查询会话列表
    public Result selectUserList(Integer userId);
}
