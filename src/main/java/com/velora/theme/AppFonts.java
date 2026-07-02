package com.velora.theme;

import java.awt.Font;

public final class AppFonts {
    public static Font title(float size) {
        return new Font("Cinzel", Font.PLAIN, Math.round(size));
    }

    public static Font body(float size) {
        return new Font("Inter", Font.PLAIN, Math.round(size));
    }

    public static Font bodyBold(float size) {
        return new Font("Inter", Font.BOLD, Math.round(size));
    }

    private AppFonts() {}
}
