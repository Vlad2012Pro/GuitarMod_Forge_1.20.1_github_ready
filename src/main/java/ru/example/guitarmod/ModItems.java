package ru.example.guitarmod;

import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS =
        DeferredRegister.create(ForgeRegistries.ITEMS, GuitarMod.MODID);
    public static final RegistryObject<Item> GUITAR =
        ITEMS.register("guitar", () -> new Item(new Item.Properties().stacksTo(1)));
}
