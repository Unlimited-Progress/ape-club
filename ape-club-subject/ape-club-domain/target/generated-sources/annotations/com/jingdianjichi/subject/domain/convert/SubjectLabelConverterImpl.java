package com.jingdianjichi.subject.domain.convert;

import com.jingdianjichi.subject.domain.entity.SubjectLabelBO;
import com.jingdianjichi.subject.infra.basic.entity.SubjectLabel;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-03-16T15:29:09+0800",
    comments = "version: 1.4.2.Final, compiler: javac, environment: Java 11.0.22 (Amazon.com Inc.)"
)
public class SubjectLabelConverterImpl implements SubjectLabelConverter {

    @Override
    public SubjectLabel convertBoToLabel(SubjectLabelBO subjectLabelBO) {
        if ( subjectLabelBO == null ) {
            return null;
        }

        SubjectLabel subjectLabel = new SubjectLabel();

        subjectLabel.setId( subjectLabelBO.getId() );
        subjectLabel.setLabelName( subjectLabelBO.getLabelName() );
        subjectLabel.setSortNum( subjectLabelBO.getSortNum() );
        if ( subjectLabelBO.getCategoryId() != null ) {
            subjectLabel.setCategoryId( String.valueOf( subjectLabelBO.getCategoryId() ) );
        }

        return subjectLabel;
    }

    @Override
    public List<SubjectLabelBO> convertLabelToBoList(List<SubjectLabel> subjectLabelList) {
        if ( subjectLabelList == null ) {
            return null;
        }

        List<SubjectLabelBO> list = new ArrayList<SubjectLabelBO>( subjectLabelList.size() );
        for ( SubjectLabel subjectLabel : subjectLabelList ) {
            list.add( subjectLabelToSubjectLabelBO( subjectLabel ) );
        }

        return list;
    }

    protected SubjectLabelBO subjectLabelToSubjectLabelBO(SubjectLabel subjectLabel) {
        if ( subjectLabel == null ) {
            return null;
        }

        SubjectLabelBO subjectLabelBO = new SubjectLabelBO();

        subjectLabelBO.setId( subjectLabel.getId() );
        subjectLabelBO.setLabelName( subjectLabel.getLabelName() );
        subjectLabelBO.setSortNum( subjectLabel.getSortNum() );
        if ( subjectLabel.getCategoryId() != null ) {
            subjectLabelBO.setCategoryId( Long.parseLong( subjectLabel.getCategoryId() ) );
        }

        return subjectLabelBO;
    }
}
