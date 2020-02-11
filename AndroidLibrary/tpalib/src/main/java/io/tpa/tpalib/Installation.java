package io.tpa.tpalib;

import android.support.annotation.NonNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.UUID;

class Installation {

    private static InstallationID sID = null;
    public static final String INSTALLATION_FILE_NAME = "INSTALLATION_FILE_NAME";

    static class InstallationID {

        private final String id;
        private final boolean isNewInstallation;

        InstallationID(String id, boolean isNewInstallation) {
            this.id = id;
            this.isNewInstallation = isNewInstallation;
        }

        String getId() {
            return id;
        }

        boolean isNewInstallation() {
            return isNewInstallation;
        }
    }

    synchronized static InstallationID id(@NonNull String filesPath) {
        if (sID == null) {
            File installation = new File(filesPath, INSTALLATION_FILE_NAME);
            try {
                boolean isNewInstallation = false;
                if (!installation.exists()) {
                    isNewInstallation = true;
                    writeInstallationFile(installation, UUID.randomUUID().toString());
                }
                String id = readInstallationFile(installation);
                sID = new InstallationID(id, isNewInstallation);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return sID;
    }

    @NonNull
    static String readInstallationFile(@NonNull File installation) throws IOException {
        RandomAccessFile f = new RandomAccessFile(installation, "r");
        byte[] bytes = new byte[(int) f.length()];
        f.readFully(bytes);
        f.close();
        return new String(bytes);
    }

    static void writeInstallationFile(@NonNull File installation, @NonNull String id) throws IOException {
        FileOutputStream out = new FileOutputStream(installation);
        out.write(id.getBytes());
        out.close();
    }
}