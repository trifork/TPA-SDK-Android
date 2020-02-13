package io.tpa.tpalib;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

class AsyncHelper {

    @Nullable
    private static Executor executor;

    @NonNull
    private static Executor getExecutor() {
        if (executor == null) {
            executor = Executors.newCachedThreadPool();
        }
        return executor;
    }

    @SafeVarargs
    static <Params, Progress, Result> AsyncTask<Params, Progress, Result> executeAsyncTask(AsyncTask<Params, Progress, Result> asyncTask, Params... params) {
        return asyncTask.executeOnExecutor(getExecutor(), params);
    }
}
