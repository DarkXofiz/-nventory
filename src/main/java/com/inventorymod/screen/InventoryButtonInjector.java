package com.inventorymod.screen;

import com.inventorymod.handler.InventoryActionHandler;
import com.inventorymod.render.StyledButton;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class InventoryButtonInjector {

    private static final int W = 110;  // genislik
    private static final int H = 22;   // yukseklik
    private static final int GAP = 5;  // aralik

    /**
     * Sandik / Ender Chest / Shulker Box
     * 4 buton: Herseyi At(kirmizi), Herseyi Koy(mavi), Herseyi Al(yesil), Copleri At(mor)
     */
    public static void injectChestButtons(HandledScreen<?> screen, MinecraftClient client) {
        int x = (screen.width - 176) / 2 + 176 + 6;
        int y = (screen.height - 166) / 2 + 4;

        add(screen, x, y + step(0), "Herseyi At",
            btn -> InventoryActionHandler.dropAllFromInventory(screen, client),
            StyledButton.Style.DROP_ALL);

        add(screen, x, y + step(1), "Herseyi Koy",
            btn -> InventoryActionHandler.putAllToChest(screen, client),
            StyledButton.Style.PUT);

        add(screen, x, y + step(2), "Herseyi Al",
            btn -> InventoryActionHandler.takeAllFromChest(screen, client),
            StyledButton.Style.TAKE);

        add(screen, x, y + step(3), "Copleri At",
            btn -> InventoryActionHandler.dropJunkItems(screen, client),
            StyledButton.Style.JUNK);
    }

    /**
     * Oyuncu Envanteri (E tusu)
     * 3 buton: Herseyi At(kirmizi), Oto Ekipman(sari), Copleri At(mor)
     */
    public static void injectInventoryButtons(InventoryScreen screen, MinecraftClient client) {
        int x = (screen.width - 176) / 2 + 176 + 6;
        int y = (screen.height - 166) / 2 + 40;

        add(screen, x, y + step(0), "Herseyi At",
            btn -> InventoryActionHandler.dropAllFromInventory(screen, client),
            StyledButton.Style.DROP_ALL);

        add(screen, x, y + step(1), "Oto Ekipman",
            btn -> InventoryActionHandler.autoEquipBest(screen, client),
            StyledButton.Style.EQUIP);

        add(screen, x, y + step(2), "Copleri At",
            btn -> InventoryActionHandler.dropJunkItems(screen, client),
            StyledButton.Style.JUNK);
    }

    private static int step(int i) { return (H + GAP) * i; }

    private static void add(HandledScreen<?> screen, int x, int y,
                            String label, ButtonWidget.PressAction action,
                            StyledButton.Style style) {
        StyledButton btn = new StyledButton(x, y, W, H, Text.literal(label), action, style);
        Screens.getButtons(screen).add(btn);
    }
}
