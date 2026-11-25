# 拍摄道具服装核对助手（个人自用，不上架）

安卓本地小工具：**今日清单 + 套用模板 + 快速备忘 + 三个闹钟**。离线可用，数据只存在手机上。

## 跑起来

1. Android Studio 打开本目录 → Sync
2. Run（minSdk 26 / Android 8.0+）
3. 首次启动会申请通知权限，务必允许，否则闹钟不弹

## 目录

```
app/src/main/java/com/gua/props/
├── MainActivity.kt        入口 + 页面切换
├── db/                    建表 + 三个 DAO（只有这层写 SQL）
├── model/                 三个数据类
├── ui/                    四个页面 + 两个弹窗（纯展示，不碰数据库）
├── alarm/                 闹钟排期 / 到点通知 / 开机重排
└── util/                  日期 + 偏好设置
```

## 数据库

`/data/data/com.gua.props/databases/shoot.db`，三张表：`plan_item` / `tpl_item` / `memo`。

**不要提交**：`*.db`、`*.zip`、`*.jks`（已在 .gitignore 里）。

## 设置页两个必须做的动作

- 允许通知
- 把 App 加入电池优化白名单（小米/华为/OPPO 尤其重要，否则闹钟会被系统杀掉）
