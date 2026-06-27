package com.inventorymod.screen;

import com.inventorymod.handler.InventoryActionHandler;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class InventoryButtonInjector {

    private static final int BUTTON_WIDTH  = 100;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_GAP    = 4;

    /**
     * Sandık / Ender Chest / Shulker Box için 5 buton.
     * Copleri At buradaki envanter slotlarını da tarar — Ender Chest'e item koyunca çalışır.
     */
    public static void injectChestButtons(HandledScreen<?> screen, MinecraftClient client) {
        int guiLeft = (screen.width - 176) / 2;
        int guiTop  = (screen.height - 166) / 2;
        int buttonX = guiLeft + 176 + 6;
        int startY  = guiTop + 4;

        addButton(screen, buttonX, startY + step(0), "Herseyi At",
            btn -> InventoryActionHandler.dropAllFromInventory(screen, client));

        addButton(screen, buttonX, startY + step(1), "Oto Ekipman",
            btn -> InventoryActionHandler.autoEquipBest(screen, client));

        addButton(screen, buttonX, startY + step(2), "Herseyi Koy",
            btn -> InventoryActionHandler.putAllToChest(screen, client));

        addButton(screen, buttonX, startY + step(3), "Herseyi Al",
            btn -> InventoryActionHandler.takeAllFromChest(screen, client));

        addButton(screen, buttonX, startY + step(4), "Copleri At",
            btn -> InventoryActionHandler.dropJunkItems(screen, client));
    }

    /**
     * Oyuncu Envanteri (E tusu) icin 3 buton.
     * startY biraz asagida — zirh slotlarinin altina dusuyor.
     */
    public static void injectInventoryButtons(InventoryScreen screen, MinecraftClient client) {
        int guiLeft = (screen.width - 176) / 2;
        int guiTop  = (screen.height - 166) / 2;
        int buttonX = guiLeft + 176 + 6;
        // +50 ile asagi kaydiriyoruz — zirh alaninin altina geliyor
        int startY  = guiTop + 80;

        addButton(screen, buttonX, startY + step(0), "Herseyi At",
            btn -> InventoryActionHandler.dropAllFromInventory(screen, client));

        addButton(screen, buttonX, startY + step(1), "Oto Ekipman",
            btn -> InventoryActionHandler.autoEquipBest(screen, client));

        addButton(screen, buttonX, startY + step(2), "Copleri At",
            btn -> InventoryActionHandler.dropJunkItems(screen, client));
    }

    private static int step(int index) {
        return (BUTTON_HEIGHT + BUTTON_GAP) * index;
    }

    private static void addButton(HandledScreen<?> screen, int x, int y,
                                  String label, ButtonWidget.PressAction action) {
        ButtonWidget btn = ButtonWidget.builder(Text.literal(label), action)
            .dimensions(x, y, BUTTON_WIDTH, BUTTON_HEIGHT)
            .build();
        Screens.getButtons(screen).add(btn);
    }
}
