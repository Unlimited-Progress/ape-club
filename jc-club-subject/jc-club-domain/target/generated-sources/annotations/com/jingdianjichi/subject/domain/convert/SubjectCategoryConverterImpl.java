package com.jingdianjichi.subject.domain.convert;

import com.jingdianjichi.subject.domain.entity.SubjectCategoryBO;
import com.jingdianjichi.subject.infra.basic.entity.SubjectCategory;
import javax.annotation.Generated;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-09-16T21:23:23+0800",
    comments = "version: 1.4.2.Final, compiler: javac, environment: Java 1.8.0_422 (Amazon.com Inc.)"
)
public class SubjectCategoryConverterImpl implements SubjectCategoryConverter {

    @Override
    public SubjectCategory convertToCategory(SubjectCategoryBO subjectCategoryBO) {
        if ( subjectCategoryBO == null ) {
            return null;
        }

        SubjectCategory subjectCategory = new SubjectCategory();

        subjectCategory.setId( subjectCategoryBO.getId() );
        subjectCategory.setCategoryName( subjectCategoryBO.getCategoryName() );
        subjectCategory.setCategoryType( subjectCategoryBO.getCategoryType() );
        subjectCategory.setImageUrl( subjectCategoryBO.getImageUrl() );
        subjectCategory.setParentId( subjectCategoryBO.getParentId() );
        subjectCategory.setIsDeleted( subjectCategoryBO.getIsDeleted() );

        return subjectCategory;
    }

    @Override
    public List<SubjectCategoryBO> convertCategoryToBO(List<SubjectCategory> categoryList) {
        if (categoryList == null) {
            return null;
        } else {
            List<SubjectCategoryBO> list = new ArrayList(categoryList.size());
            Iterator var3 = categoryList.iterator();

            while(var3.hasNext()) {
                SubjectCategory subjectCategory = (SubjectCategory)var3.next();
                list.add(this.subjectCategoryToSubjectCategoryBO(subjectCategory));
            }

            return list;
        }
    }

    protected SubjectCategoryBO subjectCategoryToSubjectCategoryBO(SubjectCategory subjectCategory) {
        if (subjectCategory == null) {
            return null;
        } else {
            SubjectCategoryBO subjectCategoryBO = new SubjectCategoryBO();
            subjectCategoryBO.setId(subjectCategory.getId());
            subjectCategoryBO.setCategoryName(subjectCategory.getCategoryName());
            subjectCategoryBO.setCategoryType(subjectCategory.getCategoryType());
            subjectCategoryBO.setImageUrl(subjectCategory.getImageUrl());
            subjectCategoryBO.setParentId(subjectCategory.getParentId());
            return subjectCategoryBO;
        }
    }
}
