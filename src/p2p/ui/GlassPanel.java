package p2p.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class GlassPanel extends JPanel {
    private final int radius = 20;

    public GlassPanel() {
        setOpaque(false);
        setBackground(Theme.PANEL_BG);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Blurred Drop Shadow
        for(int i=0; i<15; i++) { // Slightly larger shadow for main panels
             g2.setColor(new Color(0, 0, 0, 8 - (i/2))); 
             g2.fillRoundRect(i, i, getWidth() - (i*2), getHeight() - (i*2), radius, radius);
        }

        // Fill background with translucency (Offset to sit above shadow)
        g2.setColor(getBackground());
        g2.fillRoundRect(4, 4, getWidth() - 8, getHeight() - 8, radius, radius);

        // Add subtle highlight for "glass" reflection edge
        g2.setColor(Theme.GLASS_HIGHLIGHT);
        g2.drawRoundRect(4, 4, getWidth() - 8, getHeight() - 8, radius, radius);

        g2.dispose();
        super.paintComponent(g);
    }
}
