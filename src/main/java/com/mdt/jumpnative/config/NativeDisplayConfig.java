package com.mdt.jumpnative.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Objects;
import java.util.Properties;

public final class NativeDisplayConfig {
    private static final String DEFAULT_RESOURCE = "plugin-config.properties";
    private static final String DEFAULT_FILE_NAME = "plugin-config.properties";

    private final File dataRoot;
    private final boolean showInPlayerList;
    private final boolean showJoinMessage;
    private final boolean debugLogging;
    private final String playerListSuffixFormat;
    private final String joinMessageFormat;
    private final String jumpPluginClassName;
    private final String jumpApiMethodName;

    private NativeDisplayConfig(
        File dataRoot,
        boolean showInPlayerList,
        boolean showJoinMessage,
        boolean debugLogging,
        String playerListSuffixFormat,
        String joinMessageFormat,
        String jumpPluginClassName,
        String jumpApiMethodName
    ) {
        this.dataRoot = dataRoot;
        this.showInPlayerList = showInPlayerList;
        this.showJoinMessage = showJoinMessage;
        this.debugLogging = debugLogging;
        this.playerListSuffixFormat = playerListSuffixFormat;
        this.joinMessageFormat = joinMessageFormat;
        this.jumpPluginClassName = jumpPluginClassName;
        this.jumpApiMethodName = jumpApiMethodName;
    }

    public static NativeDisplayConfig load(File dataRoot) throws IOException {
        if (!dataRoot.exists() && !dataRoot.mkdirs()) {
            throw new IOException("无法创建数据目录: " + dataRoot);
        }

        File configFile = new File(dataRoot, DEFAULT_FILE_NAME);
        if (!configFile.exists()) {
            copyDefaultConfig(configFile);
        }

        Properties properties = new Properties();
        try (InputStream inputStream = new FileInputStream(configFile);
             InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
            properties.load(reader);
        }

        return new NativeDisplayConfig(
            dataRoot,
            readBoolean(properties, "在玩家列表显示", true),
            readBoolean(properties, "进服提示显示", true),
            readBoolean(properties, "调试日志", false),
            read(properties, "玩家列表后缀格式", " [accent][%s][]"),
            read(properties, "进服提示格式", "[lime]%s[] 已进入服务器，comid: [accent]%s[]"),
            read(properties, "跳转插件类", "com.mdt.jump.JumpComIdPlugin"),
            read(properties, "跳转API方法", "getApi")
        );
    }

    private static void copyDefaultConfig(File configFile) throws IOException {
        try (InputStream inputStream = NativeDisplayConfig.class.getClassLoader().getResourceAsStream(DEFAULT_RESOURCE)) {
            if (inputStream == null) {
                throw new IOException("找不到默认配置资源: " + DEFAULT_RESOURCE);
            }
            File parent = configFile.getParentFile();
            if (parent != null && !parent.exists() && !parent.mkdirs()) {
                throw new IOException("无法创建配置目录: " + parent);
            }
            Files.copy(inputStream, configFile.toPath());
        }
    }

    private static String read(Properties properties, String key, String defaultValue) {
        String value = properties.getProperty(key);
        return value == null || value.trim().isEmpty() ? defaultValue : value.trim();
    }

    private static boolean readBoolean(Properties properties, String key, boolean defaultValue) {
        String value = properties.getProperty(key);
        return value == null || value.trim().isEmpty() ? defaultValue : Boolean.parseBoolean(value.trim());
    }

    public File dataRoot() {
        return dataRoot;
    }

    public boolean showInPlayerList() {
        return showInPlayerList;
    }

    public boolean showJoinMessage() {
        return showJoinMessage;
    }

    public boolean debugLogging() {
        return debugLogging;
    }

    public String playerListSuffixFormat() {
        return playerListSuffixFormat;
    }

    public String joinMessageFormat() {
        return joinMessageFormat;
    }

    public String jumpPluginClassName() {
        return jumpPluginClassName;
    }

    public String jumpApiMethodName() {
        return jumpApiMethodName;
    }
}
