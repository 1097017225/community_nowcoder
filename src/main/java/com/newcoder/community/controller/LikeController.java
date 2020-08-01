package com.newcoder.community.controller;

import com.newcoder.community.Event.EventProducer;
import com.newcoder.community.annotation.LoginRequired;
import com.newcoder.community.entity.Event;
import com.newcoder.community.entity.User;
import com.newcoder.community.service.LikeService;
import com.newcoder.community.util.CommunityConstant;
import com.newcoder.community.util.CommunityUtil;
import com.newcoder.community.util.HostHolder;
import com.newcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class LikeController implements CommunityConstant {

    @Autowired
    private LikeService likeService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private EventProducer producer;

    @Autowired
    private RedisTemplate redisTemplate;

    @PostMapping("/like")
    @ResponseBody
    public String like(int entityType, int entityId, int entityUserId, int postId) {

        User user = hostHolder.getUser();

        if (user == null) {
            return CommunityUtil.getJSONString(1, "请先登陆!");
        }

        likeService.like(user.getId(), entityType, entityId, entityUserId);

        long likeCount = likeService.findEntityLikeCount(entityType, entityId);

        int likeStatus = likeService.findEntityLikeStatus(user.getId(), entityType, entityId);

        Map<String, Object> map = new HashMap<>();
        map.put("likeCount", likeCount);
        map.put("likeStatus", likeStatus);

        if (likeStatus == 1) {
            Event event = new Event();
            event.setTopic(TOPIC_LIKE);
            event.setUserId(hostHolder.getUser().getId());
            event.setEntityType(entityType);
            event.setEntityId(entityId);
            event.setEntityUserId(entityUserId);
            event.setData("postId", postId);
            producer.fireEvent(event);
        }

        if (entityType == ENTITY_TYPE_POST) {
            String key = RedisKeyUtil.getPostScoreKey();
            redisTemplate.opsForSet().add(key, postId);
        }

        return CommunityUtil.getJSONString(0, null, map);
    }
}
