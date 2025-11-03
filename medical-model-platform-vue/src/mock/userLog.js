/**
 * 用户日志模拟数据
 * 用于前端开发和测试
 */

// 模拟日志数据
const mockLogs = [
  {
    id: 1,
    logType: 'LOGIN',
    userId: 1001,
    username: 'admin',
    status: 1,
    ipAddress: '127.0.0.1',
    location: '本地',
    userAgent: 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36',
    loginMessage: '登录成功',
    logTime: '2025-01-15 14:30:25'
  },
  {
    id: 2,
    logType: 'OPERATION',
    userId: 1001,
    username: 'admin',
    operationModule: 'USER',
    operationType: 'CREATE',
    description: '创建用户账号',
    requestMethod: 'POST',
    requestUrl: '/api/user',
    requestParams: '{"username":"testuser","email":"test@example.com"}',
    responseResult: '{"success":true,"message":"用户创建成功"}',
    status: 1,
    ipAddress: '127.0.0.1',
    location: '本地',
    costTime: 156,
    logTime: '2025-01-15 14:25:10'
  },
  {
    id: 3,
    logType: 'LOGIN',
    userId: null,
    username: 'unknown',
    status: 0,
    ipAddress: '192.168.1.100',
    location: '北京',
    userAgent: 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)',
    loginMessage: '用户名或密码错误',
    logTime: '2025-01-15 14:20:45'
  },
  {
    id: 4,
    logType: 'OPERATION',
    userId: 1002,
    username: 'user01',
    operationModule: 'CREDIT',
    operationType: 'TRANSFER',
    description: '积分转账操作',
    requestMethod: 'POST',
    requestUrl: '/api/credit/transfer',
    requestParams: '{"fromUserId":1002,"toUserId":1003,"amount":100}',
    responseResult: '{"success":true,"message":"转账成功"}',
    status: 1,
    ipAddress: '192.168.1.50',
    location: '上海',
    costTime: 234,
    logTime: '2025-01-15 14:15:30'
  },
  {
    id: 5,
    logType: 'OPERATION',
    userId: 1001,
    username: 'admin',
    operationModule: 'SYSTEM',
    operationType: 'DELETE',
    description: '删除系统配置',
    requestMethod: 'DELETE',
    requestUrl: '/api/system/config/123',
    status: 0,
    ipAddress: '127.0.0.1',
    location: '本地',
    errorMessage: '配置项不存在或已被删除',
    costTime: 89,
    logTime: '2025-01-15 14:10:15'
  }
]

 
// 模拟模块选项
const mockModules = [
  { value: 'USER', label: '用户管理' },
  { value: 'CREDIT', label: '积分系统' },
  { value: 'SYSTEM', label: '系统管理' },
  { value: 'MODEL', label: '模型管理' },
  { value: 'AI_ENGINE', label: 'AI引擎' }
]

// 模拟类型选项
const mockTypes = {
  USER: [
    { value: 'CREATE', label: '创建' },
    { value: 'UPDATE', label: '更新' },
    { value: 'DELETE', label: '删除' },
    { value: 'VIEW', label: '查看' }
  ],
  CREDIT: [
    { value: 'TRANSFER', label: '转账' },
    { value: 'RECHARGE', label: '充值' },
    { value: 'WITHDRAW', label: '提现' },
    { value: 'CONSUME', label: '消费' }
  ],
  SYSTEM: [
    { value: 'CONFIG', label: '配置' },
    { value: 'BACKUP', label: '备份' },
    { value: 'RESTORE', label: '恢复' },
    { value: 'MONITOR', label: '监控' }
  ]
}

export {
  mockLogs,
  mockModules,
  mockTypes
} 
