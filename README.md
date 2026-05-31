# 简呼 Call

面向老年人的极简拨号 Android App。家人提前设置联系人、称谓、号码和头像，老人只需要点击熟悉的大卡片即可拨号。

这是最终可打包版本：使用原生 Android Java + Gradle，不再依赖 Kivy / Buildozer / python-for-android，因此不会遇到 CPython 编译失败问题。

## 已实现

- 原生 Android 项目，可直接生成 APK。
- 主界面：全屏柔和背景 + 玻璃态联系人卡片列表。
- 联系人卡片：圆形头像、大号中文称谓、电话图标。
- 整张卡片可点击。
- 点击后 TTS 播报“正在呼叫 [称谓]”，随后直接拨号。
- 没有 `CALL_PHONE` 权限时自动降级打开系统拨号盘。
- 长按顶部标题“简呼”3 秒进入家属验证。
- 验证题：`3 + 5 = ?`
- 家属设置页：添加、删除、编辑称谓、真实姓名、电话号码、头像。
- 本地 JSON 保存联系人数据。

## GitHub Actions 打包

上传到 GitHub 后，仓库根目录必须直接看到：

```text
settings.gradle
build.gradle
app/build.gradle
app/src/main/AndroidManifest.xml
.github/workflows/android-debug.yml
```

然后：

1. 打开仓库 `Actions`。
2. 选择 `Android Debug APK`。
3. 点击 `Run workflow`。
4. 构建完成后下载 artifact：`jianhu-call-debug-apk`。
5. 解压后得到 debug APK。

## 本地打包

如果你安装了 Android Studio 或 Android SDK + Gradle：

```bash
gradle :app:assembleDebug
```

APK 输出位置：

```text
app/build/outputs/apk/debug/app-debug.apk
```

## 使用方式

1. 安装 APK。
2. 打开 App。
3. 长按顶部“简呼”3 秒。
4. 输入 `8` 进入设置。
5. 填写联系人称谓和号码，可选头像。
6. 保存后返回主界面。
7. 老人点击联系人卡片即可拨号。

## 注意

- 直接拨号需要 `CALL_PHONE` 权限。
- 如果用户拒绝权限，App 会打开系统拨号盘而不是直接拨出。
- 头像通过 Android 系统文件选择器选择，并持久化 URI 读取权限。
