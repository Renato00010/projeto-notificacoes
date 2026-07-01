package com.unilabs.provider;

import com.unilabs.dto.ChannelType;

public class NotificationDeliveryException extends Exception {

    public NotificationDeliveryException(String message) {
        super(message);
    }

    public NotificationDeliveryException(String message, Throwable cause) {
        super(message, cause);
    }
}
