package com.inventorymod.handler;

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
     * Oyuncu slotlarini bul.
     * 1. player.getInventory() ile eslesen slotlar (normal sandik)
     * 2. Eslesme yoksa son 36 slot (EnderChest, PlayerVaults - sunucu tarafli)
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

        if (result.isEmpty() && handler.slots.size() >= 36) {
            List<Slot> all = handler.slots;
            result.addAll(all.subList(all.size() - 36, all.size()));
        }

        return result;
    }

    private static List<Slot> getChestSlots(HandledScreen<?> screen, MinecraftClient client) {
        if (client.player == null) return List.of();
        ScreenHandler handler = screen.getScreenHandler();
        List<Slot> result = new ArrayList<>();
        for (Slot slot : handler.slots) {
            if (slot.inventory != client.player.getInventory()) {
                result.add(slot);
            }
        }
        if (result.size() == handler.slots.size() && handler.slots.size() >= 36) {
            result = new ArrayList<>(handler.slots.subList(0, handler.slots.size() - 36));
        }
        return result;
    }

    public static void putAllToChest(HandledScreen<?> screen, MinecraftClient client) {
        if (client.player == null || client.interactionManager == null) return;
        ScreenHandler handler = screen.getScreenHandler();
        for (Slot slot : getPlayerSlots(screen, client)) {
            if (!slot.getStack().isEmpty())
                client.interactionManager.clickSlot(handler.syncId, slot.id, 0, SlotActionType.QUICK_MOVE, client.player);
        }
    }

    public static void takeAllFromChest(HandledScreen<?> screen, MinecraftClient client) {
        if (client.player == null || client.interactionManager == null) return;
        ScreenHandler handler = screen.getScreenHandler();
        for (Slot slot : getChestSlots(screen, client)) {
            if (!slot.getStack().isEmpty())
                client.interactionManager.clickSlot(handler.syncId, slot.id, 0, SlotActionType.QUICK_MOVE, client.player);
        }
    }

    /**
     * HERSEYi AT
     * Yontem:
     * 1. Slotu sol tik ile el'e al (pickup)
     * 2. Ekranin disina sol tik at (drop)
     * Bu yontem EnderChest ve PlayerVaults dahil TUM ekranlarda calisir.
     */
    public static void dropAllFromInventory(HandledScreen<?> screen, MinecraftClient client) {
        if (client.player == null || client.interactionManager == null) return;
        ScreenHandler handler = screen.getScreenHandler();

        for (Slot slot : getPlayerSlots(screen, client)) {
            if (slot.getStack().isEmpty()) continue;

            // 1. Adim: itemi ele al
            client.interactionManager.clickSlot(
                handler.syncId, slot.id, 0, SlotActionType.PICKUP, client.player);

            // 2. Adim: elde item varsa dis alana birak (slotIndex = -999)
            if (!client.player.currentScreenHandler.getCursorStack().isEmpty()) {
                client.interactionManager.clickSlot(
                    handler.syncId, -999, 0, SlotActionType.PICKUP, client.player);
            }
        }
    }

    /**
     * COPLERi AT
     * Ayni pickup+drop yontemi — tum ekranlarda calisir.
     * Cop = deri zirh, tas/ahsap alet, cop yiyecek, cop blok, koruma 1 set
     */
    public static void dropJunkItems(HandledScreen<?> screen, MinecraftClient client) {
        if (client.player == null || client.interactionManager == null) return;
        ScreenHandler handler = screen.getScreenHandler();

        for (Slot slot : getPlayerSlots(screen, client)) {
            ItemStack stack = slot.getStack();
            if (stack.isEmpty() || !isJunk(stack)) continue;

            // 1. Adim: itemi ele al
            client.interactionManager.clickSlot(
                handler.syncId, slot.id, 0, SlotActionType.PICKUP, client.player);

            // 2. Adim: dis alana birak
            if (!client.player.currentScreenHandler.getCursorStack().isEmpty()) {
                client.interactionManager.clickSlot(
                    handler.syncId, -999, 0, SlotActionType.PICKUP, client.player);
            }
        }
    }

    public static void autoEquipBest(HandledScreen<?> screen, MinecraftClient client) {
        if (client.player == null || client.interactionManager == null) return;
        ScreenHandler handler = screen.getScreenHandler();
        for (Slot slot : getPlayerSlots(screen, client)) {
            ItemStack stack = slot.getStack();
            if (!stack.isEmpty() && isEquipment(stack))
                client.interactionManager.clickSlot(handler.syncId, slot.id, 0, SlotActionType.QUICK_MOVE, client.player);
        }
    }

    /**
     * COP KONTROLU
     * Atar: deri zirh, tas/ahsap alet, koruma 1 duzeyindeki setler,
     *       cop yiyecek, cop blok, cop malzeme
     * ATMAZ: demir/altin/elmas/netherite gear
     */
    public static boolean isJunk(ItemStack stack) {
        String id = stack.getItem().toString().toLowerCase();

        // Iyi ekipman — kesinlikle atma
        if (isGoodEquipment(id)) return false;

        // Cop yiyecekler
        if (stack.isOf(Items.ROTTEN_FLESH))      return true;
        if (stack.isOf(Items.SPIDER_EYE))        return true;
        if (stack.isOf(Items.POISONOUS_POTATO))  return true;
        if (stack.isOf(Items.BEEF))              return true;
        if (stack.isOf(Items.PORKCHOP))          return true;
        if (stack.isOf(Items.MUTTON))            return true;
        if (stack.isOf(Items.CHICKEN))           return true;
        if (stack.isOf(Items.RABBIT))            return true;

        // Cop bloklar
        if (stack.isOf(Items.DIRT))              return true;
        if (stack.isOf(Items.GRAVEL))            return true;
        if (stack.isOf(Items.SAND))              return true;
        if (stack.isOf(Items.COBBLESTONE))       return true;
        if (stack.isOf(Items.COBBLED_DEEPSLATE)) return true;
        if (stack.isOf(Items.NETHERRACK))        return true;
        if (stack.isOf(Items.STONE))             return true;
        if (stack.isOf(Items.ANDESITE))          return true;
        if (stack.isOf(Items.DIORITE))           return true;
        if (stack.isOf(Items.GRANITE))           return true;

        // Cop malzeme
        if (stack.isOf(Items.STICK))             return true;
        if (stack.isOf(Items.STRING))            return true;
        if (stack.isOf(Items.BONE))              return true;
        if (stack.isOf(Items.GUNPOWDER))         return true;
        if (stack.isOf(Items.FLINT))             return true;
        if (stack.isOf(Items.INK_SAC))           return true;
        if (stack.isOf(Items.ARROW))             return true;

        // Deri zirh (koruma 1 = deri set genellikle)
        if (id.contains("leather_helmet"))       return true;
        if (id.contains("leather_chestplate"))   return true;
        if (id.contains("leather_leggings"))     return true;
        if (id.contains("leather_boots"))        return true;

        // Zincir zirh
        if (id.contains("chainmail_helmet"))     return true;
        if (id.contains("chainmail_chestplate")) return true;
        if (id.contains("chainmail_leggings"))   return true;
        if (id.contains("chainmail_boots"))      return true;

        // Tas aletler
        if (id.contains("stone_sword"))          return true;
        if (id.contains("stone_pickaxe"))        return true;
        if (id.contains("stone_axe"))            return true;
        if (id.contains("stone_shovel"))         return true;
        if (id.contains("stone_hoe"))            return true;

        // Ahsap aletler
        if (id.contains("wooden_sword"))         return true;
        if (id.contains("wooden_pickaxe"))       return true;
        if (id.contains("wooden_axe"))           return true;
        if (id.contains("wooden_shovel"))        return true;

        // Altin zirh (zayif)
        if (id.contains("golden_helmet"))        return true;
        if (id.contains("golden_chestplate"))    return true;
        if (id.contains("golden_leggings"))      return true;
        if (id.contains("golden_boots"))         return true;

        return false;
    }

    private static boolean isGoodEquipment(String id) {
        boolean mat  = id.contains("iron_") || id.contains("diamond_") || id.contains("netherite_");
        boolean gear = id.contains("helmet") || id.contains("chestplate")
                    || id.contains("leggings") || id.contains("boots")
                    || id.contains("sword") || id.contains("pickaxe")
                    || id.contains("axe") || id.contains("bow")
                    || id.contains("crossbow") || id.contains("trident")
                    || id.contains("shield");
        return mat && gear;
    }

    private static boolean isEquipment(ItemStack stack) {
        String id = stack.getItem().toString().toLowerCase();
        return id.contains("helmet") || id.contains("chestplate")
            || id.contains("leggings") || id.contains("boots")
            || id.contains("sword") || id.contains("axe")
            || id.contains("bow") || id.contains("crossbow")
            || id.contains("trident") || id.contains("shield");
    }
}
