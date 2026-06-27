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

    /**
     * Player envanter slotlarını bulur
     */
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

    /**
     * Ender Chest, Vault vb. container slotlarını bulur
     */
    private static List<Slot> getNonPlayerSlots(HandledScreen<?> screen, MinecraftClient client) {
        if (client.player == null) return List.of();
        ScreenHandler handler = screen.getScreenHandler();
        List<Slot> result = new ArrayList<>();

        for (Slot slot : handler.slots) {
            if (slot.inventory != client.player.getInventory() && !slot.getStack().isEmpty()) {
                result.add(slot);
            }
        }

        // Ender Chest ve benzeri custom GUI'ler için fallback
        if (result.isEmpty() && handler.slots.size() > 36) {
            int chestSize = handler.slots.size() - 36;
            for (int i = 0; i < chestSize; i++) {
                Slot slot = handler.slots.get(i);
                if (!slot.getStack().isEmpty()) {
                    result.add(slot);
                }
            }
        }
        return result;
    }

    private static void dropSlot(HandledScreen<?> screen, MinecraftClient client, Slot slot) {
        if (client.player == null || client.interactionManager == null || slot.getStack().isEmpty()) return;

        ScreenHandler handler = screen.getScreenHandler();
        client.interactionManager.clickSlot(
            handler.syncId, slot.id, 1, SlotActionType.THROW, client.player
        );

        InventoryMod.LOGGER.info("[InvMod] Dropped → Slot: {} | Item: {}", slot.id, slot.getStack().getItem());
    }

    /**
     * ANA METOD - Herseyi At (Tuş + Buton)
     */
    public static void dropAllFromCurrentContainer(HandledScreen<?> screen, MinecraftClient client) {
        if (client.player == null || client.interactionManager == null) return;

        ScreenHandler handler = screen.getScreenHandler();
        String handlerName = handler.getClass().getSimpleName();

        InventoryMod.LOGGER.info("[InvMod] Drop All → Handler: {} | Slot: {}", handlerName, handler.slots.size());

        List<Slot> slotsToDrop = getNonPlayerSlots(screen, client);

        if (slotsToDrop.isEmpty()) {
            slotsToDrop = getPlayerSlots(screen, client);
        }

        if (slotsToDrop.isEmpty()) {
            InventoryMod.LOGGER.warn("[InvMod] Drop edilecek slot bulunamadı!");
            return;
        }

        int count = 0;
        for (Slot slot : slotsToDrop) {
            dropSlot(screen, client, slot);
            count++;
        }

        InventoryMod.LOGGER.info("[InvMod] {} item başarıyla yere atıldı!", count);
    }

    // Diğer metotlar (değişmedi)
    public static void putAllToChest(HandledScreen<?> screen, MinecraftClient client) { /* ... */ }
    public static void takeAllFromChest(HandledScreen<?> screen, MinecraftClient client) { /* ... */ }
    public static void dropJunkItems(HandledScreen<?> screen, MinecraftClient client) { /* ... */ }
    public static void autoEquipBest(HandledScreen<?> screen, MinecraftClient client) { /* ... */ }

    public static boolean isJunk(ItemStack stack) { /* ... mevcut kodun ... */ return false; }
    private static boolean isGoodEquipment(String id) { /* ... */ return false; }
    private static boolean isEquipment(ItemStack stack) { /* ... */ return false; }
}
