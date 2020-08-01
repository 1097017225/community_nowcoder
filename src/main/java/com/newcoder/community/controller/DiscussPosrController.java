package com.newcoder.community.controller;

import com.newcoder.community.Event.EventProducer;
import com.newcoder.community.entity.*;
import com.newcoder.community.service.CommentService;
import com.newcoder.community.service.DiscussPostService;
import com.newcoder.community.service.LikeService;
import com.newcoder.community.service.UserService;
import com.newcoder.community.util.CommunityConstant;
import com.newcoder.community.util.CommunityUtil;
import com.newcoder.community.util.HostHolder;
import com.newcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/discuss")
public class DiscussPosrController implements CommunityConstant {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private LikeService likeService;

    @Autowired
    private EventProducer producer;

    @Autowired
    private RedisTemplate redisTemplate;

    @PostMapping("/add")
    @ResponseBody
    public String addDiscussPost(String title, String content) {
        User user = hostHolder.getUser();
        if (user == null) {
            return CommunityUtil.getJSONString(403, "您还未登陆");
        }

        DiscussPost discussPost = new DiscussPost();
        discussPost.setUserId(user.getId());
        discussPost.setTitle(title);
        discussPost.setContent(content);
        discussPost.setCreateTime(new Date());
        discussPostService.insertDiscussPost(discussPost);

        Event event = new Event();
        event.setTopic(TOPIC_PUBLISH);
        event.setUserId(user.getId());
        event.setEntityType(ENTITY_TYPE_POST);
        event.setEntityId(discussPost.getId());
        producer.fireEvent(event);

        //计算帖子的分数
        String key = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(key, discussPost.getId());

        //报错情况以后统一处理
        return CommunityUtil.getJSONString(0, "发布成功!");
    }

    @GetMapping("/detail/{id}")
    public String getDiscussPost(@PathVariable("id") int id, Model model, Page page) {

        DiscussPost post = discussPostService.findDiscussPostById(id);
        model.addAttribute("post", post);
        User user = userService.findUserById(post.getUserId());
        model.addAttribute("user", user);

        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId());
        model.addAttribute("likeCount", likeCount);
        int likeStatus = hostHolder.getUser() == null ? 0 :likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_POST, post.getId());
        model.addAttribute("likeStatus", likeStatus);

        page.setLimit(5);
        page.setPath("/discuss/detail/" + id);
        page.setRows(post.getCommentCount());

        //评论：给帖子的评论
        //回复：给评论的评论
        List<Comment> commentList = commentService.findCommentsByEntity(ENTITY_TYPE_POST, post.getId(), page.getOffset(), page.getLimit());
        //评论VO列表
        List<Map<String, Object>> commentVoList = new ArrayList<>();
        if (commentList != null) {
            for (Comment comment : commentList) {
                //一个评论VO
                Map<String, Object> commentVo = new HashMap<>();
                commentVo.put("comment", comment);
                commentVo.put("user", userService.findUserById(comment.getUserId()));

                likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("likeCount", likeCount);

                likeStatus = hostHolder.getUser() == null ? 0 :likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("likeStatus", likeStatus);

                //回复
                List<Comment> replyList = commentService.findCommentsByEntity(ENTITY_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE);
                List<Map<String, Object>> replyVoList = new ArrayList<>();
                if (replyList != null) {
                    for (Comment reply : replyList) {
                        Map<String, Object> replyVo = new HashMap<>();
                        replyVo.put("reply", reply);
                        replyVo.put("user", userService.findUserById(reply.getUserId()));

                        likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, reply.getId());
                        replyVo.put("likeCount", likeCount);

                        likeStatus = hostHolder.getUser() == null ? 0 :likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, reply.getId());
                        replyVo.put("likeStatus", likeStatus);

                        User target = reply.getTargetId() == 0 ? null : userService.findUserById(reply.getTargetId());
                        replyVo.put("target", target);
                        replyVoList.add(replyVo);
                    }
                }

                commentVo.put("replys", replyVoList);

                //回复数量
                int replyCount = commentService.findCommentCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("replyCount", replyCount);

                commentVoList.add(commentVo);
            }
        }

        model.addAttribute("comments", commentVoList);
        return "/site/discuss-detail";
    }
    //置顶
    @PostMapping("/top")
    @ResponseBody
    public String setTop(int id) {

        discussPostService.updateType(id, 1);
        Event event = new Event();
        event.setTopic(TOPIC_PUBLISH);
        event.setUserId(hostHolder.getUser().getId());
        event.setEntityType(ENTITY_TYPE_POST);
        event.setEntityId(id);
        producer.fireEvent(event);
        return CommunityUtil.getJSONString(0);
    }
    //加精
    @PostMapping("/wonderful")
    @ResponseBody
    public String setWonderful(int id) {

        discussPostService.updateStatus(id, 1);
        Event event = new Event();
        event.setTopic(TOPIC_PUBLISH);
        event.setUserId(hostHolder.getUser().getId());
        event.setEntityType(ENTITY_TYPE_POST);
        event.setEntityId(id);
        producer.fireEvent(event);

        String key = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(key, id);

        return CommunityUtil.getJSONString(0);
    }
    //删除
    @PostMapping("/delete")
    @ResponseBody
    public String setDelete(int id) {

        discussPostService.updateStatus(id, 2);
        Event event = new Event();
        event.setTopic(TOPIC_DELETE);
        event.setUserId(hostHolder.getUser().getId());
        event.setEntityType(ENTITY_TYPE_POST);
        event.setEntityId(id);
        producer.fireEvent(event);
        return CommunityUtil.getJSONString(0);
    }
}
