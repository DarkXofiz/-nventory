package com.inventorymod;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InventoryMod implements ModInitializer {
    public static final String MOD_ID = "inventorymod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("InventoryMod yüklendi!");
    }
}
