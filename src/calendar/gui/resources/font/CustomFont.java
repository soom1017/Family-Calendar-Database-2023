package calendar.gui.resources.font;

import java.awt.Font;
import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.border.Border;
import javax.swing.JButton;

public abstract class CustomFont {
    public static final Font HEADER_FONT = new Font("Arial", Font.BOLD, 18);
    public static final Font DEFAULT_FONT = new Font("Arial", Font.BOLD, 16);
    public static final Font DETAIL_FONT = new Font("Arial", Font.PLAIN, 14);
    private static final Color DEFAULT_BUTTON_COLOR = new Color(100, 122, 104);
    private static final Color HIGHLIGHT_BUTTON_COLOR = new Color(255, 102, 102);
    private static final Border BUTTON_BORDER = BorderFactory.createEmptyBorder(10, 15, 10, 15);

    public static void applyButtonStyles(JButton button) {
        button.setForeground(DEFAULT_BUTTON_COLOR);
        button.setFocusPainted(true);
        button.setBorder(BUTTON_BORDER);
        button.setFont(DEFAULT_FONT);
    }

    public static void applyHighlightedButtonStyles(JButton button) {
        button.setBackground(HIGHLIGHT_BUTTON_COLOR);
        button.setForeground(Color.WHITE);
        button.setBorder(BUTTON_BORDER);
        button.setFont(DEFAULT_FONT);
        button.setOpaque(true);
    }
}
