package com.okbug.platform.service.system.message.sender;

import com.okbug.platform.entity.auth.User;
import com.okbug.platform.entity.system.message.Message;
import com.okbug.platform.mapper.auth.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * 邮件发送服务：根据消息与用户信息发送邮件
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailSenderService {

    private final JavaMailSender mailSender;
    private final UserMapper userMapper;

    @Value("${spring.mail.username:}")
    private String fromAddress;

    /**
     * 发送站内消息对应的邮件副本
     */
    public void sendMessageEmail(Message message) {
        if (message == null || message.getUserId() == null) {
            log.warn("[EmailSender] 消息或用户为空，跳过发送");
            return;
        }
        User user = userMapper.selectById(message.getUserId());
        if (user == null || user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            log.info("[EmailSender] 用户邮箱不存在，跳过: userId={}", message.getUserId());
            return;
        }

        SimpleMailMessage mail = new SimpleMailMessage();
        if (fromAddress != null && !fromAddress.trim().isEmpty()) {
            mail.setFrom(fromAddress);
        }
        mail.setTo(user.getEmail());
        mail.setSubject(message.getTitle() == null ? "" : message.getTitle());
        mail.setText(message.getContent() == null ? "" : message.getContent());

        mailSender.send(mail);
    }
}


