package ru.example.guitarmod.client;

import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.components.Button;
import ru.example.guitarmod.ClientAudio;

public class MusicScreen extends Screen {
    private String status="Выбери MP3, WAV или OGG";
    public MusicScreen(){super(Component.literal("Музыка гитары"));}
    @Override protected void init() {
        int cx=width/2, y=height/2-40;
        addRenderableWidget(Button.builder(Component.literal("Загрузить музыку"), b -> chooseFile())
            .bounds(cx-100,y,200,20).build());
        addRenderableWidget(Button.builder(Component.literal("Стоп"), b -> {ClientAudio.stopLocal();status="Остановлено";})
            .bounds(cx-100,y+28,95,20).build());
        addRenderableWidget(Button.builder(Component.literal("Закрыть"), b -> onClose())
            .bounds(cx+5,y+28,95,20).build());
    }
    private void chooseFile() {
        // File chooser runs on Swing EDT; actual audio playback is delegated to the audio worker.
        SwingUtilities.invokeLater(() -> {
            JFileChooser chooser=new JFileChooser();
            chooser.setDialogTitle("Выбери аудиофайл");
            int result=chooser.showOpenDialog(null);
            if(result==JFileChooser.APPROVE_OPTION) {
                File f=chooser.getSelectedFile();
                String n=f.getName().toLowerCase(java.util.Locale.ROOT);
                if(!(n.endsWith(".mp3")||n.endsWith(".wav")||n.endsWith(".ogg"))) {
                    status="Поддерживаются MP3, WAV и OGG"; return;
                }
                ClientAudio.playFile(f);
                status="Играет: "+f.getName();
            }
        });
    }
    @Override public void render(GuiGraphics g,int mx,int my,float partial) {
        renderBackground(g);
        g.drawCenteredString(font,title,width/2,height/2-75,0xFFFFFF);
        g.drawCenteredString(font,status,width/2,height/2+65,0xAAAAAA);
        super.render(g,mx,my,partial);
    }
    @Override public boolean isPauseScreen(){return false;}
}
