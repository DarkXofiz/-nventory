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
     * Sandık, Ender Chest, Shulker Box ekranı için butonlar.
     * 4 buton: Herseyi At, Oto Ekipman, Herseyi Koy, Herseyi Al
     * Copleri At SADECE envanter ekranında var — burada YOK (fazladan buton sorunu düzeltildi)
     */
    public static void injectChestButtons(HandledScreen<?> screen, MinecraftClient client) {
        int guiLeft = (screen.width - 176) / 2;
        int guiTop  = (screen.height - 166) / 2;

        int buttonX = guiLeft + 176 + 6;
        int startY  = guiTop + 4;

        addButton(screen, buttonX, startY + (BUTTON_HEIGHT + BUTTON_GAP) * 0,
            "Herseyi At",  btn -> InventoryActionHandler.dropAllFromInventory(screen, client));

        addButton(screen, buttonX, startY + (BUTTON_HEIGHT + BUTTON_GAP) * 1,
            "Oto Ekipman", btn -> InventoryActionHandler.autoEquipBest(screen, client));

        addButton(screen, buttonX, startY + (BUTTON_HEIGHT + BUTTON_GAP) * 2,
            "Herseyi Koy", btn -> InventoryActionHandler.putAllToChest(screen, client));

        addButton(screen, buttonX, startY + (BUTTON_HEIGHT + BUTTON_GAP) * 3,
            "Herseyi Al",  btn -> InventoryActionHandler.takeAllFromChest(screen, client));
    }

    /**
     * Oyuncu Envanteri (E tuşu) için butonlar.
     * 3 buton: Herseyi At, Oto Ekipman, Copleri At
     * Herseyi Koy/Al yok — karşı taraf sandık değil
     */
    public static void injectInventoryButtons(InventoryScreen screen, MinecraftClient client) {
        // Envanter ekranı 176x166, ama sol tarafta craft alanı var
        int guiLeft = (screen.width - 176) / 2;
        int guiTop  = (screen.height - 166) / 2;

        int buttonX = guiLeft + 176 + 6;
        int startY  = guiTop + 4;

        addButton(screen, buttonX, startY + (BUTTON_HEIGHT + BUTTON_GAP) * 0,
            "Herseyi At",  btn -> InventoryActionHandler.dropAllFromInventory(screen, client));

        addButton(screen, buttonX, startY + (BUTTON_HEIGHT + BUTTON_GAP) * 1,
            "Oto Ekipman", btn -> InventoryActionHandler.autoEquipBest(screen, client));

        addButton(screen, buttonX, startY + (BUTTON_HEIGHT + BUTTON_GAP) * 2,
            "Copleri At",  btn -> InventoryActionHandler.dropJunkFromInventory(screen, client));
    }

    private static void addButton(HandledScreen<?> screen, int x, int y,
                                  String label, ButtonWidget.PressAction action) {
        ButtonWidget btn = ButtonWidget.builder(Text.literal(label), action)
            .dimensions(x, y, BUTTON_WIDTH, BUTTON_HEIGHT)
            .build();
        Screens.getButtons(screen).add(btn);
    }
}
