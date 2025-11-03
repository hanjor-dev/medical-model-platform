# 🔐 Git 使用指南

> Medical Model Platform - Git版本管理规范

**最后更新**: 2025-10-31  
**文档版本**: v1.0

---

## 📋 目录

- [快速开始](#快速开始)
- [Git工作流程](#git工作流程)
- [常用命令](#常用命令)
- [分支管理](#分支管理)
- [提交规范](#提交规范)
- [版本回滚](#版本回滚)
- [故障排查](#故障排查)
- [最佳实践](#最佳实践)

---

## 🚀 快速开始

### 初次使用（5分钟）

```bash
# 1. 配置Git用户信息
git config --global user.name "Your Name"
git config --global user.email "your@email.com"

# 2. 进入项目目录
cd medical-model-platform

# 3. 初始化Git仓库（如果还没有）
git init

# 4. 查看当前状态
git status

# 5. 创建.gitignore文件（已存在则跳过）
# 已自动生成，无需手动创建
```

### 首次提交

```bash
# 1. 查看将要提交的文件
git status

# 2. 添加所有文件到暂存区
git add .

# 3. 创建初始提交
git commit -m "初始提交: 项目基础代码"

# 4. 创建标签（可选）
git tag -a v3.0.0 -m "正式版本: JDK 21 + Spring Boot 3.3.6"
```

---

## 📝 Git工作流程

### 标准开发流程

```bash
# === 步骤1: 创建功能分支 ===
git checkout -b feature/user-management

# === 步骤2: 开发过程中定期提交 ===
# 修改代码...
git add <files>
git commit -m "feat(user): 添加用户注册功能"

# 修改代码...
git add <files>
git commit -m "feat(user): 添加用户登录功能"

# === 步骤3: 完成后合并到主分支 ===
git checkout main
git merge feature/user-management

# === 步骤4: 清理功能分支（可选） ===
git branch -d feature/user-management
```

### 项目升级流程

```bash
# === 升级前备份 ===
git checkout main
git add .
git commit -m "升级前备份: v2.0.0"
git tag -a v2.0.0-before-upgrade -m "升级前稳定版本"

# === 创建升级分支 ===
git checkout -b upgrade/jdk21-springboot3

# === 升级过程中分阶段提交 ===
# 阶段1: 修改pom.xml
git add pom.xml
git commit -m "升级: Spring Boot 2.7.4 → 3.3.6"

# 阶段2: 命名空间迁移
git add src/
git commit -m "迁移: javax → jakarta"

# 阶段3: Swagger迁移
git add src/
git commit -m "迁移: Springfox → SpringDoc"

# === 升级完成后合并 ===
git checkout main
git merge upgrade/jdk21-springboot3
git tag -a v3.0.0 -m "正式版本: JDK 21 + Spring Boot 3.3.6"
```

---

## 🔧 常用命令

### 基础命令

```bash
# === 状态查看 ===
git status                    # 查看工作区状态
git status -s                 # 简短格式
git diff                      # 查看工作区变更
git diff --staged             # 查看暂存区变更

# === 日志查看 ===
git log                       # 查看提交历史
git log --oneline             # 单行显示
git log --oneline --graph     # 图形化显示
git log --since="2 weeks ago" # 最近两周的提交
git log --author="hanjor"     # 特定作者的提交

# === 文件操作 ===
git add <file>                # 添加文件到暂存区
git add .                     # 添加所有变更
git rm <file>                 # 删除文件
git mv <old> <new>            # 重命名文件
```

### 提交命令

```bash
# === 提交 ===
git commit -m "提交信息"      # 提交暂存区
git commit -am "提交信息"     # 添加并提交（已跟踪文件）
git commit --amend            # 修改上一次提交

# === 暂存管理 ===
git stash                     # 暂存当前工作
git stash list                # 查看暂存列表
git stash apply               # 应用最新暂存
git stash pop                 # 应用并删除暂存
git stash drop                # 删除暂存
```

### 分支命令

```bash
# === 查看分支 ===
git branch                    # 查看本地分支
git branch -a                 # 查看所有分支
git branch -v                 # 查看分支及最后提交

# === 创建分支 ===
git branch <name>             # 创建分支
git checkout -b <name>        # 创建并切换分支

# === 切换分支 ===
git checkout <name>           # 切换分支
git switch <name>             # 同上（新命令）

# === 删除分支 ===
git branch -d <name>          # 安全删除（已合并）
git branch -D <name>          # 强制删除（未合并）

# === 合并分支 ===
git merge <branch>            # 合并分支
git merge --no-ff <branch>    # 禁用快进合并
```

### 标签命令

```bash
# === 查看标签 ===
git tag                       # 列出所有标签
git tag -l "v3.*"            # 模式匹配
git show <tag>                # 查看标签详情

# === 创建标签 ===
git tag <name>                # 轻量标签
git tag -a <name> -m "说明"   # 附注标签（推荐）

# === 删除标签 ===
git tag -d <name>             # 删除本地标签

# === 检出标签 ===
git checkout <tag>            # 检出标签
git checkout -b <branch> <tag> # 从标签创建分支
```

### 撤销命令

```bash
# === 撤销工作区修改 ===
git checkout -- <file>        # 撤销文件修改
git restore <file>            # 同上（新命令）

# === 撤销暂存 ===
git reset HEAD <file>         # 取消暂存
git restore --staged <file>   # 同上（新命令）

# === 撤销提交 ===
git reset --soft HEAD~1       # 软重置（保留修改）
git reset HEAD~1              # 混合重置（保留工作区）
git reset --hard HEAD~1       # 硬重置（完全撤销，⚠️ 危险）
git revert <commit>           # 创建反向提交
```

---

## 🌿 分支管理

### 分支命名规范

```bash
# === 主分支 ===
main / master                 # 主分支（生产代码）

# === 功能分支 ===
feature/<功能名称>
feature/user-authentication   # 用户认证功能
feature/file-upload           # 文件上传功能

# === 升级分支 ===
upgrade/<升级内容>
upgrade/jdk21-springboot3     # JDK和Spring Boot升级
upgrade/docker-environment    # Docker环境升级

# === 修复分支 ===
fix/<问题描述>
hotfix/<紧急问题>
fix/database-connection       # 数据库连接问题
hotfix/security-vulnerability # 安全漏洞修复

# === 实验分支 ===
experiment/<实验内容>
experiment/virtual-threads    # 虚拟线程实验
```

### 分支生命周期

```bash
# === 1. 创建功能分支 ===
git checkout -b feature/new-feature

# === 2. 开发过程中定期提交 ===
git add <files>
git commit -m "feat: 添加新功能"

# === 3. 完成后合并到主分支 ===
git checkout main
git merge feature/new-feature

# === 4. 删除功能分支（可选） ===
git branch -d feature/new-feature
```

---

## 📝 提交规范

### 提交信息格式

```
<类型>(<范围>): <简短描述>

<详细描述>

<关联信息>
```

### 类型说明

| 类型 | 说明 | 示例 |
|-----|------|------|
| `feat` | 新功能 | `feat(auth): 添加JWT认证` |
| `fix` | 修复Bug | `fix(db): 修复连接池泄漏` |
| `docs` | 文档 | `docs(readme): 更新安装说明` |
| `style` | 格式化 | `style: 格式化代码` |
| `refactor` | 重构 | `refactor(service): 优化用户服务` |
| `perf` | 性能优化 | `perf(query): 优化数据库查询` |
| `test` | 测试 | `test(user): 添加用户测试` |
| `build` | 构建 | `build(maven): 升级依赖版本` |
| `chore` | 其他 | `chore(deps): 更新依赖` |

### 提交示例

```bash
# === 好的提交信息 ✅ ===
git commit -m "feat(upgrade): 升级Spring Boot到3.3.6

- 修改pom.xml parent版本
- 更新所有相关依赖
- 验证编译通过

关联: #123"

git commit -m "fix(config): 修复Redis连接配置

问题: Docker环境下Redis连接失败
原因: 配置中使用了localhost而不是服务名
解决: 将redis.host从localhost改为redis

测试: 连接正常，缓存读写正常"

# === 不好的提交信息 ❌ ===
git commit -m "修改了一些文件"
git commit -m "fix"
git commit -m "更新"
```

---

## 🔄 版本回滚

### 场景1: 撤销最近的提交

```bash
# === 软重置（保留修改，只撤销提交） ===
git reset --soft HEAD~1

# === 混合重置（撤销提交和暂存，保留修改） ===
git reset HEAD~1

# === 硬重置（完全撤销，⚠️ 危险！） ===
git reset --hard HEAD~1
```

### 场景2: 回滚到特定版本

```bash
# === 1. 查看提交历史 ===
git log --oneline

# === 2. 回滚到特定提交 ===
git reset --hard <commit-hash>

# 或者回滚到标签
git reset --hard v2.0.0-before-upgrade
```

### 场景3: 撤销特定文件的修改

```bash
# === 撤销工作区的修改（未暂存） ===
git checkout -- <file>
git restore <file>  # 新命令

# === 取消暂存（已add但未commit） ===
git reset HEAD <file>
git restore --staged <file>  # 新命令

# === 撤销已提交的文件（创建反向提交） ===
git revert <commit-hash>
```

### 场景4: 使用reflog恢复

```bash
# === Git的"后悔药" ===
git reflog  # 查看所有操作历史

# === 找到想要恢复的操作，记下hash ===
# 示例输出:
# a1b2c3d HEAD@{0}: reset: moving to HEAD~1
# e4f5g6h HEAD@{1}: commit: 重要的提交

# === 恢复到"重要的提交" ===
git reset --hard e4f5g6h
```

---

## 🐛 故障排查

### 问题1: Git没有初始化

**症状**:
```
fatal: not a git repository
```

**解决**:
```bash
git init
```

### 问题2: 提交时提示配置用户信息

**症状**:
```
*** Please tell me who you are.
```

**解决**:
```bash
git config --global user.name "Your Name"
git config --global user.email "your@email.com"
```

### 问题3: 分支切换失败

**症状**:
```
error: Your local changes would be overwritten by checkout
```

**解决**:
```bash
# 方案1: 提交修改
git add .
git commit -m "保存当前工作"

# 方案2: 暂存修改
git stash
git checkout <branch>
git stash pop

# 方案3: 放弃修改
git checkout -- .
```

### 问题4: 合并冲突

**症状**:
```
CONFLICT (content): Merge conflict in file.txt
```

**解决**:
```bash
# 1. 查看冲突文件
git status

# 2. 手动编辑冲突文件
# 找到<<<<<<< ======= >>>>>>>标记
# 删除标记，保留想要的内容

# 3. 标记冲突已解决
git add <file>

# 4. 完成合并
git commit -m "解决合并冲突"

# 或者放弃合并
git merge --abort
```

### 问题5: 误删除了文件

**解决**:
```bash
# === 如果还没有commit ===
git checkout -- <file>

# === 如果已经commit但还没有推送 ===
git reset --hard HEAD~1

# === 如果想找回很久以前删除的文件 ===
git log --all -- <file>  # 找到删除前的提交
git checkout <commit-hash> -- <file>  # 恢复文件
```

### 问题6: 想要撤销刚才的操作

```bash
# === Git有"后悔药" - reflog ===
git reflog  # 查看所有操作历史

# === 找到想要恢复的操作，记下hash ===
git reset --hard <hash>
```

---

## 💡 最佳实践

### 1. 提交粒度控制

#### ✅ 好的提交粒度
```bash
# 一次提交只做一件事
git add pom.xml
git commit -m "build(deps): 升级Spring Boot到3.3.6"

git add src/main/java/com/okbug/platform/config/
git commit -m "refactor(config): javax → jakarta命名空间迁移"

git add src/main/resources/application.yml
git commit -m "config(spring): 移除ant-path-matcher配置"
```

#### ❌ 不好的提交粒度
```bash
# 一次提交做太多事
git add .
git commit -m "升级Spring Boot并修改所有配置和代码"
```

### 2. 提交前检查清单

```bash
# ✅ 提交前务必检查：

# 1. 查看修改的文件
git status

# 2. 查看具体修改内容
git diff

# 3. 确认没有多余的文件
# （如.class文件、日志文件等）

# 4. 编译和测试
mvn clean compile
mvn test

# 5. 撰写清晰的提交信息

# 6. 提交
git add <files>
git commit -m "清晰的提交信息"
```

### 3. 分支管理策略

```bash
# === 保护主分支 ===
# main/master分支应该是稳定的
# 所有开发都在功能分支进行

# === 功能分支开发 ===
git checkout -b feature/new-feature
# 开发...
git commit -m "feat: 添加新功能"
# 测试通过后合并
git checkout main
git merge feature/new-feature

# === 及时删除无用分支 ===
git branch -d feature/completed-feature
```

### 4. 标签管理策略

```bash
# === 版本号规范（语义化版本） ===
# 主版本号.次版本号.修订号
v3.0.0 - 重大版本升级
v3.1.0 - 新功能版本
v3.0.1 - Bug修复版本

# === 创建标签 ===
git tag -a v3.0.0 -m "正式版本: v3.0.0

主要特性:
- JDK 21支持
- Spring Boot 3.3.6
- Docker化开发环境
- 虚拟线程支持

发布日期: 2025-10-31
维护者: hanjor"

# === 特殊标签 ===
v2.0.0-before-upgrade  # 升级前备份
v3.0.0-milestone-1     # 里程碑1
v3.0.0-alpha           # Alpha版本
v3.0.0-beta            # Beta版本
```

### 5. .gitignore最佳实践

```bash
# === 项目的.gitignore已配置 ===

# Maven
target/

# IDEA
.idea/
*.iml

# Docker volumes（数据文件不要提交）
docker/mysql/data/
docker/redis/data/
docker/rabbitmq/data/
docker/.env  # 环境变量不要提交

# 日志文件
logs/
*.log

# 编译文件
*.class
*.jar
*.war

# 临时文件
*.tmp
*.bak
```

### 6. 定期维护

```bash
# === 清理本地仓库 ===
# 查看仓库大小
du -sh .git

# 垃圾回收
git gc

# 激进优化
git gc --aggressive --prune=now

# === 清理远程分支引用（如有远程仓库） ===
git remote prune origin

# === 查看大文件 ===
git rev-list --objects --all | \
  git cat-file --batch-check='%(objecttype) %(objectname) %(objectsize) %(rest)' | \
  sed -n 's/^blob //p' | \
  sort --numeric-sort --key=2 | \
  tail -10
```

---

## 🎨 实用Git技巧

### 1. 查看美化的提交历史

```bash
# === 单行图形化 ===
git log --oneline --graph --all

# === 详细图形化 ===
git log --graph --all --decorate --date=relative

# === 自定义格式 ===
git log --pretty=format:"%h - %an, %ar : %s" --graph

# === 创建别名（一次配置，永久使用） ===
git config --global alias.lg "log --oneline --graph --all --decorate"
# 使用: git lg
```

### 2. 搜索和过滤提交

```bash
# === 按作者搜索 ===
git log --author="hanjor"

# === 按时间范围 ===
git log --since="2 weeks ago"
git log --after="2025-10-01" --before="2025-10-31"

# === 按提交信息搜索 ===
git log --grep="升级"

# === 按文件搜索 ===
git log -- pom.xml
git log -- src/main/java/com/okbug/platform/config/

# === 按修改内容搜索 ===
git log -S "Spring Boot"  # 搜索添加或删除了"Spring Boot"的提交
```

### 3. 比较差异

```bash
# === 比较工作区和暂存区 ===
git diff

# === 比较暂存区和最后一次提交 ===
git diff --staged

# === 比较两个提交 ===
git diff v2.0.0 v3.0.0

# === 比较两个分支 ===
git diff main upgrade/jdk21-springboot3

# === 只显示文件名 ===
git diff --name-only

# === 统计修改 ===
git diff --stat
```

### 4. 暂存工作区（Stash）

```bash
# === 场景: 正在开发功能A，突然需要切换去修复Bug ===

# 1. 暂存当前工作
git stash
# 或添加描述
git stash save "功能A开发中，未完成"

# 2. 切换分支修复Bug
git checkout -b hotfix/urgent-bug
# 修复...
git commit -m "fix: 修复紧急Bug"

# 3. 切回原分支
git checkout feature/feature-A

# 4. 恢复工作
git stash list    # 查看暂存列表
git stash apply   # 恢复最新暂存（保留stash）
# 或
git stash pop     # 恢复并删除stash

# 5. 删除暂存
git stash drop stash@{0}  # 删除特定暂存
git stash clear           # 清空所有暂存
```

### 5. 修改提交历史（慎用）

```bash
# === 修改最后一次提交信息 ===
git commit --amend -m "新的提交信息"

# === 修改最后一次提交内容 ===
git add <忘记添加的文件>
git commit --amend --no-edit  # 不修改提交信息

# === 合并最近的提交（交互式rebase） ===
git rebase -i HEAD~3  # 合并最近3次提交
# 在编辑器中将pick改为squash（或s）

# ⚠️ 注意: 只对未推送的提交使用这些命令！
```

---

## 📚 相关资源

### 官方文档
- [Git官方文档](https://git-scm.com/doc)
- [Pro Git电子书](https://git-scm.com/book/zh/v2)

### 可视化工具
- **Windows**: 
  - GitKraken
  - SourceTree
  - TortoiseGit
- **IDEA内置Git**: 
  - VCS → Git → Show Git Log
  - Alt+9 打开Git面板

### 学习网站
- [Git在线练习](https://learngitbranching.js.org/?locale=zh_CN)
- [GitHub Git速查表](https://training.github.com/)

---

## 🎯 项目Git使用总结

### 必做操作

```bash
# 1. 初始化和配置
git init
git config --global user.name "Your Name"
git config --global user.email "your@email.com"

# 2. 首次提交和备份
git add .
git commit -m "初始提交: v3.0.0"
git tag -a v3.0.0 -m "正式版本"

# 3. 功能开发流程
git checkout -b feature/new-feature
# 开发...
git add <files>
git commit -m "feat: 添加新功能"
git checkout main
git merge feature/new-feature

# 4. 定期维护
git log --oneline --graph  # 查看历史
git gc                      # 垃圾回收
```

### 禁止操作

- ❌ 不要使用 `git reset --hard` 除非你确定要丢失修改
- ❌ 不要修改已推送的提交历史（本地可以）
- ❌ 不要提交敏感信息（密码、密钥等）
- ❌ 不要提交编译产物（target/、*.class等）

---

## 📞 获取帮助

### 命令行帮助

```bash
# 查看命令帮助
git help <command>
git <command> --help

# 示例
git help commit
git commit --help
```

### 相关文档

- **README.md** - 项目总览
- **A_Docker配置和常用命令.md** - Docker环境配置
- **B_JDK21和SpringBoot3升级总结.md** - 升级详情

### 联系方式

- **项目负责人**: hanjor
- **Email**: hanjor@qq.com

---

## 🎯 快速命令速查表

```bash
# === 基本操作 ===
git status                      # 查看状态
git add .                       # 添加所有变更
git commit -m "信息"            # 提交
git log --oneline --graph       # 查看历史

# === 分支操作 ===
git branch                      # 查看分支
git checkout -b <name>          # 创建并切换分支
git merge <branch>              # 合并分支
git branch -d <name>            # 删除分支

# === 标签操作 ===
git tag                         # 查看标签
git tag -a <name> -m "说明"     # 创建标签
git show <tag>                  # 查看标签详情

# === 撤销操作 ===
git checkout -- <file>          # 撤销文件修改
git reset HEAD <file>           # 取消暂存
git reset --soft HEAD~1         # 撤销提交（保留修改）
git reset --hard HEAD~1         # 撤销提交（⚠️ 丢失修改）

# === 暂存操作 ===
git stash                       # 暂存工作
git stash list                  # 查看暂存
git stash pop                   # 恢复并删除暂存

# === 查看操作 ===
git diff                        # 查看修改
git log --author="hanjor"       # 查看作者提交
git log --grep="升级"           # 搜索提交信息
```

---

**文档版本**: v1.0  
**最后更新**: 2025-10-31  
**维护者**: hanjor  
**项目**: Medical Model Platform

---

*祝Git使用顺利！* 🔐✨

