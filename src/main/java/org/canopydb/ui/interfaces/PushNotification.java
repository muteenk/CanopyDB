package org.canopydb.ui.interfaces;

import org.canopydb.ui.molecules.Notification;

@FunctionalInterface
public interface PushNotification {
    void push(String title, String message, Notification.NotificationType notificationType);
}
