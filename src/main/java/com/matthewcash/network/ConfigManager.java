package com.matthewcash.network;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;

public class ConfigManager {
    public static CommentedFileConfig config = CommentedFileConfig
        .builder(ProxyCore.dataDirectory.resolve("config.toml").toFile())
        .defaultData(
            ProxyCore.class.getResource(
                "/config.toml"
            )
        )
        .autosave()
        .preserveInsertionOrder()
        .sync()
        .build();

    public static void loadConfig() {
        ProxyCore.dataDirectory.toFile().mkdirs();

        config.load();
    }
}
