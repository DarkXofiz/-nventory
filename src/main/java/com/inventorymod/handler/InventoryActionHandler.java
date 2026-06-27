package com.inventorymod.handler;

import com.inventorymod.InventoryMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.screen.EnderChestScreenHandler;

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
     * Chest, Ender Chest, PlayerVaults gibi container slotlarını bulur
     */
    private static List<Slot> getNonPlayerSlots(HandledScreen<?> screen, MinecraftClient client) {
        if (client.player == null) return List.of();
        ScreenHandler handler = screen.getScreenHandler();
        List<Slot> result = new ArrayList<>();

        // Normal container slotları (Ender Chest, Vault vb.)
        for (Slot slot : handler.slots) {
            if (slot.inventory != client.player.getInventory() && !slot.getStack().isEmpty()) {
                result.add(slot);
            }
        }

        // Hiçbir şey bulunmadıysa (bazı custom GUI'ler) üstteki slotları al
        if (result.isEmpty() && handler.slots.size() > 36) {
            int chestSlotCount = handler.slots.size() - 36;
            for (int i = 0; i < chestSlotCount; i++) {
                Slot slot = handler.slots.get(i);
                if (!slot.getStack().isEmpty()) {
                    result.add(slot);
                }
            }
        }
        return result;
    }

    /**
     * Tek bir slotu yere atar
     */
    private static void dropSlot(HandledScreen<?> screen, MinecraftClient client, Slot slot) {
        if (client.player == null || client.interactionManager == null || slot.getStack().isEmpty()) {
            return;
        }

        ScreenHandler handler = screen.getScreenHandler();
        client.interactionManager.clickSlot(
                handler.syncId,
                slot.id,
                1, // 1 = Tüm stack (Ctrl+Q)
                SlotActionType.THROW,
                client.player
        );

        InventoryMod.LOGGER.info("[InvMod] Dropped → Slot: {} | Item: {}", slot.id, slot.getStack().getItem());
    }

    /**
     * ★ ANA METOD: "Herseyi At" butonu ve tuş için (Ender Chest + Vault + Normal)
     */
    public static void dropAllFromCurrentContainer(HandledScreen<?> screen, MinecraftClient client) {
        if (client.player == null || client.interactionManager == null) return;

        ScreenHandler handler = screen.getScreenHandler();
        String handlerName = handler.getClass().getSimpleName();

        InventoryMod.LOGGER.info("[InvMod] Drop All Başladı → Handler: {} | Toplam Slot: {}", 
                handlerName, handler.slots.size());

        List<Slot> slotsToDrop = getNonPlayerSlots(screen, client);

        // Eğer chest slotu bulunamadıysa normal player slotlarına düş
        if (slotsToDrop.isEmpty()) {
            slotsToDrop = getPlayerSlots(screen, client);
        }

        if (slotsToDrop.isEmpty()) {
            InventoryMod.LOGGER.warn("[InvMod] Hiç drop edilecek slot bulunamadı!");
            return;
        }

        int droppedCount = 0;
        for (Slot slot : slotsToDrop) {
            dropSlot(screen, client, slot);
            droppedCount++;
        }

        InventoryMod.LOGGER.info("[InvMod] Başarıyla {} item yere atıldı!", droppedCount);
    }

    // ====================== DİĞER ÖZELLİKLER ======================

    public static void putAllToChest(HandledScreen<?> screen, MinecraftClient client) {
        if (client.player == null || client.interactionManager == null) return;
        ScreenHandler handler = screen.getScreenHandler();
        for (Slot slot : getPlayerSlots(screen, client)) {
            if (!slot.getStack().isEmpty()) {
                client.interactionManager.clickSlot(handler.syncId, slot.id, 0, SlotActionType.QUICK_MOVE, client.player);
            }
        }
    }

    public static void takeAllFromChest(HandledScreen<?> screen, MinecraftClient client) {
        if (client.player == null || client.interactionManager == null) return;
        ScreenHandler handler = screen.getScreenHandler();
        for (Slot slot : getNonPlayerSlots(screen, client)) {
            if (!slot.getStack().isEmpty()) {
                client.interactionManager.clickSlot(handler.syncId, slot.id, 0, SlotActionType.QUICK_MOVE, client.player);
            }
        }
    }

    public static void dropJunkItems(HandledScreen<?> screen, MinecraftClient client) {
        if (client.player == null || client.interactionManager == null) return;
        for (Slot slot : getPlayerSlots(screen, client)) {
            if (!slot.getStack().isEmpty() && isJunk(slot.getStack())) {
                dropSlot(screen, client, slot);
            }
        }
    }

    public static void autoEquipBest(HandledScreen<?> screen, MinecraftClient client) {
        if (client.player == null || client.interactionManager == null) return;
        ScreenHandler handler = screen.getScreenHandler();
        for (Slot slot : getPlayerSlots(screen, client)) {
            ItemStack stack = slot.getStack();
            if (!stack.isEmpty() && isEquipment(stack)) {
                client.interactionManager.clickSlot(handler.syncId, slot.id, 0, SlotActionType.QUICK_MOVE, client.player);
            }
        }
    }

    // ====================== YARDIMCI METOTLAR ======================

    public static boolean isJunk(ItemStack stack) {
        if (stack.isEmpty()) return false;
        String id = stack.getItem().toString().toLowerCase();

        if (isGoodEquipment(id)) return false;

        // Junk listesi
        return stack.isOf(Items.ROTTEN_FLESH) || stack.isOf(Items.SPIDER_EYE) ||
               stack.isOf(Items.POISONOUS_POTATO) || stack.isOf(Items.DIRT) ||
               stack.isOf(Items.COBBLESTONE) || stack.isOf(Items.STONE) ||
               id.contains("leather_") || id.contains("golden_") ||
               id.contains("wooden_") || id.contains("stone_");
    }

    private static boolean isGoodEquipment(String id) {
        return (id.contains("iron_") || id.contains("diamond_") || id.contains("netherite_")) &&
               (id.contains("helmet") || id.contains("chestplate") || id.contains("leggings") ||
                id.contains("boots") || id.contains("sword") || id.contains("pickaxe") ||
                id.contains("axe") || id.contains("bow"));
    }

    private static boolean isEquipment(ItemStack stack) {
        if (stack.isEmpty()) return false;
        String id = stack.getItem().toString().toLowerCase();
        return id.contains("helmet") || id.contains("chestplate") || id.contains("leggings") ||
               id.contains("boots") || id.contains("sword") || id.contains("axe") ||
               id.contains("bow") || id.contains("shield");
    }
                            }
