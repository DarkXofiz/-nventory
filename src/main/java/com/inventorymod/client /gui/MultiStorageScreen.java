package com.inventorymod.client.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import java.util.ArrayList;
import java.util.List;

public class MultiStorageScreen extends Screen {

    // Aktif sekme: 0=Geniş Sandık, 1=Ender Chest, 2=PV
    private int activeTab = 0;

    // Her sekme için 54 slotluk depo (6x9)
    private final List<ItemStack>[] storageInventories = new List[3];

    // Oyuncu envanteri (27 slot + 9 hotbar) — tüm sekmelerde aynı
    private List<ItemStack> playerInventory = new ArrayList<>();
    private List<ItemStack> playerHotbar   = new ArrayList<>();

    // GUI konumları
    private int guiLeft, guiTop;
    private static final int GUI_WIDTH  = 460;
    private static final int GUI_HEIGHT = 340;

    // Slot boyutu
    private static final int SLOT_SIZE = 18;

    // Tab isimleri
    private static final String[] TAB_NAMES = {"Geniş Sandık", "Ender Chest", "PV"};

    // Seçili slot (sürükle-bırak için)
    private int heldFromTab   = -1;
    private int heldFromIndex = -1;
    private ItemStack heldStack = ItemStack.EMPTY;

    public MultiStorageScreen() {
        super(Text.literal("Envanter"));

        // Depoları başlat
        for (int t = 0; t < 3; t++) {
            storageInventories[t] = new ArrayList<>();
            for (int i = 0; i < 54; i++) {
                storageInventories[t].add(ItemStack.EMPTY);
            }
        }

        // Oyuncu envanterini başlat
        for (int i = 0; i < 27; i++) playerInventory.add(ItemStack.EMPTY);
        for (int i = 0; i < 9;  i++) playerHotbar.add(ItemStack.EMPTY);
    }

    @Override
    protected void init() {
        guiLeft = (this.width  - GUI_WIDTH)  / 2;
        guiTop  = (this.height - GUI_HEIGHT) / 2;

        addButtons();
    }

    private void addButtons() {
        clearChildren();

        int btnX = guiLeft + GUI_WIDTH - 115;
        int btnY = guiTop + 30;
        int btnW = 110;
        int btnH = 16;
        int gap  = 4;

        addDrawableChild(ButtonWidget.builder(Text.literal("Herşeyi At"),
                b -> actionThrowAll()).dimensions(btnX, btnY, btnW, btnH).build());
        btnY += btnH + gap;

        addDrawableChild(ButtonWidget.builder(Text.literal("Oto Ekipman"),
                b -> actionAutoEquip()).dimensions(btnX, btnY, btnW, btnH).build());
        btnY += btnH + gap;

        addDrawableChild(ButtonWidget.builder(Text.literal("Herşeyi Koy"),
                b -> actionPutAll()).dimensions(btnX, btnY, btnW, btnH).build());
        btnY += btnH + gap;

        addDrawableChild(ButtonWidget.builder(Text.literal("Herşeyi Al"),
                b -> actionTakeAll()).dimensions(btnX, btnY, btnW, btnH).build());
        btnY += btnH + gap;

        addDrawableChild(ButtonWidget.builder(Text.literal("Çöpleri At"),
                b -> actionThrowJunk()).dimensions(btnX, btnY, btnW, btnH).build());
    }

    // ─── RENDER ──────────────────────────────────────────────

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        drawBackground(context);
        drawTabs(context, mouseX, mouseY);
        drawStorageSlots(context, mouseX, mouseY);
        drawPlayerSlots(context, mouseX, mouseY);
        drawLabels(context);
        super.render(context, mouseX, mouseY, delta);

        // Elde tuttuğumuz item
        if (!heldStack.isEmpty()) {
            context.drawItem(heldStack, mouseX - 8, mouseY - 8);
        }
    }

    private void drawBackground(DrawContext context) {
        // Ana panel arka planı (Minecraft gri)
        context.fill(guiLeft, guiTop + 20, guiLeft + GUI_WIDTH, guiTop + GUI_HEIGHT, 0xFFC6C6C6);
        // Üst kenar (açık)
        context.fill(guiLeft, guiTop + 20, guiLeft + GUI_WIDTH, guiTop + 21, 0xFFFFFFFF);
        context.fill(guiLeft, guiTop + 20, guiLeft + 1, guiTop + GUI_HEIGHT, 0xFFFFFFFF);
        // Alt & sağ kenar (koyu)
        context.fill(guiLeft, guiTop + GUI_HEIGHT - 1, guiLeft + GUI_WIDTH, guiTop + GUI_HEIGHT, 0xFF555555);
        context.fill(guiLeft + GUI_WIDTH - 1, guiTop + 20, guiLeft + GUI_WIDTH, guiTop + GUI_HEIGHT, 0xFF555555);
    }

    private void drawTabs(DrawContext context, int mouseX, int mouseY) {
        int tabY   = guiTop + 2;
        int tabH   = 20;
        int tabX   = guiLeft + 4;
        int[] tabWidths = {100, 100, 60};

        for (int i = 0; i < 3; i++) {
            int tw = tabWidths[i];
            boolean active = (i == activeTab);
            int bg = active ? 0xFFC6C6C6 : 0xFF9E9E9E;

            context.fill(tabX, tabY, tabX + tw, tabY + tabH, bg);
            // Kenarlık
            context.fill(tabX, tabY, tabX + tw, tabY + 1, 0xFFFFFFFF);
            context.fill(tabX, tabY, tabX + 1, tabY + tabH, 0xFFFFFFFF);
            context.fill(tabX + tw - 1, tabY, tabX + tw, tabY + tabH, 0xFF555555);
            if (!active) {
                context.fill(tabX, tabY + tabH - 1, tabX + tw, tabY + tabH, 0xFF555555);
            }

            context.drawText(this.textRenderer, TAB_NAMES[i], tabX + 4, tabY + 6, active ? 0x111111 : 0x3F3F3F, false);
            tabX += tw + 2;
        }
    }

    private void drawStorageSlots(DrawContext context, int mouseX, int mouseY) {
        int startX = guiLeft + 8;
        int startY = guiTop  + 35;

        List<ItemStack> inv = storageInventories[activeTab];

        for (int i = 0; i < 54; i++) {
            int col = i % 9;
            int row = i / 9;
            int sx  = startX + col * SLOT_SIZE;
            int sy  = startY + row * SLOT_SIZE;

            drawSlot(context, sx, sy, inv.get(i), mouseX, mouseY);
        }
    }

    private void drawPlayerSlots(DrawContext context, int mouseX, int mouseY) {
        int startX = guiLeft + 8;
        int invY   = guiTop  + 250;
        int hotY   = guiTop  + 315;

        // 3 satır envanter (27 slot)
        for (int i = 0; i < 27; i++) {
            int col = i % 9;
            int row = i / 9;
            int sx  = startX + col * SLOT_SIZE;
            int sy  = invY   + row * SLOT_SIZE;
            drawSlot(context, sx, sy, playerInventory.get(i), mouseX, mouseY);
        }

        // Hotbar (9 slot)
        for (int i = 0; i < 9; i++) {
            int sx = startX + i * SLOT_SIZE;
            drawSlot(context, sx, hotY, playerHotbar.get(i), mouseX, mouseY);
        }
    }

    private void drawSlot(DrawContext context, int x, int y, ItemStack stack, int mouseX, int mouseY) {
        boolean hovered = mouseX >= x && mouseX < x + SLOT_SIZE && mouseY >= y && mouseY < y + SLOT_SIZE;

        // Slot arka plan
        context.fill(x, y, x + SLOT_SIZE, y + SLOT_SIZE, hovered ? 0xFF9A9A9A : 0xFF8B8B8B);
        // Kenarlık: sol/üst koyu, sağ/alt açık (Minecraft klasik)
        context.fill(x, y, x + SLOT_SIZE, y + 1, 0xFF555555);
        context.fill(x, y, x + 1, y + SLOT_SIZE, 0xFF555555);
        context.fill(x, y + SLOT_SIZE - 1, x + SLOT_SIZE, y + SLOT_SIZE, 0xFFFFFFFF);
        context.fill(x + SLOT_SIZE - 1, y, x + SLOT_SIZE, y + SLOT_SIZE, 0xFFFFFFFF);

        if (!stack.isEmpty()) {
            context.drawItem(stack, x + 1, y + 1);
            context.drawItemInSlot(this.textRenderer, stack, x + 1, y + 1);
        }
    }

    private void drawLabels(DrawContext context) {
        context.drawText(this.textRenderer, TAB_NAMES[activeTab], guiLeft + 8, guiTop + 24, 0x404040, false);
        context.drawText(this.textRenderer, "Envanter", guiLeft + 8, guiTop + 242, 0x404040, false);
    }

    // ─── MOUSE CLICKS ────────────────────────────────────────

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Tab tıklama
        int tabY = guiTop + 2;
        int tabH = 20;
        int tabX = guiLeft + 4;
        int[] tabWidths = {100, 100, 60};
        for (int i = 0; i < 3; i++) {
            int tw = tabWidths[i];
            if (mouseX >= tabX && mouseX < tabX + tw && mouseY >= tabY && mouseY < tabY + tabH) {
                activeTab = i;
                return true;
            }
            tabX += tw + 2;
        }

        // Slot tıklama — depo slotları
        int startX = guiLeft + 8;
        int startY = guiTop  + 35;
        for (int i = 0; i < 54; i++) {
            int col = i % 9;
            int row = i / 9;
            int sx  = startX + col * SLOT_SIZE;
            int sy  = startY + row * SLOT_SIZE;
            if (mouseX >= sx && mouseX < sx + SLOT_SIZE && mouseY >= sy && mouseY < sy + SLOT_SIZE) {
                clickSlot(storageInventories[activeTab], i, activeTab, button);
                return true;
            }
        }

        // Envanter slotları
        int invY = guiTop + 250;
        for (int i = 0; i < 27; i++) {
            int col = i % 9; int row = i / 9;
            int sx = startX + col * SLOT_SIZE;
            int sy = invY   + row * SLOT_SIZE;
            if (mouseX >= sx && mouseX < sx + SLOT_SIZE && mouseY >= sy && mouseY < sy + SLOT_SIZE) {
                clickSlot(playerInventory, i, -1, button);
                return true;
            }
        }

        // Hotbar slotları
        int hotY = guiTop + 315;
        for (int i = 0; i < 9; i++) {
            int sx = startX + i * SLOT_SIZE;
            if (mouseX >= sx && mouseX < sx + SLOT_SIZE && mouseY >= hotY && mouseY < hotY + SLOT_SIZE) {
                clickSlot(playerHotbar, i, -2, button);
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void clickSlot(List<ItemStack> list, int index, int tab, int button) {
        if (heldStack.isEmpty()) {
            // Slottan al
            if (!list.get(index).isEmpty()) {
                heldStack    = list.get(index).copy();
                heldFromTab   = tab;
                heldFromIndex = index;
                list.set(index, ItemStack.EMPTY);
            }
        } else {
            // Slota bırak
            ItemStack existing = list.get(index);
            if (existing.isEmpty()) {
                list.set(index, heldStack.copy());
            } else {
                // Swap
                list.set(index, heldStack.copy());
                heldStack = existing.copy();
                return;
            }
            heldStack     = ItemStack.EMPTY;
            heldFromTab   = -1;
            heldFromIndex = -1;
        }
    }

    // ─── BUTON AKSIYONLARI ───────────────────────────────────

    private void actionThrowAll() {
        List<ItemStack> storage = storageInventories[activeTab];
        for (int i = 0; i < storage.size(); i++) storage.set(i, ItemStack.EMPTY);
    }

    private void actionAutoEquip() {
        // Zırh giyme — gerçek sunucu tarafında yapılır
        // Client-side simülasyon
    }

    private void actionPutAll() {
        List<ItemStack> storage = storageInventories[activeTab];
        transferToStorage(playerInventory, storage);
        transferToStorage(playerHotbar,    storage);
    }

    private void actionTakeAll() {
        List<ItemStack> storage = storageInventories[activeTab];
        for (int i = 0; i < storage.size(); i++) {
            ItemStack stack = storage.get(i);
            if (stack.isEmpty()) continue;
            int emptySlot = findEmpty(playerInventory);
            if (emptySlot >= 0) {
                playerInventory.set(emptySlot, stack.copy());
                storage.set(i, ItemStack.EMPTY);
            }
        }
    }

    private void actionThrowJunk() {
        // Oyuncunun envanterindeki zırh, alet dışı itemleri at
        clearJunk(storageInventories[activeTab]);
        clearJunk(playerInventory);
        clearJunk(playerHotbar);
    }

    private void clearJunk(List<ItemStack> list) {
        // Basit örnek: boş olmayan ama araç/zırh olmayan her şeyi sil
        for (int i = 0; i < list.size(); i++) {
            ItemStack s = list.get(i);
            if (!s.isEmpty() && !s.getItem().toString().contains("sword")
                    && !s.getItem().toString().contains("axe")
                    && !s.getItem().toString().contains("pickaxe")
                    && !s.getItem().toString().contains("armor")
                    && !s.getItem().toString().contains("helmet")
                    && !s.getItem().toString().contains("chestplate")
                    && !s.getItem().toString().contains("leggings")
                    && !s.getItem().toString().contains("boots")) {
                list.set(i, ItemStack.EMPTY);
            }
        }
    }

    private void transferToStorage(List<ItemStack> from, List<ItemStack> storage) {
        for (int i = 0; i < from.size(); i++) {
            ItemStack stack = from.get(i);
            if (stack.isEmpty()) continue;
            int emptySlot = findEmpty(storage);
            if (emptySlot >= 0) {
                storage.set(emptySlot, stack.copy());
                from.set(i, ItemStack.EMPTY);
            }
        }
    }

    private int findEmpty(List<ItemStack> list) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).isEmpty()) return i;
        }
        return -1;
    }

    @Override
    public boolean shouldPause() { return false; }

    @Override
    public boolean shouldCloseOnEsc() { return true; }
}
