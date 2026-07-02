package com.velora.theme;

import java.awt.*;
import javax.swing.JButton;

public class PrimaryButton extends JButton {
    private float hover;
    private float press;

    public PrimaryButton(String text) {
        super(text);
        setFont(AppFonts.bodyBold(14));
        setForeground(new Color(28, 20, 12));
        setOpaque(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    public void animate() {
        hover += ((getModel().isRollover() ? 1f : 0f) - hover) * .13f;
        press += ((getModel().isPressed() ? 1f : 0f) - press) * .2f;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics raw) {
        Graphics2D g = (Graphics2D) raw.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(203, 163, 106, Math.round(35 + hover * 75)));
        g.fillRoundRect(-2, 3, getWidth() + 4, getHeight(), 12, 12);
        int inset = Math.round(press * 2);
        g.setPaint(new GradientPaint(0, 0, AppColors.GOLD_LIGHT.brighter(),
            0, getHeight(), AppColors.GOLD.darker()));
        g.fillRoundRect(inset, inset, getWidth() - inset * 2, getHeight() - inset * 2, 10, 10);
        g.dispose();
        super.paintComponent(raw);
    }
}
