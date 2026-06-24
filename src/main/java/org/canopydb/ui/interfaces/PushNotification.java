package org.canopydb.ui.interfaces;

import org.canopydb.ui.singletons.NotificationManager;

@FunctionalInterface
public interface PushNotification {
    void push(String title, String message, NotificationManager.NotificationType notificationType);
}
