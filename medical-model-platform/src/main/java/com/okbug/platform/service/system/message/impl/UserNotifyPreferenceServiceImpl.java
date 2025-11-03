package com.okbug.platform.service.system.message.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.okbug.platform.common.base.ErrorCode;
import com.okbug.platform.common.base.ServiceException;
import com.okbug.platform.dto.system.DictDataDTO;
import com.okbug.platform.entity.system.message.UserNotifyChannelPref;
import com.okbug.platform.entity.system.message.UserNotifyTypePref;
import com.okbug.platform.domain.notify.MessageChannel;
import com.okbug.platform.domain.notify.MessageType;
import com.okbug.platform.mapper.system.message.UserNotifyChannelPrefMapper;
import com.okbug.platform.mapper.system.message.UserNotifyTypePrefMapper;
import com.okbug.platform.service.system.SystemDictService;
import com.okbug.platform.service.system.message.UserNotifyPreferenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
 
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserNotifyPreferenceServiceImpl implements UserNotifyPreferenceService {

    private final UserNotifyChannelPrefMapper channelMapper;
    private final UserNotifyTypePrefMapper typeMapper;
    private final SystemDictService systemDictService;

    @Override
    /**
     * 获取用户渠道偏好；若无个性化记录，则按字典返回默认开启集合，且强制开启 inbox。
     *
     * @param userId 用户ID
     * @return 渠道偏好列表
     */
    public List<UserNotifyChannelPref> listChannelPrefs(Long userId) {
        if (userId == null) return Collections.emptyList();
        LambdaQueryWrapper<UserNotifyChannelPref> qw = new LambdaQueryWrapper<>();
        qw.eq(UserNotifyChannelPref::getUserId, userId);
        List<UserNotifyChannelPref> list = channelMapper.selectList(qw);
        if (list != null && !list.isEmpty()) {
            // 强制站内(inbox)为开启（仅返回视图层，不改库）
            list.forEach(p -> {
                String code = p.getChannelCode() == null ? null : p.getChannelCode().toLowerCase();
                if (MessageChannel.INBOX.code().equals(code)) {
                    p.setEnabled(1);
                }
            });
            return list;
        }
        // 无个性化配置时，按字典返回默认开启的渠道列表（不落库）
        List<DictDataDTO> options = systemDictService.getChildrenOptionsByCode("DICT_4.1");
        if (options == null) return Collections.emptyList();
        return options.stream().map(opt -> {
            UserNotifyChannelPref p = new UserNotifyChannelPref();
            p.setUserId(userId);
            String biz = opt.getValue() != null && !opt.getValue().trim().isEmpty()
                    ? opt.getValue().toLowerCase()
                    : opt.getCode();
            p.setChannelCode(biz);
            p.setEnabled(1); // 默认开启
            // doNotDisturb 留空
            return p;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    /**
     * 保存或更新用户渠道偏好。
     * 规则：inbox 频道必须开启且不可关闭；空列表将被忽略。
     */
    public void saveOrUpdateChannelPrefs(Long userId, List<UserNotifyChannelPref> prefs, Long operatorId, String operatorName) {
        if (userId == null) {
            throw new ServiceException(ErrorCode.PARAM_MISSING, "用户ID不能为空");
        }
        if (prefs == null) return;
        for (UserNotifyChannelPref p : prefs) {
            if (p == null) continue;
            p.setUserId(userId);
            if (p.getChannelCode() == null || p.getChannelCode().trim().isEmpty()) {
                throw new ServiceException(ErrorCode.PARAM_MISSING, "channelCode不能为空");
            }
            // 业务规则：站内(inbox)必须开启且不可关闭
            String code = p.getChannelCode().toLowerCase();
            if (MessageChannel.INBOX.code().equals(code)) {
                p.setEnabled(1);
            }
            UserNotifyChannelPref exist = getChannelPref(userId, p.getChannelCode());
            if (exist == null) {
                p.setCreateTime(LocalDateTime.now());
                p.setUpdateTime(LocalDateTime.now());
                channelMapper.insert(p);
                log.info("[NotifyPref][channel] created: userId={}, channel={} ", userId, p.getChannelCode());
            } else {
                p.setId(exist.getId());
                p.setUpdateTime(LocalDateTime.now());
                channelMapper.updateById(p);
                log.info("[NotifyPref][channel] updated: userId={}, channel={} ", userId, p.getChannelCode());
            }
        }
    }

    @Override
    /**
     * 获取用户类型偏好；若无个性化记录，则按字典返回默认开启集合，且强制开启 system。
     */
    public List<UserNotifyTypePref> listTypePrefs(Long userId) {
        if (userId == null) return Collections.emptyList();
        LambdaQueryWrapper<UserNotifyTypePref> qw = new LambdaQueryWrapper<>();
        qw.eq(UserNotifyTypePref::getUserId, userId);
        List<UserNotifyTypePref> list = typeMapper.selectList(qw);
        if (list != null && !list.isEmpty()) {
            // 强制系统(system)为开启（仅返回视图层，不改库）
            list.forEach(p -> {
                String code = p.getTypeCode() == null ? null : p.getTypeCode().toLowerCase();
                if (MessageType.SYSTEM.code().equals(code)) {
                    p.setEnabled(1);
                }
            });
            return list;
        }
        // 无个性化配置时，按字典返回默认开启的类型列表（不落库）
        List<DictDataDTO> options = systemDictService.getChildrenOptionsByCode("DICT_4.2");
        if (options == null) return Collections.emptyList();
        return options.stream().map(opt -> {
            UserNotifyTypePref p = new UserNotifyTypePref();
            p.setUserId(userId);
            String biz = opt.getValue() != null && !opt.getValue().trim().isEmpty()
                    ? opt.getValue().toLowerCase()
                    : opt.getCode();
            p.setTypeCode(biz);
            p.setEnabled(1); // 默认开启
            return p;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    /**
     * 保存或更新用户类型偏好。
     * 规则：system 类型必须开启且不可关闭；空列表将被忽略。
     */
    public void saveOrUpdateTypePrefs(Long userId, List<UserNotifyTypePref> prefs, Long operatorId, String operatorName) {
        if (userId == null) {
            throw new ServiceException(ErrorCode.PARAM_MISSING, "用户ID不能为空");
        }
        if (prefs == null) return;
        for (UserNotifyTypePref p : prefs) {
            if (p == null) continue;
            p.setUserId(userId);
            if (p.getTypeCode() == null || p.getTypeCode().trim().isEmpty()) {
                throw new ServiceException(ErrorCode.PARAM_MISSING, "typeCode不能为空");
            }
            // 业务规则：系统消息(system)必须开启且不可关闭
            String code = p.getTypeCode().toLowerCase();
            if (MessageType.SYSTEM.code().equals(code)) {
                p.setEnabled(1);
            }
            UserNotifyTypePref exist = getTypePref(userId, p.getTypeCode());
            if (exist == null) {
                p.setCreateTime(LocalDateTime.now());
                p.setUpdateTime(LocalDateTime.now());
                typeMapper.insert(p);
                log.info("[NotifyPref][type] created: userId={}, type={} ", userId, p.getTypeCode());
            } else {
                p.setId(exist.getId());
                p.setUpdateTime(LocalDateTime.now());
                typeMapper.updateById(p);
                log.info("[NotifyPref][type] updated: userId={}, type={} ", userId, p.getTypeCode());
            }
        }
    }

    @Override
    /**
     * 判断指定用户的渠道是否生效；inbox 恒为 true；未配置默认开启。
     */
    public boolean isChannelEnabled(Long userId, String channelCode) {
        if (userId == null || channelCode == null) return true;
        // 站内(inbox)永远开启
        if (MessageChannel.INBOX.code().equalsIgnoreCase(channelCode)) {
            return true;
        }
        UserNotifyChannelPref p = getChannelPref(userId, channelCode);
        if (p == null || p.getEnabled() == null) {
            // 默认策略：全部开启
            return true;
        }
        return p.getEnabled() == 1;
    }

    @Override
    /**
     * 判断指定用户的消息类型是否生效；system 恒为 true；未配置默认开启。
     */
    public boolean isTypeEnabled(Long userId, String typeCode) {
        if (userId == null || typeCode == null) return true;
        // 系统(system)永远开启
        if (MessageType.SYSTEM.code().equalsIgnoreCase(typeCode)) {
            return true;
        }
        UserNotifyTypePref p = getTypePref(userId, typeCode);
        if (p == null || p.getEnabled() == null) {
            // 默认策略：全部开启
            return true;
        }
        return p.getEnabled() == 1;
    }

    /**
     * 查询单条渠道偏好记录（私有方法）。
     */
    private UserNotifyChannelPref getChannelPref(Long userId, String channelCode) {
        LambdaQueryWrapper<UserNotifyChannelPref> qw = new LambdaQueryWrapper<>();
        qw.eq(UserNotifyChannelPref::getUserId, userId)
          .eq(UserNotifyChannelPref::getChannelCode, channelCode);
        return channelMapper.selectOne(qw);
    }

    /**
     * 查询单条类型偏好记录（私有方法）。
     */
    private UserNotifyTypePref getTypePref(Long userId, String typeCode) {
        LambdaQueryWrapper<UserNotifyTypePref> qw = new LambdaQueryWrapper<>();
        qw.eq(UserNotifyTypePref::getUserId, userId)
          .eq(UserNotifyTypePref::getTypeCode, typeCode);
        return typeMapper.selectOne(qw);
    }
}


