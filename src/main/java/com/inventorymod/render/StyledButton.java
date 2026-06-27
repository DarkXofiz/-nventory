package com.inventorymod.render;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/**
 * Ozel stilize buton — yuvarlak kenarlar, renk kodlu, profesyonel gorunum.
 * Her buton tipi icin farkli renk:
 *   DROP_ALL  = Kirmizi      #C0392B
 *   EQUIP     = Altin sari   #F39C12
 *   PUT       = Mavi         #2980B9
 *   TAKE      = Yesil        #27AE60
 *   JUNK      = Mor          #8E44AD
 */
public class StyledButton extends ButtonWidget {

    public enum Style { DROP_ALL, EQUIP, PUT, TAKE, JUNK }

    private final Style style;

    // Ana renk (normal)
    private static final int COLOR_DROP_ALL  = 0xFFC0392B;
    private static final int COLOR_EQUIP     = 0xFFF39C12;
    private static final int COLOR_PUT       = 0xFF2980B9;
    private static final int COLOR_TAKE      = 0xFF27AE60;
    private static final int COLOR_JUNK      = 0xFF8E44AD;

    // Hover rengi (daha acik)
    private static final int COLOR_DROP_ALL_H  = 0xFFE74C3C;
    private static final int COLOR_EQUIP_H     = 0xFFF1C40F;
    private static final int COLOR_PUT_H       = 0xFF3498DB;
    private static final int COLOR_TAKE_H      = 0xFF2ECC71;
    private static final int COLOR_JUNK_H      = 0xFF9B59B6;

    public StyledButton(int x, int y, int width, int height,
                        Text text, PressAction action, Style style) {
        super(x, y, width, height, text, action, ButtonWidget.DEFAULT_NARRATION_SUPPLIER);
        this.style = style;
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        boolean hovered = isHovered();
        int bg    = getBgColor(hovered);
        int border = getDarker(bg);

        int x = getX(), y = getY(), w = getWidth(), h = getHeight();
        int r = 4; // yuvarlak kose yaricapi

        // Dis kenarlık (koyu)
        drawRoundedRect(context, x - 1, y - 1, w + 2, h + 2, r + 1, border);
        // Ana arka plan
        drawRoundedRect(context, x, y, w, h, r, bg);
        // Ust parlama efekti
        int shine = 0x20FFFFFF;
        drawRoundedRect(context, x + 1, y + 1, w - 2, h / 2 - 1, r - 1, shine);

        // Yazi
        int textColor = 0xFFFFFFFF;
        context.drawCenteredTextWithShadow(
            net.minecraft.client.MinecraftClient.getInstance().textRenderer,
            getMessage(), x + w / 2, y + (h - 8) / 2, textColor);
    }

    private int getBgColor(boolean hovered) {
        return switch (style) {
            case DROP_ALL -> hovered ? COLOR_DROP_ALL_H : COLOR_DROP_ALL;
            case EQUIP    -> hovered ? COLOR_EQUIP_H    : COLOR_EQUIP;
            case PUT      -> hovered ? COLOR_PUT_H      : COLOR_PUT;
            case TAKE     -> hovered ? COLOR_TAKE_H     : COLOR_TAKE;
            case JUNK     -> hovered ? COLOR_JUNK_H     : COLOR_JUNK;
        };
    }

    private int getDarker(int color) {
        int r = ((color >> 16) & 0xFF) / 2;
        int g = ((color >> 8)  & 0xFF) / 2;
        int b = ((color)       & 0xFF) / 2;
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    /**
     * Yuvarlak kose dikdortgen cizer.
     * Gercek GPU rounded rect yerine pixel-perfect dolgu kullanir — her versiyonda calisir.
     */
    private void drawRoundedRect(DrawContext ctx, int x, int y, int w, int h, int r, int color) {
        if (r <= 0) {
            ctx.fill(x, y, x + w, y + h, color);
            return;
        }
        // Ortadaki genis alan
        ctx.fill(x + r, y,     x + w - r, y + h,     color); // merkez
        ctx.fill(x,     y + r, x + r,     y + h - r, color); // sol
        ctx.fill(x + w - r, y + r, x + w, y + h - r, color); // sag

        // 4 kose icin ceyrek daire
        drawCorner(ctx, x,         y,         r,  1,  1, color);
        drawCorner(ctx, x + w - r, y,         r, -1,  1, color);
        drawCorner(ctx, x,         y + h - r, r,  1, -1, color);
        drawCorner(ctx, x + w - r, y + h - r, r, -1, -1, color);
    }

    private void drawCorner(DrawContext ctx, int ox, int oy, int r, int sx, int sy, int color) {
        for (int row = 0; row < r; row++) {
            double limit = Math.sqrt((double)(r * r) - (r - row - 1) * (r - row - 1));
            int filled = (int) Math.round(limit);
            int py = sy > 0 ? oy + row : oy + (r - 1 - row);
            int px = sx > 0 ? ox + (r - filled) : ox + filled - filled + (r - filled);
            int px2 = sx > 0 ? ox + r : ox + filled;
            if (px < px2) ctx.fill(px, py, px2, py + 1, color);
        }
    }
}
