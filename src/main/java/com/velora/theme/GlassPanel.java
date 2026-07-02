package com.velora.theme;

import java.awt.*;
import javax.swing.JPanel;

public class GlassPanel extends JPanel {
    private final int arc;
    private final Color fill;

    public GlassPanel() {
        this(UIConstants.CARD_ARC, AppColors.PANEL);
    }

    public GlassPanel(int arc, Color fill) {
        this.arc = arc;
        this.fill = fill;
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics raw) {
        Graphics2D g = (Graphics2D) raw.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(0, 0, 0, 55));
        g.fillRoundRect(3, 6, getWidth() - 6, getHeight() - 7, arc, arc);
        g.setPaint(new GradientPaint(0, 0, fill.brighter(), getWidth(), getHeight(), fill));
        g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
        g.setColor(AppColors.BORDER);
        g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
        g.dispose();
        super.paintComponent(raw);
    }
}
