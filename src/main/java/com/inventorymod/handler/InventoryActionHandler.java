package com.inventorymod.handler;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

import java.util.ArrayList;
import java.util.List;

/**
 * Tüm envanter işlemlerini yöneten sınıf.
 * 
 * SORUN ANALİZİ VE DÜZELTMELER:
 * - put_all (Hepsini Koy): Player inventory slotlarını (36-71 arası) chest slotlarına (0-N) taşır.
 *   BUG: Yanlış slot index kullanımı düzeltildi. QUICK_MOVE action tipi kullanılıyor.
 * - take_all (Hepsini Al): Chest slotlarını player inventory'ye taşır.
 *   BUG: Slot range hesabı düzeltildi.
 * - drop_all (Hepsini At): Gerçek drop işlemi yapar, silme değil.
 *   BUG: SlotActionType.THROW kullanılması gerekiyor, DELETE değil.
 * - drop_junk (Çöpleri At): Değersiz itemleri atar.
 * - auto_equip (Oto Ekipman): En iyi ekipmanı otomatik giyer.
 */
public class InventoryActionHandler {

    /**
     * Envanterden sandığa HERŞEYİ KOY
     * Oyuncunun envanterindeki tüm itemleri sandığa taşır.
     */
    public static void putAllToChest(HandledScreen<?> screen, MinecraftClient client) {
        if (client.player == null || client.interactionManager == null) return;

        ScreenHandler handler = screen.getScreenHandler();
        List<Slot> slots = handler.slots;

        // Önce player inventory slotlarını bul (chest slotlarından sonra gelir)
        // Vanilla: Chest 3x9=27 slot (index 0-26), Player inventory 27-62, Hotbar 63-71
        // Büyük Chest: 6x9=54 slot (index 0-53), Player inventory 54-89, Hotbar 90-98

        int totalSlots = slots.size();
        // Player slotları her zaman son 36 slottur (27 inventory + 9 hotbar)
        int playerStartIndex = totalSlots - 36;

        // Player inventory'deki her slotu tara
        for (int i = playerStartIndex; i < totalSlots; i++) {
            Slot slot = slots.get(i);
            if (!slot.getStack().isEmpty()) {
                // QUICK_MOVE = Shift+Click, itemi karşı tarafa taşır
                // Bu işlem oyuncudan sandığa doğru çalışır
                client.interactionManager.clickSlot(
                    handler.syncId,
                    slot.id,
                    0,
                    SlotActionType.QUICK_MOVE,
                    client.player
                );
            }
        }
    }

    /**
     * Sandıktan envanteri HERŞEYİ AL
     * Sandıktaki tüm itemleri oyuncu envanterine taşır.
     */
    public static void takeAllFromChest(HandledScreen<?> screen, MinecraftClient client) {
        if (client.player == null || client.interactionManager == null) return;

        ScreenHandler handler = screen.getScreenHandler();
        List<Slot> slots = handler.slots;

        int totalSlots = slots.size();
        // Chest slotları her zaman başta gelir (ilk N slot)
        int playerStartIndex = totalSlots - 36;
        // Sadece chest slotlarını (0'dan playerStartIndex'e kadar) tara
        int chestSlotCount = playerStartIndex;

        for (int i = 0; i < chestSlotCount; i++) {
            Slot slot = slots.get(i);
            if (!slot.getStack().isEmpty()) {
                // QUICK_MOVE sandıktan envanteri alır
                client.interactionManager.clickSlot(
                    handler.syncId,
                    slot.id,
                    0,
                    SlotActionType.QUICK_MOVE,
                    client.player
                );
            }
        }
    }

    /**
     * Envanteri HERŞEYİ AT (yere düşür, silme değil!)
     * CRITICAL BUG FIX: Önceki kod SlotActionType.DELETE kullanıyordu,
     * bu itemleri tamamen yok ediyordu. THROW kullanarak yere bırakıyoruz.
     */
    public static void dropAllFromInventory(HandledScreen<?> screen, MinecraftClient client) {
        if (client.player == null || client.interactionManager == null) return;

        ScreenHandler handler = screen.getScreenHandler();
        List<Slot> slots = handler.slots;

        int totalSlots = slots.size();
        int playerStartIndex = totalSlots - 36;

        for (int i = playerStartIndex; i < totalSlots; i++) {
            Slot slot = slots.get(i);
            if (!slot.getStack().isEmpty()) {
                // button=1 = Ctrl+Q ile tüm stack'i at
                // SlotActionType.THROW = gerçekten yere bırakır
                client.interactionManager.clickSlot(
                    handler.syncId,
                    slot.id,
                    1, // 1 = tüm stack'i at (0 = tek item atar)
                    SlotActionType.THROW,
                    client.player
                );
            }
        }
    }

    /**
     * ÇÖPLERI AT - Değersiz/işe yaramaz itemleri atar
     * Çöp olarak sayılan itemler: çubuk, taş, kir, kum, çakıl, tahta vb.
     */
    public static void dropJunkItems(HandledScreen<?> screen, MinecraftClient client) {
        if (client.player == null || client.interactionManager == null) return;

        ScreenHandler handler = screen.getScreenHandler();
        List<Slot> slots = handler.slots;

        int totalSlots = slots.size();
        int playerStartIndex = totalSlots - 36;

        for (int i = playerStartIndex; i < totalSlots; i++) {
            Slot slot = slots.get(i);
            ItemStack stack = slot.getStack();
            if (!stack.isEmpty() && isJunk(stack)) {
                client.interactionManager.clickSlot(
                    handler.syncId,
                    slot.id,
                    1,
                    SlotActionType.THROW,
                    client.player
                );
            }
        }
    }

    /**
     * OTO EKİPMAN - En iyi zırhı/silahı otomatik giyer
     * Shift+Click ile ekipman slotlarına taşır
     */
    public static void autoEquipBest(HandledScreen<?> screen, MinecraftClient client) {
        if (client.player == null || client.interactionManager == null) return;

        ScreenHandler handler = screen.getScreenHandler();
        List<Slot> slots = handler.slots;

        int totalSlots = slots.size();
        int playerStartIndex = totalSlots - 36;

        // Zırh ve silah itemlerini bul ve giy
        for (int i = playerStartIndex; i < totalSlots; i++) {
            Slot slot = slots.get(i);
            ItemStack stack = slot.getStack();
            if (!stack.isEmpty() && isEquipment(stack)) {
                // QUICK_MOVE zırh/silah itemlerini doğrudan ekipman slotuna taşır
                client.interactionManager.clickSlot(
                    handler.syncId,
                    slot.id,
                    0,
                    SlotActionType.QUICK_MOVE,
                    client.player
                );
            }
        }
    }

    // ============ YARDIMCI METODLAR ============

    /**
     * Çöp item kontrolü - genişletilebilir liste
     */
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
            || stack.isOf(Items.GUNPOWDER);  // Eğer lazım değilse eklenebilir
    }

    /**
     * Ekipman item kontrolü
     */
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
