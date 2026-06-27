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
     * Her zaman oyuncunun 36 slotunu bul.
     * Strateji: once player.getInventory() eslemesi, sonra son 36 slot fallback.
     * DEBUG logu ile hangi slotlarin secildigini gosterir.
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

        if (result.isEmpty()) {
            // Fallback: son 36 slot
            List<Slot> all = handler.slots;
            int total = all.size();
            InventoryMod.LOGGER.info("[InvMod] Player slots bulunamadi, fallback: toplam slot={}, son 36 aliniyor", total);
            if (total >= 36) {
                result.addAll(all.subList(total - 36, total));
            }
        } else {
            InventoryMod.LOGGER.info("[InvMod] Player slots bulundu: {} slot", result.size());
        }

        return result;
    }

    private static List<Slot> getChestSlots(HandledScreen<?> screen, MinecraftClient client) {
        if (client.player == null) return List.of();
        ScreenHandler handler = screen.getScreenHandler();
        List<Slot> result = new ArrayList<>();
        boolean hasPlayerSlots = false;

        for (Slot slot : handler.slots) {
            if (slot.inventory == client.player.getInventory()) {
                hasPlayerSlots = true;
            } else {
                result.add(slot);
            }
        }

        // Sunucu tarafli ekran — ilk (total-36) slotu chest say
        if (!hasPlayerSlots && handler.slots.size() >= 36) {
            result.clear();
            result.addAll(handler.slots.subList(0, handler.slots.size() - 36));
        }

        return result;
    }

    /**
     * Drop yontemi: SlotActionType.THROW ile tum stack'i at.
     * Bu client-side envanter slotlari icin her zaman calisir.
     * Ender Chest/PV2 acikken bile envanter slotlari CLIENT taraflidir.
     */
    private static void dropSlot(HandledScreen<?> screen, MinecraftClient client, Slot slot) {
        if (client.player == null || client.interactionManager == null) return;
        ScreenHandler handler = screen.getScreenHandler();

        // button=1, SlotActionType.THROW = tum stack'i yere at (Ctrl+Q gibi)
        client.interactionManager.clickSlot(
            handler.syncId, slot.id, 1, SlotActionType.THROW, client.player);

        InventoryMod.LOGGER.info("[InvMod] Drop slot id={} item={}", slot.id, slot.getStack().getItem());
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
        List<Slot> slots = getPlayerSlots(screen, client);
        InventoryMod.LOGGER.info("[InvMod] dropAll: {} slot bulundu", slots.size());
        for (Slot slot : slots) {
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

    /**
     * OTO EKIPMAN — zirh ve silah giyer.
     * QUICK_MOVE client-side envanterden ekipman slotuna tasir, sunucu onaylar.
     * SW gibi sunucularda ekipman degistirme yasak olabilir — o zaman giymiyor.
     */
    public static void autoEquipBest(HandledScreen<?> screen, MinecraftClient client) {
        if (client.player == null || client.interactionManager == null) return;
        ScreenHandler handler = screen.getScreenHandler();

        // Envanter ekraninda (E tusu) slot 0-8 = armor+offhand+craft
        // QUICK_MOVE zirhi dogru slota gonderir
        for (Slot slot : getPlayerSlots(screen, client)) {
            ItemStack stack = slot.getStack();
            if (!stack.isEmpty() && isEquipment(stack)) {
                client.interactionManager.clickSlot(
                    handler.syncId, slot.id, 0, SlotActionType.QUICK_MOVE, client.player);
                InventoryMod.LOGGER.info("[InvMod] Equip: slot={} item={}", slot.id, stack.getItem());
            }
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
            
