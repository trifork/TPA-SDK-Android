package io.tpa.tpalib.distribution;

import android.os.AsyncTask;

public class AsyncHelper {

    @SafeVarargs
    public static <Params, Progress, Result> AsyncTask<Params, Progress, Result> executeAsyncTask(AsyncTask<Params, Progress, Result> asyncTask, Params... params) {
        return asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
    }
}
