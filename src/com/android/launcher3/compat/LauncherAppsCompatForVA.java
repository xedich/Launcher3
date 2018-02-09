package com.android.launcher3.compat;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.ResolveInfo;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.UserHandle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.android.launcher3.util.PackageUserKey;
import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.ipc.VPackageManager;
import com.lody.virtual.os.VUserHandle;
import com.lody.virtual.os.VUserInfo;
import com.lody.virtual.os.VUserManager;
import com.lody.virtual.remote.InstalledAppInfo;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * author: weishu on 18/2/9.
 */

public class LauncherAppsCompatForVA extends LauncherAppsCompat {
    private static final String TAG = "LauncherAppsCompatForVA";

    private final VirtualCore mVirtualCore;

    LauncherAppsCompatForVA() {
        mVirtualCore = VirtualCore.get();
    }

    @Override
    public List<LauncherActivityInfo> getActivityList(String packageName, UserHandle user) {
        if (packageName == null) {
            List<LauncherActivityInfo> result = new ArrayList<>();
            List<InstalledAppInfo> installedApps = mVirtualCore.getInstalledApps(0);
            for (InstalledAppInfo installedApp : installedApps) {
                List<LauncherActivityInfo> activityListForPackage = getActivityListForPackage(installedApp.packageName);
                assert activityListForPackage != null;
                result.addAll(activityListForPackage);
            }
            return result;
        } else {
            return getActivityListForPackage(packageName);
        }
    }

    @Override
    public LauncherActivityInfo resolveActivity(Intent intent, UserHandle user) {
        Context context = mVirtualCore.getContext();
        int userId = 0;

        VPackageManager pm = VPackageManager.get();
        List<ResolveInfo> ris = pm.queryIntentActivities(intent, intent.resolveType(context), 0, userId);

        // Otherwise, try to find a main launcher activity.
        if (ris == null || ris.size() <= 0) {
            // reuse the intent instance
            intent.removeCategory(Intent.CATEGORY_INFO);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.setPackage(intent.getPackage());
            ris = pm.queryIntentActivities(intent, intent.resolveType(context), 0, userId);
        }
        if (ris == null || ris.size() <= 0) {
            return null;
        }

        ActivityInfo activityInfo = ris.get(0).activityInfo;
        try {
            Constructor<LauncherActivityInfo> constructor = LauncherActivityInfo.class.getConstructor(Context.class, ActivityInfo.class, UserHandle.class);
            return constructor.newInstance(context, activityInfo, user);
        } catch (Throwable e) {
            Log.e(TAG, "create launcherActivityInfo failed", e);
            return null;
        }
    }

    @Override
    public void startActivityForProfile(ComponentName component, UserHandle user, Rect sourceBounds, Bundle opts) {
        throw new RuntimeException("unSupported for startActivity with system multi account");
    }

    @Override
    public ApplicationInfo getApplicationInfo(String packageName, int flags, UserHandle user) {
        InstalledAppInfo installedAppInfo = mVirtualCore.getInstalledAppInfo(packageName, flags);
        if (installedAppInfo == null) {
            return null;
        }
        return installedAppInfo.getApplicationInfo(0);
    }

    @Override
    public void showAppDetailsForProfile(ComponentName component, UserHandle user, Rect sourceBounds, Bundle opts) {
        // TODO: 18/2/9
    }

    @Override
    public void addOnAppsChangedCallback(OnAppsChangedCallbackCompat listener) {
        // TODO: 18/2/9
    }

    @Override
    public void removeOnAppsChangedCallback(OnAppsChangedCallbackCompat listener) {
        // TODO: 18/2/9
    }

    @Override
    public boolean isPackageEnabledForProfile(String packageName, UserHandle user) {
        return mVirtualCore.isAppInstalled(packageName);
    }

    @Override
    public boolean isActivityEnabledForProfile(ComponentName component, UserHandle user) {
        // TODO: 18/2/9
        return true;
    }

    @Override
    public List<ShortcutConfigActivityInfo> getCustomShortcutActivityList(@Nullable PackageUserKey packageUser) {
        // TODO: 18/2/9
        return Collections.EMPTY_LIST;
    }

    private List<LauncherActivityInfo> getActivityListForPackage(String packageName) {
        // return mLauncherApps.getActivityList(packageName, user);
        List<LauncherActivityInfo> result = new ArrayList<>();
        for (VUserInfo vUserInfo : VUserManager.get().getUsers()) {
            result.addAll(getActivityListForPackageAsUser(packageName, vUserInfo.id));
        }
        return result;
    }

    private List<LauncherActivityInfo> getActivityListForPackageAsUser(String packageName, int vuid) {

        List<LauncherActivityInfo> result = new ArrayList<>();
        Context context = mVirtualCore.getContext();
        VPackageManager pm = VPackageManager.get();
        Intent intentToResolve = new Intent(Intent.ACTION_MAIN);
        intentToResolve.addCategory(Intent.CATEGORY_INFO);
        intentToResolve.setPackage(packageName);
        List<ResolveInfo> ris = pm.queryIntentActivities(intentToResolve, intentToResolve.resolveType(context), 0, vuid);

        // Otherwise, try to find a main launcher activity.
        if (ris == null || ris.size() <= 0) {
            // reuse the intent instance
            intentToResolve.removeCategory(Intent.CATEGORY_INFO);
            intentToResolve.addCategory(Intent.CATEGORY_LAUNCHER);
            intentToResolve.setPackage(packageName);
            ris = pm.queryIntentActivities(intentToResolve, intentToResolve.resolveType(context), 0, vuid);
        }
        if (ris == null || ris.size() <= 0) {
            return result;
        }

        for (ResolveInfo resolveInfo: ris) {
            ActivityInfo activityInfo = resolveInfo.activityInfo;
            try {
                Constructor<LauncherActivityInfo> constructor = LauncherActivityInfo.class.getDeclaredConstructor(Context.class, ActivityInfo.class, UserHandle.class);
                constructor.setAccessible(true);
                int uid = VUserHandle.getUserId(vuid);
                UserHandle userHandle = UserHandle.class.getDeclaredConstructor(int.class).newInstance(uid);
                LauncherActivityInfo launcherActivityInfo = constructor.newInstance(context, activityInfo, userHandle);
                result.add(launcherActivityInfo);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        return result;
    }
}
