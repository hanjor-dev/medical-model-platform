<template>
  <div class="permission-center-page">
    <div class="page-content">
      <el-card shadow="never" class="content-card">
        <template #header>
          <AppBreadcrumb />
        </template>

        <el-tabs v-model="activeTab" class="permission-tabs">
          <!-- 角色权限 -->
          <el-tab-pane label="角色权限" name="role">
            <div class="role-permission">
              <div class="sidebar">
                <div class="block-title">角色</div>
                <el-radio-group v-model="selectedRole" class="role-list">
                  <el-radio-button v-for="item in roleOptions" :key="item.value" :label="item.value">{{ item.label }}</el-radio-button>
                </el-radio-group>

                <el-divider />

                <div class="filters">
                  <el-select v-model="roleFilter.type" placeholder="权限类型" size="small" clearable class="mr8">
                    <el-option label="全部类型" value="" />
                    <el-option label="菜单 MENU" value="MENU" />
                    <el-option label="按钮 BUTTON" value="BUTTON" />
                    <el-option label="接口 API" value="API" />
                  </el-select>
                  <el-input v-model="roleFilter.keyword" placeholder="搜索关键词" size="small" clearable suffix-icon="el-icon-search" />
                </div>
              </div>

              <div class="tree-area">
                <div class="toolbar">
                  <div class="left">
                    <el-button size="small" @click="expandAll(true)">展开</el-button>
                    <el-button size="small" @click="expandAll(false)">折叠</el-button>
                    <el-button size="small" @click="checkAll(true)">全选</el-button>
                    <el-button size="small" @click="checkAll(false)">清空</el-button>
                  </div>
                  <div class="right">
                    <el-button type="primary" :loading="savingRole" @click="onSaveRole">保存</el-button>
                  </div>
                </div>
                <el-scrollbar class="tree-scroll">
                  <el-tree
                    ref="roleTreeRef"
                    :data="roleTreeData"
                    :props="treeProps"
                    node-key="id"
                    show-checkbox
                    default-expand-all
                    class="permission-tree"
                  />
                </el-scrollbar>
              </div>
            </div>
          </el-tab-pane>

          <!-- 权限管理 -->
          <el-tab-pane label="权限管理" name="permission">
            <div class="permission-management">
              <div class="table-toolbar">
                <el-select v-model="listFilter.type" placeholder="类型" clearable class="mr8">
                  <el-option label="全部类型" value="" />
                  <el-option label="菜单 MENU" value="MENU" />
                  <el-option label="按钮 BUTTON" value="BUTTON" />
                  <el-option label="接口 API" value="API" />
                </el-select>
                <el-input v-model="listFilter.keyword" placeholder="关键词" clearable class="mr8" />
                <el-button type="primary" disabled>新建权限</el-button>
              </div>
              <el-table ref="permissionTableRef" :data="permissionList" border stripe class="permission-table" :loading="listLoading">
                <el-table-column prop="permissionName" label="名称" min-width="140" />
                <el-table-column prop="permissionCode" label="编码" min-width="160" />
                <el-table-column prop="permissionType" label="类型" width="110">
                  <template #default="{ row }">
                    <el-tag :type="typeTagType(row.permissionType)" size="small">{{ row.permissionType }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="path" label="路由路径" min-width="160" />
                <el-table-column prop="component" label="组件" min-width="160" />
                <el-table-column prop="sort" label="排序" width="80" align="center" />
                <el-table-column prop="status" label="状态" width="100" align="center">
                  <template #default="{ row }">
                    <el-switch :model-value="row.status === 1" disabled />
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="160" fixed="right" align="center">
                  <template #default>
                    <el-button link type="primary" size="small" disabled>编辑</el-button>
                    <el-divider direction="vertical" />
                    <el-button link type="danger" size="small" disabled>删除</el-button>
                  </template>
                </el-table-column>
              </el-table>
              <div class="table-pagination">
                <el-pagination
                  layout="prev, pager, next"
                  :total="total"
                  :page-size="pageSize"
                  :current-page="currentPage"
                  @current-change="onPageChange"
                  small
                  background
                />
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </el-card>
    </div>
  </div>
  
</template>

<script>
import { defineComponent, ref, watch, nextTick, onMounted } from 'vue'
import { permissionApi } from '@/api/permission'
import AppBreadcrumb from '@/components/common/Breadcrumb.vue'

export default defineComponent({
  name: 'PermissionCenter',
  components: { AppBreadcrumb },
  setup() {
    const activeTab = ref('role')

    // 角色权限
    const roleOptions = ref([
      { label: '普通用户 USER', value: 'USER' },
      { label: '管理员 ADMIN', value: 'ADMIN' }
    ])
    const selectedRole = ref('ADMIN')
    const roleFilter = ref({ type: '', keyword: '' })

    const treeProps = { children: 'children', label: 'permissionName' }
    const roleTreeData = ref([])
    const savingRole = ref(false)

    const roleTreeRef = ref(null)
    const expandAll = (val) => {
      const tree = roleTreeRef.value
      if (!tree) return
      const nodes = tree.store.nodesMap
      Object.keys(nodes).forEach(key => {
        nodes[key].expanded = val
      })
    }
    const checkAll = (val) => {
      const tree = roleTreeRef.value
      if (!tree) return
      const allKeys = []
      const walk = (arr) => arr.forEach(n => { allKeys.push(n.id); if (n.children) walk(n.children) })
      walk(roleTreeData.value)
      tree.setCheckedKeys(val ? allKeys : [])
    }

    const loadRoleTree = async () => {
      try {
        const [treeResp, rolePermResp] = await Promise.all([
          permissionApi.getPermissionTree(),
          permissionApi.getRolePermissions(selectedRole.value)
        ])
        if (treeResp.success) {
          roleTreeData.value = Array.isArray(treeResp.data) ? treeResp.data : []
        } else {
          roleTreeData.value = []
        }

        // 仅勾选叶子节点
        const selectedIds = (rolePermResp.success && Array.isArray(rolePermResp.data))
          ? rolePermResp.data.map(p => p.id)
          : []

        const collectParentIds = (arr, parentSet) => {
          if (!Array.isArray(arr)) return
          arr.forEach(n => {
            if (Array.isArray(n.children) && n.children.length > 0) {
              parentSet.add(n.id)
              collectParentIds(n.children, parentSet)
            }
          })
        }
        const parentIdSet = new Set()
        collectParentIds(roleTreeData.value, parentIdSet)
        const leafOnlyIds = selectedIds.filter(id => !parentIdSet.has(id))

        await nextTick()
        roleTreeRef.value && roleTreeRef.value.setCheckedKeys(leafOnlyIds)
      } catch (e) {
        console.error('加载角色权限树失败', e)
      }
    }

    const onSaveRole = async () => {
      if (!roleTreeRef.value) return
      savingRole.value = true
      try {
        const checkedKeys = roleTreeRef.value.getCheckedKeys()
        const halfCheckedKeys = typeof roleTreeRef.value.getHalfCheckedKeys === 'function' ? roleTreeRef.value.getHalfCheckedKeys() : []
        const keys = Array.from(new Set([...(checkedKeys || []), ...(halfCheckedKeys || [])]))
        const resp = await permissionApi.updateRolePermissions({ role: selectedRole.value, permissionIds: keys })
        if (resp.success) {
          window.ElMessage && window.ElMessage.success('角色权限保存成功')
        } else {
          window.ElMessage && window.ElMessage.error(resp.message || '保存失败')
        }
      } catch (e) {
        console.error('保存角色权限失败', e)
        window.ElMessage && window.ElMessage.error('保存失败')
      } finally {
        savingRole.value = false
      }
    }

    // 权限管理（列表静态数据）
    const listFilter = ref({ type: '', keyword: '' })
    const permissionList = ref([])
    const listLoading = ref(false)
    const total = ref(0)
    const pageSize = ref(10)
    const currentPage = ref(1)
    const typeTagType = (type) => {
      if (type === 'MENU') return 'success'
      if (type === 'BUTTON') return 'info'
      if (type === 'API') return 'warning'
      return ''
    }

    const fetchPermissionPage = async () => {
      listLoading.value = true
      try {
        const resp = await permissionApi.getPermissionPage({
          current: currentPage.value,
          size: pageSize.value,
          permissionType: listFilter.value.type,
          keyword: listFilter.value.keyword
        })
        if (resp.success && resp.data) {
          const page = resp.data
          permissionList.value = Array.isArray(page.records) ? page.records : []
          total.value = page.total || 0
          pageSize.value = page.size || pageSize.value
          currentPage.value = page.current || currentPage.value
        } else {
          permissionList.value = []
          total.value = 0
        }
      } catch (e) {
        console.error('加载权限分页失败', e)
      } finally {
        listLoading.value = false
      }
    }

    const onPageChange = (p) => {
      currentPage.value = p
      fetchPermissionPage()
    }

    // 修复表格在 Tab 中切换导致的 ResizeObserver 报错
    const permissionTableRef = ref(null)
    watch(activeTab, (val) => {
      if (val === 'permission') {
        nextTick(() => {
          const table = permissionTableRef.value
          if (table && typeof table.doLayout === 'function') {
            table.doLayout()
          }
        })
      }
    })

    // 监听
    watch(selectedRole, () => {
      if (activeTab.value === 'role') loadRoleTree()
    })
    watch(listFilter, () => {
      if (activeTab.value === 'permission') {
        currentPage.value = 1
        fetchPermissionPage()
      }
    }, { deep: true })

    watch(activeTab, (val) => {
      if (val === 'role') {
        loadRoleTree()
      } else if (val === 'permission') {
        fetchPermissionPage()
      }
    })

    onMounted(() => {
      loadRoleTree()
    })

    return {
      activeTab,
      // 角色权限
      roleOptions,
      selectedRole,
      roleFilter,
      roleTreeRef,
      expandAll,
      checkAll,
      treeProps,
      roleTreeData,
      savingRole,
      onSaveRole,
      // 权限管理
      listFilter,
      permissionList,
      typeTagType,
      listLoading,
      total,
      pageSize,
      currentPage,
      onPageChange,
      permissionTableRef,
      
    }
  }
})
</script>

<style lang="scss" scoped>
.permission-center-page {
  padding: 8px 0 12px;
  background: transparent;
  display: flex;
  flex-direction: column;
  min-height: calc(100vh - var(--app-header-height) - var(--main-padding-v));

  .page-content { flex: 1; display: flex; min-height: 0; }
  .content-card { flex: 1 1 auto; width: 100%; min-height: 100%; display: flex; flex-direction: column; }
  .content-card :deep(.el-card__body) { flex: 1; display: flex; flex-direction: column; min-height: 0; }

  .page-header { margin-bottom: 12px; }
  .permission-tabs {
    .role-permission {
      display: grid;
      grid-template-columns: 300px 1fr;
      gap: 16px;

      .sidebar {
        border-right: 1px solid #f0f0f0;
        padding-right: 12px;
        .block-title { font-weight: 600; color: #606266; margin-bottom: 8px; }
        .role-list { display: flex; flex-direction: column; gap: 8px; }
        .filters { display: flex; gap: 8px; margin-top: 8px; }
      }

      .tree-area {
        .toolbar {
          display: flex;
          align-items: center;
          justify-content: space-between;
          margin-bottom: 8px;
        }
        .tree-scroll { height: 420px; }
        .permission-tree { --el-tree-node-content-height: 30px; }
      }
    }

    .permission-management {
      .table-toolbar {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-bottom: 12px;
      }
      .permission-table { font-size: 13px; }
      .table-pagination { display: flex; justify-content: flex-end; margin-top: 12px; }
    }
  }

  .mr8 { margin-right: 8px; }
}
</style>


