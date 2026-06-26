package com.inventorymod.mixin;

import net.minecraft.client.gui.screen.ingame.EnderChestScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Ender Chest ekranına sağ tarafta 5 buton ekler.
 */
@Mixin(EnderChestScreen.class)
public abstract class EnderChestScreenMixin extends net.minecraft.client.gui.screen.ingame.HandledScreen<GenericContainerScreenHandler> {

    public EnderChestScreenMixin(GenericContainerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void injectButtons(CallbackInfo ci) {
        addSideButtons();
    }

    private void addSideButtons() {
        int btnX = this.x + this.backgroundWidth + 4;
        int btnW = 90;
        int btnH = 18;
        int gap  = 3;
        int btnY = this.y + 4;

        this.addDrawableChild(
            ButtonWidget.builder(Text.literal("Herşeyi At"), b -> throwAll())
                .dimensions(btnX, btnY, btnW, btnH).build()
        );
        btnY += btnH + gap;

        this.addDrawableChild(
            ButtonWidget.builder(Text.literal("Oto Ekipman"), b -> autoEquip())
                .dimensions(btnX, btnY, btnW, btnH).build()
        );
        btnY += btnH + gap;

        this.addDrawableChild(
            ButtonWidget.builder(Text.literal("Herşeyi Koy"), b -> putAll())
                .dimensions(btnX, btnY, btnW, btnH).build()
        );
        btnY += btnH + gap;

        this.addDrawableChild(
            ButtonWidget.builder(Text.literal("Herşeyi Al"), b -> takeAll())
                .dimensions(btnX, btnY, btnW, btnH).build()
        );
        btnY += btnH + gap;

        this.addDrawableChild(
            ButtonWidget.builder(Text.literal("Çöpleri At"), b -> throwJunk())
                .dimensions(btnX, btnY, btnW, btnH).build()
        );
    }

    private void throwAll() {
        if (this.client == null || this.client.player == null) return;
        int size = this.handler.getRows() * 9;
        for (int i = 0; i < size; i++) {
            ItemStack stack = this.handler.getSlot(i).getStack();
            if (!stack.isEmpty()) {
                this.client.player.dropItem(stack.copy(), false);
                this.handler.getSlot(i).setStack(ItemStack.EMPTY);
            }
        }
    }

    private void autoEquip() {
        if (this.client == null || this.client.player == null) return;
        int size = this.handler.getRows() * 9;
        for (int i = 0; i < size; i++) {
            if (!this.handler.getSlot(i).getStack().isEmpty()) {
                this.handler.quickMove(this.client.player, i);
            }
        }
    }

    private void putAll() {
        if (this.client == null || this.client.player == null) return;
        int size = this.handler.getRows() * 9;
        int total = this.handler.slots.size();
        for (int i = size; i < total; i++) {
            if (!this.handler.getSlot(i).getStack().isEmpty()) {
                this.handler.quickMove(this.client.player, i);
            }
        }
    }

    private void takeAll() {
        if (this.client == null || this.client.player == null) return;
        int size = this.handler.getRows() * 9;
        for (int i = 0; i < size; i++) {
            if (!this.handler.getSlot(i).getStack().isEmpty()) {
                this.handler.quickMove(this.client.player, i);
            }
        }
    }

    private void throwJunk() {
        if (this.client == null || this.client.player == null) return;
        int size = this.handler.getRows() * 9;
        for (int i = 0; i < size; i++) {
            ItemStack stack = this.handler.getSlot(i).getStack();
            if (!stack.isEmpty() && isJunk(stack)) {
                this.client.player.dropItem(stack.copy(), false);
                this.handler.getSlot(i).setStack(ItemStack.EMPTY);
            }
        }
    }

    private boolean isJunk(ItemStack stack) {
        String name = stack.getItem().toString().toLowerCase();
        return !name.contains("sword") && !name.contains("axe")
            && !name.contains("pickaxe") && !name.contains("shovel")
            && !name.contains("hoe") && !name.contains("helmet")
            && !name.contains("chestplate") && !name.contains("leggings")
            && !name.contains("boots") && !name.contains("shield")
            && !name.contains("bow") && !name.contains("crossbow")
            && !name.contains("trident") && !name.contains("totem");
    }
}
