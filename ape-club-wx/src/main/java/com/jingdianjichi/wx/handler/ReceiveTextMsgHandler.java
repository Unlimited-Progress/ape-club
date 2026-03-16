package com.jingdianjichi.wx.handler;

import com.jingdianjichi.wx.redis.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class ReceiveTextMsgHandler implements WxChatMsgHandler {

    private static final String KEY_WORD = "验证码";

    private static final String LOGIN_PREFIX = "loginCode";

    private static final int CODE_RETRY_TIMES = 10;

    @Resource
    private RedisUtil redisUtil;

    @Override
    public WxChatMsgTypeEnum getMsgType() {
        return WxChatMsgTypeEnum.TEXT_MSG;
    }

    @Override
    public String dealMsg(Map<String, String> messageMap) {
        log.info("接收到文本消息事件");
        String content = messageMap.get("Content");
        if (!KEY_WORD.equals(content)) {
            return "";
        }
        String fromUserName = messageMap.get("FromUserName");
        String toUserName = messageMap.get("ToUserName");

        Random random = new Random();
        String validCode = null;
        for (int i = 0; i < CODE_RETRY_TIMES; i++) {
            String code = String.format("%03d", random.nextInt(1000));
            String loginKey = redisUtil.buildKey(LOGIN_PREFIX, code);
            if (redisUtil.setNx(loginKey, fromUserName, 5L, TimeUnit.MINUTES)) {
                validCode = code;
                break;
            }
        }

        String numContent;
        if (validCode == null) {
            log.error("验证码生成失败，Redis中短时间内命中冲突次数过多");
            numContent = "当前访问人数较多，请稍后再试";
        } else {
            numContent = "您当前的验证码是：" + validCode + "！ 5分钟内有效";
        }
        String replyContent = "<xml>\n" +
                "  <ToUserName><![CDATA[" + fromUserName + "]]></ToUserName>\n" +
                "  <FromUserName><![CDATA[" + toUserName + "]]></FromUserName>\n" +
                "  <CreateTime>12345678</CreateTime>\n" +
                "  <MsgType><![CDATA[text]]></MsgType>\n" +
                "  <Content><![CDATA[" + numContent + "]]></Content>\n" +
                "</xml>";

        return replyContent;
    }

}
