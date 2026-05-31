# 上传说明

把本目录中的所有文件上传到 GitHub 仓库根目录。

仓库根目录应直接看到：

```text
settings.gradle
build.gradle
app/
.github/
README.md
```

不要把整个 `call_android_final` 文件夹套进仓库，否则 Actions 找不到 Gradle 项目。
