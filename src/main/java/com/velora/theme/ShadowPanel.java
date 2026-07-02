package com.velora.theme;

import java.awt.*;
import javax.swing.JPanel;

public class ShadowPanel extends JPanel {
    private final int arc;

    public ShadowPanel(int arc) {
        this.arc = arc;
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics raw) {
        Graphics2D g = (Graphics2D) raw.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        for (int i = 12; i >= 2; i -= 2) {
            g.setColor(new Color(0, 0, 0, 5));
            g.fillRoundRect(i / 2, i / 2, getWidth() - i, getHeight() - i / 2, arc, arc);
        }
        g.dispose();
        super.paintComponent(raw);
    }
}
