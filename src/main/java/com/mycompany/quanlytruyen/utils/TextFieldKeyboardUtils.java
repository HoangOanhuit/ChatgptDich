package com.mycompany.quanlytruyen.utils;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Utility class to enhance text components with standard keyboard shortcuts
 * Supports: Ctrl+A (Select All), Ctrl+C (Copy), Ctrl+V (Paste), Ctrl+X (Cut)
 * 
 * @author Admin
 */
public class TextFieldKeyboardUtils {
    
    /**
     * Adds keyboard shortcuts to any JTextComponent (JTextField, JTextArea, JTextPane, etc.)
     * @param textComponent The text component to enhance
     */
    public static void addKeyboardShortcuts(JTextComponent textComponent) {
        textComponent.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleKeyboardShortcuts(e, textComponent);
            }
        });
    }
    
    /**
     * Adds keyboard shortcuts to multiple text components at once
     * @param textComponents Array of text components to enhance
     */
    public static void addKeyboardShortcuts(JTextComponent... textComponents) {
        for (JTextComponent component : textComponents) {
            addKeyboardShortcuts(component);
        }
    }
    
    /**
     * Handle keyboard shortcuts for text components
     * @param e KeyEvent
     * @param textComponent Target text component
     */
    private static void handleKeyboardShortcuts(KeyEvent e, JTextComponent textComponent) {
        // Check if Ctrl is pressed (or Cmd on Mac)
        boolean ctrlPressed = e.isControlDown() || e.isMetaDown();
        
        if (!ctrlPressed) return;
        
        switch (e.getKeyCode()) {
            case KeyEvent.VK_A:
                // Ctrl+A - Select All
                e.consume();
                textComponent.selectAll();
                break;
                
            case KeyEvent.VK_C:
                // Ctrl+C - Copy
                e.consume();
                copyText(textComponent);
                break;
                
            case KeyEvent.VK_V:
                // Ctrl+V - Paste
                e.consume();
                pasteText(textComponent);
                break;
                
            case KeyEvent.VK_X:
                // Ctrl+X - Cut
                e.consume();
                cutText(textComponent);
                break;
        }
    }
    
    /**
     * Copy selected text to clipboard
     * @param textComponent Source text component
     */
    private static void copyText(JTextComponent textComponent) {
        String selectedText = textComponent.getSelectedText();
        if (selectedText != null && !selectedText.isEmpty()) {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            StringSelection stringSelection = new StringSelection(selectedText);
            clipboard.setContents(stringSelection, null);
        }
    }
    
    /**
     * Paste text from clipboard to text component
     * @param textComponent Target text component
     */
    private static void pasteText(JTextComponent textComponent) {
        try {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            if (clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
                String clipboardText = (String) clipboard.getData(DataFlavor.stringFlavor);
                
                if (textComponent.getSelectedText() != null) {
                    // Replace selected text
                    textComponent.replaceSelection(clipboardText);
                } else {
                    // Insert at cursor position
                    int caretPosition = textComponent.getCaretPosition();
                    textComponent.getDocument().insertString(caretPosition, clipboardText, null);
                }
            }
        } catch (Exception ex) {
            // Handle clipboard errors silently
            ex.printStackTrace();
        }
    }
    
    /**
     * Cut selected text to clipboard
     * @param textComponent Source text component
     */
    private static void cutText(JTextComponent textComponent) {
        String selectedText = textComponent.getSelectedText();
        if (selectedText != null && !selectedText.isEmpty()) {
            // Copy to clipboard
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            StringSelection stringSelection = new StringSelection(selectedText);
            clipboard.setContents(stringSelection, null);
            
            // Remove selected text
            textComponent.replaceSelection("");
        }
    }
    
    /**
     * Creates a context menu with Copy/Paste/Cut/Select All options
     * @param textComponent Target text component
     * @return JPopupMenu with standard text operations
     */
    public static JPopupMenu createContextMenu(JTextComponent textComponent) {
        JPopupMenu contextMenu = new JPopupMenu();
        
        // Select All
        JMenuItem selectAll = new JMenuItem("Chọn tất cả (Ctrl+A)");
        selectAll.addActionListener(e -> textComponent.selectAll());
        contextMenu.add(selectAll);
        
        contextMenu.addSeparator();
        
        // Cut
        JMenuItem cut = new JMenuItem("Cắt (Ctrl+X)");
        cut.addActionListener(e -> cutText(textComponent));
        contextMenu.add(cut);
        
        // Copy
        JMenuItem copy = new JMenuItem("Sao chép (Ctrl+C)");
        copy.addActionListener(e -> copyText(textComponent));
        contextMenu.add(copy);
        
        // Paste
        JMenuItem paste = new JMenuItem("Dán (Ctrl+V)");
        paste.addActionListener(e -> pasteText(textComponent));
        contextMenu.add(paste);
        
        // Add context menu to text component
        textComponent.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                showContextMenu(e);
            }
            
            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) {
                showContextMenu(e);
            }
            
            private void showContextMenu(java.awt.event.MouseEvent e) {
                if (e.isPopupTrigger()) {
                    // Update menu item states based on current selection
                    boolean hasSelection = textComponent.getSelectedText() != null;
                    boolean hasClipboard = false;
                    
                    try {
                        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                        hasClipboard = clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor);
                    } catch (Exception ex) {
                        // Ignore clipboard errors
                    }
                    
                    cut.setEnabled(hasSelection && textComponent.isEditable());
                    copy.setEnabled(hasSelection);
                    paste.setEnabled(hasClipboard && textComponent.isEditable());
                    
                    contextMenu.show(textComponent, e.getX(), e.getY());
                }
            }
        });
        
        return contextMenu;
    }
    
    /**
     * Enhanced setup method that adds both keyboard shortcuts and context menu
     * @param textComponent Text component to enhance
     * @param enableContextMenu Whether to add right-click context menu
     */
    public static void setupEnhancedTextComponent(JTextComponent textComponent, boolean enableContextMenu) {
        // Add keyboard shortcuts
        addKeyboardShortcuts(textComponent);
        
        // Add context menu if requested
        if (enableContextMenu) {
            createContextMenu(textComponent);
        }
    }
    
    /**
     * Enhanced setup method with context menu enabled by default
     * @param textComponent Text component to enhance
     */
    public static void setupEnhancedTextComponent(JTextComponent textComponent) {
        setupEnhancedTextComponent(textComponent, true);
    }
}