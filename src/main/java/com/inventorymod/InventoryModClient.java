package com.inventorymod;

import com.inventorymod.screen.InventoryButtonInjector;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;

public class InventoryModClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {

            // Envanter ekrani (E tusu) — 3 buton
            if (screen instanceof InventoryScreen s) {
                InventoryButtonInjector.injectInventoryButtons(s, client);
            }
            // Diger TUM sandik/chest ekranlari:
            // Normal sandik, Ender Chest, PlayerVaults, Shulker Box, vb.
            // HandledScreen hepsinin ana sinifi — hepsini yakalar
            else if (screen instanceof HandledScreen<?> s) {
                InventoryButtonInjector.injectChestButtons(s, client);
            }
        });
    }
}
