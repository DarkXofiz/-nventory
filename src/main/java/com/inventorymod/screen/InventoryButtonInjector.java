package com.inventorymod.screen;

import com.inventorymod.handler.InventoryActionHandler;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/**
 * Sandık ekranına Türkçe butonları ekler.
 * Access Widener ile HandledScreen'in x, y, backgroundWidth alanlarına erişir.
 * Mixin KULLANILMAZ.
 */
public class InventoryButtonInjector {

    private static final int BUTTON_WIDTH  = 100;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_GAP    = 4;

    public static void injectButtons(HandledScreen<?> screen, MinecraftClient client) {
        // Access Widener sayesinde protected alanlara doğrudan erişebiliriz
        int guiLeft  = screen.x;
        int guiTop   = screen.y;
        int guiWidth = screen.backgroundWidth;

        // Butonlar sandığın sağ kenarından 6px ileride başlar
        int buttonX = guiLeft + guiWidth + 6;
        int startY  = guiTop + 4;

        addButton(screen, buttonX, startY + (BUTTON_HEIGHT + BUTTON_GAP) * 0,
            "Herşeyi At",   btn -> InventoryActionHandler.dropAllFromInventory(screen, client));

        addButton(screen, buttonX, startY + (BUTTON_HEIGHT + BUTTON_GAP) * 1,
            "Oto Ekipman",  btn -> InventoryActionHandler.autoEquipBest(screen, client));

        addButton(screen, buttonX, startY + (BUTTON_HEIGHT + BUTTON_GAP) * 2,
            "Herşeyi Koy",  btn -> InventoryActionHandler.putAllToChest(screen, client));

        addButton(screen, buttonX, startY + (BUTTON_HEIGHT + BUTTON_GAP) * 3,
            "Herşeyi Al",   btn -> InventoryActionHandler.takeAllFromChest(screen, client));

        addButton(screen, buttonX, startY + (BUTTON_HEIGHT + BUTTON_GAP) * 4,
            "Çöpleri At",   btn -> InventoryActionHandler.dropJunkItems(screen, client));
    }

    private static void addButton(HandledScreen<?> screen,
                                  int x, int y, String label,
                                  ButtonWidget.PressAction action) {
        ButtonWidget btn = ButtonWidget.builder(Text.literal(label), action)
            .dimensions(x, y, BUTTON_WIDTH, BUTTON_HEIGHT)
            .build();
        Screens.getButtons(screen).add(btn);
    }
}
