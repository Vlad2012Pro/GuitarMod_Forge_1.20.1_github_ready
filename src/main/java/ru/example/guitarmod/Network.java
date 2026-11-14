package ru.example.guitarmod;

import java.util.UUID;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class Network {
    private static final String VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
        new ResourceLocation(GuitarMod.MODID, "main"), () -> VERSION, VERSION::equals, VERSION::equals);
    private static int id = 0;
    public static void init() {
        CHANNEL.messageBuilder(AudioStart.class, id++, NetworkDirection.PLAY_TO_CLIENT)
            .encoder(AudioStart::encode).decoder(AudioStart::decode)
            .consumerMainThread((m,c) -> { ClientAudio.startRemote(m.owner, m.sampleRate, m.channels); c.get().setPacketHandled(true); }).add();
        CHANNEL.messageBuilder(AudioChunk.class, id++, NetworkDirection.PLAY_TO_CLIENT)
            .encoder(AudioChunk::encode).decoder(AudioChunk::decode)
            .consumerMainThread((m,c) -> { ClientAudio.acceptRemote(m.owner, m.data); c.get().setPacketHandled(true); }).add();
        CHANNEL.messageBuilder(AudioStop.class, id++, NetworkDirection.PLAY_TO_CLIENT)
            .encoder(AudioStop::encode).decoder(AudioStop::decode)
            .consumerMainThread((m,c) -> { ClientAudio.stopRemote(m.owner); c.get().setPacketHandled(true); }).add();

        CHANNEL.messageBuilder(AudioStart.class, id++, NetworkDirection.PLAY_TO_SERVER)
            .encoder(AudioStart::encode).decoder(AudioStart::decode)
            .consumerNetworkThread((m,c) -> { ServerPlayer p=c.get().getSender(); if(p!=null) relay(p,m); c.get().setPacketHandled(true); }).add();
        CHANNEL.messageBuilder(AudioChunk.class, id++, NetworkDirection.PLAY_TO_SERVER)
            .encoder(AudioChunk::encode).decoder(AudioChunk::decode)
            .consumerNetworkThread((m,c) -> { ServerPlayer p=c.get().getSender(); if(p!=null) relay(p,m); c.get().setPacketHandled(true); }).add();
        CHANNEL.messageBuilder(AudioStop.class, id++, NetworkDirection.PLAY_TO_SERVER)
            .encoder(AudioStop::encode).decoder(AudioStop::decode)
            .consumerNetworkThread((m,c) -> { ServerPlayer p=c.get().getSender(); if(p!=null) relay(p,m); c.get().setPacketHandled(true); }).add();
    }
    private static void relay(ServerPlayer sender, Object message) {
        Vec3 pos=sender.position();
        for(ServerPlayer target: sender.serverLevel().players())
            if(target != sender && target.distanceToSqr(pos) <= 400.0)
                CHANNEL.send(PacketDistributor.PLAYER.with(() -> target), message);
    }
    public record AudioStart(UUID owner,int sampleRate,int channels) {
        void encode(net.minecraft.network.FriendlyByteBuf b){b.writeUUID(owner);b.writeInt(sampleRate);b.writeInt(channels);}
        static AudioStart decode(net.minecraft.network.FriendlyByteBuf b){return new AudioStart(b.readUUID(),b.readInt(),b.readInt());}
    }
    public record AudioChunk(UUID owner,byte[] data) {
        void encode(net.minecraft.network.FriendlyByteBuf b){b.writeUUID(owner);b.writeVarInt(data.length);b.writeBytes(data);}
        static AudioChunk decode(net.minecraft.network.FriendlyByteBuf b){UUID owner=b.readUUID();int n=b.readVarInt();if(n<0||n>24000)throw new IllegalArgumentException("Bad audio chunk");byte[] d=new byte[n];b.readBytes(d);return new AudioChunk(owner,d);}
    }
    public record AudioStop(UUID owner) {
        void encode(net.minecraft.network.FriendlyByteBuf b){b.writeUUID(owner);}
        static AudioStop decode(net.minecraft.network.FriendlyByteBuf b){return new AudioStop(b.readUUID());}
    }
}
