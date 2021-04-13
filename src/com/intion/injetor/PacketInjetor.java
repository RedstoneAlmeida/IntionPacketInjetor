package com.intion.injetor;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerResourcePackEvent;
import cn.nukkit.event.player.PlayerToggleSneakEvent;
import cn.nukkit.event.server.DataPacketReceiveEvent;
import cn.nukkit.event.server.DataPacketSendEvent;
import cn.nukkit.level.GameRule;
import cn.nukkit.network.protocol.*;
import cn.nukkit.resourcepacks.ResourcePack;
import cn.nukkit.resourcepacks.ResourcePackManager;
import cn.nukkit.resourcepacks.ZippedResourcePack;
import com.google.gson.JsonObject;
import com.intion.injetor.behavior.BehaviorPack;
import com.intion.injetor.behavior.BehaviorPackManager;
import com.intion.injetor.behavior.ZippedBehaviorPack;
import com.intion.injetor.network.ResourcePacksInfoPacket;
import net.minidev.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class PacketInjetor implements Listener {

    private Loader plugin;
    private BehaviorPackManager manager;

    public PacketInjetor(Loader plugin)
    {
        this.plugin = plugin;
        manager = plugin.getBehaviorPackManager();
        //packManager = new ResourcePackManager(new File(plugin.getDataFolder() + "/behavior_packs/"));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPacketSend(DataPacketSendEvent event)
    {
        DataPacket packet = event.getPacket();
        if (packet instanceof StartGamePacket)
        {
            ((StartGamePacket) packet).gameRules.setGameRule(GameRule.EXPERIMENTAL_GAMEPLAY, true);
        } else if (packet instanceof ResourcePackStackPacket)
        {
            System.out.println("aaaaa2");
            ((ResourcePackStackPacket) packet).behaviourPackStack = manager.getList();
            ((ResourcePackStackPacket) packet).resourcePackStack = plugin.getServer().getResourcePackManager().getResourceStack();
            ((ResourcePackStackPacket) packet).isExperimental = manager.isHasClientScripts();
        }
        if (packet.toString().contains("ResourcePack"))
            System.out.println("Send: " + packet.toString());
    }

    @EventHandler
    public void onShift(PlayerToggleSneakEvent event)
    {
        Player player = event.getPlayer();
        ScriptCustomEventPacket packet = new ScriptCustomEventPacket();
        packet.eventName = "minecraft:ui_event";
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DataOutputStream a = new DataOutputStream(out);
        try {
            a.writeUTF("mod:ui_event");
        } catch (IOException e) {
            e.printStackTrace();
        }

        packet.eventData = out.toByteArray();
        player.dataPacket(packet);
    }

    @EventHandler
    public void onResourcePack(PlayerResourcePackEvent event)
    {
        ResourcePacksInfoPacket packet = new ResourcePacksInfoPacket();
        packet.behaviourPackEntries = manager.getList();
        packet.resourcePackEntries = this.plugin.getServer().getResourcePackManager().getResourceStack();
        packet.scripting = manager.isHasClientScripts();
        packet.mustAccept = this.plugin.getServer().getForceResources();
        event.setPacket(packet);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPacketReceive(DataPacketReceiveEvent event)
    {
        DataPacket packet = event.getPacket();
        Player player = event.getPlayer();

        if (packet instanceof ScriptCustomEventPacket)
        {
            String eventName = ((ScriptCustomEventPacket) packet).eventName;
            String data = new String(((ScriptCustomEventPacket) packet).eventData);
            System.out.println(String.format("%s -> %s", eventName, data));
        } else if (packet instanceof ResourcePackClientResponsePacket && ((ResourcePackClientResponsePacket) packet).responseStatus == ResourcePackClientResponsePacket.STATUS_SEND_PACKS)
        {
            System.out.println("aaaaaaa");
            List<ResourcePackClientResponsePacket.Entry> provided = new ArrayList<>();
            for (ResourcePackClientResponsePacket.Entry entry : ((ResourcePackClientResponsePacket) packet).packEntries)
            {
                BehaviorPack pack = manager.getPackById(entry.uuid);
                if (pack == null)
                    continue;
                ResourcePackDataInfoPacket resourcePackDataInfoPacket = new ResourcePackDataInfoPacket();
                resourcePackDataInfoPacket.packId = pack.getPackId();
                resourcePackDataInfoPacket.maxChunkSize = 1048576;
                resourcePackDataInfoPacket.chunkCount = pack.getPackSize() / resourcePackDataInfoPacket.maxChunkSize;
                resourcePackDataInfoPacket.compressedPackSize = pack.getPackSize();
                resourcePackDataInfoPacket.sha256 = pack.getSha256();
                resourcePackDataInfoPacket.type = ResourcePackDataInfoPacket.TYPE_BEHAVIOR;
                player.dataPacket(resourcePackDataInfoPacket);
                provided.add(entry);
            }
            ResourcePackClientResponsePacket.Entry[] entries = new ResourcePackClientResponsePacket.Entry[this.plugin.getServer().getResourcePackManager().getResourceStack().length];
            int i = 0;
            for (ResourcePack pack : this.plugin.getServer().getResourcePackManager().getResourceStack())
            {
                entries[i] = new ResourcePackClientResponsePacket.Entry(pack.getPackId(), pack.getPackVersion());
                i++;
            }
            ((ResourcePackClientResponsePacket) packet).packEntries = entries;
        } else if (packet instanceof ResourcePackChunkRequestPacket)
        {
            System.out.println("aaaaaaaaaaaaaaaa");
            BehaviorPack pack = manager.getPackById(((ResourcePackChunkRequestPacket) packet).packId);
            if (pack == null)
                return;
            ResourcePackChunkDataPacket resourcePackChunkDataPacket = new ResourcePackChunkDataPacket();
            resourcePackChunkDataPacket.packId = pack.getPackId();
            resourcePackChunkDataPacket.chunkIndex = ((ResourcePackChunkRequestPacket) packet).chunkIndex;
            resourcePackChunkDataPacket.data = pack.getPackChunk(1048576 * ((ResourcePackChunkRequestPacket) packet).chunkIndex, 1048576);
            resourcePackChunkDataPacket.progress = (1048576L * ((ResourcePackChunkRequestPacket) packet).chunkIndex);
            player.dataPacket(resourcePackChunkDataPacket);
            event.setCancelled(true);
        }

        if (packet.toString().contains("ResourcePack"))
            System.out.println("Receive: " + packet.toString());
    }

    public List<ResourcePackClientResponsePacket.Entry> getEntries(List<ResourcePackClientResponsePacket.Entry> entry1, List<ResourcePackClientResponsePacket.Entry> entry2)
    {
        for (ResourcePackClientResponsePacket.Entry entry : entry2)
        {
            if (entry1.contains(entry))
                entry1.remove(entry);
            else
                entry1.add(entry);
        }
        return entry1;
    }

}
