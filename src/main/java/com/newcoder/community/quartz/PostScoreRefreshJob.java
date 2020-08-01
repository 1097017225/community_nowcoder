package com.newcoder.community.quartz;

import com.newcoder.community.entity.DiscussPost;
import com.newcoder.community.service.DiscussPostService;
import com.newcoder.community.service.ElasticsearchService;
import com.newcoder.community.service.LikeService;
import com.newcoder.community.util.CommunityConstant;
import com.newcoder.community.util.RedisKeyUtil;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PostScoreRefreshJob implements Job, CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(PostScoreRefreshJob.class);

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private ElasticsearchService elasticsearchService;

    @Autowired
    private static final Date epoch;

    static {
        try {
            epoch = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2014-08-01 00:00:00");
        } catch (ParseException e) {
            throw new RuntimeException("初始化牛客纪元失败", e);
        }
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {

        String key = RedisKeyUtil.getPostScoreKey();
        BoundSetOperations operations = redisTemplate.boundSetOps(key);
        if (operations.size() == 0) {
            logger.info("任务取消，没有要刷新的帖子");
            return;
        }

        logger.info("任务开始，正在刷新帖子任务：" + operations.size());
        while (operations.size() > 0) {
            refresh( (Integer) operations.pop());
        }
        logger.info("任务结束，帖子分数刷新完毕：" + operations.size());

    }

    private void refresh(int id) {

        DiscussPost post = discussPostService.findDiscussPostById(id);
        if (post == null) {
            logger.info("该帖子不存在：id=" + id);
            return;
        }
        
        //是否加精
        boolean wonderful =  post.getStatus() == 1;
        //评论数量
        int commentCount = post.getCommentCount();
        //点赞数量
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, id);

        double w = (wonderful ? 75 : 0) + commentCount * 10 + likeCount * 2;
        double score = Math.log10(Math.max(w, 1))
                + (post.getCreateTime().getTime() - epoch.getTime()) / (1000 * 24 * 3600);
        discussPostService.updateScore(id, score);
        post.setScore(score);
        elasticsearchService.saveDiscussPost(post);
    }
}
