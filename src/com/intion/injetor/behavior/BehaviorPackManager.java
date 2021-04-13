package com.intion.injetor.behavior;

import cn.nukkit.Server;
import cn.nukkit.plugin.PluginLogger;
import cn.nukkit.resourcepacks.ResourcePack;
import cn.nukkit.utils.Config;
import com.google.common.io.Files;
import com.intion.injetor.Loader;
import com.sun.javaws.exceptions.InvalidArgumentException;

import java.io.File;
import java.util.*;

public class BehaviorPackManager {

    private Loader plugin;

    private String path;

    private BehaviorPack[] behaviorPacks;
    private Map<UUID, BehaviorPack> uuidList = new HashMap<>();

    private boolean hasClientScripts = false;

    public BehaviorPackManager(Loader plugin, String path) {
        this.plugin = plugin;
        this.path = path;
        PluginLogger pluginLogger = plugin.getLogger();
        File file = new File(path);
        if (!file.exists())
        {
            pluginLogger.debug("Behavior packs path " + path + " does not exist creating directory");
            file.mkdir();
        } else if (!file.isDirectory())
        {
            try {
                throw new Exception("Behavior packs path " + path + "exists and is not a directory");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        plugin.saveResource("behavior_packs.yml");
        Config config = new Config(plugin.getDataFolder() + "/behavior_packs.yml", Config.YAML);
        pluginLogger.info("Loading behavior packs...");

        List<BehaviorPack> loadedResourcePacks = new ArrayList<>();
        if (file.listFiles() != null) {
            for (File pack : file.listFiles()) {
                try {
                    BehaviorPack resourcePack = null;

                    if (!pack.isDirectory()) { //directory resource packs temporarily unsupported
                        switch (Files.getFileExtension(pack.getName())) {
                            case "zip":
                            case "mcpack":
                                resourcePack = new ZippedBehaviorPack(pack);
                                break;
                            default:
                                Server.getInstance().getLogger().warning(Server.getInstance().getLanguage()
                                        .translateString("nukkit.resources.unknown-format", pack.getName()));
                                break;
                        }
                    }

                    if (resourcePack != null) {
                        loadedResourcePacks.add(resourcePack);
                        this.uuidList.put(resourcePack.getPackId(), resourcePack);

                        if (resourcePack.hasClientScripts()) {
                            this.hasClientScripts = true;
                        }
                    }
                } catch (IllegalArgumentException e) {
                    Server.getInstance().getLogger().warning(Server.getInstance().getLanguage()
                            .translateString("nukkit.resources.fail", pack.getName(), e.getMessage()));
                }
            }
        }

        this.behaviorPacks = loadedResourcePacks.toArray(new BehaviorPack[0]);
        pluginLogger.info("Success loaded " + loadedResourcePacks.size() + " behavior packs");
    }

    public BehaviorPack getPackById(UUID uuid)
    {
        if (this.uuidList.containsKey(uuid))
            return this.uuidList.get(uuid);
        return null;
    }

    public boolean isHasClientScripts() {
        return hasClientScripts;
    }

    public BehaviorPack[] getList()
    {
        return this.behaviorPacks;
    }

    public Map<UUID, BehaviorPack> getUuidList() {
        return uuidList;
    }

    public String getPath() {
        return path;
    }
}
