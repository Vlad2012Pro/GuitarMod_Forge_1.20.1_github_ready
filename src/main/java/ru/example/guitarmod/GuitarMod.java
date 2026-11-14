package ru.example.guitarmod;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@Mod(GuitarMod.MODID)
public class GuitarMod {
    public static final String MODID = "guitarmod";
    public GuitarMod() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ModItems.ITEMS.register(bus);
        Network.init();
    }

    @EventBusSubscriber(modid = MODID)
    public static class Events {
        @SubscribeEvent
        public static void onServerStarting(ServerStartingEvent event) {
            GuitarMod.LOG("Guitar Mod loaded. Press G while holding the guitar.");
        }
    }
    public static void LOG(String message) {
        System.out.println("[GuitarMod] " + message);
    }
}
