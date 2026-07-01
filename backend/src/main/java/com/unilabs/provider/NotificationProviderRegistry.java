package com.unilabs.provider;

import com.unilabs.dto.ChannelType;
import com.unilabs.dto.NotificationRequest;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class NotificationProviderRegistry {

    private final Map<ChannelType, NotificationProvider> providersByChannel;

    public NotificationProviderRegistry(List<NotificationProvider> providers) {
        this.providersByChannel = new EnumMap<>(ChannelType.class);
        for (NotificationProvider provider : providers) {
            providersByChannel.put(provider.channelType(), provider);
        }
    }

    public NotificationProvider getProvider(ChannelType channelType) {
        NotificationProvider provider = providersByChannel.get(channelType);
        if (provider == null) {
            throw new IllegalArgumentException("Nenhum provider configurado para o canal: " + channelType);
        }
        return provider;
    }

    public void deliver(NotificationRequest request, String renderedSubject, String renderedBody)
            throws NotificationDeliveryException {
        getProvider(request.getChannelType()).deliver(request, renderedSubject, renderedBody);
    }

    public String resolveProviderName(ChannelType channelType) {
        return getProvider(channelType).providerName();
    }
}
