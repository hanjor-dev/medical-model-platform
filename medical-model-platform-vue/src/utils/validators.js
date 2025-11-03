export function requireText(message = '该字段不能为空') {
  return (val) => !!(val && String(val).trim()) || message
}

export function maxLen(max, message) {
  return (val) => (String(val || '').length <= max) || (message || `最多 ${max} 个字符`)
}

export function betweenLen(min, max, message) {
  return (val) => {
    const len = String(val || '').trim().length
    return (len >= min && len <= max) || (message || `长度需在 ${min}-${max}`)
  }
}

export function isCron(message = 'Cron 表达式无效') {
  const cronRegex = /^(\*|([0-5]?\d))(\s+)(\*|([01]?\d|2[0-3]))(\s+)(\*|([012]?\d|3[01]))(\s+)(\*|(1[0-2]|0?[1-9]))(\s+)(\*|([0-6]))$/
  return (val) => cronRegex.test(String(val || '').trim()) || message
}

export function isUrl(message = '链接格式不正确') {
  // 不在函数定义时抛异常，避免不可达分支
  return (val) => {
    if (!val) return true
    try {
      // eslint-disable-next-line no-new
      new URL(val)
      return true
    } catch (e) {
      return message
    }
  }
}

export function notBefore(now = Date.now(), message = '时间不能早于当前') {
  return (val) => !val || new Date(val).getTime() >= now || message
}


