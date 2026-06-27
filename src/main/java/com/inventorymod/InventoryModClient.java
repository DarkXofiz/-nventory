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
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {

            // Normal sandık + Ender Chest (ikisi de GenericContainerScreen kullanır 1.21'de)
            if (screen instanceof GenericContainerScreen s) {
                InventoryButtonInjector.injectChestButtons(s, client);
            }
            // Shulker Box
            else if (screen instanceof ShulkerBoxScreen s) {
                InventoryButtonInjector.injectChestButtons(s, client);
            }
            // Oyuncu Envanteri
            else if (screen instanceof InventoryScreen s) {
                InventoryButtonInjector.injectInventoryButtons(s, client);
            }
        });
    }
}
