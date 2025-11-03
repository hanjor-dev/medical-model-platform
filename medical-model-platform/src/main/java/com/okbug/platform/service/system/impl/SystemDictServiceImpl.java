/**
 * 系统字典服务实现类：系统字典业务逻辑实现
 * 
 * 核心功能：
 * 1. 实现系统字典的完整CRUD操作
 * 2. 管理Redis缓存机制，提高字典访问性能
 * 3. 支持多级字典结构管理
 * 4. 提供字典值获取的便捷方法
 * 5. 集成缓存更新，支持字典热更新
 * 6. 支持字典状态控制和排序管理
 * 7. 提供字典映射关系和批量操作功能
 * 
 * @author hanjor
 * @version 2.0
 * @date 2025-01-15 10:00:00
 */
package com.okbug.platform.service.system.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.okbug.platform.common.base.ErrorCode;
import com.okbug.platform.common.base.ServiceException;
import com.okbug.platform.dto.system.SystemDictDTO;
import com.okbug.platform.dto.system.SystemDictQueryDTO;
import com.okbug.platform.dto.system.SystemDictCreateDTO;
import com.okbug.platform.dto.system.SystemDictUpdateDTO;
import com.okbug.platform.dto.system.DictTreeDTO;
import com.okbug.platform.dto.system.DictDataDTO;
import com.okbug.platform.dto.system.DictModuleOptionDTO;
import com.okbug.platform.dto.system.ImportResultDTO;
import com.okbug.platform.entity.system.SystemDict;
import com.okbug.platform.mapper.system.SystemDictMapper;
import com.okbug.platform.service.system.SystemDictService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.OutputStreamWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * 系统字典服务实现类
 * 实现SystemDictService接口定义的所有业务方法
 * 使用Redis缓存提高字典访问性能，支持多级字典结构管理
 */
@Service
@Slf4j
public class SystemDictServiceImpl implements SystemDictService {
    
    @Autowired
    private SystemDictMapper systemDictMapper;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    /**
     * Redis缓存配置
     */
    private static final String DICT_CACHE_PREFIX = "system:dict:";
    private static final int CACHE_TTL = 3600; // 1小时
    
    @Override
    public IPage<SystemDictDTO> getDicts(SystemDictQueryDTO queryDto) {
        log.debug("开始查询系统字典，查询条件: {}", queryDto);
        
        // 构建查询条件
        LambdaQueryWrapper<SystemDict> query = new LambdaQueryWrapper<>();
        query.eq(SystemDict::getIsDeleted, 0);
        
        // 字典编码模糊搜索
        if (StringUtils.hasText(queryDto.getDictCode())) {
            query.like(SystemDict::getDictCode, queryDto.getDictCode());
        }
        
        // 关键词搜索（同时搜索字典名称和标签）
        if (StringUtils.hasText(queryDto.getKeyword())) {
            query.and(wrapper -> wrapper
                .like(SystemDict::getDictName, queryDto.getKeyword())
                .or()
                .like(SystemDict::getDictLabel, queryDto.getKeyword())
            );
        }
        
        // 模块筛选
        if (StringUtils.hasText(queryDto.getModule())) {
            query.eq(SystemDict::getModule, queryDto.getModule());
        }
        
        // 状态筛选
        if (queryDto.getStatus() != null) {
            query.eq(SystemDict::getStatus, queryDto.getStatus());
        }
        
        // 父级ID筛选
        if (queryDto.getParentId() != null) {
            query.eq(SystemDict::getParentId, queryDto.getParentId());
        }
        
        // 层级筛选
        if (queryDto.getLevel() != null) {
            if (queryDto.getLevel() >= 3) {
                // 三级及以下：查询level >= 3的所有记录
                query.ge(SystemDict::getLevel, 3);
            } else {
                // 一级、二级：精确匹配
            query.eq(SystemDict::getLevel, queryDto.getLevel());
            }
        }
        
        // 排序处理
        if (StringUtils.hasText(queryDto.getSortField())) {
            String sortField = queryDto.getSortField();
            boolean isAsc = "asc".equals(queryDto.getSortDirection());
            if ("dictCode".equals(sortField)) {
                query.orderBy(true, isAsc, SystemDict::getDictCode);
            } else if ("dictName".equals(sortField)) {
                query.orderBy(true, isAsc, SystemDict::getDictName);
            } else if ("module".equals(sortField)) {
                query.orderBy(true, isAsc, SystemDict::getModule);
            } else if ("level".equals(sortField)) {
                query.orderBy(true, isAsc, SystemDict::getLevel);
            } else if ("createTime".equals(sortField)) {
                query.orderBy(true, isAsc, SystemDict::getCreateTime);
            } else if ("updateTime".equals(sortField)) {
                query.orderBy(true, isAsc, SystemDict::getUpdateTime);
            } else if ("path".equals(sortField)) {
        
                // path字段使用自定义排序，这里不设置数据库排序，将在内存中处理
                query.orderByDesc(SystemDict::getCreateTime); // 设置一个默认排序以保证结果稳定
            } else {
                // 默认排序：按创建时间降序
                query.orderByDesc(SystemDict::getCreateTime);
            }
        } else {
            // 默认排序：按创建时间降序
            query.orderByDesc(SystemDict::getCreateTime);
        }
        
        // 分页查询
        Page<SystemDict> page = new Page<>(queryDto.getPageNum(), queryDto.getPageSize());
        IPage<SystemDict> result = systemDictMapper.selectPage(page, query);
        
        // 转换为DTO
        List<SystemDictDTO> dicts = result.getRecords().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
        
        // 如果是按path排序，需要在内存中进行语义版本号排序
        if (StringUtils.hasText(queryDto.getSortField()) && "path".equals(queryDto.getSortField())) {
            boolean isAsc = "asc".equals(queryDto.getSortDirection());
            dicts = sortByPath(dicts, isAsc);
        }
        
        // 构建返回结果
        Page<SystemDictDTO> dtoPage = new Page<>(queryDto.getPageNum(), queryDto.getPageSize());
        dtoPage.setRecords(dicts);
        dtoPage.setTotal(result.getTotal());
        dtoPage.setCurrent(result.getCurrent());
        dtoPage.setSize(result.getSize());
        
        log.debug("系统字典查询完成，共查询到 {} 条记录", result.getTotal());
        return dtoPage;
    }
    
    @Override
    public SystemDictDTO getDictById(Long id) {
        log.debug("根据ID查询系统字典，ID: {}", id);
        
        SystemDict dict = systemDictMapper.selectById(id);
        if (dict == null || dict.getIsDeleted() == 1) {
            log.warn("系统字典不存在或已删除，ID: {}", id);
            throw new ServiceException(ErrorCode.DATA_NOT_FOUND, "系统字典不存在");
        }
        
        return convertToDTO(dict);
    }
    
    @Override
    @Transactional
    public SystemDictDTO createDict(SystemDictCreateDTO createDto) {
        log.info("开始创建系统字典，名称: {}, 模块: {}, 父级ID: {}", 
            createDto.getDictName(), createDto.getModule(), createDto.getParentId());
        
        // 检查同一父级下字典名称是否重复
        Long parentId = createDto.getParentId() != null ? createDto.getParentId() : 0L;
        if (systemDictMapper.countByParentIdAndDictName(parentId, createDto.getDictName()) > 0) {
            log.error("同一父级下字典名称已存在，父级ID: {}, 名称: {}", parentId, createDto.getDictName());
            throw new ServiceException(ErrorCode.DATA_ALREADY_EXISTS, "同一父级下字典名称已存在");
        }
        
        // 创建字典对象
        SystemDict dict = new SystemDict();
        dict.setDictName(createDto.getDictName());
        dict.setDictLabel(createDto.getDictLabel());
        dict.setDescription(createDto.getDescription());
        dict.setParentId(parentId);
        dict.setModule(createDto.getModule());
        dict.setStatus(createDto.getStatus() != null ? createDto.getStatus() : 1); // 默认启用
        dict.setSortOrder(createDto.getSortOrder() != null ? createDto.getSortOrder() : 0); // 默认排序
        dict.setIsDeleted(0);
        dict.setCreateTime(LocalDateTime.now());
        dict.setUpdateTime(LocalDateTime.now());
        
        // 生成字典编码、层级和路径
        generateDictCodeAndPath(dict);
        
        // 保存到数据库
        systemDictMapper.insert(dict);
        
        // 检查生成的字典编码是否已存在（防止并发问题）
        if (systemDictMapper.countByDictCodeExcludeId(dict.getDictCode(), dict.getId()) > 0) {
            log.error("生成的字典编码已存在，字典编码: {}", dict.getDictCode());
            throw new ServiceException(ErrorCode.DATA_ALREADY_EXISTS, "字典编码冲突，请重试");
        }
        
        // 更新字典信息（包含生成的编码、层级和路径）
        systemDictMapper.updateById(dict);
        
        // 更新缓存
        clearDictCache(dict.getDictCode());
        
        log.info("系统字典创建成功，字典编码: {}, 名称: {}, ID: {}", 
            dict.getDictCode(), dict.getDictName(), dict.getId());
        
        return convertToDTO(dict);
    }
    
    @Override
    @Transactional
    public SystemDictDTO updateDict(Long id, SystemDictUpdateDTO updateDto) {
        log.info("开始更新系统字典，ID: {}, 更新内容: {}", id, updateDto);
        
        // 获取字典
        SystemDict dict = systemDictMapper.selectById(id);
        if (dict == null || dict.getIsDeleted() == 1) {
            log.error("系统字典不存在或已删除，ID: {}", id);
            throw new ServiceException(ErrorCode.DATA_NOT_FOUND, "字典不存在");
        }
        
        // 检查同一父级下字典名称是否重复（如果修改了名称）
        if (updateDto.getDictName() != null && !updateDto.getDictName().equals(dict.getDictName())) {
            Long parentId = updateDto.getParentId() != null ? updateDto.getParentId() : dict.getParentId();
            if (systemDictMapper.countByParentIdAndDictNameExcludeId(parentId, updateDto.getDictName(), id) > 0) {
                log.error("同一父级下字典名称已存在，父级ID: {}, 名称: {}", parentId, updateDto.getDictName());
                throw new ServiceException(ErrorCode.DATA_ALREADY_EXISTS, "同一父级下字典名称已存在");
            }
        }
        
        // 如果修改了父级关系，也要检查新父级下是否存在重复名称
        if (updateDto.getParentId() != null && !updateDto.getParentId().equals(dict.getParentId())) {
            String dictName = updateDto.getDictName() != null ? updateDto.getDictName() : dict.getDictName();
            if (systemDictMapper.countByParentIdAndDictNameExcludeId(updateDto.getParentId(), dictName, id) > 0) {
                log.error("新父级下字典名称已存在，父级ID: {}, 名称: {}", updateDto.getParentId(), dictName);
                throw new ServiceException(ErrorCode.DATA_ALREADY_EXISTS, "新父级下字典名称已存在");
            }
        }
        
        // 更新字典信息
        boolean needRecalculate = false;
        
        if (updateDto.getDictName() != null) {
            dict.setDictName(updateDto.getDictName());
        }
        if (updateDto.getDictLabel() != null) {
            dict.setDictLabel(updateDto.getDictLabel());
        }
        if (updateDto.getDescription() != null) {
            dict.setDescription(updateDto.getDescription());
        }
        if (updateDto.getModule() != null) {
            dict.setModule(updateDto.getModule());
        }
        if (updateDto.getStatus() != null) {
            dict.setStatus(updateDto.getStatus());
        }
        if (updateDto.getSortOrder() != null) {
            dict.setSortOrder(updateDto.getSortOrder());
        }
        
        // 如果修改了父级关系，需要重新计算层级和路径
        if (updateDto.getParentId() != null && !updateDto.getParentId().equals(dict.getParentId())) {
            dict.setParentId(updateDto.getParentId());
            needRecalculate = true;
        }
        
        if (needRecalculate) {
            // 对于更新操作，不重新生成编码，只更新层级和路径关系
            updateLevelAndPath(dict);
        }
        
        dict.setUpdateTime(LocalDateTime.now());
        
        // 更新数据库
        systemDictMapper.updateById(dict);
        
        // 更新缓存
        clearDictCache(dict.getDictCode());
        
        log.info("系统字典更新成功，字典编码: {}, 名称: {}, ID: {}", 
            dict.getDictCode(), dict.getDictName(), dict.getId());
        
        return convertToDTO(dict);
    }
    
    @Override
    public Boolean deleteDict(Long id) {
        log.info("开始删除系统字典，ID: {}", id);
        
        // 检查是否有子字典
        List<SystemDict> children = systemDictMapper.selectByParentId(id);
        if (!children.isEmpty()) {
            log.error("系统字典存在子字典，无法删除，ID: {}, 子字典数量: {}", id, children.size());
            throw new ServiceException(ErrorCode.OPERATION_NOT_ALLOWED, "字典存在子字典，无法删除");
        }
        
        // 检查字典是否存在
        SystemDict dict = systemDictMapper.selectById(id);
        if (dict == null) {
            log.error("系统字典不存在，ID: {}", id);
            throw new ServiceException(ErrorCode.DATA_NOT_FOUND, "字典不存在");
        }
        
        // 获取字典编码用于清除缓存
        String dictCode = dict.getDictCode();
        String dictName = dict.getDictName();
        
        // 逻辑删除字典（MyBatis-Plus 会自动设置 isDeleted = 1）
        int deleteResult = systemDictMapper.deleteById(id);
        
        if (deleteResult == 0) {
            log.error("系统字典删除失败，ID: {}", id);
            throw new ServiceException(ErrorCode.INTERNAL_ERROR, "字典删除失败");
        }
        
        // 清除缓存
        clearDictCache(dictCode);
        
        log.info("系统字典删除成功，字典编码: {}, 名称: {}, ID: {}", 
            dictCode, dictName, id);
        
        return true;
    }
    
    @Override
    public List<String> getModules() {
        log.debug("开始获取所有模块列表");
        
        // 首先查询父级字典编码为 DICT_1 的字典
        LambdaQueryWrapper<SystemDict> parentQuery = new LambdaQueryWrapper<>();
        parentQuery.eq(SystemDict::getDictCode, "DICT_1")
                   .eq(SystemDict::getIsDeleted, 0)
                   .eq(SystemDict::getStatus, 1);
        
        SystemDict parentDict = systemDictMapper.selectOne(parentQuery);
        if (parentDict == null) {
            log.warn("未找到父级字典 DICT_1");
            return new ArrayList<>();
        }
        
        // 查询父级字典 DICT_1 下的所有子级字典的 dictName 作为模块列表
        LambdaQueryWrapper<SystemDict> query = new LambdaQueryWrapper<>();
        query.select(SystemDict::getDictName)
             .eq(SystemDict::getParentId, parentDict.getId())
             .eq(SystemDict::getIsDeleted, 0)
             .eq(SystemDict::getStatus, 1)
             .orderByAsc(SystemDict::getSortOrder)
             .orderByAsc(SystemDict::getDictName);
        
        List<SystemDict> dicts = systemDictMapper.selectList(query);
        List<String> modules = dicts.stream()
            .map(SystemDict::getDictName)
            .filter(dictName -> dictName != null && !dictName.trim().isEmpty())
            .collect(Collectors.toList());
        
        log.debug("模块列表获取完成，共 {} 个模块: {}", modules.size(), modules);
        return modules;
    }
    
    @Override
    public List<DictModuleOptionDTO> getDictModules() {
        log.debug("开始获取字典模块选项列表");
        
        // 首先查询父级字典编码为 DICT_1 的字典
        LambdaQueryWrapper<SystemDict> parentQuery = new LambdaQueryWrapper<>();
        parentQuery.eq(SystemDict::getDictCode, "DICT_1")
                   .eq(SystemDict::getIsDeleted, 0)
                   .eq(SystemDict::getStatus, 1);
        
        SystemDict parentDict = systemDictMapper.selectOne(parentQuery);
        if (parentDict == null) {
            log.warn("未找到父级字典 DICT_1");
            return new ArrayList<>();
        }
        
        // 查询父级字典 DICT_1 下的所有子级字典，获取 dictCode 和 dictLabel
        LambdaQueryWrapper<SystemDict> query = new LambdaQueryWrapper<>();
        query.select(SystemDict::getDictCode, SystemDict::getDictLabel, SystemDict::getDictName)
             .eq(SystemDict::getParentId, parentDict.getId())
             .eq(SystemDict::getIsDeleted, 0)
             .eq(SystemDict::getStatus, 1)
             .orderByAsc(SystemDict::getSortOrder)
             .orderByAsc(SystemDict::getDictCode);
        
        List<SystemDict> dicts = systemDictMapper.selectList(query);
        List<DictModuleOptionDTO> moduleOptions = dicts.stream()
            .filter(dict -> dict.getDictCode() != null && !dict.getDictCode().trim().isEmpty())
            .map(dict -> {
                String modelCode = dict.getDictName();
                String moduleLabel = dict.getDictLabel() != null ? dict.getDictLabel() : modelCode;
                
                return DictModuleOptionDTO.builder()
                    .value(modelCode)
                    .label(moduleLabel)
                    .build();
            })
            .collect(Collectors.toList());
        
        log.debug("字典模块选项列表获取完成，共 {} 个模块: {}", moduleOptions.size(), moduleOptions);
        return moduleOptions;
    }
    
    @Override
    public List<DictTreeDTO> getDictTreeOptions(String module) {
        log.debug("获取字典树形结构选项，模块: {}", module);
        
        // 构建查询条件
        LambdaQueryWrapper<SystemDict> query = new LambdaQueryWrapper<>();
        query.eq(SystemDict::getIsDeleted, 0)
             .eq(SystemDict::getStatus, 1)
             .orderByAsc(SystemDict::getModule, SystemDict::getSortOrder, SystemDict::getPath);
        
        // 如果指定了模块，则只查询该模块的字典
        if (module != null && !module.trim().isEmpty()) {
            query.eq(SystemDict::getModule, module);
        }
        
        List<SystemDict> allDicts = systemDictMapper.selectList(query);
        
        // 构建树形结构
        List<DictTreeDTO> treeOptions = buildTreeOptions(allDicts);
        
        log.debug("字典树形结构选项获取完成，模块: {}, 数量: {}", module, treeOptions.size());
        return treeOptions;
    }
    
    @Override
    public void refreshDictCache() {
        log.info("开始刷新系统字典缓存");
        
        // 清除所有字典相关的Redis缓存
        Set<String> keys = redisTemplate.keys(DICT_CACHE_PREFIX + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.info("系统字典缓存刷新完成，清除 {} 个缓存键", keys.size());
        } else {
            log.info("系统字典缓存刷新完成，无缓存需要清除");
        }
    }
    
    @Override
    public void clearDictCache(String dictCode) {
        if (dictCode != null) {
            // 清除指定字典的缓存
            Set<String> keys = redisTemplate.keys(DICT_CACHE_PREFIX + "*" + dictCode + "*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.debug("清除字典缓存，字典编码: {}, 清除 {} 个缓存键", dictCode, keys.size());
            }
        } else {
            // 清除所有字典缓存
            refreshDictCache();
        }
    }
    
    @Override
    public List<DictDataDTO> getChildrenOptionsByCode(String parentDictCode) {
        if (!StringUtils.hasText(parentDictCode)) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<SystemDict> parentQ = new LambdaQueryWrapper<>();
        parentQ.eq(SystemDict::getDictCode, parentDictCode)
               .eq(SystemDict::getIsDeleted, 0)
               .eq(SystemDict::getStatus, 1);
        SystemDict parent = systemDictMapper.selectOne(parentQ);
        if (parent == null) {
            return Collections.emptyList();
        }
        List<SystemDict> children = systemDictMapper.selectByParentId(parent.getId());
        return children.stream().map(this::convertToDictDataDTO).collect(Collectors.toList());
    }


    

    
    /**
     * 生成字典编码、层级和路径
     * 根据是否有父级，查询最大编码并+1
     * 
     * @param dict 字典对象
     */
    private void generateDictCodeAndPath(SystemDict dict) {
        if (dict.getParentId() == null || dict.getParentId() == 0) {
            // 顶级字典：查询最大顶级编码+1
            generateTopLevelCode(dict);
        } else {
            // 子级字典：查询父级下最大编码+1
            generateChildLevelCode(dict);
        }
        
        // 生成完整的字典编码：DICT_ + path
        dict.setDictCode("DICT_" + dict.getPath());
        
        log.debug("生成字典编码: {}, 层级: {}, 路径: {}, 父级ID: {}", 
            dict.getDictCode(), dict.getLevel(), dict.getPath(), dict.getParentId());
    }
    
    /**
     * 生成顶级字典编码
     * 
     * @param dict 字典对象
     */
    private void generateTopLevelCode(SystemDict dict) {
        // 查询当前最大的顶级编码
        String maxCode = systemDictMapper.getMaxTopLevelCode();
        
        int nextNumber = 1; // 默认从1开始
        if (maxCode != null && maxCode.startsWith("DICT_")) {
            try {
                // 提取path中第一级的数字部分，例如 "DICT_5" -> 5，"DICT_7.4.5.1" -> 7
                String codePart = maxCode.substring(5); // 去掉 "DICT_"
                String[] parts = codePart.split("\\.");
                if (parts.length > 0 && parts[0].matches("\\d+")) {
                    nextNumber = Integer.parseInt(parts[0]) + 1;
                }
            } catch (Exception e) {
                log.warn("解析最大顶级编码失败: {}, 使用默认值1", maxCode);
            }
        }
        
        dict.setLevel(1);
        dict.setPath(String.valueOf(nextNumber));
        dict.setParentId(0L);
    }
    
    /**
     * 生成子级字典编码
     * 
     * @param dict 字典对象
     */
    private void generateChildLevelCode(SystemDict dict) {
        // 验证父级字典是否存在
        SystemDict parent = systemDictMapper.selectById(dict.getParentId());
        if (parent == null) {
            throw new ServiceException("父级字典数据不存在");
        }
        
        // 查询父级下最大的子级编码
        String maxChildCode = systemDictMapper.getMaxChildCodeByParentId(dict.getParentId());
        
        int nextNumber = 1; // 默认从1开始
        if (maxChildCode != null && maxChildCode.startsWith("DICT_")) {
            try {
                // 提取最后一级的数字，例如 "DICT_3.3.4" -> 4
                String codePart = maxChildCode.substring(5); // 去掉 "DICT_"
                String[] parts = codePart.split("\\.");
                if (parts.length > 0) {
                    String lastPart = parts[parts.length - 1];
                    if (lastPart.matches("\\d+")) {
                        nextNumber = Integer.parseInt(lastPart) + 1;
                    }
                }
            } catch (Exception e) {
                log.warn("解析最大子级编码失败: {}, 使用默认值1", maxChildCode);
            }
        }
        
        dict.setLevel(parent.getLevel() + 1);
        dict.setPath(parent.getPath() + "." + nextNumber);
    }
    
    /**
     * 更新字典的层级和路径（用于修改父级关系时）
     * 注意：这个方法不会重新生成编码，只更新层级和路径
     * 
     * @param dict 字典对象
     */
    private void updateLevelAndPath(SystemDict dict) {
        if (dict.getParentId() == null || dict.getParentId() == 0) {
            // 移动到顶级
            dict.setLevel(1);
            // 保持原有的编码，只提取数字部分作为path
            if (dict.getDictCode() != null && dict.getDictCode().startsWith("DICT_")) {
                String codePart = dict.getDictCode().substring(5);
                // 如果是多级路径，只取第一级
                String[] parts = codePart.split("\\.");
                dict.setPath(parts[0]);
            }
        } else {
            // 移动到子级
            SystemDict parent = systemDictMapper.selectById(dict.getParentId());
            if (parent == null) {
                throw new ServiceException("父级字典不存在");
            }
            
            dict.setLevel(parent.getLevel() + 1);
            // 保持原有编码的最后一级，但更新父级路径
            if (dict.getDictCode() != null && dict.getDictCode().startsWith("DICT_")) {
                String codePart = dict.getDictCode().substring(5);
                String[] parts = codePart.split("\\.");
                String lastPart = parts[parts.length - 1];
                dict.setPath(parent.getPath() + "." + lastPart);
            }
        }
        
        // 更新字典编码
        dict.setDictCode("DICT_" + dict.getPath());
    }
    
    /**
     * 构建树形结构选项
     * 
     * @param allDicts 所有字典列表
     * @return 树形结构选项列表
     */
    private List<DictTreeDTO> buildTreeOptions(List<SystemDict> allDicts) {
        if (allDicts == null || allDicts.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 按父级ID分组
        Map<Long, List<SystemDict>> dictMap = allDicts.stream()
            .collect(Collectors.groupingBy(dict -> dict.getParentId() != null ? dict.getParentId() : 0L));
        
        // 构建顶级节点
        List<SystemDict> rootDicts = dictMap.get(0L);
        if (rootDicts == null || rootDicts.isEmpty()) {
            return new ArrayList<>();
        }
        
        return rootDicts.stream()
            .map(dict -> buildTreeOptionRecursive(dict, dictMap))
            .collect(Collectors.toList());
    }
    
    /**
     * 递归构建树形结构选项
     * 
     * @param dict 当前字典节点
     * @param dictMap 按父级ID分组的字典Map
     * @return 树形结构选项
     */
    private DictTreeDTO buildTreeOptionRecursive(SystemDict dict, Map<Long, List<SystemDict>> dictMap) {
        DictTreeDTO tree = DictTreeDTO.builder()
            .id(dict.getId())
            .dictCode(dict.getDictCode())
            .dictName(dict.getDictName())
            .dictLabel(dict.getDictLabel())
            .description(dict.getDescription())
            .parentId(dict.getParentId())
            .level(dict.getLevel())
            .path(dict.getPath())
            .module(dict.getModule())
            .status(dict.getStatus())
            .sortOrder(dict.getSortOrder())
            .isRoot(dict.getParentId() == null || dict.getParentId() == 0)
            .build();
        
        // 查找子节点
        List<SystemDict> children = dictMap.get(dict.getId());
        if (children != null && !children.isEmpty()) {
            List<DictTreeDTO> childTrees = children.stream()
                .map(child -> buildTreeOptionRecursive(child, dictMap))
                .collect(Collectors.toList());
            tree.setChildren(childTrees);
            tree.setIsLeaf(false);
        } else {
            tree.setIsLeaf(true);
        }
        
        return tree;
    }
    
    /**
     * 构建字典树形结构
     * 
     * @param dictCode 字典编码
     * @return 字典树形结构
     */
    private DictTreeDTO buildDictTree(String dictCode) {
        // 获取字典分类
        SystemDict category = systemDictMapper.selectDictCategory(dictCode);
        if (category == null) {
            return null;
        }
        
        // 构建树形结构
        return buildTreeRecursive(category);
    }
    
    /**
     * 递归构建树形结构
     * 
     * @param dict 字典对象
     * @return 树形结构
     */
    private DictTreeDTO buildTreeRecursive(SystemDict dict) {
        DictTreeDTO tree = DictTreeDTO.builder()
            .id(dict.getId())
            .dictCode(dict.getDictCode())
            .dictName(dict.getDictName())
            .dictLabel(dict.getDictLabel())
            .description(dict.getDescription())
            .parentId(dict.getParentId())
            .level(dict.getLevel())
            .path(dict.getPath())
            .module(dict.getModule())
            .status(dict.getStatus())
            .sortOrder(dict.getSortOrder())
            .isRoot(dict.getParentId() == null || dict.getParentId() == 0)
            .build();
        
        // 查询子字典
        List<SystemDict> children = systemDictMapper.selectByParentId(dict.getId());
        if (!children.isEmpty()) {
            List<DictTreeDTO> childTrees = children.stream()
                .filter(child -> child.getStatus() == 1) // 只包含启用的子字典
                .map(this::buildTreeRecursive)
                .collect(Collectors.toList());
            tree.setChildren(childTrees);
            tree.setIsLeaf(false);
        } else {
            tree.setIsLeaf(true);
        }
        
        return tree;
    }
    
    /**
     * 更新字典缓存
     * 
     * @param cacheKey 缓存键
     * @param data 缓存数据
     */
    private void updateDictCache(String cacheKey, Object data) {
        try {
            redisTemplate.opsForValue().set(cacheKey, data, CACHE_TTL, TimeUnit.SECONDS);
            log.debug("更新字典缓存成功，缓存键: {}, TTL: {}秒", cacheKey, CACHE_TTL);
        } catch (Exception e) {
            log.error("更新字典缓存失败，缓存键: {}, 错误: {}", cacheKey, e.getMessage(), e);
        }
    }
    
    /**
     * 将SystemDict实体转换为SystemDictDTO
     * 
     * @param dict 系统字典实体
     * @return 系统字典DTO
     */
    private SystemDictDTO convertToDTO(SystemDict dict) {
        return SystemDictDTO.builder()
            .id(dict.getId())
            .dictCode(dict.getDictCode())
            .dictName(dict.getDictName())
            .dictLabel(dict.getDictLabel())
            .description(dict.getDescription())
            .parentId(dict.getParentId())
            .level(dict.getLevel())
            .path(dict.getPath())
            .module(dict.getModule())
            .status(dict.getStatus())
            .sortOrder(dict.getSortOrder())
            .createTime(dict.getCreateTime())
            .updateTime(dict.getUpdateTime())
            .build();
    }
    
    /**
     * 将SystemDict实体转换为DictDataDTO
     * 
     * @param dict 系统字典实体
     * @return 字典数据DTO
     */
    private DictDataDTO convertToDictDataDTO(SystemDict dict) {
        // 优先使用 dict_name 作为业务码（如 inbox/email/system/task/credit/marketing）
        String businessValue = null;
        if (dict.getDictName() != null && !dict.getDictName().trim().isEmpty()) {
            businessValue = dict.getDictName().trim();
        }
        // 兼容旧数据：从描述/名称中文中提取
        if (businessValue == null || businessValue.isEmpty()) {
            if (dict.getDescription() != null) {
                String d = dict.getDescription().toLowerCase();
                if (d.contains("inbox")) businessValue = "inbox";
                else if (d.contains("email")) businessValue = "email";
                else if (d.contains("sms")) businessValue = "sms";
                else if (d.contains("system")) businessValue = "system";
                else if (d.contains("task")) businessValue = "task";
                else if (d.contains("credit")) businessValue = "credit";
                else if (d.contains("marketing")) businessValue = "marketing";
            }
            if ((businessValue == null || businessValue.isEmpty()) && dict.getDictName() != null) {
                String n = dict.getDictName().toLowerCase();
                if (n.contains("inbox") || n.contains("站内")) businessValue = "inbox";
                else if (n.contains("email") || n.contains("邮件")) businessValue = "email";
                else if (n.contains("短信") || n.contains("sms")) businessValue = "sms";
                else if (n.contains("系统")) businessValue = "system";
                else if (n.contains("任务")) businessValue = "task";
                else if (n.contains("积分")) businessValue = "credit";
                else if (n.contains("营销")) businessValue = "marketing";
            }
        }

        return DictDataDTO.builder()
            .code(dict.getDictCode())
            .label(dict.getDictLabel())
            .description(dict.getDescription())
            .status(dict.getStatus())
            .sortOrder(dict.getSortOrder())
            .value(businessValue)
            .build();
    }
    
    /**
     * 按照path字段进行语义版本号排序
     * 
     * @param dicts 字典列表
     * @param isAsc 是否升序
     * @return 排序后的字典列表
     */
    private List<SystemDictDTO> sortByPath(List<SystemDictDTO> dicts, boolean isAsc) {
        if (dicts == null || dicts.isEmpty()) {
            return dicts;
        }
        
        return dicts.stream()
            .sorted((dict1, dict2) -> {
                String path1 = dict1.getPath();
                String path2 = dict2.getPath();
                
                // 处理null值
                if (path1 == null && path2 == null) {
                    return 0;
                }
                if (path1 == null) {
                    return isAsc ? -1 : 1;
                }
                if (path2 == null) {
                    return isAsc ? 1 : -1;
                }
                
                // 比较path的语义版本号
                int result = compareVersionPaths(path1, path2);
                return isAsc ? result : -result;
            })
            .collect(Collectors.toList());
    }
    
    /**
     * 比较两个版本路径的语义版本号
     * 
     * @param path1 路径1
     * @param path2 路径2
     * @return 比较结果，负数表示path1小于path2，正数表示path1大于path2，0表示相等
     */
    private int compareVersionPaths(String path1, String path2) {
        if (path1.equals(path2)) {
            return 0;
        }
        
        // 分割路径为层级数组
        String[] parts1 = path1.split("\\.");
        String[] parts2 = path2.split("\\.");
        
        // 逐层比较
        int minLength = Math.min(parts1.length, parts2.length);
        for (int i = 0; i < minLength; i++) {
            int result = compareVersionPart(parts1[i], parts2[i]);
            if (result != 0) {
                return result;
            }
        }
        
        // 如果前面的层级都相等，层级数少的排在前面
        return Integer.compare(parts1.length, parts2.length);
    }
    
    /**
     * 比较两个版本号部分
     * 
     * @param part1 版本号部分1
     * @param part2 版本号部分2
     * @return 比较结果
     */
    private int compareVersionPart(String part1, String part2) {
        if (part1.equals(part2)) {
            return 0;
        }
        
        // 尝试按数字比较
        try {
            Integer num1 = Integer.parseInt(part1);
            Integer num2 = Integer.parseInt(part2);
            return num1.compareTo(num2);
        } catch (NumberFormatException e) {
            // 如果不是纯数字，按字符串比较
            return part1.compareTo(part2);
        }
    }
    
    /**
     * 导出选中字典数据
     * 
     * @param ids 字典ID列表
     * @return Excel文件的字节数组
     * @throws ServiceException 当导出失败时抛出
     */
    @Override
    public byte[] exportSelectedDict(List<Long> ids) {
        try {
            log.info("开始导出选中字典，ID列表: {}", ids);
            
            if (ids == null || ids.isEmpty()) {
                throw new ServiceException(ErrorCode.PARAM_INVALID, "导出的字典ID列表不能为空");
            }
            
            // 查询要导出的字典数据
            List<SystemDict> dictList = systemDictMapper.selectBatchIds(ids);
            if (dictList.isEmpty()) {
                throw new ServiceException(ErrorCode.DATA_NOT_FOUND, "没有找到要导出的字典数据");
            }
            
            // 转换为DTO
            List<SystemDictDTO> dictDTOList = dictList.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            
            return generateExcel(dictDTOList);
            
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("导出选中字典失败，ID列表: {}", ids, e);
            throw new ServiceException(ErrorCode.OPERATION_NOT_ALLOWED, "导出失败: " + e.getMessage());
        }
    }
    
    /**
     * 导出全部字典数据
     * 
     * @param queryDto 查询条件，用于筛选要导出的数据
     * @return Excel文件的字节数组
     * @throws ServiceException 当导出失败时抛出
     */
    @Override
    public byte[] exportAllDict(SystemDictQueryDTO queryDto) {
        try {
            log.info("开始导出全部字典，查询条件: {}", queryDto);
            
            // 构建查询条件
            LambdaQueryWrapper<SystemDict> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SystemDict::getIsDeleted, 0);
            
            // 添加筛选条件
            if (StringUtils.hasText(queryDto.getKeyword())) {
                queryWrapper.and(wrapper -> wrapper
                        .like(SystemDict::getDictName, queryDto.getKeyword())
                        .or().like(SystemDict::getDictLabel, queryDto.getKeyword())
                );
            }
            
            if (StringUtils.hasText(queryDto.getDictCode())) {
                queryWrapper.like(SystemDict::getDictCode, queryDto.getDictCode());
            }
            
            if (StringUtils.hasText(queryDto.getModule())) {
                queryWrapper.eq(SystemDict::getModule, queryDto.getModule());
            }
            
            if (queryDto.getStatus() != null) {
                queryWrapper.eq(SystemDict::getStatus, queryDto.getStatus());
            }
            
            if (queryDto.getLevel() != null) {
                if (queryDto.getLevel() == 3) {
                    queryWrapper.ge(SystemDict::getLevel, 3);
                } else {
                    queryWrapper.eq(SystemDict::getLevel, queryDto.getLevel());
                }
            }
            
            // 排序
            queryWrapper.orderByAsc(SystemDict::getModule)
                    .orderByAsc(SystemDict::getSortOrder)
                    .orderByAsc(SystemDict::getDictCode);
            
            // 查询数据
            List<SystemDict> dictList = systemDictMapper.selectList(queryWrapper);
            
            // 转换为DTO
            List<SystemDictDTO> dictDTOList = dictList.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            
            return generateExcel(dictDTOList);
            
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("导出全部字典失败", e);
            throw new ServiceException(ErrorCode.OPERATION_NOT_ALLOWED, "导出失败: " + e.getMessage());
        }
    }
    
    /**
     * 导入字典数据
     * 
     * @param fileBytes Excel文件字节数组
     * @param fileName 文件名
     * @param overwrite 是否覆盖已存在的数据
     * @return 导入结果统计
     * @throws ServiceException 当导入失败时抛出
     */
    @Override
    @Transactional
    public ImportResultDTO importDict(byte[] fileBytes, String fileName, boolean overwrite) {
        try {
            log.info("开始导入字典数据，文件名: {}, 覆盖模式: {}", fileName, overwrite);
            
            // 解析Excel文件
            List<SystemDictDTO> importList = parseExcel(fileBytes, fileName);
            
            ImportResultDTO.ImportResultDTOBuilder resultBuilder = ImportResultDTO.builder();
            List<ImportResultDTO.ImportErrorDTO> errors = new ArrayList<>();
            
            int totalCount = importList.size();
            int successCount = 0;
            int failedCount = 0;
            int skippedCount = 0;
            
            for (int i = 0; i < importList.size(); i++) {
                SystemDictDTO dictDTO = importList.get(i);
                int rowNumber = i + 2; // Excel行号从2开始（第1行是表头）
                
                try {
                    // 验证必填字段
                    if (!StringUtils.hasText(dictDTO.getDictCode())) {
                        errors.add(ImportResultDTO.ImportErrorDTO.builder()
                                .rowNumber(rowNumber)
                                .dictCode(dictDTO.getDictCode())
                                .errorMessage("字典编码不能为空")
                                .build());
                        failedCount++;
                        continue;
                    }
                    
                    if (!StringUtils.hasText(dictDTO.getDictName())) {
                        errors.add(ImportResultDTO.ImportErrorDTO.builder()
                                .rowNumber(rowNumber)
                                .dictCode(dictDTO.getDictCode())
                                .errorMessage("字典名称不能为空")
                                .build());
                        failedCount++;
                        continue;
                    }
                    
                    // 检查字典是否已存在
                    SystemDict existingDict = systemDictMapper.selectOne(
                            new LambdaQueryWrapper<SystemDict>()
                                    .eq(SystemDict::getDictCode, dictDTO.getDictCode())
                                    .eq(SystemDict::getIsDeleted, 0)
                    );
                    
                    if (existingDict != null) {
                        if (!overwrite) {
                            errors.add(ImportResultDTO.ImportErrorDTO.builder()
                                    .rowNumber(rowNumber)
                                    .dictCode(dictDTO.getDictCode())
                                    .errorMessage("字典编码已存在，跳过导入")
                                    .build());
                            skippedCount++;
                            continue;
                        } else {
                            // 覆盖模式：更新现有记录
                            updateExistingDict(existingDict, dictDTO);
                            systemDictMapper.updateById(existingDict);
                            successCount++;
                        }
                    } else {
                        // 新增字典
                        SystemDict newDict = convertToEntity(dictDTO);
                        newDict.setCreateTime(LocalDateTime.now());
                        newDict.setUpdateTime(LocalDateTime.now());
                        newDict.setIsDeleted(0);
                        
                        // 计算层级和路径 - 暂时设为顶级字典
                        newDict.setParentId(0L);
                        newDict.setLevel(1);
                        newDict.setPath(newDict.getDictCode());
                        
                        systemDictMapper.insert(newDict);
                        successCount++;
                    }
                    
                } catch (Exception e) {
                    log.error("导入第{}行数据失败，字典编码: {}", rowNumber, dictDTO.getDictCode(), e);
                    errors.add(ImportResultDTO.ImportErrorDTO.builder()
                            .rowNumber(rowNumber)
                            .dictCode(dictDTO.getDictCode())
                            .errorMessage("导入失败: " + e.getMessage())
                            .build());
                    failedCount++;
                }
            }
            
            // 刷新缓存
            if (successCount > 0) {
                refreshDictCache();
            }
            
            String message = String.format("导入完成！总计: %d，成功: %d，失败: %d，跳过: %d", 
                    totalCount, successCount, failedCount, skippedCount);
            
            return resultBuilder
                    .totalCount(totalCount)
                    .successCount(successCount)
                    .failedCount(failedCount)
                    .skippedCount(skippedCount)
                    .errors(errors)
                    .message(message)
                    .build();
                    
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("导入字典数据失败", e);
            throw new ServiceException(ErrorCode.OPERATION_NOT_ALLOWED, "导入失败: " + e.getMessage());
        }
    }
    
    /**
     * 生成CSV文件
     */
    private byte[] generateExcel(List<SystemDictDTO> dictList) throws Exception {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)) {
            
            // 写入BOM以支持Excel打开中文
            outputStream.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
            
            // 创建表头
            String[] headers = {"字典编码", "字典名称", "字典标签", "所属模块", "父级编码", "层级", "排序", "状态", "描述"};
            writer.write(String.join(",", headers));
            writer.write("\n");
            
            // 填充数据
            for (SystemDictDTO dict : dictList) {
                String[] row = {
                    escapeCSV(dict.getDictCode()),
                    escapeCSV(dict.getDictName()),
                    escapeCSV(dict.getDictLabel()),
                    escapeCSV(dict.getModule()),
                    "", // parentCode暂时为空
                    String.valueOf(dict.getLevel() != null ? dict.getLevel() : 0),
                    String.valueOf(dict.getSortOrder() != null ? dict.getSortOrder() : 0),
                    dict.getStatus() != null && dict.getStatus() == 1 ? "启用" : "禁用",
                    escapeCSV(dict.getDescription())
                };
                writer.write(String.join(",", row));
                writer.write("\n");
            }
            
            writer.flush();
            return outputStream.toByteArray();
        }
    }
    
    /**
     * CSV字段转义
     */
    private String escapeCSV(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
    
    /**
     * 解析CSV文件
     */
    private List<SystemDictDTO> parseExcel(byte[] fileBytes, String fileName) throws Exception {
        List<SystemDictDTO> result = new ArrayList<>();
        
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(fileBytes);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            
            String line;
            boolean isFirstLine = true;
            
            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue; // 跳过表头
                }
                
                String[] fields = parseCSVLine(line);
                if (fields.length >= 9) {
                    SystemDictDTO dto = SystemDictDTO.builder()
                            .dictCode(fields[0].trim())
                            .dictName(fields[1].trim())
                            .dictLabel(fields[2].trim())
                            .module(fields[3].trim())
                            // .parentCode(fields[4].trim()) // 暂时跳过
                            .level(parseInteger(fields[5]))
                            .sortOrder(parseInteger(fields[6]))
                            .status("启用".equals(fields[7].trim()) ? 1 : 0)
                            .description(fields[8].trim())
                            .build();
                    
                    result.add(dto);
                }
            }
        }
        
        return result;
    }
    
    /**
     * 解析CSV行
     */
    private String[] parseCSVLine(String line) {
        List<String> result = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder current = new StringBuilder();
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++; // 跳过下一个引号
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                result.add(current.toString());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        
        result.add(current.toString());
        return result.toArray(new String[0]);
    }
    
    /**
     * 解析整数
     */
    private Integer parseInteger(String value) {
        if (value == null || value.trim().isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @Override
    public List<DictDataDTO> getConfigCategoryOptions() {
        log.debug("开始获取系统配置分类选项（DICT_1 子项）");
        // 查询父级字典编码为 DICT_1 的顶级字典
        LambdaQueryWrapper<SystemDict> parentQuery = new LambdaQueryWrapper<>();
        parentQuery.eq(SystemDict::getDictCode, "DICT_1")
                   .eq(SystemDict::getIsDeleted, 0)
                   .eq(SystemDict::getStatus, 1);

        SystemDict parentDict = systemDictMapper.selectOne(parentQuery);
        if (parentDict == null) {
            log.warn("未找到父级字典 DICT_1，返回空分类列表");
            return new ArrayList<>();
        }

        // 查询其子项作为分类
        LambdaQueryWrapper<SystemDict> query = new LambdaQueryWrapper<>();
        query.eq(SystemDict::getParentId, parentDict.getId())
             .eq(SystemDict::getIsDeleted, 0)
             .eq(SystemDict::getStatus, 1)
             .orderByAsc(SystemDict::getSortOrder)
             .orderByAsc(SystemDict::getPath);

        List<SystemDict> dicts = systemDictMapper.selectList(query);
        List<DictDataDTO> options = dicts.stream()
            .map(this::convertToDictDataDTO)
            .collect(Collectors.toList());

        log.debug("系统配置分类选项获取完成，数量: {}", options.size());
        return options;
    }

    /**
     * 获取系统配置数据类型选项（来自字典 DICT_3 的子项）
     */
    public List<DictDataDTO> getConfigTypeOptions() {
        log.debug("开始获取系统配置数据类型选项（DICT_3 子项）");

        LambdaQueryWrapper<SystemDict> parentQuery = new LambdaQueryWrapper<>();
        parentQuery.eq(SystemDict::getDictCode, "DICT_3")
                   .eq(SystemDict::getIsDeleted, 0)
                   .eq(SystemDict::getStatus, 1);

        SystemDict parentDict = systemDictMapper.selectOne(parentQuery);
        if (parentDict == null) {
            log.warn("未找到父级字典 DICT_3，返回空类型列表");
            return new ArrayList<>();
        }

        LambdaQueryWrapper<SystemDict> query = new LambdaQueryWrapper<>();
        query.eq(SystemDict::getParentId, parentDict.getId())
             .eq(SystemDict::getIsDeleted, 0)
             .eq(SystemDict::getStatus, 1)
             .orderByAsc(SystemDict::getSortOrder)
             .orderByAsc(SystemDict::getPath);

        List<SystemDict> dicts = systemDictMapper.selectList(query);
        List<DictDataDTO> options = dicts.stream()
            .map(this::convertToDictDataDTO)
            .collect(Collectors.toList());

        log.debug("系统配置数据类型选项获取完成，数量: {}", options.size());
        return options;
    }

    
    /**
     * 更新现有字典
     */
    private void updateExistingDict(SystemDict existingDict, SystemDictDTO dictDTO) {
        existingDict.setDictName(dictDTO.getDictName());
        existingDict.setDictLabel(dictDTO.getDictLabel());
        existingDict.setModule(dictDTO.getModule());
        existingDict.setSortOrder(dictDTO.getSortOrder());
        existingDict.setStatus(dictDTO.getStatus());
        existingDict.setDescription(dictDTO.getDescription());
        existingDict.setUpdateTime(LocalDateTime.now());
    }
    
    /**
     * 将DTO转换为实体
     */
    private SystemDict convertToEntity(SystemDictDTO dto) {
        SystemDict entity = new SystemDict();
        entity.setDictCode(dto.getDictCode());
        entity.setDictName(dto.getDictName());
        entity.setDictLabel(dto.getDictLabel());
        entity.setModule(dto.getModule());
        entity.setSortOrder(dto.getSortOrder());
        entity.setStatus(dto.getStatus());
        entity.setDescription(dto.getDescription());
        return entity;
    }
} 