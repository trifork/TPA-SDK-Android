package io.tpa.tpalib;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import io.tpa.tpalib.protobuf.ProtobufMessages;

final class BackendPing {

    @NonNull
    private SessionUUIDProvider sessionUUIDProvider;

    @NonNull
    private ProtobufFactory protobufFactory;

    BackendPing(@NonNull SessionUUIDProvider sessionUUIDProvider, @NonNull ProtobufFactory protobufFactory) {
        this.sessionUUIDProvider = sessionUUIDProvider;
        this.protobufFactory = protobufFactory;
    }

    @Nullable
    ProtobufMessages.BaseMessage createPing() {
        String sessionUUID = sessionUUIDProvider.getSessionUUID();
        if (sessionUUID == null) {
            return null;
        }
        return protobufFactory
                .newBuilder(sessionUUID)
                .build();
    }
}
