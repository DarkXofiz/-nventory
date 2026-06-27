package com.inventorymod.handler;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

import java.util.List;

public class InventoryActionHandler {

    // Envanterden sandiga HERSEYi KOY
    public static void putAllToChest(HandledScreen<?> screen, MinecraftClient client) {
        if (client.player == null || client.interactionManager == null) return;
        ScreenHandler handler = screen.getScreenHandler();
        List<Slot> slots = handler.slots;
        int playerStart = slots.size() - 36;
        for (int i = playerStart; i < slots.size(); i++) {
            Slot slot = slots.get(i);
            if (!slot.getStack().isEmpty()) {
                client.interactionManager.clickSlot(
                    handler.syncId, slot.id, 0, SlotActionType.QUICK_MOVE, client.player);
            }
        }
    }

    // Sandiktan envanterine HERSEYi AL
    public static void takeAllFromChest(HandledScreen<?> screen, MinecraftClient client) {
        if (client.player == null || client.interactionManager == null) return;
        ScreenHandler handler = screen.getScreenHandler();
        List<Slot> slots = handler.slots;
        int chestSlotCount = slots.size() - 36;
        for (int i = 0; i < chestSlotCount; i++) {
            Slot slot = slots.get(i);
            if (!slot.getStack().isEmpty()) {
                client.interactionManager.clickSlot(
                    handler.syncId, slot.id, 0, SlotActionType.QUICK_MOVE, client.player);
            }
        }
    }

    // Envanterden HERSEYi AT (yere birakir, silmez)
    public static void dropAllFromInventory(HandledScreen<?> screen, MinecraftClient client) {
        if (client.player == null || client.interactionManager == null) return;
        ScreenHandler handler = screen.getScreenHandler();
        List<Slot> slots = handler.slots;
        int playerStart = slots.size() - 36;
        for (int i = playerStart; i < slots.size(); i++) {
            Slot slot = slots.get(i);
            if (!slot.getStack().isEmpty()) {
                client.interactionManager.clickSlot(
                    handler.syncId, slot.id, 1, SlotActionType.THROW, client.player);
            }
        }
    }

    /**
     * COPLERi AT — hem sandik hem envanter ekraninda calisir.
     * Her zaman PLAYER slotlarini (son 36) tarar.
     * Ender Chest'e item koyunca da envanteri tarar, dogru calisir.
     */
    public static void dropJunkItems(HandledScreen<?> screen, MinecraftClient client) {
        if (client.player == null || client.interactionManager == null) return;
        ScreenHandler handler = screen.getScreenHandler();
        List<Slot> slots = handler.slots;
        int playerStart = slots.size() - 36;
        for (int i = playerStart; i < slots.size(); i++) {
            Slot slot = slots.get(i);
            ItemStack stack = slot.getStack();
            if (!stack.isEmpty() && isJunk(stack)) {
                client.interactionManager.clickSlot(
                    handler.syncId, slot.id, 1, SlotActionType.THROW, client.player);
            }
        }
    }

    // dropJunkFromInventory = ayni metod, geriye donus uyumlulugu icin
    public static void dropJunkFromInventory(HandledScreen<?> screen, MinecraftClient client) {
        dropJunkItems(screen, client);
    }

    // OTO EKiPMAN
    public static void autoEquipBest(HandledScreen<?> screen, MinecraftClient client) {
        if (client.player == null || client.interactionManager == null) return;
        ScreenHandler handler = screen.getScreenHandler();
        List<Slot> slots = handler.slots;
        int playerStart = slots.size() - 36;
        for (int i = playerStart; i < slots.size(); i++) {
            Slot slot = slots.get(i);
            ItemStack stack = slot.getStack();
            if (!stack.isEmpty() && isEquipment(stack)) {
                client.interactionManager.clickSlot(
                    handler.syncId, slot.id, 0, SlotActionType.QUICK_MOVE, client.player);
            }
        }
    }

    private static boolean isJunk(ItemStack stack) {
        return stack.isOf(Items.STICK)
            || stack.isOf(Items.ROTTEN_FLESH)
            || stack.isOf(Items.BONE)
            || stack.isOf(Items.ARROW)
            || stack.isOf(Items.COBBLESTONE)
            || stack.isOf(Items.COBBLED_DEEPSLATE)
            || stack.isOf(Items.GRAVEL)
            || stack.isOf(Items.SAND)
            || stack.isOf(Items.DIRT)
            || stack.isOf(Items.NETHERRACK)
            || stack.isOf(Items.STRING)
            || stack.isOf(Items.SPIDER_EYE)
            || stack.isOf(Items.POISONOUS_POTATO)
            || stack.isOf(Items.GUNPOWDER);
    }

    private static boolean isEquipment(ItemStack stack) {
        String id = stack.getItem().toString().toLowerCase();
        return id.contains("helmet")
            || id.contains("chestplate")
            || id.contains("leggings")
            || id.contains("boots")
            || id.contains("sword")
            || id.contains("axe")
            || id.contains("bow")
            || id.contains("crossbow")
            || id.contains("trident")
            || id.contains("shield");
    }
}

