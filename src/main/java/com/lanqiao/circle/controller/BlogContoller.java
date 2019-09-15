package com.lanqiao.circle.controller;

import com.alibaba.fastjson.JSONObject;
import com.lanqiao.circle.annotations.UserLoginToken;
import com.lanqiao.circle.entity.Blog;
import com.lanqiao.circle.entity.BlogItem;
import com.lanqiao.circle.entity.Comments;
import com.lanqiao.circle.entity.LikeKey;
import com.lanqiao.circle.mapper.BlogItemMapper;
import com.lanqiao.circle.mapper.BlogMapper;
import com.lanqiao.circle.service.BlogService;
import com.lanqiao.circle.service.CommentsService;
import com.lanqiao.circle.service.LikeService;
import com.lanqiao.circle.service.TokenService;
import com.lanqiao.circle.util.CommentUtil;
import com.lanqiao.circle.util.FileUploadUtil;
import com.lanqiao.circle.util.RedisUtil;
import com.lanqiao.circle.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * @Author 王昊
 * @Date 2019/9/11 3:09 下午
 */
@RestController
@RequestMapping("/blog")
public class BlogContoller {
    @Autowired
    TokenService tokenService;

    @Autowired
    CommentsService commentsService;

    @Autowired
    LikeService likeService;

    @Autowired
    BlogService blogService;

    @Autowired
    FileUploadUtil fileUploadUtil;

    @Autowired
    BlogItemMapper blogItemMapper;

    @Autowired
    CommentUtil commentUtil;

    @Autowired
    BlogMapper blogMapper;

    @Autowired
    RedisUtil redisUtil;
    /**
     * 实现评论功能
     *
     * @param httpServletRequest
     * @param comments
     * @return
     */
    @UserLoginToken
    @PostMapping("insertComment")
    public Result insertComment(HttpServletRequest httpServletRequest, Comments comments) {
        int userId = Integer.parseInt(tokenService.getUserId(httpServletRequest));
        comments.setUserId(userId);
        return commentsService.insertComment(comments);
    }

    /**
     * 展示评论
     *
     * @param blogId
     * @return
     */
    @PostMapping("showComment")
    public Result showComment(@RequestParam(name = "blogId") int blogId) {
        return commentsService.showComment(blogId);
    }

    /**
     * 实现点赞功能
     *
     * @param httpServletRequest
     * @param likeKey
     * @return
     */
    @UserLoginToken
    @PostMapping("insertLike")
    public Result insertLike(HttpServletRequest httpServletRequest, LikeKey likeKey) {
        int userId = Integer.parseInt(tokenService.getUserId(httpServletRequest));
        likeKey.setUserId(userId);
        return likeService.insertLike(likeKey);
    }

    /**
     * 实现转发功能
     *
     * @param httpServletRequest
     * @param repostId
     * @param forwardcontent
     * @return
     */
    @UserLoginToken
    @PostMapping("forwardBlog")
    public Result forwardBlog(HttpServletRequest httpServletRequest, @RequestParam(name = "blogId") int repostId
            , @RequestParam(name = "forwardContent") String forwardcontent) {
        int userId = Integer.parseInt(tokenService.getUserId(httpServletRequest));
        Blog blog = new Blog();
        blog.setUserId(userId);
        blog.setIsRepost(1);
        blog.setRepostId(repostId);
        blog.setContent(forwardcontent);
        return blogService.forwardBlog(blog);
    }

    @UserLoginToken
    @PostMapping("/insertBlog")
    @Transactional
    public Result insertBlog(HttpServletRequest httpServletRequest, @RequestBody HashMap<String, Object> map) {
        try {
            JSONObject json = new JSONObject(map);
            LinkedHashMap baseBlog = (LinkedHashMap) json.get("blog");
            ArrayList itemIds = (ArrayList) json.get("itemIds");
            Blog blog = new Blog();
            String userId = tokenService.getUserId(httpServletRequest);
            blog.setUserId(Integer.parseInt(userId));
            blog.setCreateTime(commentUtil.getCurTime());
            blog.setContent(baseBlog.get("content").toString());
            blog.setCircleId(Integer.parseInt(baseBlog.get("circleId").toString()));
            if (redisUtil.hasKey("circle-"+blog.getCircleId())){
                redisUtil.incr("circle-"+blog.getCircleId(),1);
            }else {
                redisUtil.set("circle-"+blog.getCircleId(),1);
            }
            blogMapper.insertSelective(blog);
            for (Object itemId:itemIds){
                BlogItem blogItem = blogItemMapper.selectByPrimaryKey(Integer.parseInt((String) itemId));
                blogItem.setBlogId(blog.getBlogId());
                blogItemMapper.updateByPrimaryKeySelective(blogItem);
            }
            return Result.createSuccessResult();

        } catch (Exception e) {
            e.printStackTrace();
            return Result.createByFailure(e.getMessage());
        }
    }


    /**
     * 发微博 图片或视频上传接口
     *
     * @param file
     * @param flag
     * @return
     */
    @UserLoginToken
    @PostMapping("/uploadResource")
    public Result uploadResource(@RequestParam(name = "file") MultipartFile file, Integer flag) {
        try {

            String url = fileUploadUtil.fileUpload(file, flag);
            if (!url.equals("-2")) {
                BlogItem blogItem = new BlogItem();
                blogItem.setResourceUrl(url);
                if (flag != 4) {
                    blogItem.setType(1);
                } else {
                    blogItem.setType(2);
                }
                blogItemMapper.insertSelective(blogItem);
                return Result.createSuccessResult(blogItem);
            } else {
                return Result.createByFailure("上传失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.createByFailure(e.getMessage());
        }

    }

    /**
     * 管理员查询用户
     */
    @PostMapping("/normalBlogs")
    public Result normalBlogs(String content,int page,int limit){
        return blogService.normalBlogs(content,page,limit);
    }

    /**
     * 管理员删除用户
     *
     */
    @PostMapping("/deleteBlog")
    public Result deleteBlog(Integer blogId){
        return blogService.deleteBlog(blogId);
    }

    /**
     * 管理员查询用户
     */
    @PostMapping("/findBlog")
    public Result findBlog(String content){
        return blogService.findBlog(content);
    }


}
