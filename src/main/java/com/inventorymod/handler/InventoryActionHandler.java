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

    /**
     * Bir slottaki itemi guvenlice yere birak.
     * Yontem: slot index -999 ile PICKUP = vanilla drop, her ekranda calisir.
     * Elde kalan item varsa temizle.
     */
    private static void dropSlot(HandledScreen<?> screen, MinecraftClient client, Slot slot) {
        if (client.player == null || client.interactionManager == null) return;
        ScreenHandler handler = screen.getScreenHandler();

        // 1. Itemi ele al
        client.interactionManager.clickSlot(
            handler.syncId, slot.id, 0, SlotActionType.PICKUP, client.player);

        // 2. Elde item varsa -999 ile yere birak
        ItemStack cursor = client.player.currentScreenHandler.getCursorStack();
        if (!cursor.isEmpty()) {
            client.interactionManager.clickSlot(
                handler.syncId, -999, 0, SlotActionType.PICKUP, client.player);
        }

        // 3. Hala elde item varsa (sunucu reddetti), geri koy
        cursor = client.player.currentScreenHandler.getCursorStack();
        if (!cursor.isEmpty()) {
            client.interactionManager.clickSlot(
                handler.syncId, slot.id, 0, SlotActionType.PICKUP, client.player);
        }
    }

    public static void putAllToChest(HandledScreen<?> screen, MinecraftClient client) {
        if (client.player == null || client.interactionManager == null) return;
        ScreenHandler handler = screen.getScreenHandler();
        for (Slot slot : getPlayerSlots(screen, client)) {
            if (!slot.getStack().isEmpty())
                client.interactionManager.clickSlot(
                    handler.syncId, slot.id, 0, SlotActionType.QUICK_MOVE, client.player);
        }
    }

    public static void takeAllFromChest(HandledScreen<?> screen, MinecraftClient client) {
        if (client.player == null || client.interactionManager == null) return;
        ScreenHandler handler = screen.getScreenHandler();
        for (Slot slot : getChestSlots(screen, client)) {
            if (!slot.getStack().isEmpty())
                client.interactionManager.clickSlot(
                    handler.syncId, slot.id, 0, SlotActionType.QUICK_MOVE, client.player);
        }
    }

    public static void dropAllFromInventory(HandledScreen<?> screen, MinecraftClient client) {
        if (client.player == null || client.interactionManager == null) return;
        for (Slot slot : getPlayerSlots(screen, client)) {
            if (!slot.getStack().isEmpty()) {
                dropSlot(screen, client, slot);
            }
        }
    }

    public static void dropJunkItems(HandledScreen<?> screen, MinecraftClient client) {
        if (client.player == null || client.interactionManager == null) return;
        for (Slot slot : getPlayerSlots(screen, client)) {
            ItemStack stack = slot.getStack();
            if (!stack.isEmpty() && isJunk(stack)) {
                dropSlot(screen, client, slot);
            }
        }
    }

    public static void autoEquipBest(HandledScreen<?> screen, MinecraftClient client) {
        if (client.player == null || client.interactionManager == null) return;
        ScreenHandler handler = screen.getScreenHandler();
        for (Slot slot : getPlayerSlots(screen, client)) {
            ItemStack stack = slot.getStack();
            if (!stack.isEmpty() && isEquipment(stack))
                client.interactionManager.clickSlot(
                    handler.syncId, slot.id, 0, SlotActionType.QUICK_MOVE, client.player);
        }
    }

    public static boolean isJunk(ItemStack stack) {
        String id = stack.getItem().toString().toLowerCase();
        if (isGoodEquipment(id)) return false;

        if (stack.isOf(Items.ROTTEN_FLESH))      return true;
        if (stack.isOf(Items.SPIDER_EYE))        return true;
        if (stack.isOf(Items.POISONOUS_POTATO))  return true;
        if (stack.isOf(Items.BEEF))              return true;
        if (stack.isOf(Items.PORKCHOP))          return true;
        if (stack.isOf(Items.MUTTON))            return true;
        if (stack.isOf(Items.CHICKEN))           return true;
        if (stack.isOf(Items.RABBIT))            return true;
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
        if (stack.isOf(Items.STICK))             return true;
        if (stack.isOf(Items.STRING))            return true;
        if (stack.isOf(Items.BONE))              return true;
        if (stack.isOf(Items.GUNPOWDER))         return true;
        if (stack.isOf(Items.FLINT))             return true;
        if (stack.isOf(Items.INK_SAC))           return true;
        if (stack.isOf(Items.ARROW))             return true;

        if (id.contains("leather_"))             return true;
        if (id.contains("chainmail_"))           return true;
        if (id.contains("golden_helmet"))        return true;
        if (id.contains("golden_chestplate"))    return true;
        if (id.contains("golden_leggings"))      return true;
        if (id.contains("golden_boots"))         return true;
        if (id.contains("stone_sword"))          return true;
        if (id.contains("stone_pickaxe"))        return true;
        if (id.contains("stone_axe"))            return true;
        if (id.contains("stone_shovel"))         return true;
        if (id.contains("stone_hoe"))            return true;
        if (id.contains("wooden_sword"))         return true;
        if (id.contains("wooden_pickaxe"))       return true;
        if (id.contains("wooden_axe"))           return true;
        if (id.contains("wooden_shovel"))        return true;

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
