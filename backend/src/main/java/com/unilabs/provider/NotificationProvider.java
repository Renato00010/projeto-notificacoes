package com.unilabs.provider;

import com.unilabs.dto.ChannelType;
import com.unilabs.dto.NotificationRequest;

public interface NotificationProvider {

    String providerName();

    ChannelType channelType();

    void deliver(NotificationRequest request, String renderedSubject, String renderedBody)
            throws NotificationDeliveryException;
}
