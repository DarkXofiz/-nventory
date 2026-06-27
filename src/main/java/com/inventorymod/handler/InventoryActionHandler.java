package com.inventorymod.handler;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

import java.util.List;

public class InventoryActionHandler {

    // ── Sandiga HERSEYi KOY ──────────────────────────────────────────────────
    public static void putAllToChest(HandledScreen<?> screen, MinecraftClient client) {
        if (client.player == null || client.interactionManager == null) return;
        ScreenHandler handler = screen.getScreenHandler();
        List<Slot> slots = handler.slots;
        int playerStart = slots.size() - 36;
        for (int i = playerStart; i < slots.size(); i++) {
            Slot slot = slots.get(i);
            if (!slot.getStack().isEmpty())
                client.interactionManager.clickSlot(handler.syncId, slot.id, 0, SlotActionType.QUICK_MOVE, client.player);
        }
    }

    // ── Sandiktan HERSEYi AL ─────────────────────────────────────────────────
    public static void takeAllFromChest(HandledScreen<?> screen, MinecraftClient client) {
        if (client.player == null || client.interactionManager == null) return;
        ScreenHandler handler = screen.getScreenHandler();
        List<Slot> slots = handler.slots;
        int chestEnd = slots.size() - 36;
        for (int i = 0; i < chestEnd; i++) {
            Slot slot = slots.get(i);
            if (!slot.getStack().isEmpty())
                client.interactionManager.clickSlot(handler.syncId, slot.id, 0, SlotActionType.QUICK_MOVE, client.player);
        }
    }

    // ── HERSEYi AT (tum envanter) ────────────────────────────────────────────
    public static void dropAllFromInventory(HandledScreen<?> screen, MinecraftClient client) {
        if (client.player == null || client.interactionManager == null) return;
        ScreenHandler handler = screen.getScreenHandler();
        List<Slot> slots = handler.slots;
        int playerStart = slots.size() - 36;
        for (int i = playerStart; i < slots.size(); i++) {
            Slot slot = slots.get(i);
            if (!slot.getStack().isEmpty())
                client.interactionManager.clickSlot(handler.syncId, slot.id, 1, SlotActionType.THROW, client.player);
        }
    }

    // ── COPLERi AT ───────────────────────────────────────────────────────────
    // Atar: deri zirh koruma 1, kazma/kilic/set ekipman degil ama cop olanlar,
    //       biftek, toprak, cobblestone, vb. cop esyalar
    public static void dropJunkItems(HandledScreen<?> screen, MinecraftClient client) {
        if (client.player == null || client.interactionManager == null) return;
        ScreenHandler handler = screen.getScreenHandler();
        List<Slot> slots = handler.slots;
        int playerStart = slots.size() - 36;
        for (int i = playerStart; i < slots.size(); i++) {
            Slot slot = slots.get(i);
            ItemStack stack = slot.getStack();
            if (!stack.isEmpty() && isJunk(stack))
                client.interactionManager.clickSlot(handler.syncId, slot.id, 1, SlotActionType.THROW, client.player);
        }
    }

    // ── OTO EKiPMAN ──────────────────────────────────────────────────────────
    public static void autoEquipBest(HandledScreen<?> screen, MinecraftClient client) {
        if (client.player == null || client.interactionManager == null) return;
        ScreenHandler handler = screen.getScreenHandler();
        List<Slot> slots = handler.slots;
        int playerStart = slots.size() - 36;
        for (int i = playerStart; i < slots.size(); i++) {
            Slot slot = slots.get(i);
            ItemStack stack = slot.getStack();
            if (!stack.isEmpty() && isEquipment(stack))
                client.interactionManager.clickSlot(handler.syncId, slot.id, 0, SlotActionType.QUICK_MOVE, client.player);
        }
    }

    // ── COP KONTROL ──────────────────────────────────────────────────────────
    // Cop = deri zirh (leather), cop yiyecekler, cope malzemeler
    // Cop DEGIL = demir/altin/elmas/netherite zirh, kilic, kazma, balta, ok, bok
    public static boolean isJunk(ItemStack stack) {
        String id = stack.getItem().toString().toLowerCase();

        // Kesinlikle cop degil — iyi ekipman
        if (isGoodEquipment(id)) return false;

        // Cop yiyecekler
        if (stack.isOf(Items.ROTTEN_FLESH))    return true;
        if (stack.isOf(Items.SPIDER_EYE))      return true;
        if (stack.isOf(Items.POISONOUS_POTATO)) return true;
        if (stack.isOf(Items.BEEF))            return true;  // pismemis biftek
        if (stack.isOf(Items.PORKCHOP))        return true;
        if (stack.isOf(Items.MUTTON))          return true;
        if (stack.isOf(Items.CHICKEN))         return true;
        if (stack.isOf(Items.RABBIT))          return true;

        // Cop bloklar
        if (stack.isOf(Items.DIRT))            return true;
        if (stack.isOf(Items.GRAVEL))          return true;
        if (stack.isOf(Items.SAND))            return true;
        if (stack.isOf(Items.COBBLESTONE))     return true;
        if (stack.isOf(Items.COBBLED_DEEPSLATE)) return true;
        if (stack.isOf(Items.NETHERRACK))      return true;
        if (stack.isOf(Items.STONE))           return true;
        if (stack.isOf(Items.ANDESITE))        return true;
        if (stack.isOf(Items.DIORITE))         return true;
        if (stack.isOf(Items.GRANITE))         return true;

        // Cop malzeme
        if (stack.isOf(Items.STICK))           return true;
        if (stack.isOf(Items.STRING))          return true;
        if (stack.isOf(Items.BONE))            return true;
        if (stack.isOf(Items.GUNPOWDER))       return true;
        if (stack.isOf(Items.FLINT))           return true;
        if (stack.isOf(Items.INK_SAC))         return true;

        // Deri zirh — her zaman cop (koruma 1 genelde deri olur)
        if (id.contains("leather_helmet"))     return true;
        if (id.contains("leather_chestplate")) return true;
        if (id.contains("leather_leggings"))   return true;
        if (id.contains("leather_boots"))      return true;

        // Tas aletler — cop
        if (id.contains("stone_sword"))        return true;
        if (id.contains("stone_pickaxe"))      return true;
        if (id.contains("stone_axe"))          return true;
        if (id.contains("stone_shovel"))       return true;
        if (id.contains("stone_hoe"))          return true;

        // Ahsap aletler — cop
        if (id.contains("wooden_sword"))       return true;
        if (id.contains("wooden_pickaxe"))     return true;
        if (id.contains("wooden_axe"))         return true;

        return false;
    }

    // iyi ekipman = demir, altin, elmas, netherite
    private static boolean isGoodEquipment(String id) {
        return (id.contains("iron_") || id.contains("golden_") || id.contains("diamond_") || id.contains("netherite_"))
            && (id.contains("helmet") || id.contains("chestplate") || id.contains("leggings") || id.contains("boots")
                || id.contains("sword") || id.contains("pickaxe") || id.contains("axe") || id.contains("bow")
                || id.contains("crossbow") || id.contains("trident") || id.contains("shield"));
    }

    private static boolean isEquipment(ItemStack stack) {
        String id = stack.getItem().toString().toLowerCase();
        return id.contains("helmet") || id.contains("chestplate") || id.contains("leggings")
            || id.contains("boots") || id.contains("sword") || id.contains("axe")
            || id.contains("bow") || id.contains("crossbow") || id.contains("trident") || id.contains("shield");
    }
}
