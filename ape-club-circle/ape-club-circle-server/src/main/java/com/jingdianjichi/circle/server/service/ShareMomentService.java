package com.jingdianjichi.circle.server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jingdianjichi.circle.api.common.PageResult;
import com.jingdianjichi.circle.api.req.GetShareMomentReq;
import com.jingdianjichi.circle.api.req.RemoveShareMomentReq;
import com.jingdianjichi.circle.api.req.SaveMomentCircleReq;
import com.jingdianjichi.circle.api.vo.ShareMomentVO;
import com.jingdianjichi.circle.server.entity.po.ShareMoment;

/**
 * <p>
 * 动态信息 服务类
 * </p>
 *
 * @author ChickenWing
 * @since 2024/05/16
 */
public interface ShareMomentService extends IService<ShareMoment> {
    /**
     * 发布内容
     */
    Boolean saveMoment(SaveMomentCircleReq req);
    /**
     * 分页查询鸡圈内容
     */
    PageResult<ShareMomentVO> getMoments(GetShareMomentReq req);
    /**
     * 删除鸡圈内容
     */
    Boolean removeMoment(RemoveShareMomentReq req);

    void incrReplyCount(Long id, int count);

}
