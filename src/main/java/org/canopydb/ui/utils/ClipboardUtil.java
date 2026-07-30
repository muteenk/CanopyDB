package org.canopydb.ui.utils;

import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

public final class ClipboardUtil {

    private ClipboardUtil() {
    }

    public static void copy(String text) {
        ClipboardContent content = new ClipboardContent();
        content.putString(text == null ? "" : text);
        Clipboard.getSystemClipboard().setContent(content);
    }
}
