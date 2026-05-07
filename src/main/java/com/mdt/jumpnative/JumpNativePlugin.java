package com.mdt.jumpnative;

import arc.Events;
import arc.util.CommandHandler;
import arc.util.Log;
import com.mdt.jumpnative.config.NativeDisplayConfig;
import com.mdt.jumpnative.service.JumpApiAccessor;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import mindustry.game.EventType.PlayerJoin;
import mindustry.game.EventType.PlayerLeave;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.mod.Plugin;

public final class JumpNativePlugin extends Plugin {
    private static final String CONFIG_DIR_NAME = "mdt-jump-plugin-native";

    private NativeDisplayConfig config;
    private JumpApiAccessor accessor;
    private final Map<String, String> baseNames = new HashMap<String, String>();

    @Override
    public void init() {
        try {
            File modsRoot = new File(mindustry.Vars.dataDirectory.absolutePath(), "mods");
            File dataRoot = new File(new File(modsRoot, "config"), CONFIG_DIR_NAME);
            config = NativeDisplayConfig.load(dataRoot);
            accessor = new JumpApiAccessor(config.jumpPluginClassName(), config.jumpApiMethodName());

            Events.on(PlayerJoin.class, event -> handleJoin(event.player));
            Events.on(PlayerLeave.class, event -> handleLeave(event.player));

            Log.info("MDT Jump Plugin Native Display loaded.");
        } catch (Exception exception) {
            throw new RuntimeException("MDT Jump Plugin Native Display 初始化失败。", exception);
        }
    }

    @Override
    public void registerServerCommands(CommandHandler handler) {
        handler.register("comid-display-reload", "重新加载显示配置并刷新在线玩家名称。", args -> {
            try {
                reloadConfig();
                refreshOnlinePlayers();
                Log.info("显示配置已重新加载。");
            } catch (Exception exception) {
                Log.err("重新加载失败: @", exception.getMessage());
            }
        });

        handler.register("comid-display-status", "查看显示插件状态。", args -> {
            Log.info(
                "list=@ join=@ debug=@ config=@",
                config.showInPlayerList(),
                config.showJoinMessage(),
                config.debugLogging(),
                config.dataRoot()
            );
        });
    }

    private void reloadConfig() throws Exception {
        File modsRoot = new File(mindustry.Vars.dataDirectory.absolutePath(), "mods");
        File dataRoot = new File(new File(modsRoot, "config"), CONFIG_DIR_NAME);
        config = NativeDisplayConfig.load(dataRoot);
        accessor = new JumpApiAccessor(config.jumpPluginClassName(), config.jumpApiMethodName());
    }

    private void refreshOnlinePlayers() {
        for (Player player : Groups.player) {
            String uuid = resolveUuid(player);
            if (uuid == null) continue;
            String baseName = baseNames.containsKey(uuid) ? baseNames.get(uuid) : stripSuffix(player.name);
            baseNames.put(uuid, baseName);
            applyName(player, baseName, resolveComId(uuid));
        }
    }

    private void handleJoin(Player player) {
        String uuid = resolveUuid(player);
        if (uuid == null) {
            return;
        }

        String baseName = stripSuffix(player.name);
        baseNames.put(uuid, baseName);
        String comId = resolveComId(uuid);
        applyName(player, baseName, comId);

        if (config.showJoinMessage() && comId != null) {
            Call.sendMessage(String.format(config.joinMessageFormat(), baseName, comId));
        }
    }

    private void handleLeave(Player player) {
        String uuid = resolveUuid(player);
        if (uuid != null) {
            baseNames.remove(uuid);
        }
    }

    private void applyName(Player player, String baseName, String comId) {
        if (config.showInPlayerList() && comId != null) {
            player.name = baseName + String.format(config.playerListSuffixFormat(), comId);
        } else {
            player.name = baseName;
        }
    }

    private String resolveComId(String uuid) {
        String value = accessor.getOrCreateComId(uuid);
        if (value == null && config.debugLogging()) {
            Log.warn("无法获取 comid: @", uuid);
        }
        return value;
    }

    private String resolveUuid(Player player) {
        try {
            Method method = player.getClass().getMethod("uuid");
            Object value = method.invoke(player);
            if (value != null) {
                return value.toString();
            }
        } catch (ReflectiveOperationException ignored) {
        }

        try {
            Field field = player.getClass().getField("uuid");
            Object value = field.get(player);
            if (value != null) {
                return value.toString();
            }
        } catch (ReflectiveOperationException ignored) {
        }

        return null;
    }

    private String stripSuffix(String name) {
        if (name == null) return "";
        int marker = name.indexOf("[accent][");
        return marker >= 0 ? name.substring(0, marker).trim() : name.trim();
    }
}
