package com.inventorymod.handler;

import com.inventorymod.InventoryMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class InventoryActionHandler {

    private static boolean isPersonalVault(HandledScreen<?> screen) {
        try {
            Text title = screen.getTitle();
            if (title == null) return false;
            String t = title.getString().toLowerCase();
            return t.contains("vault") || t.contains("pv")
                || t.contains("personal") || t.contains("kasa");
        } catch (Exception e) {
            return false;
        }
    }

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
            List<Slot> all = handler.slots;
            int total = all.size();
            InventoryMod.LOGGER.info("[InvMod] Player slots not found, fallback last 36: total={}", total);
            if (total >= 36) {
                result.addAll(all.subList(total - 36, total));
            }
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
        if (!hasPlayerSlots && handler.slots.size() >= 36) {
            result.clear();
            result.addAll(handler.slots.subList(0, handler.slots.size() - 36));
        }
        return result;
    }

    private static boolean isServerSideSlot(Slot slot, MinecraftClient client) {
        if (client.player == null) return false;
        return slot.inventory != client.player.getInventory();
    }

    private static void dropSlot(HandledScreen<?> screen, MinecraftClient client, Slot slot) {
        if (client.player == null || client.interactionManager == null) return;
        ScreenHandler handler = screen.getScreenHandler();

        if (!isServerSideSlot(slot, client)) {
            client.interactionManager.clickSlot(
                handler.syncId, slot.id, 1, SlotActionType.THROW, client.player);
            return;
        }

        ItemStack before = slot.getStack().copy();
        if (before.isEmpty()) return;

        client.interactionManager.clickSlot(
            handler.syncId, slot.id, 0, SlotActionType.PICKUP, client.player);
        client.interactionManager.clickSlot(
            handler.syncId, -999, 0, SlotActionType.PICKUP, client.player);

        ItemStack after = slot.getStack();
        if (!after.isEmpty() && after.getItem() == before.getItem()) {
            client.interactionManager.clickSlot(
                handler.syncId, slot.id, 0, SlotActionType.QUICK_MOVE, client.player);
            for (Slot ps : handler.slots) {
                if (ps.inventory == client.player.getInventory()) {
                    ItemStack s = ps.getStack();
                    if (!s.isEmpty() && s.getItem() == before.getItem()) {
                        client.interactionManager.clickSlot(
                            handler.syncId, ps.id, 1, SlotActionType.THROW, client.player);
                        break;
                    }
                }
            }
        }
    }

    public static void putAllToChest(HandledScreen<?> screen, MinecraftClient client) {
        if (client.player == null || client.interactionManager == null) return;
        ScreenHandler handler = screen.getScreenHandler();
        for (Slot slot : getPlayerSlots(screen, client)) {
            if (!slot.getStack().isEmpty()) {
                client.interactionManager.clickSlot(
                    handler.syncId, slot.id, 0, SlotActionType.QUICK_MOVE, client.player);
            }
        }
    }

    public static void takeAllFromChest(HandledScreen<?> screen, MinecraftClient client) {
        if (client.player == null || client.interactionManager == null) return;
        ScreenHandler handler = screen.getScreenHandler();
        for (Slot slot : getChestSlots(screen, client)) {
            if (!slot.getStack().isEmpty()) {
                client.interactionManager.clickSlot(
                    handler.syncId, slot.id, 0, SlotActionType.QUICK_MOVE, client.player);
            }
        }
    }

    public static void dropAllFromInventory(HandledScreen<?> screen, MinecraftClient client) {
        if (client.player == null || client.interactionManager == null) return;
        for (Slot slot : getPlayerSlots(screen, client)) {
            if (!slot.getStack().isEmpty()) dropSlot(screen, client, slot);
        }
        List<Slot> chestSlots = getChestSlots(screen, client);
        if (!chestSlots.isEmpty()) {
            for (Slot slot : chestSlots) {
                if (!slot.getStack().isEmpty()) dropSlot(screen, client, slot);
            }
        }
    }

    public static void dropJunkItems(HandledScreen<?> screen, MinecraftClient client) {
        if (client.player == null || client.interactionManager == null) return;
        for (Slot slot : getPlayerSlots(screen, client)) {
            ItemStack stack = slot.getStack();
            if (!stack.isEmpty() && isJunk(stack)) dropSlot(screen, client, slot);
        }
        List<Slot> chestSlots = getChestSlots(screen, client);
        if (!chestSlots.isEmpty()) {
            for (Slot slot : chestSlots) {
                ItemStack stack = slot.getStack();
                if (!stack.isEmpty() && isJunk(stack)) dropSlot(screen, client, slot);
            }
        }
    }

    public static void autoEquipBest(HandledScreen<?> screen, MinecraftClient client) {
        if (client.player == null || client.interactionManager == null) return;
        ScreenHandler handler = screen.getScreenHandler();
        for (Slot slot : getPlayerSlots(screen, client)) {
            ItemStack stack = slot.getStack();
            if (!stack.isEmpty() && isEquipment(stack)) {
                client.interactionManager.clickSlot(
                    handler.syncId, slot.id, 0, SlotActionType.QUICK_MOVE, client.player);
            }
        }
    }

    public static boolean isJunk(ItemStack stack) {
        String id = stack.getItem().toString().toLowerCase();

        // Netherite -> never junk
        if (id.contains("netherite_")) return false;

        // Diamond axe -> never junk
        if (id.contains("diamond_axe")) return false;

        // Diamond sword and pickaxe -> junk
        if (id.contains("diamond_sword"))   return true;
        if (id.contains("diamond_pickaxe")) return true;

        // All wooden tools
        if (id.contains("wooden_sword"))   return true;
        if (id.contains("wooden_pickaxe")) return true;
        if (id.contains("wooden_axe"))     return true;
        if (id.contains("wooden_shovel"))  return true;
        if (id.contains("wooden_hoe"))     return true;

        // All stone tools
        if (id.contains("stone_sword"))   return true;
        if (id.contains("stone_pickaxe")) return true;
        if (id.contains("stone_axe"))     return true;
        if (id.contains("stone_shovel"))  return true;
        if (id.contains("stone_hoe"))     return true;

        // All golden gear
        if (id.contains("golden_sword"))      return true;
        if (id.contains("golden_pickaxe"))    return true;
        if (id.contains("golden_axe"))        return true;
        if (id.contains("golden_shovel"))     return true;
        if (id.contains("golden_hoe"))        return true;
        if (id.contains("golden_helmet"))     return true;
        if (id.contains("golden_chestplate")) return true;
        if (id.contains("golden_leggings"))   return true;
        if (id.contains("golden_boots"))      return true;

        // All iron gear
        if (id.contains("iron_helmet"))      return true;
        if (id.contains("iron_chestplate"))  return true;
        if (id.contains("iron_leggings"))    return true;
        if (id.contains("iron_boots"))       return true;
        if (id.contains("iron_sword"))       return true;
        if (id.contains("iron_pickaxe"))     return true;
        if (id.contains("iron_axe"))         return true;
        if (id.contains("iron_shovel"))      return true;
        if (id.contains("iron_hoe"))         return true;

        // Chainmail and leather armor
        if (id.contains("chainmail_")) return true;
        if (id.contains("leather_"))   return true;

        // Check enchantments (Protection/Sharpness/Efficiency 1-20 on non-protected items)
        if (hasJunkEnchants(stack)) return true;

        // Common junk items
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

        return false;
    }

    private static boolean hasJunkEnchants(ItemStack stack) {
        if (!stack.hasEnchantments()) return false;
        String id = stack.getItem().toString().toLowerCase();

        // Netherite -> never junk regardless of enchant
        if (id.contains("netherite_")) return false;

        // Diamond axe -> never junk
        if (id.contains("diamond_axe")) return false;

        net.minecraft.nbt.NbtList enchants = stack.getEnchantments();
        for (int i = 0; i < enchants.size(); i++) {
            net.minecraft.nbt.NbtCompound e = enchants.getCompound(i);
            String enchId = e.getString("id").toLowerCase();
            int lvl = e.getInt("lvl");

            if (enchId.contains("protection") && lvl >= 1 && lvl <= 20) return true;
            if (enchId.contains("sharpness")  && lvl >= 1 && lvl <= 20) return true;
            if (enchId.contains("efficiency") && lvl >= 1 && lvl <= 20) return true;
        }
        return false;
    }

    private static boolean isEquipment(ItemStack stack) {
        String id = stack.getItem().toString().toLowerCase();
        return id.contains("helmet")     || id.contains("chestplate")
            || id.contains("leggings")   || id.contains("boots")
            || id.contains("sword")      || id.contains("axe")
            || id.contains("bow")        || id.contains("crossbow")
            || id.contains("trident")    || id.contains("shield");
    }
}
