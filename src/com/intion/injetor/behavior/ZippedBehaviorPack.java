package com.intion.injetor.behavior;

import cn.nukkit.resourcepacks.ZippedResourcePack;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.File;
import java.util.UUID;

public class ZippedBehaviorPack extends ZippedResourcePack implements BehaviorPack {

    private boolean hasClientScripts = false;

    public ZippedBehaviorPack(File file) {
        super(file);
        if (this.manifest.get("modules").isJsonArray())
        {
            for (JsonElement element : this.manifest.get("modules").getAsJsonArray())
            {
                if (element.getAsJsonObject().get("type") != null && element.getAsJsonObject().get("type").getAsString().equals("client_data"))
                {
                    this.hasClientScripts = true;
                }
            }
        }
    }

    @Override
    public boolean hasClientScripts() {
        return this.hasClientScripts;
    }
}
