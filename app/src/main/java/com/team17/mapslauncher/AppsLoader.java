package com.team17.mapslauncher;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @credit http://developer.android.com/reference/android/content/AsyncTaskLoader.html
 */
public class AppsLoader extends AsyncTaskLoader<ArrayList<AppModel>> {
    ArrayList<AppModel> mInstalledApps;
    final ArrayList<String> desiredApps = new ArrayList<String>(Arrays.asList(
            "com.spotify.music",
            "com.spotify.mobile.android.ui",
            "com.android.settings",
            "com.android.vending",
            "cuteam17.cuteam17rpi"));

    final PackageManager mPm;
    PackageIntentReceiver mPackageObserver;

    public AppsLoader(Context context) {
        super(context);

        mPm = context.getPackageManager();
    }

    @Override
    public ArrayList<AppModel> loadInBackground() {
        // retrieve the list of installed applications
        final Context context = getContext();

        List<ApplicationInfo> apps = mPm.getInstalledApplications(0);
        ArrayList<AppModel> items = new ArrayList<AppModel>(4);
        for (String i : desiredApps) {
            try {
                AppModel app = new AppModel(context, mPm.getApplicationInfo(i, 0));
                app.loadLabel(context);
                items.add(app);
            } catch (PackageManager.NameNotFoundException e) {
                Log.d("MapsLauncher", "Application: " + i + "not found.");
            }
        }

        // sort the list
        Collections.sort(items, ALPHA_COMPARATOR);

        return items;
    }

    @Override
    public void deliverResult(ArrayList<AppModel> apps) {
        if (isReset()) {
            // An async query came in while the loader is stopped.  We
            // don't need the result.
            if (apps != null) {
                onReleaseResources(apps);
            }
        }

        ArrayList<AppModel> oldApps = apps;
        mInstalledApps = apps;

        if (isStarted()) {
            // If the Loader is currently started, we can immediately
            // deliver its results.
            super.deliverResult(apps);
        }

        // At this point we can release the resources associated with
        // 'oldApps' if needed; now that the new result is delivered we
        // know that it is no longer in use.
        if (oldApps != null) {
            onReleaseResources(oldApps);
        }
    }

    @Override
    protected void onStartLoading() {
        if (mInstalledApps != null) {
            // If we currently have a result available, deliver it
            // immediately.
            deliverResult(mInstalledApps);
        }

        // watch for changes in app install and uninstall operation
        if (mPackageObserver == null) {
            mPackageObserver = new PackageIntentReceiver(this);
        }

        if (takeContentChanged() || mInstalledApps == null ) {
            // If the data has changed since the last time it was loaded
            // or is not currently available, start a load.
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        // Attempt to cancel the current load task if possible.
        cancelLoad();
    }

    @Override
    public void onCanceled(ArrayList<AppModel> apps) {
        super.onCanceled(apps);

        // At this point we can release the resources associated with 'apps'
        // if needed.
        onReleaseResources(apps);
    }

    @Override
    protected void onReset() {
        // Ensure the loader is stopped
        onStopLoading();

        // At this point we can release the resources associated with 'apps'
        // if needed.
        if (mInstalledApps != null) {
            onReleaseResources(mInstalledApps);
            mInstalledApps = null;
        }

        // Stop monitoring for changes.
        if (mPackageObserver != null) {
            getContext().unregisterReceiver(mPackageObserver);
            mPackageObserver = null;
        }
    }

    /**
     * Helper method to do the cleanup work if needed, for example if we're
     * using Cursor, then we should be closing it here
     *
     * @param apps
     */
    protected void onReleaseResources(ArrayList<AppModel> apps) {
        // do nothing
    }


    /**
     * Perform alphabetical comparison of application entry objects.
     */
    public static final Comparator<AppModel> ALPHA_COMPARATOR = new Comparator<AppModel>() {
        private final Collator sCollator = Collator.getInstance();
        @Override
        public int compare(AppModel object1, AppModel object2) {
            return sCollator.compare(object1.getLabel(), object2.getLabel());
        }
    };

}
