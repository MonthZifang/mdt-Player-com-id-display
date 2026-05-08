<div align="center">
  <a href="https://github.com/MonthZifang/YUEYUEDAO-TECH">
    <img src="./md/logo.png" alt="月月岛科技 Logo" width="720" />
  </a>

  <p><strong>月月岛科技维护 MDT Jump Plugin Native Display</strong></p>

  <p>
    <a href="https://github.com/MonthZifang/YUEYUEDAO-TECH"><strong>查看月月岛科技详情</strong></a>
  </p>
</div>

# MDT 跳转插件原版显示模块

这个插件用于把 `mdt-jump-plugin` 生成的四位 `com id` 显示到玩家列表和进服提示中。

## 配置文件

首次启动后会生成：

```text
config/mods/config/mdt-jump-plugin-native/plugin-config.properties
```

主要配置：

- `在玩家列表显示`
- `进服提示显示`
- `玩家列表后缀格式`
- `进服提示格式`
- `跳转插件类`
- `跳转API方法`

## 命令

- `comid-display-reload`
- `comid-display-status`

## 构建

```powershell
.\gradlew.bat jar
```

输出：

```text
build/libs/mdt-jump-plugin-native.jar
dist/mdt-jump-plugin-native.jar
```
