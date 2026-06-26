package com.inventorymod;

import net.fabricmc.api.ClientModInitializer;

public class InventoryModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        System.out.println("[InventoryMod] Buton modu yüklendi!");
    }
}
