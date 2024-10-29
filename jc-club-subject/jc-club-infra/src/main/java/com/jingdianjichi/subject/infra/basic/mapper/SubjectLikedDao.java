package com.jingdianjichi.subject.infra.basic.mapper;

import com.jingdianjichi.subject.infra.basic.entity.SubjectLiked;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 题目点赞表 表数据库访问层
 *
 * @author jingdianjichi
 * @since 2024-10-14 16:54:58
 */
@Repository
public interface SubjectLikedDao extends BaseMapper<SubjectLiked> {

    void batchInsertOrUpdate(@Param("entities")List<SubjectLiked> subjectLikedList);

    int countByCondition(SubjectLiked subjectLiked);

    List<SubjectLiked> queryPage(@Param("entity") SubjectLiked subjectLiked,
                                 @Param("start") int start,
                                 @Param("pageSize") Integer pageSize);
}

