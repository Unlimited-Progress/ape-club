package com.jingdianjichi.circle.server.service.impl;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jingdianjichi.circle.api.enums.IsDeletedFlagEnum;
import com.jingdianjichi.circle.api.req.GetShareCommentReq;
import com.jingdianjichi.circle.api.req.RemoveShareCommentReq;
import com.jingdianjichi.circle.api.req.SaveShareCommentReplyReq;
import com.jingdianjichi.circle.api.vo.ShareCommentReplyVO;
import com.jingdianjichi.circle.server.dao.ShareCommentReplyMapper;
import com.jingdianjichi.circle.server.dao.ShareMomentMapper;
import com.jingdianjichi.circle.server.entity.dto.UserInfo;
import com.jingdianjichi.circle.server.entity.po.ShareCommentReply;
import com.jingdianjichi.circle.server.entity.po.ShareMoment;
import com.jingdianjichi.circle.server.rpc.UserRpc;
import com.jingdianjichi.circle.server.service.ShareCommentReplyService;
import com.jingdianjichi.circle.server.util.LoginUtil;
import com.jingdianjichi.circle.server.util.TreeUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 评论及回复信息 服务实现类
 * </p>
 *
 * @author ChickenWing
 * @since 2024/05/16
 */
@Service
public class ShareCommentReplyServiceImpl extends ServiceImpl<ShareCommentReplyMapper, ShareCommentReply> implements ShareCommentReplyService {

    @Resource
    private ShareMomentMapper shareMomentMapper;
    @Resource
    private UserRpc userRpc;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveComment(SaveShareCommentReplyReq req) {
        ShareMoment moment = shareMomentMapper.selectById(req.getMomentId());
        ShareCommentReply comment = new ShareCommentReply();
        comment.setMomentId(req.getMomentId());
        comment.setReplyType(req.getReplyType());
        String loginId = LoginUtil.getLoginId();
        // 1评论 2回复
        if (req.getReplyType() == 1) {
            comment.setParentId(-1L);
            comment.setToId(req.getTargetId());
            comment.setToUser(loginId);
            comment.setToUserAuthor(Objects.nonNull(moment.getCreatedBy()) && loginId.equals(moment.getCreatedBy()) ? 1 : 0);
        } else {
            comment.setParentId(req.getTargetId());
            comment.setReplyId(req.getTargetId());
            comment.setReplyUser(loginId);
            comment.setReplayAuthor(Objects.nonNull(moment.getCreatedBy()) && loginId.equals(moment.getCreatedBy()) ? 1 : 0);
        }
        comment.setContent(req.getContent());
        if (!CollectionUtils.isEmpty(req.getPicUrlList())) {
            comment.setPicUrls(JSON.toJSONString(req.getPicUrlList()));
        }
        comment.setCreatedBy(LoginUtil.getLoginId());
        comment.setCreatedTime(new Date());
        comment.setIsDeleted(IsDeletedFlagEnum.UN_DELETED.getCode());
        //每次评论，数量加1
        shareMomentMapper.incrReplyCount(moment.getId(), 1);
        return super.save(comment);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean removeComment(RemoveShareCommentReq req) {
        ShareCommentReply comment = getById(req.getId());
        LambdaQueryWrapper<ShareCommentReply> query = Wrappers.<ShareCommentReply>lambdaQuery()
                .eq(ShareCommentReply::getMomentId, comment.getMomentId())
                .eq(ShareCommentReply::getIsDeleted, IsDeletedFlagEnum.UN_DELETED.getCode())
                .select(ShareCommentReply::getId,
                        ShareCommentReply::getMomentId,
                        ShareCommentReply::getReplyType,
                        ShareCommentReply::getContent,
                        ShareCommentReply::getPicUrls,
                        ShareCommentReply::getCreatedBy,
                        ShareCommentReply::getToUser,
                        ShareCommentReply::getParentId);
        List<ShareCommentReply> list = list(query);
        List<ShareCommentReply> replyList = new ArrayList<>();
        List<ShareCommentReply> tree = TreeUtils.buildTree(list);
        for (ShareCommentReply reply : tree) {
//            findAll 方法是一个递归方法，用于在树中查找指定ID的节点及其所有子节点，并将它们添加到结果列表中。
//
//            参数：
//            result：用于存储查找到的节点的结果列表。
//            node：当前正在检查的节点。
//            targetId：要查找的目标节点ID。
            TreeUtils.findAll(replyList, reply, req.getId());
        }
        // 关联子级对象及 moment 的回复数量
//        map() 方法用于将流中的每个元素转换为另一种形式。
//        Set 是一个不允许重复元素的集合，因此这一步也起到了去重的作用。
        Set<Long> ids = replyList.stream().map(ShareCommentReply::getId).collect(Collectors.toSet());
        LambdaUpdateWrapper<ShareCommentReply> update = Wrappers.<ShareCommentReply>lambdaUpdate()
                .eq(ShareCommentReply::getMomentId, comment.getMomentId())
                .in(ShareCommentReply::getId, ids);
        ShareCommentReply updateEntity = new ShareCommentReply();
        updateEntity.setIsDeleted(IsDeletedFlagEnum.DELETED.getCode());

//        updateEntity：要更新的实体对象。这个对象中包含需要更新的字段及其值。
//        update：一个 Wrapper 对象，用于定义更新的条件。
        int count = getBaseMapper().update(updateEntity, update);
        shareMomentMapper.incrReplyCount(comment.getMomentId(), -count);
        return true;
    }

    @Override
    public List<ShareCommentReplyVO> listComment(GetShareCommentReq req) {
        LambdaQueryWrapper<ShareCommentReply> query = Wrappers.<ShareCommentReply>lambdaQuery()
                .eq(ShareCommentReply::getMomentId, req.getId())
                .eq(ShareCommentReply::getIsDeleted, IsDeletedFlagEnum.UN_DELETED.getCode())
                .select(ShareCommentReply::getId,
                        ShareCommentReply::getMomentId,
                        ShareCommentReply::getReplyType,
                        ShareCommentReply::getContent,
                        ShareCommentReply::getPicUrls,
                        ShareCommentReply::getCreatedBy,
                        ShareCommentReply::getToUser,
                        ShareCommentReply::getCreatedTime,
                        ShareCommentReply::getParentId);
        List<ShareCommentReply> list = list(query);
        List<String> userNameList = list.stream().map(ShareCommentReply::getCreatedBy).distinct().collect(Collectors.toList());
        Map<String, UserInfo> userInfoMap = userRpc.batchGetUserInfo(userNameList);
        UserInfo defaultUser = new UserInfo();
        List<ShareCommentReplyVO> voList = list.stream().map(item -> {
            ShareCommentReplyVO vo = new ShareCommentReplyVO();
            vo.setId(item.getId());
            vo.setMomentId(item.getMomentId());
            vo.setReplyType(item.getReplyType());
            vo.setContent(item.getContent());
            if (Objects.nonNull(item.getPicUrls())) {
                vo.setPicUrlList(JSONArray.parseArray(item.getPicUrls(), String.class));
            }
            if (item.getReplyType() == 2) {
                vo.setFromId(item.getCreatedBy());
                vo.setToId(item.getToUser());
            }
            vo.setParentId(item.getParentId());
            UserInfo user = userInfoMap.getOrDefault(item.getCreatedBy(), defaultUser);
            vo.setUserName(user.getNickName());
            vo.setAvatar(user.getAvatar());
            vo.setCreatedTime(item.getCreatedTime().getTime());
            return vo;
        }).collect(Collectors.toList());
        return TreeUtils.buildTree(voList);
    }
}
