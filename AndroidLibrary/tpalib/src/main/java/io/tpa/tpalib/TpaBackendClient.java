package io.tpa.tpalib;

import android.support.annotation.NonNull;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

final class TpaBackendClient {

    private static final String TAG = "TpaBackendClient";

    private static final int CONNECT_TIMEOUT = 10000; // Wait ten seconds on connects

    private static final NetResult emptyResult = new NetResult(-1, "Empty data");

    static class NetResult {

        int statusCode;
        @NonNull
        String response;

        NetResult(int statusCode, @NonNull String response) {
            this.statusCode = statusCode;
            this.response = response;
        }
    }

    @NonNull
    static NetResult postData(@NonNull URL url, @NonNull File file, @NonNull String userAgent) throws IOException {
        return postData(url, FileUtils.readFile(file), userAgent);
    }

    @SuppressWarnings("TryFinallyCanBeTryWithResources")
    @NonNull
    static NetResult postData(@NonNull URL url, @NonNull byte[] data, @NonNull String userAgent) throws IOException {
        if (data.length == 0) {
            return emptyResult;
        }
        if (TpaDebugging.isEnabled()) {
            String debugUrl = "TPA";
            try {
                debugUrl = url.getProtocol() + "://" + url.getAuthority();
            } catch (Throwable e) {
                // Purely debug, so just ignore
            }
            TpaDebugging.log.d(TAG, "Posting " + data.length + " bytes to " + debugUrl);
        }

        // Setup connection
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(CONNECT_TIMEOUT);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("User-Agent", userAgent);
            conn.setRequestProperty("Content-Length", Integer.toString(data.length));
            conn.setDoOutput(true);
        } catch (IOException ex) {
            if (conn != null) {
                conn.disconnect();
            }
            throw ex;
        }

        // Send data
        DataOutputStream request = null;
        try {
            request = new DataOutputStream(conn.getOutputStream());
            request.write(data);
            request.flush();
        } catch (IOException ex) {
            conn.disconnect();
            throw ex;
        } finally {
            if (request != null) {
                request.close();
            }
        }

        // Get status code
        int statusCode = conn.getResponseCode();

        // Read response
        BufferedReader responseStreamReader = null;
        StringBuilder stringBuilder = new StringBuilder();
        try {
            InputStream responseStream = new BufferedInputStream(conn.getInputStream());
            responseStreamReader = new BufferedReader(new InputStreamReader(responseStream));
            String line;
            while ((line = responseStreamReader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
            responseStreamReader.close();
        } catch (IOException ex) {
            conn.disconnect();
            throw ex;
        } finally {
            if (responseStreamReader != null) {
                responseStreamReader.close();
            }
        }
        String response = stringBuilder.toString();
        conn.disconnect();

        return new NetResult(statusCode, response);
    }
}
