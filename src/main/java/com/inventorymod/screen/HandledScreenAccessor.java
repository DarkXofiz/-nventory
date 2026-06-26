package com.inventorymod.screen;

/**
 * HandledScreen'in korumalı (protected) alanlarına erişmek için interface.
 * Mixin kullanmadan erişim sağlar - Fabric API ile implement edilir.
 * 
 * NOT: Bu interface'i kullanmak için fabric.mod.json'da
 * "accessWidener" tanımlaması VEYA aşağıdaki AccessWidener dosyası gerekir.
 */
public interface HandledScreenAccessor {
    int getX();
    int getY();
    int getBackgroundWidth();
    int getBackgroundHeight();
}
