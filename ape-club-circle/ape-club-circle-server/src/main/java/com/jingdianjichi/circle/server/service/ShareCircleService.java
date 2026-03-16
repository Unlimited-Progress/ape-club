package com.jingdianjichi.circle.server.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.jingdianjichi.circle.api.req.RemoveShareCircleReq;
import com.jingdianjichi.circle.api.req.SaveShareCircleReq;
import com.jingdianjichi.circle.api.req.UpdateShareCircleReq;
import com.jingdianjichi.circle.api.vo.ShareCircleVO;
import com.jingdianjichi.circle.server.entity.po.ShareCircle;

import java.util.List;

/**
 * <p>
 * 圈子信息 服务类
 * </p>
 *
 * @author ChickenWing
 * @since 2024/05/16
 */
public interface ShareCircleService extends IService<ShareCircle> {

    //查询圈子
    List<ShareCircleVO> listResult();

    //新增圈子
    Boolean saveCircle(SaveShareCircleReq req);

    //更新圈子
    Boolean updateCircle(UpdateShareCircleReq req);

    //删除圈子
    Boolean removeCircle(RemoveShareCircleReq req);
}
