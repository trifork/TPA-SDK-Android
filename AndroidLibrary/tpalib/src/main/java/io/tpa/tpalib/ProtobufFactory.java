package io.tpa.tpalib;

import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import io.tpa.tpalib.protobuf.ProtobufMessages;
import io.tpa.tpalib.protobuf.runtime.ByteString;

final class ProtobufFactory {

    @NonNull
    private Constants constants;

    ProtobufFactory(@NonNull Constants constants) {
        this.constants = constants;
    }

    static class MessageBuilder {

        @NonNull
        private ProtobufMessages.BaseMessage.Builder builder;

        @NonNull
        private ProtobufFactory factory;

        MessageBuilder(@NonNull String sessionUUID, @NonNull ProtobufFactory factory) {
            this.factory = factory;

            builder = initializeBaseMessage(sessionUUID);
        }

        ProtobufMessages.BaseMessage build() {
            return builder.build();
        }

        MessageBuilder setFeedback(@NonNull String comment, @NonNull byte[] mediaBytes) {
            ProtobufMessages.Feedback.Builder feedback = ProtobufMessages.Feedback.newBuilder()
                                                                                  .setComment(comment)
                                                                                  .setMedia(ByteString.copyFrom(mediaBytes))
                                                                                  .setMimeType("image/png");

            builder.setFeedback(feedback);

            return this;
        }

        MessageBuilder setCrashReport(@NonNull String report, @NonNull String kind, boolean isFatal) {
            ProtobufMessages.Issue.Builder crashReport = ProtobufMessages.Issue.newBuilder()
                                                                               .setReport(report)
                                                                               .setKind(kind)
                                                                               .setFatal(isFatal);

            builder.setIssue(crashReport);

            return this;
        }

        MessageBuilder setLog(@NonNull String text, @NonNull String tag, @NonNull String logLevel) {
            ProtobufMessages.SessionLog.Builder log = ProtobufMessages.SessionLog.newBuilder()
                                                                                 .setText(text)
                                                                                 .setTag(tag)
                                                                                 .setLogLevel(logLevel);

            builder.setSessionLog(log);

            return this;
        }

        MessageBuilder setInstallation(boolean isNewInstallation) {
            ProtobufMessages.InstallationEvent.UpdateType updateType = isNewInstallation ? ProtobufMessages.InstallationEvent.UpdateType.FRESH_INSTALL : ProtobufMessages.InstallationEvent.UpdateType.UPDATE;

            ProtobufMessages.InstallationEvent.Builder installationEvent = ProtobufMessages.InstallationEvent.newBuilder()
                                                                                                             .setUpdateType(updateType);

            builder.setInstallation(installationEvent);

            return this;
        }

        MessageBuilder setSessionStart() {
            ProtobufMessages.SessionStart.Builder sessionStart = ProtobufMessages.SessionStart.newBuilder()
                                                                                              .setAppVersion(factory.constants.APP_VERSION)
                                                                                              .setVersionString(factory.constants.APP_VERSION_NAME)
                                                                                              .addAllAdditionalInfo(getSystemDetailsAsProtobufStringPair());

            builder.setSessionStart(sessionStart);

            return this;
        }

        MessageBuilder setSessionEnd() {
            builder.setSessionEnd(ProtobufMessages.SessionEnd.newBuilder());

            return this;
        }

        MessageBuilder setTrackingEvent(@NonNull TpaEvent tpaEvent) {
            ProtobufMessages.AppEvent.Builder appEvent = ProtobufMessages.AppEvent.newBuilder()
                                                                                  .setCategory(tpaEvent.getCategory())
                                                                                  .setName(tpaEvent.getName())
                                                                                  .addAllMetaText(stringMapToStringPair(tpaEvent.getTagsInternal()));

            builder.setEvent(appEvent);

            return this;
        }

        MessageBuilder setTrackingNumberEvent(@NonNull TpaNumberEvent tpaNumberEvent) {
            Double value = tpaNumberEvent.getValue();
            double usableValue = value != null ? value : 0;

            ProtobufMessages.DoubleValueEvent.Builder doubleValueEvent = ProtobufMessages.DoubleValueEvent.newBuilder()
                                                                                                          .setCategory(tpaNumberEvent.getCategory())
                                                                                                          .setName(tpaNumberEvent.getName())
                                                                                                          .setValue(usableValue)
                                                                                                          .addAllMetaText(stringMapToStringPair(tpaNumberEvent.getTagsInternal()));

            builder.setDoubleValueEvent(doubleValueEvent);

            return this;
        }

        MessageBuilder setTimingEvent(@NonNull TpaTimingEvent tpaTimingEvent, @Nullable Long duration) {
            ProtobufMessages.TimingEvent.Builder timingEvent = ProtobufMessages.TimingEvent.newBuilder()
                                                                                           .setCategory(tpaTimingEvent.getCategory())
                                                                                           .setName(tpaTimingEvent.getName())
                                                                                           .setDuration(duration == null ? tpaTimingEvent.getDuration() : duration)
                                                                                           .addAllMetaText(stringMapToStringPair(tpaTimingEvent.getTagsInternal()));

            builder.setTimingEvent(timingEvent);

            return this;
        }

        private ProtobufMessages.BaseMessage.Builder initializeBaseMessage(@NonNull String sessionUUID) {
            return ProtobufMessages.BaseMessage.newBuilder()
                                               .setTimestamp(System.currentTimeMillis() / 1000.0)
                                               .setSessionUuid(sessionUUID)
                                               .setDeviceUuid(Installation.id(factory.constants.FILES_PATH).getId())
                                               .setMessageId(UUID.randomUUID().toString());
        }

        @NonNull
        private List<ProtobufMessages.StringPair> stringMapToStringPair(@NonNull Map<String, String> map) {
            List<ProtobufMessages.StringPair> result = new ArrayList<>();

            for (Map.Entry<String, String> entry : map.entrySet()) {
                result.add(
                        ProtobufMessages.StringPair.newBuilder()
                                                   .setKey(entry.getKey())
                                                   .setValue(entry.getValue())
                                                   .build()
                );
            }

            return result;
        }

        @NonNull
        private List<ProtobufMessages.StringPair> getSystemDetailsAsProtobufStringPair() {
            return stringMapToStringPair(factory.getSystemDetails());
        }
    }

    @NonNull
    MessageBuilder newBuilder(@NonNull String sessionUUID) {
        return new MessageBuilder(sessionUUID, this);
    }

    @NonNull
    Map<String, String> getSystemDetails() {
        Map<String, String> result = new HashMap<>();

        result.put("DEVICE:HW", Build.CPU_ABI);
        result.put("DEVICE:MODEL", constants.PHONE_MANUFACTURER + " " + constants.PHONE_MODEL);
        result.put("DEVICE:PRODUCT", Build.PRODUCT);
        result.put("DEVICE:OS", Build.VERSION.CODENAME + " " + constants.ANDROID_VERSION);
        result.put("MODEL", constants.PHONE_MODEL);
        result.put("MANUFACTURER", constants.PHONE_MANUFACTURER);
        result.put("VERSION.INCREMENTAL", Build.VERSION.INCREMENTAL);
        result.put("ANDROID:SDK", "" + Build.VERSION.SDK_INT);
        result.put("VERSION.RELEASE", constants.ANDROID_VERSION);

        return result;
    }
}
