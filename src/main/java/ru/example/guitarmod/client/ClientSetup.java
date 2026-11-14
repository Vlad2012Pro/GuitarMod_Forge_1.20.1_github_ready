package ru.example.guitarmod.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;
import ru.example.guitarmod.GuitarMod;
import ru.example.guitarmod.ModItems;

@Mod.EventBusSubscriber(modid=GuitarMod.MODID,value=Dist.CLIENT,bus=Mod.EventBusSubscriber.Bus.MOD)
public class ClientSetup {
    public static KeyMapping OPEN_MUSIC;
    @SubscribeEvent public static void registerKeys(RegisterKeyMappingsEvent e) {
        OPEN_MUSIC=new KeyMapping("key.guitarmod.open_music",KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,GLFW.GLFW_KEY_G,"key.categories.guitarmod");
        e.register(OPEN_MUSIC);
    }
    @Mod.EventBusSubscriber(modid=GuitarMod.MODID,value=Dist.CLIENT)
    public static class Input {
        @SubscribeEvent public static void onKey(InputEvent.Key event) {
            if(OPEN_MUSIC!=null && OPEN_MUSIC.consumeClick()) {
                Minecraft mc=Minecraft.getInstance();
                if(mc.player!=null && (mc.player.getMainHandItem().is(ModItems.GUITAR.get()) ||
                    mc.player.getOffhandItem().is(ModItems.GUITAR.get())))
                    mc.setScreen(new MusicScreen());
            }
        }
    }
}
