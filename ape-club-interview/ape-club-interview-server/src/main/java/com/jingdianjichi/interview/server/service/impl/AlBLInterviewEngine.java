package com.jingdianjichi.interview.server.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jingdianjichi.interview.api.enums.EngineEnum;
import com.jingdianjichi.interview.api.req.InterviewSubmitReq;
import com.jingdianjichi.interview.api.req.StartReq;
import com.jingdianjichi.interview.api.vo.InterviewQuestionVO;
import com.jingdianjichi.interview.api.vo.InterviewResultVO;
import com.jingdianjichi.interview.api.vo.InterviewVO;
import com.jingdianjichi.interview.server.config.InterviewAiProperties;
import com.jingdianjichi.interview.server.service.InterviewEngine;
import com.jingdianjichi.interview.server.util.EvaluateUtils;
import com.jingdianjichi.interview.server.util.HttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@Slf4j
@SuppressWarnings("all")
public class AlBLInterviewEngine implements InterviewEngine {

    @Resource
    private InterviewAiProperties interviewAiProperties;

    @Override
    public EngineEnum engineType() {
        return EngineEnum.ALI_BL;
    }

    @Override
    public InterviewVO analyse(List<String> KeyWords) {
        InterviewVO vo = new InterviewVO();
        List<InterviewVO.Interview> questionList = KeyWords.stream().map(item -> {
            InterviewVO.Interview interview = new InterviewVO.Interview();
            interview.setKeyWord(item);
            interview.setCategoryId(-1L);
            interview.setLabelId(-1L);
            return interview;
        }).collect(Collectors.toList());
        vo.setQuestionList(questionList);
        return vo;
    }

    @Override
    public InterviewResultVO submit(InterviewSubmitReq req) {

        validateAiConfig();
        long start = System.currentTimeMillis();
        List<InterviewSubmitReq.Submit> interviews = executeInParallel(
                req.getQuestionList(),
                this::buildInterviewScoreSafely
        );
        req.setQuestionList(interviews);
        String tips = interviews.stream().map(item -> {
            String keyWord = item.getLabelName();
            String evaluate = EvaluateUtils.evaluate(item.getUserScore());
            return String.format(evaluate, keyWord);
        }).distinct().collect(Collectors.joining(";"));
        List<InterviewSubmitReq.Submit> submits = req.getQuestionList();
        double total = submits.stream().mapToDouble(InterviewSubmitReq.Submit::getUserScore).sum();
        double avg = total / submits.size();
        String avtTips = EvaluateUtils.avgEvaluate(avg);
        InterviewResultVO vo = new InterviewResultVO();
        vo.setAvgScore(avg);
        vo.setTips(tips);
        vo.setAvgTips(avtTips);
        log.info("submit total cost {}", System.currentTimeMillis() - start);
        return vo;

    }

    @Override
    public InterviewQuestionVO start(StartReq req) {

        validateAiConfig();
        long start = System.currentTimeMillis();
        List<String> keywords = req.getQuestionList().stream()
                .map(StartReq.Key::getKeyWord)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        Collections.shuffle(keywords, ThreadLocalRandom.current());
        int questionCount = Math.min(interviewAiProperties.getQuestionCount(), keywords.size());
        int candidateCount = Math.min(Math.max(questionCount * 2, questionCount), keywords.size());
        List<String> selectedKeywords = keywords.subList(0, candidateCount);
        List<InterviewQuestionVO.Interview> interviews = executeInParallel(
                selectedKeywords,
                this::buildInterviewSafely
        );
        interviews = deduplicateQuestions(interviews, questionCount);
        if (interviews.size() < questionCount) {
            List<String> usedKeywords = interviews.stream()
                    .map(InterviewQuestionVO.Interview::getKeyWord)
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.toList());
            List<String> remainingKeywords = keywords.stream()
                    .filter(item -> !usedKeywords.contains(item))
                    .collect(Collectors.toList());
            for (String keyword : remainingKeywords) {
                if (interviews.size() >= questionCount) {
                    break;
                }
                InterviewQuestionVO.Interview fallback = buildLocalUniqueQuestion(keyword, interviews.size() + 1);
                if (!containsSameQuestion(interviews, fallback)) {
                    interviews.add(fallback);
                }
            }
        }
        if (interviews.size() > questionCount) {
            interviews = interviews.subList(0, questionCount);
        }
        InterviewQuestionVO vo = new InterviewQuestionVO();
        vo.setQuestionList(interviews);
        log.info("start total cost {}", System.currentTimeMillis() - start);
        return vo;

    }


    private InterviewSubmitReq.Submit buildInterviewScore(InterviewSubmitReq.Submit submit) {
        long start = System.currentTimeMillis();
        String userPrompt = String.format(
                interviewAiProperties.getPrompt().getScoreUserTemplate(),
                safe(submit.getSubjectName()),
                safe(submit.getUserAnswer())
        );
        log.info("buildInterviewScore.prompt:{}", userPrompt);
        String body = executePrompt(interviewAiProperties.getPrompt().getScoreSystem(), userPrompt);
        String json = extractJsonPayload(extractModelText(body));
        InterviewSubmitReq.Submit interviews = JSONObject.parseObject(json, InterviewSubmitReq.Submit.class);
        if (Objects.isNull(interviews.getUserScore())) {
            interviews.setUserScore(0D);
        }
        interviews.setLabelName(submit.getLabelName());
        interviews.setSubjectName(submit.getSubjectName());
        interviews.setUserAnswer(submit.getUserAnswer());
        log.info("cost {} data:{}", System.currentTimeMillis() - start, JSON.toJSONString(interviews));
        return interviews;
    }

    private InterviewSubmitReq.Submit buildInterviewScoreSafely(InterviewSubmitReq.Submit submit) {
        try {
            return buildInterviewScore(submit);
        } catch (Exception e) {
            log.error("buildInterviewScore.error,label:{},question:{}", submit.getLabelName(), submit.getSubjectName(), e);
            InterviewSubmitReq.Submit fallback = new InterviewSubmitReq.Submit();
            fallback.setLabelName(submit.getLabelName());
            fallback.setSubjectName(submit.getSubjectName());
            fallback.setUserAnswer(submit.getUserAnswer());
            fallback.setSubjectAnswer(StringUtils.defaultIfBlank(submit.getSubjectAnswer(), "AI评分暂时不可用，请稍后重试。"));
            fallback.setUserScore(defaultScore(submit.getUserAnswer()));
            return fallback;
        }
    }

    private InterviewQuestionVO.Interview buildInterview(String keyword) {
        long start = System.currentTimeMillis();
        String userPrompt = String.format(interviewAiProperties.getPrompt().getQuestionUserTemplate(), safe(keyword));
        log.info("buildInterview.prompt:{}", userPrompt);
        String body = executePrompt(interviewAiProperties.getPrompt().getQuestionSystem(), userPrompt);
        String json = extractJsonPayload(extractModelText(body));
        InterviewQuestionVO.Interview interviews = JSONObject.parseObject(json, InterviewQuestionVO.Interview.class);
        if (StringUtils.isBlank(interviews.getLabelName())) {
            interviews.setLabelName(keyword);
        }
        if (StringUtils.isBlank(interviews.getKeyWord())) {
            interviews.setKeyWord(keyword);
        }
        if (StringUtils.isBlank(interviews.getSubjectAnswer())) {
            interviews.setSubjectAnswer(String.format("%s 这道题的参考答案未生成完整，建议从定义、原理、场景、优化、排障五个方面组织回答。", keyword));
        }
        log.info("cost {} data:{}", System.currentTimeMillis() - start, JSON.toJSONString(interviews));
        return interviews;
    }

    private InterviewQuestionVO.Interview buildInterviewSafely(String keyword) {
        try {
            return buildInterview(keyword);
        } catch (Exception e) {
            log.error("buildInterview.error,keyword:{}", keyword, e);
            return buildLocalUniqueQuestion(keyword, 1);
        }
    }

    private String executePrompt(String systemPrompt, String userPrompt) {
        JSONObject jsonData = new JSONObject();
        jsonData.put("model", interviewAiProperties.getModel());
        JSONArray messages = new JSONArray();
        messages.add(buildMessage("system", systemPrompt));
        messages.add(buildMessage("user", userPrompt));
        jsonData.put("messages", messages);
        jsonData.put("temperature", 0.2);
        jsonData.put("stream", true);

        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Authorization", "Bearer " + interviewAiProperties.getApiKey());
        headerMap.put("Content-Type", "application/json");

        String body = HttpUtils.executePost(interviewAiProperties.getEndpoint(), jsonData.toJSONString(), headerMap);
        if (StringUtils.isBlank(body)) {
            throw new IllegalArgumentException(String.format("大模型返回为空，endpoint=%s, model=%s", interviewAiProperties.getEndpoint(), interviewAiProperties.getModel()));
        }
        return body;
    }

    private String extractModelText(String body) {
        if (body.contains("data:")) {
            String text = extractStreamContent(body);
            if (StringUtils.isBlank(text)) {
                throw new IllegalArgumentException("大模型流式返回文本为空");
            }
            return text.trim();
        }
        JSONObject responseJson = extractResponseJson(body);
        JSONArray choices = responseJson.getJSONArray("choices");
        if (Objects.isNull(choices) || choices.isEmpty()) {
            throw new IllegalArgumentException("大模型返回缺少 choices 节点");
        }
        JSONObject firstChoice = choices.getJSONObject(0);
        if (Objects.isNull(firstChoice)) {
            throw new IllegalArgumentException("大模型返回 choices[0] 为空");
        }
        JSONObject message = firstChoice.getJSONObject("message");
        if (Objects.isNull(message)) {
            throw new IllegalArgumentException("大模型返回缺少 message 节点");
        }
        String text = extractMessageContent(message.get("content"));
        if (StringUtils.isBlank(text)) {
            throw new IllegalArgumentException("大模型返回文本为空");
        }
        return text.trim();
    }

    private JSONObject extractResponseJson(String body) {
        String trimmed = body.trim();
        if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
            return JSONObject.parseObject(trimmed);
        }
        String[] lines = trimmed.split("\n");
        for (int i = lines.length - 1; i >= 0; i--) {
            String line = lines[i].trim();
            if (!line.startsWith("data:")) {
                continue;
            }
            String payload = line.substring(5).trim();
            if (StringUtils.isBlank(payload) || "[DONE]".equalsIgnoreCase(payload)) {
                continue;
            }
            return JSONObject.parseObject(payload);
        }
        throw new IllegalArgumentException("大模型返回格式无法解析");
    }

    private String extractJsonPayload(String text) {
        String trimmed = text.trim();
        if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
            return trimmed;
        }
        int start = trimmed.indexOf('{');
        int end = trimmed.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return trimmed.substring(start, end + 1);
        }
        throw new IllegalArgumentException("大模型未返回合法 JSON");
    }

    private JSONObject buildMessage(String role, String content) {
        JSONObject message = new JSONObject();
        message.put("role", role);
        message.put("content", content);
        return message;
    }

    private String extractMessageContent(Object content) {
        if (Objects.isNull(content)) {
            return null;
        }
        if (content instanceof String) {
            return (String) content;
        }
        if (content instanceof JSONArray) {
            JSONArray contentArray = (JSONArray) content;
            return contentArray.stream()
                    .filter(Objects::nonNull)
                    .map(item -> {
                        if (item instanceof String) {
                            return (String) item;
                        }
                        if (!(item instanceof JSONObject)) {
                            return null;
                        }
                        JSONObject block = (JSONObject) item;
                        if (StringUtils.isNotBlank(block.getString("text"))) {
                            return block.getString("text");
                        }
                        JSONObject textObject = block.getJSONObject("text");
                        return Objects.nonNull(textObject) ? textObject.getString("value") : null;
                    })
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.joining("\n"));
        }
        return String.valueOf(content);
    }

    private String extractStreamContent(String body) {
        StringBuilder contentBuilder = new StringBuilder();
        String[] lines = body.split("\n");
        for (String rawLine : lines) {
            String line = StringUtils.trimToEmpty(rawLine);
            if (!line.startsWith("data:")) {
                continue;
            }
            String payload = line.substring(5).trim();
            if (StringUtils.isBlank(payload) || "[DONE]".equalsIgnoreCase(payload)) {
                continue;
            }
            JSONObject chunk = JSONObject.parseObject(payload);
            JSONArray choices = chunk.getJSONArray("choices");
            if (Objects.isNull(choices) || choices.isEmpty()) {
                continue;
            }
            JSONObject firstChoice = choices.getJSONObject(0);
            if (Objects.isNull(firstChoice)) {
                continue;
            }
            JSONObject delta = firstChoice.getJSONObject("delta");
            if (Objects.nonNull(delta) && StringUtils.isNotBlank(delta.getString("content"))) {
                contentBuilder.append(delta.getString("content"));
                continue;
            }
            JSONObject message = firstChoice.getJSONObject("message");
            if (Objects.nonNull(message)) {
                String content = extractMessageContent(message.get("content"));
                if (StringUtils.isNotBlank(content)) {
                    contentBuilder.append(content);
                }
            }
        }
        return contentBuilder.toString();
    }

    private void validateAiConfig() {
        if (StringUtils.isBlank(interviewAiProperties.getApiKey())) {
            throw new IllegalArgumentException("请先配置 interview.ai.api-key 或环境变量 DASHSCOPE_API_KEY");
        }
        if (StringUtils.isBlank(interviewAiProperties.getEndpoint())) {
            throw new IllegalArgumentException("请先配置 interview.ai.endpoint");
        }
        if (StringUtils.isBlank(interviewAiProperties.getModel())) {
            throw new IllegalArgumentException("请先配置 interview.ai.model");
        }
    }

    private String safe(String text) {
        return StringUtils.defaultString(text).trim();
    }

    private Double defaultScore(String userAnswer) {
        return StringUtils.isBlank(userAnswer) ? 0D : 3D;
    }

    private List<InterviewQuestionVO.Interview> deduplicateQuestions(List<InterviewQuestionVO.Interview> sourceList, int limit) {
        List<InterviewQuestionVO.Interview> result = new ArrayList<>();
        Set<String> seenQuestionSet = new LinkedHashSet<>();
        for (InterviewQuestionVO.Interview item : sourceList) {
            if (Objects.isNull(item) || StringUtils.isBlank(item.getSubjectName())) {
                continue;
            }
            String normalizedQuestion = normalizeQuestion(item.getSubjectName());
            if (seenQuestionSet.add(normalizedQuestion)) {
                result.add(item);
            }
            if (result.size() >= limit) {
                break;
            }
        }
        return result;
    }

    private boolean containsSameQuestion(List<InterviewQuestionVO.Interview> sourceList, InterviewQuestionVO.Interview target) {
        String normalizedTarget = normalizeQuestion(target.getSubjectName());
        return sourceList.stream()
                .filter(Objects::nonNull)
                .map(InterviewQuestionVO.Interview::getSubjectName)
                .filter(StringUtils::isNotBlank)
                .map(this::normalizeQuestion)
                .anyMatch(normalizedTarget::equals);
    }

    private String normalizeQuestion(String question) {
        return StringUtils.deleteWhitespace(StringUtils.defaultString(question))
                .replace("？", "?")
                .replace("，", ",")
                .toLowerCase(Locale.ROOT);
    }

    private InterviewQuestionVO.Interview buildLocalUniqueQuestion(String keyword, int index) {
        InterviewQuestionVO.Interview fallback = new InterviewQuestionVO.Interview();
        fallback.setLabelName(keyword);
        fallback.setKeyWord(keyword);
        switch ((index - 1) % 4) {
            case 1:
                fallback.setSubjectName(String.format("在生产环境中使用 %s 时，你会重点关注哪些稳定性问题？请结合排查思路说明。", keyword));
                break;
            case 2:
                fallback.setSubjectName(String.format("请围绕 %s 设计一个可落地的业务方案，并说明核心流程、技术选型和风险点。", keyword));
                break;
            case 3:
                fallback.setSubjectName(String.format("如果 %s 相关链路出现性能瓶颈，你会如何定位、优化并验证效果？", keyword));
                break;
            default:
                fallback.setSubjectName(String.format("请结合 %s 相关项目经历，系统说明其核心原理、适用场景、常见问题以及你的实践方案。", keyword));
                break;
        }
        fallback.setSubjectAnswer(String.format("%s 这道题建议从定义、原理、场景、优化、排障五个维度组织回答。", keyword));
        return fallback;
    }

    private <T, R> List<R> executeInParallel(List<T> sourceList, java.util.function.Function<T, R> function) {
        if (sourceList == null || sourceList.isEmpty()) {
            return Collections.emptyList();
        }
        int parallelism = Math.max(1, Math.min(interviewAiProperties.getParallelism(), sourceList.size()));
        ExecutorService executorService = Executors.newFixedThreadPool(parallelism);
        try {
            List<CompletableFuture<R>> futureList = sourceList.stream()
                    .map(item -> CompletableFuture.supplyAsync(() -> function.apply(item), executorService))
                    .collect(Collectors.toList());
            return futureList.stream().map(CompletableFuture::join).collect(Collectors.toList());
        } finally {
            executorService.shutdown();
        }
    }

}
