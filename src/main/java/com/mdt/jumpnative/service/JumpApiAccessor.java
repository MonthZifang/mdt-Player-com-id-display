package com.mdt.jumpnative.service;

import java.lang.reflect.Method;

public final class JumpApiAccessor {
    private final String jumpPluginClassName;
    private final String jumpApiMethodName;

    public JumpApiAccessor(String jumpPluginClassName, String jumpApiMethodName) {
        this.jumpPluginClassName = jumpPluginClassName;
        this.jumpApiMethodName = jumpApiMethodName;
    }

    public String getOrCreateComId(String uuid) {
        try {
            Class<?> pluginClass = Class.forName(jumpPluginClassName);
            Method getApiMethod = pluginClass.getMethod(jumpApiMethodName);
            Object api = getApiMethod.invoke(null);
            if (api == null) return null;

            Method getOrCreate = api.getClass().getMethod("getOrCreate", String.class);
            Object record = getOrCreate.invoke(api, uuid);
            if (record == null) return null;

            Method getComId = record.getClass().getMethod("getComId");
            Object value = getComId.invoke(record);
            return value == null ? null : value.toString();
        } catch (Throwable throwable) {
            return null;
        }
    }
}
