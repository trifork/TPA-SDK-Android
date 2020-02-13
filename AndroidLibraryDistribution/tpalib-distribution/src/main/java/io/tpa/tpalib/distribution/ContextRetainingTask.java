package io.tpa.tpalib.distribution;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;

abstract class ContextRetainingTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {

    @SuppressLint("StaticFieldLeak")
    private Context context;

    private boolean isCleanedUp = false;

    protected ContextRetainingTask(Context context) {
        this.context = context;
    }

    protected Context getContext() {
        return context;
    }

    protected void setContext(Context context) {
        if (!isCleanedUp) {
            this.context = context;
        }
    }

    @Override
    protected void onPostExecute(Result result) {
        super.onPostExecute(result);

        context = null;
        isCleanedUp = true;
    }
}
