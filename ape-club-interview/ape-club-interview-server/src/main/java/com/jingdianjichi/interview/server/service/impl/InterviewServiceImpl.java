package com.jingdianjichi.interview.server.service.impl;

import com.google.common.base.Preconditions;
import com.jingdianjichi.interview.api.req.InterviewReq;
import com.jingdianjichi.interview.api.req.InterviewSubmitReq;
import com.jingdianjichi.interview.api.req.StartReq;
import com.jingdianjichi.interview.api.vo.InterviewQuestionVO;
import com.jingdianjichi.interview.api.vo.InterviewResultVO;
import com.jingdianjichi.interview.api.vo.InterviewVO;
import com.jingdianjichi.interview.server.dao.SubjectDao;
import com.jingdianjichi.interview.server.entity.po.SubjectLabel;
import com.jingdianjichi.interview.server.service.InterviewEngine;
import com.jingdianjichi.interview.server.service.InterviewService;
import com.jingdianjichi.interview.server.util.PDFUtil;
import com.jingdianjichi.interview.server.util.keyword.KeyWordUtil;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class InterviewServiceImpl implements InterviewService, ApplicationContextAware {

    private static final Map<String, InterviewEngine> engineMap = new HashMap<>();

    @Resource
    private SubjectDao subjectLabelDao;

    @Value("${interview.file-base-url:http://localhost:5000}")
    private String interviewFileBaseUrl;


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Collection<InterviewEngine> engines = applicationContext.getBeansOfType(InterviewEngine.class).values();
        for (InterviewEngine engine : engines) {
            engineMap.put(engine.engineType().name(), engine);
        }
    }

    @Override
    public InterviewVO analyse(InterviewReq req) {
        List<String> keyWords = buildKeyWords(req.getUrl());
        InterviewEngine engine = engineMap.get(req.getEngine());
        Preconditions.checkArgument(!Objects.isNull(engine), "引擎不能为空！");
        return engine.analyse(keyWords);
    }

    @Override
    public InterviewQuestionVO start(StartReq req) {
        InterviewEngine engine = engineMap.get(req.getEngine());
        Preconditions.checkArgument(!Objects.isNull(engine), "引擎不能为空！");
        return engine.start(req);
    }


    @Override
    public InterviewResultVO submit(InterviewSubmitReq req) {
        InterviewEngine engine = engineMap.get(req.getEngine());
        Preconditions.checkArgument(!Objects.isNull(engine), "引擎不能为空！");
        return engine.submit(req);
    }

    private List<String> buildKeyWords(String url) {
        String pdfText = PDFUtil.getPdfText(normalizePdfUrl(url));
        if (!KeyWordUtil.isInit()) {
            List<String> list = subjectLabelDao.listAllLabel().stream().map(SubjectLabel::getLabelName).collect(Collectors.toList());
            KeyWordUtil.addWord(list);
        }
        return KeyWordUtil.buildKeyWordsLists(pdfText);
    }

    private String normalizePdfUrl(String url) {
        if (!StringUtils.hasText(url)) {
            return url;
        }
        String trimUrl = url.trim();
        if (trimUrl.startsWith("http://") || trimUrl.startsWith("https://")) {
            return trimUrl;
        }
        if (trimUrl.startsWith("/")) {
            return String.format("%s%s", trimTrailingSlash(interviewFileBaseUrl), trimUrl);
        }
        return String.format("%s/%s", trimTrailingSlash(interviewFileBaseUrl), trimUrl);
    }

    private String trimTrailingSlash(String url) {
        if (!StringUtils.hasText(url)) {
            return "";
        }
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

}
