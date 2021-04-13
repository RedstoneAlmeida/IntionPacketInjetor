package com.intion.injetor;

import cn.nukkit.level.GameRule;
import cn.nukkit.level.GameRules;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.plugin.PluginBase;
import com.intion.injetor.behavior.BehaviorPackManager;
import com.intion.injetor.network.ResourcePacksInfoPacket;
import sun.reflect.ConstructorAccessor;

import java.lang.reflect.*;
import java.util.EnumMap;

public class Loader extends PluginBase {

    private BehaviorPackManager behaviorPackManager;

    private final EnumMap<GameRule, GameRules.Value> gameRules = new EnumMap<>(GameRule.class);

    @Override
    public void onLoad() {
        this.getServer().getNetwork().registerPacket(ProtocolInfo.RESOURCE_PACKS_INFO_PACKET, ResourcePacksInfoPacket.class);
    }

    @Override
    public void onEnable() {
        this.behaviorPackManager = new BehaviorPackManager(this, this.getDataFolder() + "/behavior_packs/");
        this.getServer().getPluginManager().registerEvents(new PacketInjetor(this), this);

    }

    private void makeAccessible(Field field) throws Exception {
        field.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~ Modifier.FINAL);
    }

    public BehaviorPackManager getBehaviorPackManager() {
        return behaviorPackManager;
    }
}
