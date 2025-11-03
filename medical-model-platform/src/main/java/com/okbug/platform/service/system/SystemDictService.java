/**
 * 系统字典服务接口：系统字典业务逻辑层
 * 
 * 核心功能：
 * 1. 提供系统字典的完整CRUD操作
 * 2. 管理字典缓存机制，提高字典访问性能
 * 3. 支持多级字典结构管理，使用path作为dictCode确保唯一性
 * 4. 提供字典编码获取的便捷方法
 * 5. 集成Redis缓存，支持字典热更新
 * 6. 支持字典状态控制和排序管理
 * 7. 提供批量操作和字典映射功能
 * 
 * @author hanjor
 * @version 2.1
 * @date 2025-01-15 10:00:00
 */
package com.okbug.platform.service.system;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.okbug.platform.dto.system.SystemDictDTO;
import com.okbug.platform.dto.system.SystemDictQueryDTO;
import com.okbug.platform.dto.system.SystemDictCreateDTO;
import com.okbug.platform.dto.system.SystemDictUpdateDTO;
import com.okbug.platform.dto.system.DictTreeDTO;
import com.okbug.platform.dto.system.DictModuleOptionDTO;
import com.okbug.platform.dto.system.DictDataDTO;
import com.okbug.platform.dto.system.ImportResultDTO;

import java.util.List;

/**
 * 系统字典服务接口
 * 定义系统字典管理的所有业务方法
 * 包括基础CRUD、多级结构管理、字典值获取、缓存管理、状态控制、排序管理等核心功能
 */
public interface SystemDictService {
    
    /**
     * 分页查询字典列表
     * 
     * 查询功能：
     * 1. 支持按字典编码、名称、模块等条件筛选
     * 2. 支持按父级ID和层级筛选
     * 3. 支持按字典状态筛选
     * 4. 提供分页查询能力
     * 5. 结果按模块、排序字段、字典编码排序
     * 
     * @param queryDto 查询条件DTO，包含筛选条件和分页参数
     * @return 分页结果，包含字典列表和分页信息
     */
    IPage<SystemDictDTO> getDicts(SystemDictQueryDTO queryDto);
    
    /**
     * 根据ID获取字典
     * 
     * @param id 字典ID
     * @return 系统字典DTO，如果不存在则返回null
     */
    SystemDictDTO getDictById(Long id);
    
    /**
     * 创建新字典
     * 
     * 创建逻辑：
     * 1. 验证字典编码的唯一性
     * 2. 自动计算字典层级和路径
     * 3. 保存字典到数据库
     * 4. 更新Redis缓存
     * 5. 记录操作日志
     * 
     * @param createDto 创建字典的请求参数
     * @return 创建成功的字典DTO
     * @throws ServiceException 当字典编码已存在或参数无效时抛出
     */
    SystemDictDTO createDict(SystemDictCreateDTO createDto);
    
    /**
     * 更新字典
     * 
     * 更新逻辑：
     * 1. 验证字典是否存在
     * 2. 更新字典信息
     * 3. 如果修改了父级关系，重新计算层级和路径
     * 4. 更新数据库记录
     * 5. 刷新Redis缓存
     * 6. 记录操作日志
     * 
     * @param id 字典ID
     * @param updateDto 更新字典的请求参数
     * @return 更新后的字典DTO
     * @throws ServiceException 当字典不存在或参数无效时抛出
     */
    SystemDictDTO updateDict(Long id, SystemDictUpdateDTO updateDto);
    
    /**
     * 删除字典
     * 
     * 删除逻辑：
     * 1. 检查是否有子字典，如果有则不允许删除
     * 2. 执行逻辑删除（设置isDeleted = 1）
     * 3. 清除Redis缓存
     * 4. 记录操作日志
     * 
     * @param id 字典ID
     * @return 删除操作是否成功
     * @throws ServiceException 当字典不存在或有子字典时抛出
     */
    Boolean deleteDict(Long id);

    /**
     * 获取所有模块列表（旧版本，返回字符串数组）
     * 
     * @return 模块列表，如：["SYSTEM", "USER", "FILE", "TASK"]
     * @deprecated 建议使用 getDictModules() 方法获取对象格式的模块列表
     */
    @Deprecated
    List<String> getModules();
    
    /**
     * 获取字典模块选项列表（用于前端下拉选择器）
     * 
     * 功能：
     * 1. 查询字典中定义的所有模块
     * 2. 返回包含value和label的对象数组格式
     * 3. value为模块代码（英文），label为模块标签（中文）
     * 
     * @return 模块选项列表，格式：[{value: "SYSTEM", label: "系统模块"}, {value: "USER", label: "用户模块"}]
     */
    List<DictModuleOptionDTO> getDictModules();
    
    /**
     * 获取字典树形结构选项（用于父级字典选择）
     * 
     * @param module 模块名称，如果为null则获取所有模块的字典
     * @return 字典树形结构列表
     */
    List<DictTreeDTO> getDictTreeOptions(String module);

    /**
     * 获取系统配置分类选项（来自字典 DICT_3 的子项）
     * 
     * @return 分类选项列表，元素包含 code 和 label
     */
    List<DictDataDTO> getConfigCategoryOptions();

    /**
     * 获取系统配置数据类型选项（来自字典 DICT_3 的子项）
     *
     * @return 数据类型选项列表
     */
    List<DictDataDTO> getConfigTypeOptions();
    
    /**
     * 刷新字典缓存
     * 
     * 刷新逻辑：
     * 1. 清除所有字典相关的Redis缓存
     * 2. 下次访问时重新从数据库加载并缓存
     * 3. 用于字典变更后的缓存同步
     */
    void refreshDictCache();
    
    /**
     * 清除字典缓存
     * 
     * 清除逻辑：
     * 1. 清除指定字典编码的Redis缓存
     * 2. 下次访问时重新从数据库加载并缓存
     * 3. 用于单个字典变更后的缓存同步
     * 
     * @param dictCode 字典编码，如果为null则清除所有字典缓存
     */
    void clearDictCache(String dictCode);

    /**
     * 根据父级字典编码获取其子项选项（仅启用项）
     * 例如：传入 DICT_4.1 返回渠道选项；DICT_4.2 返回类型选项
     */
    List<DictDataDTO> getChildrenOptionsByCode(String parentDictCode);
    
    /**
     * 导出选中字典数据
     * 
     * @param ids 字典ID列表
     * @return Excel文件的字节数组
     * @throws ServiceException 当导出失败时抛出
     */
    byte[] exportSelectedDict(List<Long> ids);
    
    /**
     * 导出全部字典数据
     * 
     * @param queryDto 查询条件，用于筛选要导出的数据
     * @return Excel文件的字节数组
     * @throws ServiceException 当导出失败时抛出
     */
    byte[] exportAllDict(SystemDictQueryDTO queryDto);
    
    /**
     * 导入字典数据
     * 
     * @param fileBytes Excel文件字节数组
     * @param fileName 文件名
     * @param overwrite 是否覆盖已存在的数据
     * @return 导入结果统计
     * @throws ServiceException 当导入失败时抛出
     */
    ImportResultDTO importDict(byte[] fileBytes, String fileName, boolean overwrite);
}