package p2p.ui;

import java.awt.Color;
import java.awt.Font;

public class Theme {
    // Pastel Palette (Darkened/Polished)
    public static final Color BACKGROUND = new Color(255, 245, 235); // Cream -> Warm Beige
    public static final Color PANEL_BG = new Color(255, 255, 255, 220); // More opaque
    public static final Color TEXT_PRIMARY = new Color(40, 40, 40); // Darker Grey
    public static final Color TEXT_SECONDARY = new Color(100, 100, 100);

    // Accents (Darker/Richer)
    public static final Color ACCENT = new Color(255, 149, 156); // Deep Pink
    public static final Color ACCENT_HOVER = new Color(255, 120, 130); // Darker Pink
    public static final Color ACCENT_TRANSPARENT = new Color(255, 149, 156, 100);

    public static final Color SECONDARY_ACCENT = new Color(156, 205, 255); // Rich Blue
    public static final Color SUCCESS = new Color(156, 225, 171); // Rich Mint
    
    public static final Color BORDER = new Color(180, 180, 180, 50);
    public static final Color GLASS_HIGHLIGHT = new Color(255, 255, 255, 150);
    public static final Color SHADOW = new Color(0, 0, 0, 60); // Stronger Shadow

    // Generic Background Gradient (Restored)
    public static final Color GRADIENT_START = new Color(255, 230, 210); // Peach
    public static final Color GRADIENT_END = new Color(255, 200, 210); // Rose

    // Vibrant Gradients (From Reference)
    // 1. Cyan
    public static final Color GRAD_CYAN_START = new Color(0, 229, 255);
    public static final Color GRAD_CYAN_END = new Color(0, 180, 255);
    // 2. Blue
    public static final Color GRAD_BLUE_START = new Color(50, 150, 255);
    public static final Color GRAD_BLUE_END = new Color(0, 100, 240);
    // 3. Purple
    public static final Color GRAD_PURPLE_START = new Color(180, 100, 255);
    public static final Color GRAD_PURPLE_END = new Color(140, 50, 255);
    // 4. Orange/Pink
    public static final Color GRAD_ORANGE_START = new Color(255, 140, 100);
    public static final Color GRAD_ORANGE_END = new Color(255, 80, 150);

    public static final Color[][] CARD_GRADIENTS = {
        {GRAD_CYAN_START, GRAD_CYAN_END},
        {GRAD_BLUE_START, GRAD_BLUE_END},
        {GRAD_PURPLE_START, GRAD_PURPLE_END},
        {GRAD_ORANGE_START, GRAD_ORANGE_END}
    };

    // Fonts
    // Fonts - Updated to Times New Roman
    public static final Font FONT_REGULAR = new Font("Times New Roman", Font.PLAIN, 16);
    public static final Font FONT_BOLD = new Font("Times New Roman", Font.BOLD, 16);
    public static final Font FONT_SMALL = new Font("Times New Roman", Font.PLAIN, 13);
    public static final Font FONT_TITLE = new Font("Times New Roman", Font.BOLD, 26);
}
