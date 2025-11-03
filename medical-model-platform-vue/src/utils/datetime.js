export function formatDateTime(input) {
  if (!input) return ''
  const d = new Date(input)
  if (Number.isNaN(d.getTime())) return ''
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hh = String(d.getHours()).padStart(2, '0')
  const mm = String(d.getMinutes()).padStart(2, '0')
  return `${y}-${m}-${day} ${hh}:${mm}`
}

export function fromNow(input) {
  if (!input) return ''
  const t = new Date(input).getTime()
  if (Number.isNaN(t)) return ''
  const diff = Date.now() - t
  const abs = Math.abs(diff)
  const unit = abs < 6e4 ? '秒' : abs < 36e5 ? '分钟' : abs < 864e5 ? '小时' : '天'
  const val = abs < 6e4 ? Math.round(abs / 1000) : abs < 36e5 ? Math.round(abs / 60000) : abs < 864e5 ? Math.round(abs / 3600000) : Math.round(abs / 86400000)
  return diff >= 0 ? `${val}${unit}前` : `${val}${unit}后`
}


