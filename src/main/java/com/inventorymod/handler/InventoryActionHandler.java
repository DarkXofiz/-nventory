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

    // -------------------------------------------------------------------------
    // PV (Personal Vault) tespiti
    // PV1 = 27 slot, PV2 = 54 slot sunucu tarafli ozel envanter.
    // Baslik "vault", "pv", "personal" iceriyorsa PV sayilir.
    // Hicbir sunucu-mod import'u kullanilmaz — sadece baslik ve slot sayisi.
    // -------------------------------------------------------------------------
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

    /**
     * Ekranin sunucu-tarafli mi oldugunu anlar.
     * Ender Chest, PV1, PV2, ozel sunucu kasalari hepsi buraya girer.
     * Kontrol: handler slot sayisi > 36 VEYA baslikta PV ifadesi var.
     */
    private static boolean isServerSideScreen(HandledScreen<?> screen, MinecraftClient client) {
        if (client.player == null) return false;
        ScreenHandler handler = screen.getScreenHandler();

        // Eger hic player inventory slot'u yoksa kesinlikle sunucu tarafli
        for (Slot slot : handler.slots) {
            if (slot.inventory == client.player.getInventory()) {
                // En az bir player slotu var — normal/karma ekran
                return false;
            }
        }
        return true;
    }

    // -------------------------------------------------------------------------
    // SLOT BELIRLEME
    // -------------------------------------------------------------------------

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
            // Fallback: son 36 slot (sunucu tarafli ekranlar icin)
            List<Slot> all = handler.slots;
            int total = all.size();
            InventoryMod.LOGGER.info("[InvMod] Player slots bulunamadi, fallback son 36: total={}", total);
            if (total >= 36) {
                result.addAll(all.subList(total - 36, total));
            }
        } else {
            InventoryMod.LOGGER.info("[InvMod] Player slots bulundu: {}", result.size());
        }

        return result;
    }

    /**
     * Chest/PV slotlarini dondurur.
     * PV1 = ilk 27 slot, PV2 = ilk 54 slot (toplam - 36 = chest kismi).
     */
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

        // Sunucu-tarafli ekran (PV1/PV2 gibi) — player slot referansi yok
        if (!hasPlayerSlots && handler.slots.size() >= 36) {
            result.clear();
            result.addAll(handler.slots.subList(0, handler.slots.size() - 36));
            InventoryMod.LOGGER.info("[InvMod] Sunucu ekrani chest slotlari: {}", result.size());
        }

        return result;
    }

    // -------------------------------------------------------------------------
    // DROP MANTIGI
    // -------------------------------------------------------------------------

    private static boolean isServerSideSlot(Slot slot, MinecraftClient client) {
        if (client.player == null) return false;
        return slot.inventory != client.player.getInventory();
    }

    /**
     * Tek slot drop:
     *
     * [Player inventory]  -> Direkt THROW (her zaman calisir)
     * [Ender Chest / PV]  -> 3 adimli guvenli yontem:
     *   1. Slot'u mouse'a al (PICKUP, button=0)
     *   2. Envanter disina tasi — pencere disina THROW
     *   3. Eger 2. basarisiz olursa QUICK_MOVE ile envantere cek, sonra THROW
     *
     * PV1/PV2'de QUICK_MOVE sunucu tarafindan engellenebilir.
     * Bu yuzden once pickup+throw deneniri, olmuyorsa QUICK_MOVE fallback.
     */
    private static void dropSlot(HandledScreen<?> screen, MinecraftClient client, Slot slot) {
        if (client.player == null || client.interactionManager == null) return;
        ScreenHandler handler = screen.getScreenHandler();

        if (!isServerSideSlot(slot, client)) {
            // Normal player inventory — direkt Ctrl+Q (button=1, THROW)
            client.interactionManager.clickSlot(
                handler.syncId, slot.id, 1, SlotActionType.THROW, client.player);
            InventoryMod.LOGGER.info("[InvMod] DROP(player) slot={} item={}", slot.id, slot.getStack().getItem());
            return;
        }

        // Sunucu tarafli slot (Ender Chest, PV1, PV2, ozel kasa)
        ItemStack before = slot.getStack().copy();
        if (before.isEmpty()) return;

        boolean isPV = isPersonalVault(screen);
        InventoryMod.LOGGER.info("[InvMod] ServerSide slot={} item={} isPV={}", slot.id, before.getItem(), isPV);

        // --- Yontem 1: Pickup + pencere-disi birak (slot=-999) ---
        // Bu en evrensel yontemdir, PV'de de genelde calisir
        client.interactionManager.clickSlot(
            handler.syncId, slot.id, 0, SlotActionType.PICKUP, client.player);

        // El'de item var mi kontrol et (cursor stack)
        // Not: cursor stack'e dogrudan erisim versiyona gore degisir,
        // bu yuzden kisa bekleme sonrasi -999 slot'a birakiyoruz
        client.interactionManager.clickSlot(
            handler.syncId, -999, 0, SlotActionType.PICKUP, client.player);

        InventoryMod.LOGGER.info("[InvMod] DROP(server pickup+throw) slot={} item={}", slot.id, before.getItem());

        // --- Yontem 2 (Fallback): QUICK_MOVE -> envantere cek -> THROW ---
        // Slot hala dolu mu? (PICKUP basarisiz olduysa)
        // Client-side slot stack anlık guncellenebilir, kisa gecikme sonrasi kontrol
        // Fabric'te scheduleTask ile bir tick sonraya birakmak gerekebilir
        // Burada senkron yapiyoruz — cap olmayan durumlarda yeterli
        ItemStack after = slot.getStack();
        if (!after.isEmpty() && after.getItem() == before.getItem()) {
            // Slot hala dolu, QUICK_MOVE dene
            client.interactionManager.clickSlot(
                handler.syncId, slot.id, 0, SlotActionType.QUICK_MOVE, client.player);
            InventoryMod.LOGGER.info("[InvMod] DROP fallback QUICK_MOVE slot={}", slot.id);

            // Player envanterinde bu item'i bul ve THROW ile at
            for (Slot ps : handler.slots) {
                if (ps.inventory == client.player.getInventory()) {
                    ItemStack s = ps.getStack();
                    if (!s.isEmpty() && s.getItem() == before.getItem()) {
                        client.interactionManager.clickSlot(
                            handler.syncId, ps.id, 1, SlotActionType.THROW, client.player);
                        InventoryMod.LOGGER.info("[InvMod] DROP fallback THROW playerSlot={}", ps.id);
                        break;
                    }
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // PUBLIC API
    // -------------------------------------------------------------------------

    /**
     * Envanterdeki tum itemleri actik kasaya/PV'ye koyar.
     * PV1/PV2/Ender Chest hepsinde calisir.
     */
    public static void putAllToChest(HandledScreen<?> screen, MinecraftClient client) {
        if (client.player == null || client.interactionManager == null) return;
        ScreenHandler handler = screen.getScreenHandler();
        for (Slot slot : getPlayerSlots(screen, client)) {
            if (!slot.getStack().isEmpty()) {
                client.interactionManager.clickSlot(
                    handler.syncId, slot.id, 0, SlotActionType.QUICK_MOVE, client.player);
            }
        }
        InventoryMod.LOGGER.info("[InvMod] putAllToChest tamamlandi (PV/EC destekli)");
    }

    /**
     * Actik kasadan/PV'den tum itemleri envantere alir.
     * PV1 (27 slot) ve PV2 (54 slot) otomatik tespit edilir.
     */
    public static void takeAllFromChest(HandledScreen<?> screen, MinecraftClient client) {
        if (client.player == null || client.interactionManager == null) return;
        ScreenHandler handler = screen.getScreenHandler();
        List<Slot> chestSlots = getChestSlots(screen, client);
        InventoryMod.LOGGER.info("[InvMod] takeAll: {} slot (PV={}) bulundu",
            chestSlots.size(), isPersonalVault(screen));
        for (Slot slot : chestSlots) {
            if (!slot.getStack().isEmpty()) {
                client.interactionManager.clickSlot(
                    handler.syncId, slot.id, 0, SlotActionType.QUICK_MOVE, client.player);
            }
        }
    }

    /**
     * Tum itemleri yere atar.
     * Player envanter + acik kasa/PV/Ender Chest slotlari dahil.
     */
    public static void dropAllFromInventory(HandledScreen<?> screen, MinecraftClient client) {
        if (client.player == null || client.interactionManager == null) return;

        List<Slot> playerSlots = getPlayerSlots(screen, client);
        InventoryMod.LOGGER.info("[InvMod] dropAll player: {} slot", playerSlots.size());
        for (Slot slot : playerSlots) {
            if (!slot.getStack().isEmpty()) dropSlot(screen, client, slot);
        }

        List<Slot> chestSlots = getChestSlots(screen, client);
        if (!chestSlots.isEmpty()) {
            InventoryMod.LOGGER.info("[InvMod] dropAll chest/PV: {} slot", chestSlots.size());
            for (Slot slot : chestSlots) {
                if (!slot.getStack().isEmpty()) dropSlot(screen, client, slot);
            }
        }
    }

    /**
     * Cop itemleri atar.
     * Player envanter + acik kasa/PV/Ender Chest slotlari dahil.
     */
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

    /**
     * En iyi ekipmani giyer.
     * QUICK_MOVE zirhi/silahi dogru slota gonderir.
     */
    public static void autoEquipBest(HandledScreen<?> screen, MinecraftClient client) {
        if (client.player == null || client.interactionManager == null) return;
        ScreenHandler handler = screen.getScreenHandler();

        for (Slot slot : getPlayerSlots(screen, client)) {
            ItemStack stack = slot.getStack();
            if (!stack.isEmpty() && isEquipment(stack)) {
                client.interactionManager.clickSlot(
                    handler.syncId, slot.id, 0, SlotActionType.QUICK_MOVE, client.player);
                InventoryMod.LOGGER.info("[InvMod] Equip: slot={} item={}", slot.id, stack.getItem());
            }
        }
    }

    // -------------------------------------------------------------------------
    // ITEM SINIFLANDIRMA
    // -------------------------------------------------------------------------

    public static boolean isJunk(ItemStack stack) {
        String id = stack.getItem().toString().toLowerCase();

        // --- Tum materyallerde kilic, kazma, balta, kuprek, zirh — cop ---
        // Ahsap
        if (id.contains("wooden_sword"))     return true;
        if (id.contains("wooden_pickaxe"))   return true;
        if (id.contains("wooden_axe"))       return true;
        if (id.contains("wooden_shovel"))    return true;
        if (id.contains("wooden_hoe"))       return true;
        // Tas
        if (id.contains("stone_sword"))      return true;
        if (id.contains("stone_pickaxe"))    return true;
        if (id.contains("stone_axe"))        return true;
        if (id.contains("stone_shovel"))     return true;
        if (id.contains("stone_hoe"))        return true;
        // Altin
        if (id.contains("golden_sword"))     return true;
        if (id.contains("golden_pickaxe"))   return true;
        if (id.contains("golden_axe"))       return true;
        if (id.contains("golden_shovel"))    return true;
        if (id.contains("golden_hoe"))       return true;
        if (id.contains("golden_helmet"))    return true;
        if (id.contains("golden_chestplate"))return true;
        if (id.contains("golden_leggings"))  return true;
        if (id.contains("golden_boots"))     return true;
        // Elmas kilic ve elmas kazma -> cop
        if (id.contains("diamond_sword"))    return true;
        if (id.contains("diamond_pickaxe"))  return true;

        // Demir
        if (id.contains("iron_helmet"))      return true;
        if (id.contains("iron_chestplate"))  return true;
        if (id.contains("iron_leggings"))    return true;
        if (id.contains("iron_boots"))       return true;
        if (id.contains("iron_sword"))       return true;
        if (id.contains("iron_pickaxe"))     return true;
        if (id.contains("iron_axe"))         return true;
        if (id.contains("iron_shovel"))      return true;
        if (id.contains("iron_hoe"))         return true;
        // Zincir + Deri
        if (id.contains("chainmail_"))       return true;
        if (id.contains("leather_"))         return true;

        // --- Enchant kontrolu gerektiren itemler ---
        if (hasJunkEnchants(stack)) return true;

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

    /**
     * Cop enchant kontrolu:
     * Koruma/Keskinlik/Verimlilik 1-20 arasi -> cop
     *
     * HARIÇ TUTULANLAR (hic dokunulmuyor):
     *   - netherite_sword, netherite_pickaxe, netherite_axe + tum netherite set
     *   - diamond_sword,   diamond_pickaxe,   diamond_axe   + tum diamond set
     */
    private static boolean hasJunkEnchants(ItemStack stack) {
        if (!stack.hasEnchantments()) return false;

        String id = stack.getItem().toString().toLowerCase();

        // Netherite her sey -> hic dokunma
        if (id.contains("netherite_")) return false;

        // Elmas: sadece balta ve set/zirh/yay/kalkan -> hic dokunma
        // Elmas kilic ve elmas kazma -> enchant kontrolune devam et (cop olabilir)
        if (id.contains("diamond_axe"))        return false;
        if (id.contains("diamond_helmet"))     return false;
        if (id.contains("diamond_chestplate")) return false;
        if (id.contains("diamond_leggings"))   return false;
        if (id.contains("diamond_boots"))      return false;
        if (id.contains("diamond_bow"))        return false;
        if (id.contains("diamond_crossbow"))   return false;
        if (id.contains("diamond_shield"))     return false;
        if (id.contains("diamond_trident"))    return false;
        // diamond_sword ve diamond_pickaxe -> enchant kontrolune devam

        net.minecraft.nbt.NbtList enchants = stack.getEnchantments();
        for (int i = 0; i < enchants.size(); i++) {
            net.minecraft.nbt.NbtCompound e = enchants.getCompound(i);
            String enchId = e.getString("id").toLowerCase();
            int lvl = e.getInt("lvl");

            // Koruma 1-20 -> cop (netherite/diamond zirh zaten yukarda atlatildi)
            if (enchId.contains("protection") && lvl >= 1 && lvl <= 20) return true;

            // Keskinlik 1-20 -> cop (netherite/diamond kilic zaten yukarda atlatildi)
            if (enchId.contains("sharpness") && lvl >= 1 && lvl <= 20) return true;

            // Verimlilik 1-20 -> cop (netherite/diamond kazma/balta zaten yukarda atlatildi)
            if (enchId.contains("efficiency") && lvl >= 1 && lvl <= 20) return true;
        }
        return false;
    }

    private static boolean isGoodEquipment(String id) {
        // Netherite her sey -> koru
        if (id.contains("netherite_")) return true;

        // Elmas: sadece balta, zirh, yay, kalkan, trident -> koru
        // Elmas kilic (diamond_sword) ve elmas kazma (diamond_pickaxe) -> cop
        if (id.contains("diamond_axe"))        return true;
        if (id.contains("diamond_helmet"))     return true;
        if (id.contains("diamond_chestplate")) return true;
        if (id.contains("diamond_leggings"))   return true;
        if (id.contains("diamond_boots"))      return true;
        if (id.contains("diamond_bow"))        return true;
        if (id.contains("diamond_crossbow"))   return true;
        if (id.contains("diamond_shield"))     return true;
        if (id.contains("diamond_trident"))    return true;
        // diamond_sword -> cop (buraya gelmez, atilir)
        // diamond_pickaxe -> cop (buraya gelmez, atilir)

        return false;
    }

    private static boolean isEquipment(ItemStack stack) {
        String id = stack.getItem().toString().toLowerCase();
        return id.contains("helmet") || id.contains("chestplate")
            || id.contains("leggings") || id.contains("boots")
            || id.contains("sword") || id.contains("axe")
            || id.contains("bow") || id.contains("cros
