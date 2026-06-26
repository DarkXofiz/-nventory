package com.inventorymod;

import com.inventorymod.screen.InventoryButtonInjector;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.ingame.ShulkerBoxScreen;

public class InventoryModClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Chest (Sandık) açıldığında butonları ekle
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof GenericContainerScreen containerScreen) {
                InventoryButtonInjector.injectButtons(containerScreen, client);
            }
            // Shulker Box desteği
            if (screen instanceof ShulkerBoxScreen shulkerScreen) {
                InventoryButtonInjector.injectButtons(shulkerScreen, client);
            }
        });
    }
}
