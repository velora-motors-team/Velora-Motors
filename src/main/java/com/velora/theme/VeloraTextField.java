package com.velora.theme;

import java.awt.*;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class VeloraTextField extends JTextField {
    private final String placeholder;

    public VeloraTextField(String placeholder) {
        this.placeholder = placeholder;
        setOpaque(false);
        setForeground(AppColors.WHITE);
        setCaretColor(AppColors.GOLD);
        setFont(AppFonts.body(13));
        setBorder(new EmptyBorder(0, 48, 0, 14));
    }

    @Override
    protected void paintComponent(Graphics raw) {
        paintField(raw);
        super.paintComponent(raw);
        if (getText().isEmpty()) paintPlaceholder(raw);
    }

    protected void paintField(Graphics raw) {
        Graphics2D g = (Graphics2D) raw.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(8, 14, 20, 235));
        g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, UIConstants.FIELD_ARC, UIConstants.FIELD_ARC);
        g.setColor(new Color(203, 163, 106, isFocusOwner() ? 160 : 45));
        g.setStroke(new BasicStroke(isFocusOwner() ? 1.6f : 1f));
        g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, UIConstants.FIELD_ARC, UIConstants.FIELD_ARC);
        g.dispose();
    }

    protected void paintPlaceholder(Graphics raw) {
        Graphics2D g = (Graphics2D) raw.create();
        g.setFont(getFont());
        g.setColor(new Color(135, 141, 150));
        g.drawString(placeholder, 48, (getHeight() + g.getFontMetrics().getAscent()) / 2 - 2);
        g.dispose();
    }
}
