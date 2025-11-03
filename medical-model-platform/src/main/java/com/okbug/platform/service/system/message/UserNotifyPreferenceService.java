package com.okbug.platform.service.system.message;

import com.okbug.platform.entity.system.message.UserNotifyChannelPref;
import com.okbug.platform.entity.system.message.UserNotifyTypePref;

import java.util.List;

/**
 * 新版用户通知偏好服务（按渠道、按类型细粒度）
 */
public interface UserNotifyPreferenceService {

    /** 获取用户所有渠道偏好 */
    List<UserNotifyChannelPref> listChannelPrefs(Long userId);

    /** 批量保存/更新用户渠道偏好（按 userId + channelCode 唯一） */
    void saveOrUpdateChannelPrefs(Long userId, List<UserNotifyChannelPref> prefs, Long operatorId, String operatorName);

    /** 获取用户所有类型偏好 */
    List<UserNotifyTypePref> listTypePrefs(Long userId);

    /** 批量保存/更新用户类型偏好（按 userId + typeCode 唯一） */
    void saveOrUpdateTypePrefs(Long userId, List<UserNotifyTypePref> prefs, Long operatorId, String operatorName);

    /** 查询单个渠道开关（不存在则返回默认 true） */
    boolean isChannelEnabled(Long userId, String channelCode);

    /** 查询单个类型开关（不存在则返回默认 true；营销等可默认 false 视业务而定） */
    boolean isTypeEnabled(Long userId, String typeCode);
}


