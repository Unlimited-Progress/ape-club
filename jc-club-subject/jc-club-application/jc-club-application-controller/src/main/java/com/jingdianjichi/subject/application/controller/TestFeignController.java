package com.jingdianjichi.subject.application.controller;

import com.jingdianjichi.subject.infra.entity.UserInfo;
import com.jingdianjichi.subject.infra.rpc.UserRpc;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 刷题分类controller
 */

@RestController
@RequestMapping("/subject/category")
@Slf4j
public class TestFeignController {

    @Resource
    private UserRpc userRpc;
//
//    @Resource
//    private SubjectEsService subjectEsService;

    @GetMapping("/testFeign")
    public void testFeign(){
        UserInfo userInfo = userRpc.getUserInfo("jichi");
        log.info("TestFeignController.testFeign.userInfo:{}",userInfo);
    }

//    @GetMapping("testCreateIndex")
//    public void testCreateIndex(){
//
//        subjectEsService.createIndex();
//
//    }
//
//
//    @GetMapping("addDocs")
//    public void addDocs(){
//        subjectEsService.addDocs();
//    }
//
//    @GetMapping("find")
//    public void find(){
//        subjectEsService.find();
//    }
//
//    @GetMapping("search")
//    public void search(){
//        subjectEsService.search();
//    }


}

