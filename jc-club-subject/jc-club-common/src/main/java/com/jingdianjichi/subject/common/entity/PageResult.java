package com.jingdianjichi.subject.common.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 分页返回实体
 */
@Data
public class PageResult<T> implements Serializable {

    private Integer pageNo = 1;

    private Integer pageSize = 20;

    private Integer total = 0;

    private Integer totalPages = 0;

    private Integer start = 0;

    private Integer end = 0;

    private List<T> result = Collections.EMPTY_LIST;



    public void setRecords(List<T> result){
        this.total =total;
        if (result !=null && result.size() >0){
            setTotal(result.size());
        }
    }

    public void setTotal(Integer total){
        this.total =total;
        if (pageSize>0){
            this.totalPages =(total/pageSize)+(total % this.pageSize == 0 ? 0 :1);
        }else {
            this.totalPages =0;
        }

        this.start = (this.pageSize>0 ? (this.pageNo-1)*pageSize : 0)+1;
        this.end = (this.start-1 + this.pageSize*(this.pageNo>0 ? 1:0));

    }

    public void setPageNo(Integer pageNo){
        this.pageNo =pageNo;
    }

    public void setPageSize(Integer pageSize){
        this.pageSize = pageSize;
    }


}
