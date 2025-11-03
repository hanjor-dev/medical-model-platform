// 简易 HTML 清洗，保留安全内联格式
// 同步富文本工具栏能力，补充 hr 支持
const ALLOWED_TAGS = new Set(['b','strong','i','em','u','ul','ol','li','p','br','a','h1','h2','h3','blockquote','code','pre','hr','span','mark'])
const ALLOWED_ATTRS = { a: new Set(['href', 'target', 'rel']) }

export function sanitizeHtml(html) {
  if (!html) return ''
  const tpl = document.createElement('template')
  tpl.innerHTML = html
  const walker = document.createTreeWalker(tpl.content, NodeFilter.SHOW_ELEMENT, null)
  const toRemove = []

  // 过滤标签与属性
  while (walker.nextNode()) {
    const el = walker.currentNode
    const tag = el.tagName.toLowerCase()
    if (!ALLOWED_TAGS.has(tag)) {
      toRemove.push(el)
      continue
    }
    // 移除不安全属性
    const allow = ALLOWED_ATTRS[tag] || new Set()
    ;[...el.attributes].forEach(attr => {
      const name = attr.name.toLowerCase()
      const val = attr.value || ''
      const isAllowed = allow.has(name)
      const isSafeHref = name === 'href' && !/^\s*javascript:/i.test(val)
      if (!isAllowed && name !== 'style') el.removeAttribute(name)
      if (name === 'href' && !isSafeHref) el.removeAttribute('href')
      if (name === 'target') el.setAttribute('rel', 'noopener noreferrer')
    })
  }
  toRemove.forEach(n => n.replaceWith(...n.childNodes))
  return tpl.innerHTML
}


