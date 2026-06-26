package com.inventorymod.mixin;

import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HandledScreen.class)
public abstract class ChestScreenMixin extends net.minecraft.client.gui.screen.Screen {

    public ChestScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void injectButtons(CallbackInfo ci) {
        if (!(Object)this instanceof GenericContainerScreen screen) return;

        int btnX = this.width / 2 + 92 + 4;
        int btnW = 90;
        int btnH = 18;
        int gap  = 3;
        int btnY = this.height / 2 - 83;

        this.addDrawableChild(ButtonWidget.builder(Text.translatable("button.inventorymod.drop_all"), b -> dropAll(screen))
                .dimensions(btnX, btnY, btnW, btnH).build());
        btnY += btnH + gap;

        this.addDrawableChild(ButtonWidget.builder(Text.translatable("button.inventorymod.auto_equip"), b -> autoEquip(screen))
                .dimensions(btnX, btnY, btnW, btnH).build());
        btnY += btnH + gap;

        this.addDrawableChild(ButtonWidget.builder(Text.translatable("button.inventorymod.put_all"), b -> putAll(screen))
                .dimensions(btnX, btnY, btnW, btnH).build());
        btnY += btnH + gap;

        this.addDrawableChild(ButtonWidget.builder(Text.translatable("button.inventorymod.take_all"), b -> takeAll(screen))
                .dimensions(btnX, btnY, btnW, btnH).build());
        btnY += btnH + gap;

        this.addDrawableChild(ButtonWidget.builder(Text.translatable("button.inventorymod.drop_junk"), b -> dropJunk(screen))
                .dimensions(btnX, btnY, btnW, btnH).build());
    }

    private void dropAll(GenericContainerScreen screen) {
        if (this.client == null || this.client.player == null) return;
        var handler = screen.getScreenHandler();
        int size = handler.getRows() * 9;
        for (int i = 0; i < size; i++) {
            ItemStack stack = handler.getSlot(i).getStack();
            if (!stack.isEmpty()) {
                this.client.player.dropItem(stack.copy(), false);
                handler.getSlot(i).setStack(ItemStack.EMPTY);
            }
        }
    }

    private void autoEquip(GenericContainerScreen screen) {
        if (this.client == null || this.client.player == null) return;
        var handler = screen.getScreenHandler();
        int size = handler.getRows() * 9;
        for (int i = 0; i < size; i++) {
            if (!handler.getSlot(i).getStack().isEmpty()) {
                handler.quickMove(this.client.player, i);
            }
        }
    }

    private void putAll(GenericContainerScreen screen) {
        if (this.client == null || this.client.player == null) return;
        var handler = screen.getScreenHandler();
        int size = handler.getRows() * 9;
        int total = handler.slots.size();
        for (int i = size; i < total; i++) {
            if (!handler.getSlot(i).getStack().isEmpty()) {
                handler.quickMove(this.client.player, i);
            }
        }
    }

    private void takeAll(GenericContainerScreen screen) {
        if (this.client == null || this.client.player == null) return;
        var handler = screen.getScreenHandler();
        int size = handler.getRows() * 9;
        for (int i = 0; i < size; i++) {
            if (!handler.getSlot(i).getStack().isEmpty()) {
                handler.quickMove(this.client.player, i);
            }
        }
    }

    private void dropJunk(GenericContainerScreen screen) {
        if (this.client == null || this.client.player == null) return;
        var handler = screen.getScreenHandler();
        int size = handler.getRows() * 9;
        for (int i = 0; i < size; i++) {
            ItemStack stack = handler.getSlot(i).getStack();
            if (!stack.isEmpty() && isJunk(stack)) {
                this.client.player.dropItem(stack.copy(), false);
                handler.getSlot(i).setStack(ItemStack.EMPTY);
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
            && !name.contains("bow") && !name.contains("trident")
            && !name.contains("totem");
    }
}
