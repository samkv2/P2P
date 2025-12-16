package p2p.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class CuteTextField extends JTextField {
    public CuteTextField() {
        setOpaque(false); // We paint background manually
        setBackground(new Color(0, 0, 0, 0)); 
        setForeground(Color.WHITE); // White text on dark bg
        setFont(Theme.FONT_REGULAR);
        setBorder(new EmptyBorder(10, 20, 10, 20)); // More padding for pill shape
        setCaretColor(Theme.GRAD_CYAN_START); // Cyan blinking cursor
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        int radius = h; // Full pill shape

        // 1. Bottom Glow (Cyan)
        g2.setColor(new Color(0, 229, 255, 100)); // Cyan with alpha
        // Draw a blurred oval at the bottom center
        g2.fillOval(20, h - 10, w - 40, 15);
        
        // 2. Main Background (Dark Grey)
        g2.setColor(new Color(40, 44, 52)); // Dark Grey like reference
        g2.fillRoundRect(2, 2, w - 4, h - 8, radius, radius);

        // 3. Inner Shadow / Border (Emboss effect)
        g2.setStroke(new BasicStroke(3f));
        g2.setColor(new Color(60, 64, 72)); // Lighter grey for border
        g2.drawRoundRect(3, 3, w - 6, h - 10, radius, radius);

        // 4. Highlight (Top rim)
        g2.setStroke(new BasicStroke(1f));
        g2.setColor(new Color(255, 255, 255, 50));
        g2.drawRoundRect(4, 4, w - 8, h - 12, radius, radius);

        g2.dispose();

        super.paintComponent(g);
    }
}
