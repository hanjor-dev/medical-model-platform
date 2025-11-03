package com.okbug.platform.domain.notify;

import com.okbug.platform.dto.system.DictDataDTO;
import com.okbug.platform.service.system.SystemDictService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * 动态通知字典：从 system_dict 读取“渠道/类型”配置，提供运行时常量视图。
 * 顶级编码：
 * - 渠道：DICT_4.1
 * - 类型：DICT_4.2
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotifyDictionary {

    private final SystemDictService systemDictService;

    private static final String CHANNEL_DICT_CODE = "DICT_4.1";
    private static final String TYPE_DICT_CODE = "DICT_4.2";

    private final AtomicReference<Set<String>> channelCodesRef = new AtomicReference<>(Collections.emptySet());
    private final AtomicReference<Set<String>> typeCodesRef = new AtomicReference<>(Collections.emptySet());

    @PostConstruct
    public void load() {
        refresh();
    }

    public synchronized void refresh() {
        this.channelCodesRef.set(loadCodes(CHANNEL_DICT_CODE));
        this.typeCodesRef.set(loadCodes(TYPE_DICT_CODE));
        log.info("[NotifyDictionary] loaded: channels={}, types={}", channelCodesRef.get(), typeCodesRef.get());
    }

    public Set<String> channelCodes() {
        return channelCodesRef.get();
    }

    public Set<String> typeCodes() {
        return typeCodesRef.get();
    }

    public String channelCodesCsv() {
        Set<String> set = channelCodes();
        if (set.isEmpty()) return "";
        return String.join(",", set);
    }

    private Set<String> loadCodes(String dictCode) {
        try {
            List<DictDataDTO> items = systemDictService.getChildrenOptionsByCode(dictCode);
            if (items == null) return Collections.emptySet();
            // 使用 value 优先，否则回退 code；统一为小写并去重、保持插入顺序
            return items.stream()
                    .map(d -> {
                        String v = d.getValue();
                        if (v == null || v.trim().isEmpty()) v = d.getCode();
                        return v == null ? null : v.trim().toLowerCase();
                    })
                    .filter(Objects::nonNull)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        } catch (Exception e) {
            log.warn("[NotifyDictionary] load failed: code={}, error={}", dictCode, e.getMessage());
            return Collections.emptySet();
        }
    }
}


