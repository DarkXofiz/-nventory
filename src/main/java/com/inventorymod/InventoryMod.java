package com.inventorymod;

import com.inventorymod.client.gui.MultiStorageScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import org.lwjgl.glfw.GLFW;

public class InventoryMod implements ClientModInitializer {

    public static final String MOD_ID = "inventorymod";

    private static KeyBinding openKey;

    @Override
    public void onInitializeClient() {
        // Tuş: B tuşu ile aç (isteğe göre değiştir)
        openKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.inventorymod.open",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_B,
                "category.inventorymod"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (openKey.wasPressed()) {
                if (client.player != null) {
                    MinecraftClient.getInstance().setScreen(new MultiStorageScreen());
                }
            }
        });
    }
}
