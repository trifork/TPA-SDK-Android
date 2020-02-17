package io.tpa.tpalib;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import io.tpa.tpalib.protobuf.ProtobufMessages;
import io.tpa.tpalib.protobuf.runtime.CodedOutputStream;

final class TpaProtobufQueue implements ProtobufReceiver {

    private final static String TAG = "TpaProtobufQueue";

    @NonNull
    private BlockingQueue<MessageWrapper> messageQueue;
    @NonNull
    private FilePersistThread filePersistThread;
    @NonNull
    private ProtobufNetworkThread networkThread;

    TpaProtobufQueue(@Nullable URL protobufUploadURL, @NonNull BackendPing backendPing, @NonNull Constants constants) {
        messageQueue = new ArrayBlockingQueue<>(50);
        filePersistThread = new FilePersistThread(messageQueue, constants.FILES_PATH);
        networkThread = new ProtobufNetworkThread(filePersistThread.networkLock, protobufUploadURL, constants.FILES_PATH, UserAgent.get(constants), backendPing);
    }

    @Override
    public boolean saveProtobufMessage(@NonNull ProtobufMessages.BaseMessage message, boolean sendImmediately) {
        // Guard for session uuid null - this can happen in weird circumstances when an app is opened and closed rapidly.
        if (message.getSessionUuid() == null) {
            TpaDebugging.log.w(TAG, "A message was dropped due to missing session uuid!");
            return false;
        }

        // Don't queue messages when app is not in foreground
        if (filePersistThread.isPaused()) {
            TpaDebugging.log.w(TAG, "A message was dropped because it was sent after session end.");
            return false;
        }

        boolean offer = messageQueue.offer(new MessageWrapper(message, sendImmediately));
        if (!offer) {
            TpaDebugging.log.w(TAG, "A message was dropped because the queue was full");
            return false;
        }

        return true;
    }

    @Override
    public void restart() {
        filePersistThread.restart();
        if (!filePersistThread.isAlive()) {
            filePersistThread.start();
        }
        if (!networkThread.isAlive()) {
            networkThread.start();
        }
    }

    @Override
    public void stop() {
        filePersistThread.pause();
    }

    private static class MessageWrapper {

        @NonNull
        private ProtobufMessages.BaseMessage message;
        private boolean sendImmediately;

        MessageWrapper(@NonNull ProtobufMessages.BaseMessage message, boolean sendImmediately) {
            this.message = message;
            this.sendImmediately = sendImmediately;
        }

        void writeToStream(@Nullable OutputStream stream) throws IOException {
            if (stream == null) {
                return;
            }

            int size = message.getSerializedSize();
            byte[] outputArray = new byte[size];
            CodedOutputStream output = CodedOutputStream.newInstance(outputArray);
            message.writeTo(output);

            stream.write(encodeLEB128(size));
            stream.write(outputArray);

            stream.flush();
        }

        private byte[] encodeLEB128(int value) {
            ArrayList<Byte> byteList = new ArrayList<>();

            int remaining = value >>> 7;
            while (remaining != 0) {
                byteList.add((byte) ((value & 0x7f) | 0x80));
                value = remaining;
                remaining >>>= 7;
            }
            byteList.add((byte) (value & 0x7f));

            // Convert list to array
            byte[] result = new byte[byteList.size()];
            for (int i = 0; i < byteList.size(); i++) {
                result[i] = byteList.get(i);
            }
            return result;
        }
    }

    private static class FilePersistThread extends Thread {

        private static final long BASE_CHECK_INTERVAL = 60000;
        private final static long TWO_DAYS_IN_MILLI = 24 * 60 * 60 * 1000;

        @NonNull
        private BlockingQueue<MessageWrapper> messageQueue;

        // Create new file at thread start.
        @Nullable
        private File persistFile;
        @Nullable
        private OutputStream stream;
        @NonNull
        private final Object networkLock;
        @NonNull
        private final Object wakeUpLock;
        @NonNull
        private final String filesPath;
        @NonNull
        private AtomicBoolean isPaused;
        private long interval;

        FilePersistThread(@NonNull BlockingQueue<MessageWrapper> messageQueue, @NonNull String filesPath) {
            this.messageQueue = messageQueue;
            this.networkLock = new Object();
            this.wakeUpLock = new Object();
            this.filesPath = filesPath;
            this.isPaused = new AtomicBoolean(false);
            this.interval = new Random().nextInt(11) * 1000 + BASE_CHECK_INTERVAL;
        }

        void pause() {
            isPaused.set(true);
        }

        void restart() {
            isPaused.set(false);
            synchronized (wakeUpLock) {
                wakeUpLock.notify();
            }
        }

        boolean isPaused() {
            return isPaused.get();
        }

        @Override
        public void run() {
            cleanupLogFiles();
            while (true) {
                MessageWrapper messageWrapper = null;
                try {
                    messageWrapper = messageQueue.poll(interval, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    TpaDebugging.log.d(TAG, "Interrupted");
                }
                try {
                    if (messageWrapper != null) {
                        sendMessage(messageWrapper);
                    } else if (!isPaused()) { // Only attempt to ping if we're in the foreground
                        closePersistFile(); // Make sure we finish the current file first
                        triggerSendOfLogs();
                    }
                } catch (IOException ex) {
                    TpaDebugging.log.e(TAG, "Couldn't create or cleanup persist files");
                }
                synchronized (wakeUpLock) {
                    while (isPaused() && messageQueue.size() > 0) {
                        try {
                            wakeUpLock.wait();
                        } catch (InterruptedException ignored) {
                        }
                    }
                }
            }
        }

        private void sendMessage(@NonNull MessageWrapper messageWrapper) throws IOException {
            ensureFileExists();
            messageWrapper.writeToStream(stream);
            handleRoll(messageWrapper.sendImmediately);
        }

        private void handleRoll(boolean sendImmediately) throws IOException {
            // Always create a new file if we are sending the last one, or if currentPersistFile is unusable
            boolean fileOkay = persistFile != null && persistFile.exists() && persistFile.canWrite();

            if (!fileOkay || sendImmediately || persistFile.length() > TpaConstants.PROTOBUF_MAX_FILE_LENGTH) {
                closePersistFile();
                triggerSendOfLogs();
            }
        }

        private void triggerSendOfLogs() {
            synchronized (networkLock) {
                networkLock.notify();
            }
        }

        @NonNull
        private File createNewPersistFile() throws IOException {
            String dateString = String.valueOf(System.currentTimeMillis());
            String prefix = "tpa_message_" + dateString + "_";
            return File.createTempFile(prefix, TpaConstants.PROTOBUF_MESSAGE_EXTENSION, new File(filesPath));
        }

        private void cleanupLogFiles() {
            File outputDir = new File(filesPath);
            final File[] storedProtobufFiles = outputDir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    return filename != null && filename.endsWith(TpaConstants.PROTOBUF_MESSAGE_EXTENSION);
                }
            });

            if (storedProtobufFiles == null) {
                return;
            }

            for (File f : storedProtobufFiles) {
                if (f != null) {
                    Date lastModDate = new Date(f.lastModified());
                    Date twoDaysAgo = new Date(System.currentTimeMillis() - TWO_DAYS_IN_MILLI);
                    long fileSize = f.length();

                    if (lastModDate.after(twoDaysAgo) && fileSize != 0) {
                        f.renameTo(new File(f.getAbsolutePath() + TpaConstants.PROTOBUF_MESSAGE_READY_EXTENSION));
                    } else {
                        //if the stored log file is older than two days
                        f.delete();
                    }
                }
            }
        }

        private void ensureFileExists() throws IOException {
            if (persistFile == null) {
                persistFile = createNewPersistFile();
                stream = new BufferedOutputStream(new FileOutputStream(persistFile, true));
            }
        }

        private void closePersistFile() throws IOException {
            if (stream != null) {
                stream.flush();
                stream.close();
                stream = null;
            }

            if (persistFile != null) {
                persistFile.renameTo(new File(persistFile.getAbsolutePath() + TpaConstants.PROTOBUF_MESSAGE_READY_EXTENSION));
                persistFile = null;
            }
        }
    }

    private static class ProtobufNetworkThread extends Thread {

        private static final long PING_INTERVAL = 120000;

        @Nullable
        private final URL uploadURL;
        @NonNull
        private final Object networkNotifyLock;
        @NonNull
        private final String filesPath;
        @NonNull
        private final String userAgent;
        @NonNull
        private final BackendPing backendPing;
        private final long startOffset;
        private long lastUpload = 0;

        ProtobufNetworkThread(@NonNull Object networkNotifyLock, @Nullable URL uploadURL, @NonNull String filesPath, @NonNull String userAgent, @NonNull BackendPing backendPing) {
            this.networkNotifyLock = networkNotifyLock;
            this.uploadURL = uploadURL;
            this.filesPath = filesPath;
            this.userAgent = userAgent;
            this.backendPing = backendPing;
            Random random = new Random();
            this.startOffset = random.nextInt(11) * 1000;
        }

        private boolean uploadMessage(@NonNull File persistFile) {
            if (uploadURL == null) {
                TpaDebugging.log.d(TAG, "Uploading failed caused by missing URL");
                return false;
            }

            try {
                TpaDebugging.log.d(TAG, "Uploading persist file: " + persistFile.getName());
                TpaBackendClient.NetResult result = TpaBackendClient.postData(uploadURL, persistFile, userAgent);

                TpaDebugging.log.d(TAG, "Got response (" + result.statusCode + "): " + result.response);

                // If everything was good - remove pending message file
                if (result.statusCode == 200 || result.statusCode == -1) {
                    persistFile.delete();
                    return true;
                }
            } catch (IOException e) {
                TpaDebugging.log.e(TAG, "Couldn't send protobuf message: ", e);
            } catch (Exception e) {
                TpaDebugging.log.e(TAG, "Failed to read msg from TPA: " + e.getMessage(), e);
            }
            return false;
        }

        @SuppressWarnings("TryFinallyCanBeTryWithResources")
        private boolean sendPing() {
            if (uploadURL == null) {
                return false;
            }
            ProtobufMessages.BaseMessage ping = backendPing.createPing();
            if (ping == null) {
                return false;
            }
            MessageWrapper pingMessage = new MessageWrapper(ping, true);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try {
                pingMessage.writeToStream(outputStream);
                TpaBackendClient.NetResult result = TpaBackendClient.postData(uploadURL, outputStream.toByteArray(), userAgent);
                if (result.statusCode == 200 || result.statusCode == -1) {
                    return true;
                }
            } catch (IOException ex) {
                TpaDebugging.log.e(TAG, "Failed to send ping message");
            } finally {
                try {
                    outputStream.close();
                } catch (IOException ignored) {
                }
            }
            return false;
        }

        @Nullable
        private File[] getPendingLogFiles(@NonNull File directory) {
            File[] pendingLogFiles = directory.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String fileName) {
                    return fileName != null && fileName.endsWith(TpaConstants.PROTOBUF_MESSAGE_EXTENSION + TpaConstants.PROTOBUF_MESSAGE_READY_EXTENSION);
                }
            });
            if (pendingLogFiles == null) {
                return null;
            }

            //sort to make sure log files are sent in order (oldest first - timestamp is in file name)
            Arrays.sort(pendingLogFiles, new Comparator<File>() {
                @Override
                public int compare(File f1, File f2) {
                    return f1.getName().compareTo(f2.getName());
                }
            });
            return pendingLogFiles;
        }

        private boolean sendLogFilesOrPing() {
            File outputDir = new File(filesPath);
            File[] pendingLogFiles = getPendingLogFiles(outputDir);
            if (pendingLogFiles == null || pendingLogFiles.length == 0) {
                if (shouldPing() && sendPing()) { // Check if we should send ping and then check the result of ping to ensure it was delivered successfully
                    lastUpload = System.currentTimeMillis();
                }
                return true;
            }
            while (pendingLogFiles != null && pendingLogFiles.length > 0) {
                boolean success = uploadMessage(pendingLogFiles[0]);
                if (!success) {
                    return false;
                }
                pendingLogFiles = getPendingLogFiles(outputDir);
                lastUpload = System.currentTimeMillis();
            }
            return true;
        }

        private boolean hasPendingLogFiles() {
            File directory = new File(filesPath);
            File[] pendingLogFiles = directory.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String fileName) {
                    return fileName != null && fileName.endsWith(TpaConstants.PROTOBUF_MESSAGE_EXTENSION + TpaConstants.PROTOBUF_MESSAGE_READY_EXTENSION);
                }
            });
            return pendingLogFiles != null && pendingLogFiles.length > 0;
        }

        private boolean shouldPing() {
            return lastUpload != 0 && System.currentTimeMillis() - lastUpload >= PING_INTERVAL;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(startOffset);
            } catch (InterruptedException ex) {
                TpaDebugging.log.d(TAG, "Start Offset was interrupted");
            }
            int numberOfFailures = 0;
            //noinspection InfiniteLoopStatement
            while (true) {
                try {
                    synchronized (networkNotifyLock) {
                        while (!hasPendingLogFiles() && !shouldPing()) {
                            networkNotifyLock.wait();
                        }
                    }
                    boolean success = sendLogFilesOrPing();
                    if (!success) {
                        numberOfFailures++;
                        long timeToWait = Math.min(128000, (long) Math.pow(2, numberOfFailures) * 1000);
                        TpaDebugging.log.d(TAG, "Error uploading files, waiting " + timeToWait + " ms before retrying retrying.");
                        Thread.sleep(timeToWait);
                    } else {
                        numberOfFailures = 0;
                    }
                } catch (InterruptedException e) {
                    TpaDebugging.log.d(TAG, "Interrupt");
                }
            }
        }
    }
}
