<template>
  <div class="tiptap-editor">
    <div class="tiptap-toolbar">
      <button class="btn" :class="{ on: editor?.isActive('bold') }" @click.prevent="editor?.chain().focus().toggleBold().run()">B</button>
      <button class="btn" :class="{ on: editor?.isActive('italic') }" @click.prevent="editor?.chain().focus().toggleItalic().run()"><i>I</i></button>
      <button class="btn" :class="{ on: editor?.isActive('underline') }" @click.prevent="editor?.chain().focus().toggleUnderline().run()">U</button>
      <input class="color" type="color" title="文字颜色" @input="e=>setColor(e.target.value)" />
      <input class="color" type="color" title="背景高亮" value="#ff0000" @input="e=>setHighlight(e.target.value)" />
      <span class="sep" />
      <button class="btn" :class="{ on: editor?.isActive({ textAlign: 'left' }) }" @click.prevent="editor?.chain().focus().setTextAlign('left').run()">L</button>
      <button class="btn" :class="{ on: editor?.isActive({ textAlign: 'center' }) }" @click.prevent="editor?.chain().focus().setTextAlign('center').run()">C</button>
      <button class="btn" :class="{ on: editor?.isActive({ textAlign: 'right' }) }" @click.prevent="editor?.chain().focus().setTextAlign('right').run()">R</button>
      <span class="sep" />
      <button class="btn" :class="{ on: editor?.isActive('bulletList') }" @click.prevent="editor?.chain().focus().toggleBulletList().run()">• List</button>
      <button class="btn" :class="{ on: editor?.isActive('orderedList') }" @click.prevent="editor?.chain().focus().toggleOrderedList().run()">1. List</button>
      <span class="sep" />
      <button class="btn" @click.prevent="insertLink">Link</button>
      <button class="btn" @click.prevent="editor?.chain().focus().setHorizontalRule().run()">HR</button>
      <button class="btn" @click.prevent="editor?.chain().focus().undo().run()">Undo</button>
      <button class="btn" @click.prevent="editor?.chain().focus().redo().run()">Redo</button>
    </div>
    <EditorContent v-if="editor" :editor="editor" class="tiptap-content" />
  </div>
</template>

<script setup>
import { ref, watch, onMounted, onBeforeUnmount } from 'vue'
import { Editor, EditorContent } from '@tiptap/vue-3'
import StarterKit from '@tiptap/starter-kit'
import Link from '@tiptap/extension-link'
import Placeholder from '@tiptap/extension-placeholder'
import TextAlign from '@tiptap/extension-text-align'
import Underline from '@tiptap/extension-underline'
import TextStyle from '@tiptap/extension-text-style'
import Color from '@tiptap/extension-color'
import Highlight from '@tiptap/extension-highlight'
import { sanitizeHtml } from '@/utils/sanitize'

const props = defineProps({
  modelValue: { type: String, default: '' },
  placeholder: { type: String, default: '请输入内容…' }
})
const emit = defineEmits(['update:modelValue'])

const editor = ref(null)

function sanitize(html) { return sanitizeHtml(html) }

onMounted(() => {
  editor.value = new Editor({
    extensions: [
      StarterKit,
      Underline,
      TextStyle,
      Color,
      Highlight.configure({ multicolor: true }),
      Placeholder.configure({ placeholder: props.placeholder }),
      TextAlign.configure({ types: ['heading', 'paragraph'] }),
      Link.configure({ openOnClick: true, autolink: true, HTMLAttributes: { target: '_blank', rel: 'noopener noreferrer nofollow' } })
    ],
    content: sanitize(props.modelValue),
    editorProps: {
      attributes: { class: 'prose prose-sm prose-slate' }
    },
    onUpdate: ({ editor }) => {
      const html = sanitize(editor.getHTML())
      emit('update:modelValue', html)
    }
  })
})

onBeforeUnmount(() => {
  editor.value?.destroy()
})

watch(() => props.modelValue, (val) => {
  const safe = sanitize(val)
  if (editor.value && safe !== editor.value.getHTML()) {
    editor.value.commands.setContent(safe, false)
  }
})

function insertLink() {
  const url = window.prompt('请输入链接地址：')
  if (!url) return
  editor.value?.chain().focus().extendMarkRange('link').setLink({ href: url }).run()
}

function setColor(color) {
  const ed = editor.value
  if (!color || !ed) return
  // 有些打包环境会导致 TextStyle 未正确注册，直接调用会抛错
  const hasTextStyle = !!(ed.schema && ed.schema.marks && ed.schema.marks.textStyle)
  if (!hasTextStyle) {
    console.warn('[RichTextEditor] TextStyle extension missing; color picker disabled')
    return
  }
  try {
    ed.chain().focus().setColor(color).run()
  } catch (e) {
    console.error('[RichTextEditor] setColor failed:', e)
  }
}

function setHighlight(color) {
  const ed = editor.value
  if (!color || !ed) return
  try {
    ed.chain().focus().setHighlight({ color }).run()
  } catch (e) {
    console.error('[RichTextEditor] setHighlight failed:', e)
  }
}
</script>

<style scoped>
.tiptap-editor { border:1px solid #e2e8f0; border-radius: 10px; overflow: hidden; background:#fff; transition: box-shadow .15s ease, border-color .15s ease; }
.tiptap-editor:focus-within { border-color:#1890ff; box-shadow: 0 0 0 3px rgba(24,144,255,.15); }
.tiptap-toolbar { padding:8px; background:#f8fafc; border-bottom:1px solid #e2e8f0; display:flex; gap:6px; flex-wrap: wrap; }
.btn { height:28px; padding:0 10px; border-radius: 6px; border:1px solid #e2e8f0; background:#fff; cursor:pointer; font-weight:700; }
.btn.on { background:#0ea5e9; border-color:#0ea5e9; color:#fff; }
.sep { width:1px; background:#e2e8f0; margin:0 4px; }
.color { width:28px; height:28px; padding:0; border:1px solid #e2e8f0; border-radius: 6px; background:#fff; cursor:pointer; }
.tiptap-content { padding: 0; }
.tiptap-content :deep(.ProseMirror) { min-height: 160px; padding: 10px; outline: none; }
.tiptap-content :deep(.ProseMirror p.is-editor-empty:first-child::before) { content: attr(data-placeholder); float:left; color:#94a3b8; pointer-events:none; height:0; }
.tiptap-content :deep(a) { color:#2563eb; text-decoration: underline; }
.tiptap-content :deep(img) { max-width: 100%; height: auto; }
</style>


