package io.tpa.tpalib;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;

class VersionCode {

    private static final String VERSION_CODE = "VERSIONCODE";

    synchronized static VersionCodeChange updateVersionCode(@Nullable String versionCode, @NonNull String filesPath) throws InstallationValidationException {
        if (versionCode == null) { //Return failure if versionCode is null, this method was likely used incorrectly
            throw new InstallationValidationException(InstallationValidationException.Reason.MISSING_VERSION_CODE);
        }

        File file = new File(filesPath, VERSION_CODE);
        if (!file.exists()) { //File does not exist, we assume this is a new installation and save the version code
            writeVersionCode(file, versionCode);
            return VersionCodeChange.CREATED;
        } else {
            String savedVersionCode = readVersionCode(file); //Read version code from file and check it against the provided.
            if (versionCode.equals(savedVersionCode)) {
                return VersionCodeChange.NO_CHANGE; //Matching version codes, no update
            } else {
                writeVersionCode(file, versionCode); //Non-matching version codes, write version code to file
                return VersionCodeChange.UPDATED;
            }
        }
    }

    private synchronized static String readVersionCode(File file) throws InstallationValidationException {
        try {
            RandomAccessFile versionFile = new RandomAccessFile(file, "r");
            byte[] bytes = new byte[(int) versionFile.length()];
            versionFile.readFully(bytes);
            versionFile.close();
            return new String(bytes);
        } catch (Exception ex) {
            throw new InstallationValidationException(InstallationValidationException.Reason.READ_ERROR, ex);
        }
    }

    private synchronized static void writeVersionCode(File file, String versionCode) throws InstallationValidationException {
        try {
            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(versionCode.getBytes());
            outputStream.close();
        } catch (Exception ex) {
            throw new InstallationValidationException(InstallationValidationException.Reason.WRITE_ERROR, ex);
        }
    }

    static class InstallationValidationException extends RuntimeException {

        enum Reason {
            MISSING_VERSION_CODE("Missing version code"),
            WRITE_ERROR("Error writing to file"),
            READ_ERROR("Error reading from file");

            final String message;

            Reason(String message) {
                this.message = message;
            }
        }

        InstallationValidationException(Reason reason) {
            super(reason.message);
        }

        InstallationValidationException(Reason reason, Throwable throwable) {
            super(reason.message, throwable);
        }
    }

    enum VersionCodeChange {

        CREATED,
        UPDATED,
        NO_CHANGE
    }
}
