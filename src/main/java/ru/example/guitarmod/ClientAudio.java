package ru.example.guitarmod;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import javax.sound.sampled.*;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

public final class ClientAudio {
    private static final Map<UUID, SourceDataLine> REMOTE = new ConcurrentHashMap<>();
    private static final ExecutorService AUDIO = Executors.newCachedThreadPool(r -> {
        Thread t=new Thread(r,"GuitarMod-Audio"); t.setDaemon(true); return t;
    });
    private static volatile SourceDataLine localLine;
    private static volatile UUID localId;

    public static void playFile(File file) {
        stopLocal();
        AUDIO.execute(() -> {
            UUID id = Minecraft.getInstance().player == null ? UUID.randomUUID() : Minecraft.getInstance().player.getUUID();
            try (AudioInputStream in = AudioSystem.getAudioInputStream(file)) {
                AudioFormat target = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 2, 4, 44100, false);
                try (AudioInputStream pcm = AudioSystem.getAudioInputStream(target, in)) {
                    SourceDataLine line = AudioSystem.getSourceDataLine(target);
                    line.open(target); line.start(); localLine=line; localId=id;
                    Network.CHANNEL.sendToServer(new Network.AudioStart(id,44100,2));
                    byte[] buf=new byte[12000]; int n;
                    while((n=pcm.read(buf))!=-1 && localLine==line) {
                        line.write(buf,0,n);
                        byte[] part=Arrays.copyOf(buf,n);
                        Network.CHANNEL.sendToServer(new Network.AudioChunk(id,part));
                    }
                    line.drain();
                }
            } catch(Exception e) {
                GuitarMod.LOG("Не удалось открыть аудио: "+e.getMessage());
            } finally { stopLocal(); }
        });
    }
    public static void stopLocal() {
        SourceDataLine l=localLine; UUID id=localId; localLine=null; localId=null;
        if(l!=null){l.stop();l.flush();l.close();}
        if(id!=null) Network.CHANNEL.sendToServer(new Network.AudioStop(id));
    }
    public static void startRemote(UUID owner,int rate,int channels) {
        AUDIO.execute(() -> {
            try {
                AudioFormat f=new AudioFormat(rate,16,channels,true,false);
                SourceDataLine l=AudioSystem.getSourceDataLine(f); l.open(f); l.start();
                SourceDataLine old=REMOTE.put(owner,l); if(old!=null) old.close();
            } catch(Exception e){GuitarMod.LOG("Remote audio init failed: "+e.getMessage());}
        });
    }
    public static void acceptRemote(UUID owner,byte[] bytes) {
        SourceDataLine l=REMOTE.get(owner); if(l!=null) AUDIO.execute(() -> l.write(bytes,0,bytes.length));
    }
    public static void stopRemote(UUID owner) {
        SourceDataLine l=REMOTE.remove(owner); if(l!=null) AUDIO.execute(() -> {l.drain();l.stop();l.close();});
    }
}
