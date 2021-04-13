package com.intion.injetor.network;

import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.resourcepacks.ResourcePack;
import com.intion.injetor.behavior.BehaviorPack;

import java.util.Arrays;

public class ResourcePacksInfoPacket extends cn.nukkit.network.protocol.ResourcePacksInfoPacket {

    public static final byte NETWORK_ID = 6;
    public boolean mustAccept;
    public boolean scripting;
    public ResourcePack[] behaviourPackEntries = new BehaviorPack[0];
    public ResourcePack[] resourcePackEntries = new ResourcePack[0];

    public ResourcePacksInfoPacket() {
    }

    @Override
    public byte pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
        this.mustAccept = this.getBoolean();
        this.scripting = this.getBoolean();

        int behaviorPackCount = this.getLShort();
        while (behaviorPackCount-- > 0)
        {
            this.getString();
            this.getString();
            this.getLLong();
            this.getString();
            this.getString();
            this.getString();
            this.getBoolean();
        }

        int resourcePackCount = this.getLShort();
        while (resourcePackCount-- > 0)
        {
            this.getString();
            this.getString();
            this.getLLong();
            this.getString();
            this.getString();
            this.getString();
            this.getBoolean();
            this.getBoolean();
        }

        System.out.println("W?");

    }

    @Override
    public void encode() {
        this.reset();
        this.putBoolean(this.mustAccept);
        this.putBoolean(this.scripting);

        encodePacks(this.behaviourPackEntries, true);
        encodePacks(this.resourcePackEntries, false);
        this.putBoolean(false);
    }

    private void encodePacks(ResourcePack[] packs, boolean scripting) {
        this.putLShort(packs.length);
        for (ResourcePack entry : packs) {
            this.putString(entry.getPackId().toString());
            this.putString(entry.getPackVersion());
            this.putLLong(entry.getPackSize());
            this.putString(""); // encryption key
            this.putString(""); // sub-pack name
            this.putString(""); // content identity
            this.putBoolean(scripting);
        }
    }

    public String toString() {
        return "ResourcePacksInfoPacket(mustAccept=" + this.mustAccept + ", scripting=" + this.scripting + ", behaviourPackEntries=" + Arrays.deepToString(this.behaviourPackEntries) + ", resourcePackEntries=" + Arrays.deepToString(this.resourcePackEntries) + ")";
    }
}
