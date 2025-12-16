package p2p.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class PeerCard extends JPanel {
    private final String peerName;
    private final boolean isSelected;
    private final Color[] gradient;

    public PeerCard(String peerName, boolean isSelected, int index) {
        this.peerName = peerName;
        this.isSelected = isSelected;

        // Pick gradient based on index (or hash) to be consistent
        int gradientIndex = Math.abs(peerName.hashCode()) % Theme.CARD_GRADIENTS.length;
        if (peerName.equals("Global Chat")) {
            this.gradient = Theme.CARD_GRADIENTS[3]; // Orange for Global
        } else {
            this.gradient = Theme.CARD_GRADIENTS[gradientIndex];
        }

        setOpaque(false);
        setLayout(new BorderLayout());
        // Increased padding as requested (approx +3px from previous)
        setBorder(new EmptyBorder(15, 18, 15, 18)); 

        // Content
        JLabel nameLabel = new JLabel(peerName);
        nameLabel.setFont(Theme.FONT_BOLD); // Times New Roman Bold
        nameLabel.setForeground(Color.WHITE); // Always white on vivid gradient
        
        add(nameLabel, BorderLayout.CENTER);
        
        // Status Icon (Right side)
        JLabel icon = new JLabel("●"); // Simple dot
        icon.setForeground(new Color(255, 255, 255, 200));
        add(icon, BorderLayout.EAST);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        int radius = 25; // High border radius like reference

        // Blurred Drop Shadow (Simulated with layers)
        // Draw 10 layers with decreasing opacity to simulate gaussian blur
        for(int i=0; i<10; i++) {
             g2.setColor(new Color(0, 0, 0, 10 - i)); // Very subtle alpha per layer
             // Expand outwards slightly per layer
             g2.fillRoundRect(2 + i, 4 + i, w - 4 - (i*2), h - 6 - (i*2), radius, radius);
        }

        // Card Gradient Background (Inset slightly to sit on top of shadow)
        GradientPaint gp = new GradientPaint(0, 0, gradient[0], w, h, gradient[1]);
        g2.setPaint(gp);
        
        // Adjust bounds to be smaller than shadow
        // 2px margin from edges for shadow breathing room
        g2.fillRoundRect(4, 2, w - 8, h - 8, radius, radius);

        // Glass Shine
        GradientPaint shine = new GradientPaint(0, 0, new Color(255,255,255,50), 0, h/2, new Color(255,255,255,0));
        g2.setPaint(shine);
        g2.fillRoundRect(2, 2, w - 8, h/2, radius, radius);

        g2.dispose();
        // super.paintComponent(g); // Do not call super, we fully painted background
    }
    
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(200, 60); // Fixed height for cards
    }
}
