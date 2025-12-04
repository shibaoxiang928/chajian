# Gradle Wrapper

这个目录包含Gradle Wrapper的文件。

## 重要文件

- `gradle-wrapper.properties` - Wrapper配置文件，指定Gradle版本
- `gradle-wrapper.jar` - Wrapper可执行JAR文件（需要下载）

## 如果缺少 gradle-wrapper.jar

如果 `gradle-wrapper.jar` 文件不存在，可以通过以下方式获取：

### 方法一：使用Gradle命令生成（推荐）

```bash
# 如果系统已安装Gradle
gradle wrapper

# 或者指定版本
gradle wrapper --gradle-version 8.2
```

### 方法二：手动下载

1. 访问：https://raw.githubusercontent.com/gradle/gradle/v8.2.0/gradle/wrapper/gradle-wrapper.jar
2. 下载文件并保存到 `gradle/wrapper/gradle-wrapper.jar`

### 方法三：让GitHub Actions自动生成

GitHub Actions在运行时如果没有找到wrapper jar，会尝试自动下载。如果失败，可以在workflow中添加：

```yaml
- name: Generate Gradle Wrapper
  run: |
    if [ ! -f gradle/wrapper/gradle-wrapper.jar ]; then
      gradle wrapper --gradle-version 8.2
    fi
```

## 注意事项

- `gradlew` 文件需要有执行权限（Linux/Mac）
- `gradlew.bat` 是Windows版本
- Wrapper会自动下载指定版本的Gradle（首次运行时）

