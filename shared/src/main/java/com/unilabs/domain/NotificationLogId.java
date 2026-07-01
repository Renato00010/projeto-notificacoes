package com.unilabs.domain;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

public class NotificationLogId implements Serializable {
    private Long id;
    private LocalDateTime createdAt;

    public NotificationLogId() {}

    public NotificationLogId(Long id, LocalDateTime createdAt) {
        this.id = id;
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NotificationLogId that = (NotificationLogId) o;
        return Objects.equals(id, that.id) && Objects.equals(createdAt, that.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, createdAt);
    }
}