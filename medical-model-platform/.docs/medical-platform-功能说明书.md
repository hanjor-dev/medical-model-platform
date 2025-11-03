# 医学影像模型管理平台功能说明书

**项目名称**: medical-model-platform  
**版本**: 2.0.0  
**创建时间**: 2024-12-19  
**文档状态**: 持续更新中  

## 项目概述

medical-model-platform是一个基于Spring Boot的医学影像模型管理业务平台，专注于用户业务逻辑处理，与AI计算引擎解耦。主要功能包括：
- 用户管理和权限控制
- 文件上传和存储管理
- 任务编排和状态管理
- 积分管理和计费
- 3D模型数据管理和展示
- 实时任务状态推送
- AI计算引擎管理和调度

## 系统架构

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   前端应用      │    │ Medical Model   │    │   MySQL数据库   │
│   (Vue/React)   │◄──►│   Platform      │◄──►│   主业务数据    │
└─────────────────┘    │  (业务平台)     │    └─────────────────┘
                       └─────────────────┘
                              │
                              ▼
                       ┌─────────────────┐
                       │   RabbitMQ     │
                       │   消息队列      │◄──┐
                       └─────────────────┘   │
                              │              │
                              ▼              │
                   ┌─────────────────┐       │
                   │ AI Compute      │       │
                   │ Engine 1        │───────┘
                   └─────────────────┘
                              │
                   ┌─────────────────┐
                   │ AI Compute      │
                   │ Engine 2        │───────┐
                   └─────────────────┘       │
                              │              │
                   ┌─────────────────┐       │
                   │ AI Compute      │       │
                   │ Engine N        │───────┘
                   └─────────────────┘
```

## 核心功能模块

### 1. 用户管理模块

#### 1.1 用户注册
**功能描述**: 新用户注册账号，系统自动分配默认角色

**业务流程**:
```
用户提交注册信息 → 验证用户名唯一性 → 密码加密 → 创建用户记录 → 分配默认角色 → 返回注册结果
```

**核心代码逻辑**:
```java
@PostMapping("/register")
public ApiResponse<Boolean> register(@RequestBody @Valid UserRegisterDTO dto) {
    // 1. 校验用户名是否已存在
    if (userService.existsByUsername(dto.getUsername())) {
        throw new ServiceException("用户名已存在");
    }
    
    // 2. 创建用户
    User user = User.builder()
        .userId(idGenerator.generateUserId())
        .username(dto.getUsername())
        .passwordHash(passwordEncoder.encode(dto.getPassword()))
        .email(dto.getEmail())
        .status(UserStatus.ACTIVE)
        .createdAt(LocalDateTime.now())
        .build();
    
    // 3. 保存用户信息
    userService.save(user);
    
    // 4. 分配默认角色
    userRoleService.assignDefaultRole(user.getUserId());
    
    return ApiResponse.success(true);
}
```

**异常处理**:
- 用户名重复：抛出ServiceException
- 邮箱格式错误：参数校验异常
- 数据库操作失败：事务回滚

**权限要求**: 无（公开接口）

---

#### 1.2 用户登录
**功能描述**: 用户通过用户名和密码登录系统，支持JWT Token认证

**业务流程**:
```
用户提交登录信息 → 验证用户存在性 → 校验密码 → 生成JWT Token → 返回登录结果
```

**核心代码逻辑**:
```java
@PostMapping("/login")
public ApiResponse<LoginResponseDTO> login(@RequestBody @Valid UserLoginDTO dto) {
    // 1. 查询用户
    User user = userService.findByUsername(dto.getUsername());
    if (user == null) {
        throw new ServiceException("用户不存在");
    }
    
    // 2. 校验用户状态
    if (user.getStatus() != UserStatus.ACTIVE) {
        throw new ServiceException("用户被禁用");
    }
    
    // 3. 校验密码
    if (!passwordEncoder.matches(dto.getPassword(), user.getPasswordHash())) {
        throw new ServiceException("密码错误");
    }
    
    // 4. 生成JWT Token
    String token = jwtTokenProvider.generateToken(user);
    
    // 5. 构建响应
    LoginResponseDTO response = LoginResponseDTO.builder()
        .token(token)
        .userId(user.getUserId())
        .username(user.getUsername())
        .roles(userRoleService.getUserRoles(user.getUserId()))
        .build();
    
    return ApiResponse.success(response);
}
```

---

#### 1.3 用户权限管理
**功能描述**: 基于角色的权限控制系统（RBAC）

**角色体系**:
- **USER**: 普通用户，可以创建任务、查看自己的数据
- **ADMIN**: 普通管理员，可以管理子账号、分配积分
- **SUPER_ADMIN**: 超级管理员，可以管理所有用户、系统配置

**权限控制实现**:
```java
@PreAuthorize("hasRole('ADMIN')")
@GetMapping("/users")
public ApiResponse<List<UserDTO>> getUsers() {
    // 管理员可以查看用户列表
}

@PreAuthorize("hasRole('USER') and @userService.isOwner(#taskId, authentication.name)")
@DeleteMapping("/tasks/{taskId}")
public ApiResponse<Boolean> deleteTask(@PathVariable Long taskId) {
    // 用户只能删除自己的任务
}
```

---

### 2. 文件上传模块

#### 2.1 文件上传到阿里云OSS
**功能描述**: 支持ZIP格式医学影像文件上传，自动创建计算任务

**业务流程**:
```
用户上传文件 → 文件验证 → 存储到OSS → 创建Task记录 → 创建ModelInfo记录 → 积分预检查 → 推送到任务队列 → 返回上传结果
```

**核心代码逻辑**:
```java
@PostMapping("/files/upload")
public ApiResponse<TaskCreationResponseDTO> uploadFile(@RequestBody FileUploadDTO uploadDto) {
    // 1. 参数验证
    validateUploadParams(uploadDto);
    
    // 2. 积分预检查
    validateUserPoints(uploadDto.getUserId(), uploadDto.getTaskType());
    
    // 3. 创建Task对象
    Task task = taskService.createTask(uploadDto);
    
    // 4. 创建ModelInfo对象
    ModelInfo modelInfo = modelInfoService.createModelInfo(task, uploadDto);
    
    // 5. 保存到数据库
    taskService.save(task);
    modelInfoService.save(modelInfo);
    
    // 6. 推送到计算引擎队列
    computeTaskDispatcher.dispatchTask(task);
    
    // 7. 构建响应
    TaskCreationResponseDTO response = TaskCreationResponseDTO.builder()
        .taskId(task.getId())
        .orderNo(task.getOrderNo())
        .status(task.getStatus())
        .message("文件上传成功，任务已创建")
        .build();
    
    return ApiResponse.success(response);
}
```

**文件格式要求**:
- 支持格式：ZIP (包含DICOM文件)
- 文件大小限制：通过系统配置控制
- 命名规范：避免特殊字符

**异常处理**:
- 文件格式不支持：抛出ServiceException
- 积分不足：抛出InsufficientPointsException
- OSS上传失败：抛出FileUploadException

---

#### 2.2 STL文件上传
**功能描述**: 支持STL格式3D模型文件上传，用于任务结果展示

**业务流程**:
```
用户上传STL文件 → 文件验证 → 存储到OSS → 关联任务信息 → 创建Result记录 → 返回上传结果
```

**核心代码逻辑**:
```java
@PostMapping("/files/stl")
public ApiResponse<String> uploadStlFiles(@RequestBody StlUploadDTO dto) {
    // 1. 参数验证
    if (dto.getTaskId() == null || CollUtil.isEmpty(dto.getStlFiles())) {
        throw new ServiceException("参数错误，缺少必要数据");
    }
    
    // 2. 查询任务信息并验证权限
    Task task = taskService.getByIdAndUserId(dto.getTaskId(), getCurrentUserId());
    if (task == null) {
        throw new ServiceException("任务不存在或无权访问");
    }
    
    // 3. 处理STL文件
    List<Result> results = new ArrayList<>();
    for (StlFileInfo stlFile : dto.getStlFiles()) {
        Result result = Result.builder()
            .taskId(dto.getTaskId())
            .fileName(stlFile.getFileName())
            .filePath(stlFile.getFilePath())
            .fileSize(stlFile.getFileSize())
            .fileType("STL")
            .uploadTime(LocalDateTime.now())
            .build();
        results.add(result);
    }
    
    // 4. 批量保存结果
    resultService.saveBatch(results);
    
    // 5. 更新任务状态
    if (task.getStatus() == TaskStatus.IN_PROGRESS) {
        taskService.updateStatus(task.getId(), TaskStatus.COMPLETED);
        
        // 6. 推送完成消息
        webSocketService.sendTaskCompleteMessage(task.getOrderNo(), task.getTaskName(), TaskStatus.COMPLETED);
    }
    
    return ApiResponse.success("STL文件上传成功");
}
```

---

### 3. 任务管理模块

#### 3.1 任务创建和编排
**功能描述**: 创建AI计算任务并进行任务编排调度

**详细流程**:
```
1. 接收文件上传请求
   ↓
2. 生成唯一订单号
   ↓
3. 创建Task记录
   ↓
4. 创建ModelInfo记录
   ↓
5. 选择可用的计算引擎
   ↓
6. 推送任务到指定引擎队列
   ↓
7. 返回任务创建结果
```

**任务调度器实现**:
```java
@Service
public class TaskSchedulerService {
    
    public void scheduleTask(Task task) {
        // 1. 查找可用的计算引擎
        List<ComputeEngineInfo> availableEngines = engineRegistryService.getAvailableEngines();
        
        // 2. 负载均衡选择引擎
        ComputeEngineInfo selectedEngine = loadBalancer.selectEngine(availableEngines, task);
        
        if (selectedEngine != null) {
            // 3. 发送到指定引擎队列
            sendTaskToSpecificEngine(task, selectedEngine);
            log.info("Task {} assigned to engine {}", task.getId(), selectedEngine.getEngineId());
        } else {
            // 4. 放入通用队列等待
            sendTaskToGeneralQueue(task);
            log.warn("No available engine found, task {} queued for general processing", task.getId());
        }
    }
    
    private void sendTaskToSpecificEngine(Task task, ComputeEngineInfo engine) {
        ComputeTaskMessage message = ComputeTaskMessage.builder()
            .taskId(task.getId())
            .taskType(task.getTaskType())
            .inputFilePath(task.getInputFilePath())
            .outputPath(task.getOutputPath())
            .parameters(task.getParameters())
            .priority(task.getPriority())
            .assignedEngineId(engine.getEngineId())
            .build();
            
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.COMPUTE_TASK_EXCHANGE,
            "engine." + engine.getEngineId(),
            message
        );
    }
}
```

---

#### 3.2 任务状态管理
**功能描述**: 管理任务从创建到完成的完整生命周期

**任务状态流转**:
```
CREATED (已创建)
    ↓
QUEUED (队列中)
    ↓
ASSIGNED (已分配)
    ↓
IN_PROGRESS (执行中)
    ↓
COMPLETED (已完成) / FAILED (失败) / CANCELLED (已取消)
```

**状态更新接口**:
```java
@RestController
@RequestMapping("/api/v1/tasks")
public class TaskStatusController {
    
    @PutMapping("/{taskId}/status")
    public ApiResponse<Boolean> updateTaskStatus(
            @PathVariable Long taskId,
            @RequestBody TaskStatusUpdateDTO statusDto) {
        
        // 1. 验证任务存在
        Task task = taskService.getById(taskId);
        if (task == null) {
            throw new ServiceException("任务不存在");
        }
        
        // 2. 验证状态转换的合法性
        if (!isValidStatusTransition(task.getStatus(), statusDto.getNewStatus())) {
            throw new ServiceException("非法的状态转换");
        }
        
        // 3. 更新任务状态
        taskService.updateTaskStatus(taskId, statusDto.getNewStatus(), statusDto.getMessage());
        
        // 4. 推送状态变更消息
        webSocketService.sendTaskStatusUpdate(task.getOrderNo(), statusDto.getNewStatus());
        
        // 5. 处理状态变更后的业务逻辑
        handleStatusChangeBusinessLogic(task, statusDto.getNewStatus());
        
        return ApiResponse.success(true);
    }
    
    private void handleStatusChangeBusinessLogic(Task task, TaskStatus newStatus) {
        switch (newStatus) {
            case COMPLETED:
                // 处理任务完成逻辑
                handleTaskCompletion(task);
                break;
            case FAILED:
                // 处理任务失败逻辑
                handleTaskFailure(task);
                break;
            case CANCELLED:
                // 处理任务取消逻辑
                handleTaskCancellation(task);
                break;
        }
    }
}
```

---

#### 3.3 任务监控和故障转移
**功能描述**: 监控任务执行状态，处理超时和故障转移

**监控逻辑**:
```java
@Component
public class TaskMonitoringService {
    
    @Scheduled(fixedDelay = 30000) // 30秒检查一次
    public void monitorRunningTasks() {
        List<Task> runningTasks = taskService.getRunningTasks();
        
        for (Task task : runningTasks) {
            try {
                // 1. 检查任务超时
                if (isTaskTimeout(task)) {
                    handleTaskTimeout(task);
                    continue;
                }
                
                // 2. 检查执行引擎健康状态
                ComputeEngineInfo engine = getAssignedEngine(task);
                if (engine == null || !engine.isHealthy()) {
                    handleEngineFailure(task, engine);
                    continue;
                }
                
                // 3. 检查任务心跳
                if (!hasRecentHeartbeat(task)) {
                    handleMissingHeartbeat(task);
                }
                
            } catch (Exception e) {
                log.error("Error monitoring task {}: {}", task.getId(), e.getMessage(), e);
            }
        }
    }
    
    private void handleTaskTimeout(Task task) {
        log.warn("Task {} timeout, marking as failed", task.getId());
        taskService.updateTaskStatus(task.getId(), TaskStatus.FAILED, "任务执行超时");
        
        // 通知用户
        webSocketService.sendTaskFailureMessage(task.getOrderNo(), "任务执行超时");
    }
    
    private void handleEngineFailure(Task task, ComputeEngineInfo engine) {
        log.warn("Engine {} failed, rescheduling task {}", 
                engine != null ? engine.getEngineId() : "unknown", task.getId());
        
        // 重置任务状态并重新调度
        taskService.updateTaskStatus(task.getId(), TaskStatus.QUEUED, "引擎故障，重新调度");
        taskSchedulerService.scheduleTask(task);
    }
}
```

---

### 4. 积分管理模块

#### 4.1 积分扣减机制
**功能描述**: 根据任务类型和系统配置自动扣减用户积分

**扣减策略**:
```java
@Service
public class PointDeductionService {
    
    @Transactional(rollbackFor = Exception.class)
    public void deductPointsForTask(Task task) {
        // 1. 获取系统配置
        SystemConfig paymentMode = systemConfigService.getByKey("payment_mode");
        SystemConfig calculationPrice = systemConfigService.getByKey("calculation_price");
        
        Integer requiredPoints = Integer.parseInt(calculationPrice.getConfigValue());
        
        if ("prepaid_mode".equals(paymentMode.getConfigValue())) {
            // 预付费模式：立即扣减积分
            pointTransactionService.deductPoints(
                task.getUserId(), 
                requiredPoints, 
                PointType.HIGH_LEVEL,
                "AI计算任务：" + task.getOrderNo()
            );
            
            // 更新任务付款状态
            taskService.updatePaymentStatus(task.getId(), PaymentStatus.PAID);
            
            log.info("预付费模式：任务 {} 扣减积分 {}", task.getId(), requiredPoints);
            
        } else {
            // 后付费模式：标记为待付款
            taskService.updatePaymentStatus(task.getId(), PaymentStatus.PENDING);
            
            log.info("后付费模式：任务 {} 标记为待付款", task.getId());
        }
    }
    
    @Transactional(rollbackFor = Exception.class)
    public void processPostpaidPayment(Long taskId, Long userId) {
        Task task = taskService.getById(taskId);
        if (task == null || !task.getUserId().equals(userId)) {
            throw new ServiceException("任务不存在或无权访问");
        }
        
        if (task.getPaymentStatus() != PaymentStatus.PENDING) {
            throw new ServiceException("任务不在待付款状态");
        }
        
        SystemConfig calculationPrice = systemConfigService.getByKey("calculation_price");
        Integer requiredPoints = Integer.parseInt(calculationPrice.getConfigValue());
        
        // 扣减积分
        pointTransactionService.deductPoints(
            userId, 
            requiredPoints, 
            PointType.HIGH_LEVEL,
            "AI计算任务付款：" + task.getOrderNo()
        );
        
        // 更新付款状态
        taskService.updatePaymentStatus(taskId, PaymentStatus.PAID);
    }
}
```

---

#### 4.2 积分分配管理
**功能描述**: 管理员向子账号分配积分

**分配流程**:
```java
@Service
public class PointAllocationService {
    
    // 使用ConcurrentHashMap管理细粒度锁
    private final ConcurrentHashMap<Long, Object> userLocks = new ConcurrentHashMap<>();
    
    @Transactional(rollbackFor = Exception.class)
    public String allocatePoints(AllocatePointsDTO dto) {
        // 1. 权限检查
        UserInfoDTO currentUser = userService.getCurrentUser();
        if (!hasAdminRole(currentUser)) {
            throw new ServiceException("无权限操作");
        }
        
        // 2. 验证子账号关系
        User subUser = userService.getSubUserById(currentUser.getUserId(), dto.getSubUserId());
        if (subUser == null) {
            throw new ServiceException("子账号不存在");
        }
        
        // 3. 使用细粒度锁防止并发问题
        Object userLock = userLocks.computeIfAbsent(dto.getSubUserId(), k -> new Object());
        
        synchronized (userLock) {
            try {
                // 4. 检查管理员积分余额
                if (!hasEnoughPoints(currentUser.getUserId(), dto.getPointType(), dto.getPoints())) {
                    throw new ServiceException("积分余额不足");
                }
                
                // 5. 执行积分转移
                transferPoints(currentUser.getUserId(), dto.getSubUserId(), dto);
                
                // 6. 记录分配日志
                recordAllocationLog(currentUser.getUserId(), dto);
                
                return "积分分配成功";
                
            } finally {
                // 7. 清理锁对象（可选的内存优化）
                userLocks.remove(dto.getSubUserId(), userLock);
            }
        }
    }
    
    private void transferPoints(Long fromUserId, Long toUserId, AllocatePointsDTO dto) {
        if (dto.getPointType() == PointType.HIGH_LEVEL) {
            // 高级积分转移
            pointTransactionService.deductPoints(fromUserId, dto.getPoints(), PointType.HIGH_LEVEL, 
                "分配给子账号：" + toUserId);
            pointTransactionService.addPoints(toUserId, dto.getPoints(), PointType.HIGH_LEVEL, 
                "从管理员获得积分：" + fromUserId);
        } else {
            // 普通积分转移
            pointTransactionService.deductPoints(fromUserId, dto.getPoints(), PointType.LOW_LEVEL, 
                "分配给子账号：" + toUserId);
            pointTransactionService.addPoints(toUserId, dto.getPoints(), PointType.LOW_LEVEL, 
                "从管理员获得积分：" + fromUserId);
        }
    }
}
```

---

### 5. 模型管理模块

#### 5.1 模型信息管理
**功能描述**: 管理3D模型的基本信息和展示配置

**模型信息结构**:
```java
@Entity
@Table(name = "model_info")
public class ModelInfo {
    private Long id;
    private String orderNo;          // 关联任务订单号
    private Long userId;             // 用户ID
    private String modelName;        // 模型名称
    private String description;      // 模型描述
    private Integer categoryId;      // 分类ID
    private String categoryName;     // 分类名称
    private ModelStatus status;      // 模型状态
    private LocalDateTime createdAt; // 创建时间
    private LocalDateTime updatedAt; // 更新时间
}

@Entity
@Table(name = "models")
public class Model {
    private Long id;
    private Long modelInfoId;        // 关联ModelInfo
    private String stlFilePath;      // STL文件路径
    private String stlName;          // STL文件名
    private String color;            // 显示颜色
    private String opacity;          // 透明度
    private Boolean visible;         // 是否可见
    private String description;      // 描述信息
    private LocalDateTime createdAt; // 创建时间
}
```

**模型管理服务**:
```java
@Service
public class ModelInfoService {
    
    public ModelInfo createModelInfo(Task task, FileUploadDTO uploadDto) {
        ModelInfo modelInfo = ModelInfo.builder()
            .orderNo(task.getOrderNo())
            .userId(task.getUserId())
            .modelName(uploadDto.getModelName())
            .description(uploadDto.getDescription())
            .categoryId(uploadDto.getCategoryId())
            .categoryName(uploadDto.getCategoryName())
            .status(ModelStatus.DISABLED) // 默认禁用，任务完成后启用
            .createdAt(LocalDateTime.now())
            .build();
            
        return modelInfoRepository.save(modelInfo);
    }
    
    public void enableModelInfo(Long modelInfoId) {
        ModelInfo modelInfo = modelInfoRepository.findById(modelInfoId)
            .orElseThrow(() -> new ServiceException("模型信息不存在"));
            
        modelInfo.setStatus(ModelStatus.ENABLED);
        modelInfo.setUpdatedAt(LocalDateTime.now());
        
        modelInfoRepository.save(modelInfo);
        
        log.info("ModelInfo {} enabled successfully", modelInfoId);
    }
    
    public List<ModelInfoDTO> getUserModels(Long userId, ModelQueryDTO queryDto) {
        return modelInfoRepository.findUserModels(userId, queryDto)
            .stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
}
```

---

#### 5.2 模型数据创建
**功能描述**: 根据AI计算结果自动创建模型数据记录

**创建流程**:
```java
@Service
public class ModelCreationService {
    
    public void createModelsFromResults(Task task, List<ResultFileInfo> resultFiles) {
        try {
            // 1. 查找对应的ModelInfo
            ModelInfo modelInfo = modelInfoService.getByOrderNo(task.getOrderNo());
            if (modelInfo == null) {
                throw new ServiceException("ModelInfo not found for task: " + task.getId());
            }
            
            // 2. 批量创建Model记录
            List<Model> models = createModelRecords(modelInfo, resultFiles);
            
            // 3. 保存模型数据
            modelRepository.saveAll(models);
            
            // 4. 启用ModelInfo
            modelInfoService.enableModelInfo(modelInfo.getId());
            
            // 5. 记录创建日志
            log.info("Created {} models for task {}, ModelInfo {}", 
                models.size(), task.getId(), modelInfo.getId());
                
        } catch (Exception e) {
            log.error("Failed to create models for task {}: {}", task.getId(), e.getMessage(), e);
            throw new ServiceException("模型创建失败: " + e.getMessage());
        }
    }
    
    private List<Model> createModelRecords(ModelInfo modelInfo, List<ResultFileInfo> resultFiles) {
        List<Model> models = new ArrayList<>();
        
        for (ResultFileInfo fileInfo : resultFiles) {
            // 获取用户配置的显示属性
            ModelDisplayConfig displayConfig = getModelDisplayConfig(
                fileInfo.getFileName(), modelInfo.getUserId());
            
            Model model = Model.builder()
                .modelInfoId(modelInfo.getId())
                .stlFilePath(fileInfo.getFilePath())
                .stlName(extractFileName(fileInfo.getFileName()))
                .color(displayConfig.getColor())
                .opacity(displayConfig.getOpacity())
                .visible(true)
                .description(modelInfo.getOrderNo())
                .createdAt(LocalDateTime.now())
                .build();
                
            models.add(model);
        }
        
        return models;
    }
    
    private ModelDisplayConfig getModelDisplayConfig(String fileName, Long userId) {
        // 从用户配置或系统默认配置中获取显示属性
        UserDisplayConfig userConfig = userDisplayConfigService.getByUserId(userId);
        
        if (userConfig != null) {
            return userConfig.getConfigForFile(fileName);
        }
        
        // 返回系统默认配置
        return ModelDisplayConfig.getDefault();
    }
}
```

---

### 6. 实时通信模块

#### 6.1 WebSocket连接管理
**功能描述**: 实现任务状态的实时推送和用户通知

**WebSocket配置**:
```java
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new TaskStatusWebSocketHandler(), "/ws/task-status")
                .addHandler(new TaskProgressWebSocketHandler(), "/ws/task-progress")
                .setAllowedOrigins("*");
    }
}

@Component
public class TaskStatusWebSocketHandler extends TextWebSocketHandler {
    
    // 按用户ID分组管理WebSocket会话
    private final ConcurrentHashMap<Long, Set<WebSocketSession>> userSessions = new ConcurrentHashMap<>();
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        try {
            // 从session中获取用户ID
            Long userId = getUserIdFromSession(session);
            if (userId != null) {
                userSessions.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(session);
                log.info("WebSocket connected for user {}, session {}", userId, session.getId());
            }
        } catch (Exception e) {
            log.error("Error establishing WebSocket connection: {}", e.getMessage(), e);
        }
    }
    
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        try {
            Long userId = getUserIdFromSession(session);
            if (userId != null) {
                Set<WebSocketSession> sessions = userSessions.get(userId);
                if (sessions != null) {
                    sessions.remove(session);
                    if (sessions.isEmpty()) {
                        userSessions.remove(userId);
                    }
                }
                log.info("WebSocket disconnected for user {}, session {}", userId, session.getId());
            }
        } catch (Exception e) {
            log.error("Error closing WebSocket connection: {}", e.getMessage(), e);
        }
    }
    
    public void sendMessageToUser(Long userId, Object message) {
        Set<WebSocketSession> sessions = userSessions.get(userId);
        if (sessions != null) {
            String jsonMessage = JsonUtils.toJson(message);
            sessions.forEach(session -> {
                try {
                    if (session.isOpen()) {
                        session.sendMessage(new TextMessage(jsonMessage));
                    }
                } catch (Exception e) {
                    log.error("Failed to send message to session {}: {}", session.getId(), e.getMessage());
                }
            });
        }
    }
}
```

**消息推送服务**:
```java
@Service
public class WebSocketNotificationService {
    
    @Autowired
    private TaskStatusWebSocketHandler taskStatusHandler;
    
    public void sendTaskStartNotification(Task task) {
        TaskStartMessage message = TaskStartMessage.builder()
            .taskId(task.getId())
            .orderNo(task.getOrderNo())
            .taskName(task.getTaskName())
            .status(TaskStatus.IN_PROGRESS)
            .estimatedTime(getEstimatedExecutionTime(task))
            .timestamp(System.currentTimeMillis())
            .build();
            
        taskStatusHandler.sendMessageToUser(task.getUserId(), message);
        log.info("Sent task start notification for task {}", task.getId());
    }
    
    public void sendTaskCompleteNotification(Task task, List<ResultFileInfo> results) {
        TaskCompleteMessage message = TaskCompleteMessage.builder()
            .taskId(task.getId())
            .orderNo(task.getOrderNo())
            .taskName(task.getTaskName())
            .status(task.getStatus())
            .resultCount(results.size())
            .completedAt(System.currentTimeMillis())
            .modelInfoId(getModelInfoId(task))
            .build();
            
        taskStatusHandler.sendMessageToUser(task.getUserId(), message);
        log.info("Sent task complete notification for task {}", task.getId());
    }
    
    public void sendTaskFailureNotification(Task task, String errorMessage) {
        TaskFailureMessage message = TaskFailureMessage.builder()
            .taskId(task.getId())
            .orderNo(task.getOrderNo())
            .taskName(task.getTaskName())
            .status(TaskStatus.FAILED)
            .errorMessage(errorMessage)
            .failedAt(System.currentTimeMillis())
            .build();
            
        taskStatusHandler.sendMessageToUser(task.getUserId(), message);
        log.error("Sent task failure notification for task {}: {}", task.getId(), errorMessage);
    }
}
```

---

### 7. 计算引擎管理模块

#### 7.1 计算引擎注册和发现
**功能描述**: 管理AI计算引擎的注册、健康检查和负载均衡

**引擎注册服务**:
```java
@Service
public class ComputeEngineRegistryService {
    
    // 引擎信息缓存，key: engineId, value: engineInfo
    private final ConcurrentHashMap<String, ComputeEngineInfo> engineRegistry = new ConcurrentHashMap<>();
    
    public ComputeEngineInfo registerEngine(EngineRegistrationDTO registrationDto) {
        ComputeEngineInfo engineInfo = ComputeEngineInfo.builder()
            .engineId(registrationDto.getEngineId())
            .hostname(registrationDto.getHostname())
            .port(registrationDto.getPort())
            .capacity(registrationDto.getCapacity())
            .currentLoad(0)
            .supportedAlgorithms(registrationDto.getSupportedAlgorithms())
            .status(EngineStatus.AVAILABLE)
            .lastHeartbeat(System.currentTimeMillis())
            .registeredAt(System.currentTimeMillis())
            .build();
            
        // 保存到缓存
        engineRegistry.put(engineInfo.getEngineId(), engineInfo);
        
        // 持久化到数据库
        computeEngineRepository.save(convertToEntity(engineInfo));
        
        log.info("Engine registered: {}", engineInfo);
        return engineInfo;
    }
    
    public void updateEngineHeartbeat(String engineId, EngineHeartbeatDTO heartbeat) {
        ComputeEngineInfo engineInfo = engineRegistry.get(engineId);
        if (engineInfo != null) {
            engineInfo.setCurrentLoad(heartbeat.getCurrentLoad());
            engineInfo.setStatus(heartbeat.getStatus());
            engineInfo.setLastHeartbeat(System.currentTimeMillis());
            
            // 更新数据库
            computeEngineRepository.updateHeartbeat(engineId, heartbeat);
        }
    }
    
    public List<ComputeEngineInfo> getAvailableEngines() {
        long currentTime = System.currentTimeMillis();
        long heartbeatTimeout = 60000; // 60秒心跳超时
        
        return engineRegistry.values().stream()
            .filter(engine -> engine.getStatus() == EngineStatus.AVAILABLE)
            .filter(engine -> currentTime - engine.getLastHeartbeat() < heartbeatTimeout)
            .filter(engine -> engine.getCurrentLoad() < engine.getCapacity())
            .collect(Collectors.toList());
    }
}
```

**负载均衡器**:
```java
@Component
public class ComputeEngineLoadBalancer {
    
    public ComputeEngineInfo selectEngine(List<ComputeEngineInfo> availableEngines, Task task) {
        if (availableEngines.isEmpty()) {
            return null;
        }
        
        // 根据任务类型选择合适的引擎
        List<ComputeEngineInfo> suitableEngines = availableEngines.stream()
            .filter(engine -> supportsTaskType(engine, task.getTaskType()))
            .collect(Collectors.toList());
            
        if (suitableEngines.isEmpty()) {
            return null;
        }
        
        // 使用加权轮询算法选择引擎
        return selectByWeightedRoundRobin(suitableEngines);
    }
    
    private ComputeEngineInfo selectByWeightedRoundRobin(List<ComputeEngineInfo> engines) {
        // 计算每个引擎的权重（基于负载和容量）
        return engines.stream()
            .min(Comparator.comparingDouble(this::calculateLoadRatio))
            .orElse(engines.get(0));
    }
    
    private double calculateLoadRatio(ComputeEngineInfo engine) {
        return (double) engine.getCurrentLoad() / engine.getCapacity();
    }
    
    private boolean supportsTaskType(ComputeEngineInfo engine, TaskType taskType) {
        return engine.getSupportedAlgorithms().contains(taskType.getAlgorithmName());
    }
}
```

---

#### 7.2 计算任务分发
**功能描述**: 将任务分发到合适的计算引擎

**任务分发器**:
```java
@Service
public class ComputeTaskDispatcher {
    
    @Autowired
    private ComputeEngineRegistryService engineRegistry;
    
    @Autowired
    private ComputeEngineLoadBalancer loadBalancer;
    
    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    public void dispatchTask(Task task) {
        try {
            // 1. 查找可用引擎
            List<ComputeEngineInfo> availableEngines = engineRegistry.getAvailableEngines();
            
            // 2. 选择最佳引擎
            ComputeEngineInfo selectedEngine = loadBalancer.selectEngine(availableEngines, task);
            
            if (selectedEngine != null) {
                // 3. 分发到指定引擎
                dispatchToSpecificEngine(task, selectedEngine);
                
                // 4. 更新引擎负载
                updateEngineLoad(selectedEngine.getEngineId(), 1);
                
                log.info("Task {} dispatched to engine {}", task.getId(), selectedEngine.getEngineId());
            } else {
                // 5. 没有可用引擎，放入通用队列
                dispatchToGeneralQueue(task);
                
                log.warn("No available engine for task {}, queued for general processing", task.getId());
            }
            
        } catch (Exception e) {
            log.error("Failed to dispatch task {}: {}", task.getId(), e.getMessage(), e);
            throw new ServiceException("任务分发失败: " + e.getMessage());
        }
    }
    
    private void dispatchToSpecificEngine(Task task, ComputeEngineInfo engine) {
        ComputeTaskMessage message = ComputeTaskMessage.builder()
            .taskId(task.getId())
            .orderNo(task.getOrderNo())
            .taskType(task.getTaskType())
            .inputFilePath(task.getInputFilePath())
            .outputPath(generateOutputPath(task))
            .parameters(parseTaskParameters(task))
            .priority(task.getPriority())
            .assignedEngineId(engine.getEngineId())
            .createdAt(System.currentTimeMillis())
            .build();
            
        // 发送到引擎专用队列
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.COMPUTE_TASK_EXCHANGE,
            "engine." + engine.getEngineId(),
            message
        );
        
        // 更新任务状态
        taskService.updateTaskStatus(task.getId(), TaskStatus.ASSIGNED, 
            "已分配到引擎: " + engine.getEngineId());
    }
    
    private void dispatchToGeneralQueue(Task task) {
        ComputeTaskMessage message = ComputeTaskMessage.builder()
            .taskId(task.getId())
            .orderNo(task.getOrderNo())
            .taskType(task.getTaskType())
            .inputFilePath(task.getInputFilePath())
            .outputPath(generateOutputPath(task))
            .parameters(parseTaskParameters(task))
            .priority(task.getPriority())
            .createdAt(System.currentTimeMillis())
            .build();
            
        // 发送到通用队列
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.COMPUTE_TASK_EXCHANGE,
            RabbitMQConfig.GENERAL_TASK_ROUTING_KEY,
            message
        );
        
        // 更新任务状态
        taskService.updateTaskStatus(task.getId(), TaskStatus.QUEUED, "已加入通用任务队列");
    }
}
```

---

## 系统配置管理

### 配置项结构
```yaml
# application.yml
server:
  port: 8080
  servlet:
    context-path: /api

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/medical_platform?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:password}
    driver-class-name: com.mysql.cj.jdbc.Driver
    
  redis:
    host: ${REDIS_HOST:localhost}
    port: ${REDIS_PORT:6379}
    password: ${REDIS_PASSWORD:}
    database: 0
    
  rabbitmq:
    host: ${RABBITMQ_HOST:localhost}
    port: ${RABBITMQ_PORT:5672}
    username: ${RABBITMQ_USERNAME:guest}
    password: ${RABBITMQ_PASSWORD:guest}
    virtual-host: /medical-platform

# 文件存储配置
file-storage:
  default-platform: aliyun-oss
  aliyun-oss:
    access-key: ${ALIYUN_ACCESS_KEY}
    secret-key: ${ALIYUN_SECRET_KEY}
    endpoint: https://oss-cn-beijing.aliyuncs.com
    bucket-name: medical-platform
    domain: https://medical-platform.oss-cn-beijing.aliyuncs.com/
    base-path: medical-models/

# JWT配置
jwt:
  secret: ${JWT_SECRET:medical-platform-secret-key}
  expiration: 86400000  # 24小时

# WebSocket配置
websocket:
  allowed-origins: "*"
  heartbeat-interval: 30000  # 30秒心跳

# 任务配置
task:
  default-timeout: 3600000    # 1小时默认超时
  max-retry-count: 3          # 最大重试次数
  monitoring-interval: 30000  # 30秒监控间隔
```

---

## API接口规范

### RESTful API设计
```java
// 用户管理接口
@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    
    @PostMapping("/register")
    public ApiResponse<Boolean> register(@RequestBody @Valid UserRegisterDTO dto);
    
    @PostMapping("/login") 
    public ApiResponse<LoginResponseDTO> login(@RequestBody @Valid UserLoginDTO dto);
    
    @GetMapping("/profile")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<UserProfileDTO> getProfile();
    
    @PutMapping("/profile")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<Boolean> updateProfile(@RequestBody @Valid UserProfileUpdateDTO dto);
}

// 任务管理接口
@RestController
@RequestMapping("/api/v1/tasks")
public class TaskController {
    
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<TaskCreationResponseDTO> createTask(@RequestBody @Valid TaskCreationDTO dto);
    
    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<PageResult<TaskDTO>> getTasks(@ModelAttribute TaskQueryDTO queryDto);
    
    @GetMapping("/{taskId}")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<TaskDetailDTO> getTaskDetail(@PathVariable Long taskId);
    
    @DeleteMapping("/{taskId}")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<Boolean> deleteTask(@PathVariable Long taskId);
}

// 模型管理接口
@RestController
@RequestMapping("/api/v1/models")
public class ModelController {
    
    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<PageResult<ModelInfoDTO>> getModels(@ModelAttribute ModelQueryDTO queryDto);
    
    @GetMapping("/{modelInfoId}")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<ModelDetailDTO> getModelDetail(@PathVariable Long modelInfoId);
    
    @GetMapping("/{modelInfoId}/models")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<List<ModelDTO>> getModelComponents(@PathVariable Long modelInfoId);
}
```

---

## 数据库设计

### 核心表结构
```sql
-- 用户表
CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL UNIQUE COMMENT '用户ID',
  `username` varchar(50) NOT NULL UNIQUE COMMENT '用户名',
  `password_hash` varchar(255) NOT NULL COMMENT '密码哈希',
  `email` varchar(100) COMMENT '邮箱',
  `phone` varchar(20) COMMENT '手机号',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1=正常，0=禁用',
  `parent_user_id` bigint COMMENT '父用户ID（子账号）',
  `low_points` int NOT NULL DEFAULT 0 COMMENT '普通积分',
  `high_points` int NOT NULL DEFAULT 0 COMMENT '高级积分',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_username` (`username`),
  KEY `idx_parent_user_id` (`parent_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 任务表
CREATE TABLE `tasks` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_no` varchar(32) NOT NULL UNIQUE COMMENT '订单号',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `task_name` varchar(100) NOT NULL COMMENT '任务名称',
  `task_type` varchar(20) NOT NULL COMMENT '任务类型',
  `input_file_path` varchar(500) NOT NULL COMMENT '输入文件路径',
  `output_path` varchar(500) COMMENT '输出路径',
  `parameters` text COMMENT '任务参数JSON',
  `status` varchar(20) NOT NULL DEFAULT 'CREATED' COMMENT '任务状态',
  `payment_status` varchar(20) NOT NULL DEFAULT 'UNPAID' COMMENT '付款状态',
  `assigned_engine_id` varchar(50) COMMENT '分配的引擎ID',
  `priority` int NOT NULL DEFAULT 0 COMMENT '优先级',
  `progress` int NOT NULL DEFAULT 0 COMMENT '进度百分比',
  `error_message` text COMMENT '错误信息',
  `started_at` datetime COMMENT '开始执行时间',
  `completed_at` datetime COMMENT '完成时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_order_no` (`order_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_assigned_engine` (`assigned_engine_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务表';

-- 模型信息表
CREATE TABLE `model_info` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_no` varchar(32) NOT NULL COMMENT '关联订单号',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `model_name` varchar(100) NOT NULL COMMENT '模型名称',
  `description` text COMMENT '模型描述',
  `category_id` int COMMENT '分类ID',
  `category_name` varchar(50) COMMENT '分类名称',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：1=启用，0=禁用',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_order_no` (`order_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模型信息表';

-- 模型文件表
CREATE TABLE `models` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `model_info_id` bigint NOT NULL COMMENT '模型信息ID',
  `stl_file_path` varchar(500) NOT NULL COMMENT 'STL文件路径',
  `stl_name` varchar(100) NOT NULL COMMENT 'STL文件名',
  `color` varchar(20) COMMENT '显示颜色',
  `opacity` varchar(10) COMMENT '透明度',
  `visible` tinyint NOT NULL DEFAULT 1 COMMENT '是否可见',
  `description` varchar(255) COMMENT '描述',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_model_info_id` (`model_info_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模型文件表';

-- 计算引擎表
CREATE TABLE `compute_engines` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `engine_id` varchar(50) NOT NULL UNIQUE COMMENT '引擎唯一标识',
  `hostname` varchar(100) NOT NULL COMMENT '主机名',
  `port` int NOT NULL COMMENT '端口号',
  `capacity` int NOT NULL COMMENT '最大并发数',
  `current_load` int NOT NULL DEFAULT 0 COMMENT '当前负载',
  `supported_algorithms` text COMMENT '支持的算法列表JSON',
  `status` varchar(20) NOT NULL DEFAULT 'AVAILABLE' COMMENT '引擎状态',
  `last_heartbeat` bigint COMMENT '最后心跳时间戳',
  `registered_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_engine_id` (`engine_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='计算引擎表';
```

---

## 部署和运维

### Docker部署配置
```dockerfile
# Dockerfile
FROM openjdk:8-jre-alpine

VOLUME /tmp
COPY target/medical-model-platform.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]

EXPOSE 8080
```

```yaml
# docker-compose.yml
version: '3.8'
services:
  medical-platform:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - DB_HOST=mysql
      - REDIS_HOST=redis
      - RABBITMQ_HOST=rabbitmq
    depends_on:
      - mysql
      - redis
      - rabbitmq
    volumes:
      - ./logs:/app/logs

  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: rootpassword
      MYSQL_DATABASE: medical_platform
    volumes:
      - mysql_data:/var/lib/mysql
      - ./sql:/docker-entrypoint-initdb.d
    ports:
      - "3306:3306"

  redis:
    image: redis:6-alpine
    ports:
      - "6379:6379"

  rabbitmq:
    image: rabbitmq:3-management
    environment:
      RABBITMQ_DEFAULT_USER: admin
      RABBITMQ_DEFAULT_PASS: admin
    ports:
      - "5672:5672"
      - "15672:15672"

volumes:
  mysql_data:
```

---

## 总结

医学影像模型管理平台作为业务系统，专注于：

1. **业务逻辑处理**: 用户管理、权限控制、积分管理等核心业务功能
2. **任务编排**: 负责任务的创建、调度、状态管理和结果处理
3. **数据管理**: 管理所有业务数据的持久化和查询
4. **用户交互**: 提供完整的API接口和实时通信能力
5. **资源管理**: 管理计算引擎的注册、发现和负载均衡

通过清晰的模块划分和标准化的接口设计，确保系统的可维护性、可扩展性和高可用性。

---

**文档版本**: v2.0  
**最后更新**: 2024-12-19  
**维护状态**: 活跃开发中 