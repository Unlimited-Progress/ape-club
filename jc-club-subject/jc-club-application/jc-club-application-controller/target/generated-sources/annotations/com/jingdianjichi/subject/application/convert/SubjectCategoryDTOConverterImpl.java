package com.jingdianjichi.subject.application.convert;

import com.jingdianjichi.subject.application.dto.SubjectCategoryDTO;
import com.jingdianjichi.subject.domain.entity.SubjectCategoryBO;
import javax.annotation.Generated;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-09-16T21:23:24+0800",
    comments = "version: 1.4.2.Final, compiler: javac, environment: Java 1.8.0_422 (Amazon.com Inc.)"
)
public class SubjectCategoryDTOConverterImpl implements SubjectCategoryDTOConverter {


    public SubjectCategoryDTOConverterImpl() {
    }

    public List<SubjectCategoryDTO> convertBoToCategoryDTOList(List<SubjectCategoryBO> subjectCategoryDTO) {
        if (subjectCategoryDTO == null) {
            return null;
        } else {
            List<SubjectCategoryDTO> list = new ArrayList(subjectCategoryDTO.size());
            Iterator var3 = subjectCategoryDTO.iterator();

            while(var3.hasNext()) {
                SubjectCategoryBO subjectCategoryBO = (SubjectCategoryBO)var3.next();
                list.add(this.convertBoToCategoryDTO(subjectCategoryBO));
            }

            return list;
        }
    }

    public SubjectCategoryBO convertDtoToCategoryBO(SubjectCategoryDTO subjectCategoryDTO) {
        if (subjectCategoryDTO == null) {
            return null;
        } else {
            SubjectCategoryBO subjectCategoryBO = new SubjectCategoryBO();
            subjectCategoryBO.setId(subjectCategoryDTO.getId());
            subjectCategoryBO.setCategoryName(subjectCategoryDTO.getCategoryName());
            subjectCategoryBO.setCategoryType(subjectCategoryDTO.getCategoryType());
            subjectCategoryBO.setImageUrl(subjectCategoryDTO.getImageUrl());
            subjectCategoryBO.setParentId(subjectCategoryDTO.getParentId());
            subjectCategoryBO.setIsDeleted( subjectCategoryDTO.getIsDeleted() );
//            subjectCategoryBO.setCount(subjectCategoryDTO.getCount());
            return subjectCategoryBO;
        }
    }

    public SubjectCategoryDTO convertBoToCategoryDTO(SubjectCategoryBO subjectCategoryBO) {
        if (subjectCategoryBO == null) {
            return null;
        } else {
            SubjectCategoryDTO subjectCategoryDTO = new SubjectCategoryDTO();
            subjectCategoryDTO.setId(subjectCategoryBO.getId());
            subjectCategoryDTO.setCategoryName(subjectCategoryBO.getCategoryName());
            subjectCategoryDTO.setCategoryType(subjectCategoryBO.getCategoryType());
            subjectCategoryDTO.setImageUrl(subjectCategoryBO.getImageUrl());
            subjectCategoryDTO.setParentId(subjectCategoryBO.getParentId());
            subjectCategoryBO.setIsDeleted( subjectCategoryBO.getIsDeleted() );
//            subjectCategoryDTO.setCount(subjectCategoryBO.getCount());
            return subjectCategoryDTO;
        }
    }
}
