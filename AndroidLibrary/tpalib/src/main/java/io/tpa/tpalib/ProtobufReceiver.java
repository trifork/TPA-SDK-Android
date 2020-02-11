package io.tpa.tpalib;

import android.support.annotation.NonNull;

import io.tpa.tpalib.protobuf.ProtobufMessages;

interface ProtobufReceiver {

    void saveProtobufMessage(@NonNull ProtobufMessages.BaseMessage message, boolean sendImmediately);

    void restart();

    void stop();
}
