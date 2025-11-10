package com.mycompany.quanlytruyen.utils;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

public final class UIUtils {
    private UIUtils() {
    }

    public static void enableTextComponentShortcuts(Component component) {
        if (component instanceof JTextComponent textComponent) {
            InputMap inputMap = textComponent.getInputMap(JComponent.WHEN_FOCUSED);
            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK), "copy");
            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK), "paste");
            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_DOWN_MASK), "cut");
            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_DOWN_MASK), "select-all");
        }

        if (component instanceof Container container) {
            for (Component child : container.getComponents()) {
                enableTextComponentShortcuts(child);
            }
        }
    }
}
