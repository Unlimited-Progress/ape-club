package com.jingdianjichi.circle.server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jingdianjichi.circle.api.req.GetShareCommentReq;
import com.jingdianjichi.circle.api.req.RemoveShareCommentReq;
import com.jingdianjichi.circle.api.req.SaveShareCommentReplyReq;
import com.jingdianjichi.circle.api.vo.ShareCommentReplyVO;
import com.jingdianjichi.circle.server.entity.po.ShareCommentReply;

import java.util.List;

/**
 * <p>
 * 评论及回复信息 服务类
 * </p>
 *
 * @author ChickenWing
 * @since 2024/05/16
 */
public interface ShareCommentReplyService extends IService<ShareCommentReply> {

    /**
     * 发布评论
     */
    Boolean saveComment(SaveShareCommentReplyReq req);
    //删除评论
    Boolean removeComment(RemoveShareCommentReq req);
    /**
     * 查询该动态下的评论
     */
    List<ShareCommentReplyVO> listComment(GetShareCommentReq req);

}
