package com.inventorymod.handler;

import com.inventorymod.InventoryMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

import java.util.ArrayList;
import java.util.List;

public class InventoryActionHandler {

    private static List<Slot> getPlayerSlots(HandledScreen<?> screen, MinecraftClient client) {
        if (client.player == null) return List.of();
        ScreenHandler handler = screen.getScreenHandler();
        List<Slot> result = new ArrayList<>();
        for (Slot slot : handler.slots) {
            if (slot.inventory == client.player.getInventory()) {
                result.add(slot);
            }
        }
        return result;
    }

    private static List<Slot> getNonPlayerSlots(HandledScreen<?> screen, MinecraftClient client) {
        if (client.player == null) return List.of();
        ScreenHandler handler = screen.getScreenHandler();
        List<Slot> result = new ArrayList<>();

        for (Slot slot : handler.slots) {
            if (slot.inventory != client.player.getInventory() && !slot.getStack().isEmpty()) {
                result.add(slot);
            }
        }

        if (result.isEmpty() && handler.slots.size() > 36) {
            int chestSize = handler.slots.size() - 36;
            for (int i = 0; i < chestSize; i++) {
                Slot slot = handler.slots.get(i);
                if (!slot.getStack().isEmpty()) result.add(slot);
            }
        }
        return result;
    }

    private static void dropSlot(HandledScreen<?> screen, MinecraftClient client, Slot slot) {
        if (client.player == null || client.interactionManager == null || slot.getStack().isEmpty()) return;

        ScreenHandler handler = screen.getScreenHandler();
        client.interactionManager.clickSlot(handler.syncId, slot.id, 1, SlotActionType.THROW, client.player);
        InventoryMod.LOGGER.info("[InvMod] Dropped → Slot: {} | Item: {}", slot.id, slot.getStack().getItem());
    }

    /** ANA METOD - Herseyi At */
    public static void dropAllFromCurrentContainer(HandledScreen<?> screen, MinecraftClient client) {
        if (client.player == null || client.interactionManager == null) return;

        ScreenHandler handler = screen.getScreenHandler();
        InventoryMod.LOGGER.info("[InvMod] Drop All → Handler: {} | Slot: {}", 
            handler.getClass().getSimpleName(), handler.slots.size());

        List<Slot> slotsToDrop = getNonPlayerSlots(screen, client);
        if (slotsToDrop.isEmpty()) {
            slotsToDrop = getPlayerSlots(screen, client);
        }

        int count = 0;
        for (Slot slot : slotsToDrop) {
            dropSlot(screen, client, slot);
            count++;
        }

        InventoryMod.LOGGER.info("[InvMod] {} item atıldı!", count);
    }

    // Diğer metotlarınız (kısaltıldı, kendi kodunuzdan kopyalayın)
    public static void putAllToChest(HandledScreen<?> screen, MinecraftClient client) { /* ... */ }
    public static void takeAllFromChest(HandledScreen<?> screen, MinecraftClient client) { /* ... */ }
    public static void dropJunkItems(HandledScreen<?> screen, MinecraftClient client) { /* ... */ }
    public static void autoEquipBest(HandledScreen<?> screen, MinecraftClient client) { /* ... */ }

    public static boolean isJunk(ItemStack stack) { /* ... */ return false; }
    private static boolean isGoodEquipment(String id) { /* ... */ return false; }
    private static boolean isEquipment(ItemStack stack) { /* ... */ return false; }
}
