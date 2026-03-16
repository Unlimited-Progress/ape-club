package com.jingdianjichi.circle.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.jingdianjichi.circle.api.enums.IsDeletedFlagEnum;
import com.jingdianjichi.circle.api.req.RemoveShareCircleReq;
import com.jingdianjichi.circle.api.req.SaveShareCircleReq;
import com.jingdianjichi.circle.api.req.UpdateShareCircleReq;
import com.jingdianjichi.circle.api.vo.ShareCircleVO;
import com.jingdianjichi.circle.server.dao.ShareCircleMapper;
import com.jingdianjichi.circle.server.entity.po.ShareCircle;
import com.jingdianjichi.circle.server.service.ShareCircleService;
import com.jingdianjichi.circle.server.util.LoginUtil;
import javafx.scene.shape.Circle;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;


/**
 * <p>
 * 圈子信息 服务实现类
 * </p>
 *
 * @author ChickenWing
 * @since 2024/05/16
 */
@Service
public class ShareCircleServiceImpl extends ServiceImpl<ShareCircleMapper, ShareCircle> implements ShareCircleService {

    /*
    * 本地缓存
    * initialCapacity(1)
    设置缓存的初始容量为 1。这意味着缓存初始化时会预留足够的空间来存储 1 个条目。
    注意：初始容量并不限制缓存的最大容量，只是一个预分配的建议值
    *
    * expireAfterWrite(Duration.ofSeconds(30))
    设置缓存条目在写入后 30 秒后过期。这意味着如果一个条目在 30 秒内没有被写入（更新或插入），它将被自动移除。
    注意：expireAfterWrite 是基于写入时间的过期策略，与 expireAfterAccess（基于访问时间的过期策略）不同。

    */

    private static final Cache<Integer, List<ShareCircleVO>> CACHE = Caffeine.newBuilder().initialCapacity(1)
            .maximumSize(1).expireAfterWrite(Duration.ofSeconds(30)).build();

    //查询圈子
    @Override
    public List<ShareCircleVO> listResult() {
        List<ShareCircleVO> res = CACHE.getIfPresent(1);
        //判断缓存是否为空，不为空直接返回，
        //如果为空则生成一个新的 List<ShareCircleVO>并放入缓存再返回
        return Optional.ofNullable(res).orElseGet(()->{
            List<ShareCircle> list = super.list(Wrappers.<ShareCircle>lambdaQuery()
                    .eq(ShareCircle::getIsDeleted, IsDeletedFlagEnum.UN_DELETED.getCode()));
            //查询父级圈子
            List<ShareCircle> parentList = list.stream()
                    .filter(item -> item.getParentId() == -1L)
                    .collect(Collectors.toList());
            //一个父级id对应一个圈子
            Map<Long, List<ShareCircle>> map = list.stream().collect(Collectors.groupingBy(ShareCircle::getParentId));
            List<ShareCircleVO> collect = parentList.stream().map(item -> {
                ShareCircleVO vo = new ShareCircleVO();
                vo.setId(item.getId());
                vo.setCircleName(item.getCircleName());
                vo.setIcon(item.getIcon());
                //获取子圈
                List<ShareCircle> shareCircles = map.get(item.getId());

                //为空返回空集合
                if (CollectionUtils.isEmpty(shareCircles)) {
                    vo.setChildren(Collections.emptyList());
                } else {
                    List<ShareCircleVO> children = shareCircles.stream().map(cItem -> {
                        ShareCircleVO cVo = new ShareCircleVO();
                        cVo.setId(cItem.getId());
                        cVo.setCircleName(cItem.getCircleName());
                        cVo.setIcon(cItem.getIcon());
                        //子圈无子圈了
                        cVo.setChildren(Collections.emptyList());
                        return cVo;
                    }).collect(Collectors.toList());
                    vo.setChildren(children);
                }
                return vo;
            }).collect(Collectors.toList());
            CACHE.put(1,collect);

            return collect;
        });
    }

    //新增圈子
    @Override
    public Boolean saveCircle(SaveShareCircleReq req) {
        ShareCircle circle = new ShareCircle();
        BeanUtils.copyProperties(req, circle);
        circle.setIsDeleted(IsDeletedFlagEnum.UN_DELETED.getCode());
        circle.setCreatedTime(new Date());
        circle.setCreatedBy(LoginUtil.getLoginId());
        //删除缓存
        CACHE.invalidateAll();
        return save(circle);
    }

    //更新圈子
    @Override
    public Boolean updateCircle(UpdateShareCircleReq req) {
        LambdaUpdateWrapper<ShareCircle> update = Wrappers.<ShareCircle>lambdaUpdate()
                .eq(ShareCircle::getId, req.getId())
                .eq(ShareCircle::getIsDeleted, IsDeletedFlagEnum.UN_DELETED.getCode())
                .set(Objects.nonNull(req.getParentId()), ShareCircle::getParentId, req.getParentId())
                .set(Objects.nonNull(req.getIcon()), ShareCircle::getIcon, req.getIcon())
                .set(Objects.nonNull(req.getCircleName()), ShareCircle::getCircleName, req.getCircleName())
                .set(ShareCircle::getUpdateBy, LoginUtil.getLoginId())
                .set(ShareCircle::getUpdateTime, new Date());

        CACHE.invalidateAll();
        return update(update);
    }

    //删除圈子
    @Override
    public Boolean removeCircle(RemoveShareCircleReq req) {

        boolean remove = super.update(Wrappers.<ShareCircle>lambdaUpdate()
                .eq(ShareCircle::getParentId, req.getId())
                .eq(ShareCircle::getIsDeleted, IsDeletedFlagEnum.UN_DELETED.getCode())
                .set(ShareCircle::getIsDeleted, IsDeletedFlagEnum.DELETED.getCode()));

        super.update(Wrappers.<ShareCircle>lambdaUpdate().eq(ShareCircle::getParentId, req.getId())
                .eq(ShareCircle::getIsDeleted, IsDeletedFlagEnum.UN_DELETED.getCode())
                .set(ShareCircle::getIsDeleted, IsDeletedFlagEnum.DELETED.getCode()));
        CACHE.invalidateAll();
        return remove;
    }
}
