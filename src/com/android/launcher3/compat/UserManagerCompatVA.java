package com.android.launcher3.compat;

import android.os.UserHandle;

import com.lody.virtual.os.VUserHandle;
import com.lody.virtual.os.VUserInfo;
import com.lody.virtual.os.VUserManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * author: weishu on 18/2/11.
 */

public class UserManagerCompatVA extends UserManagerCompat {
    private VUserManager mUserManager;

    public UserManagerCompatVA() {
        this.mUserManager = VUserManager.get();
    }

    @Override
    public void enableAndResetCache() {

    }

    @Override
    public List<UserHandle> getUserProfiles() {
        List<VUserInfo> users = mUserManager.getUsers();
        List<UserHandle> result = new ArrayList<>();
        for (VUserInfo user : users) {
            result.add(mirror.android.os.UserHandle.of.call(user.id));
        }
        return result;
    }

    @Override
    public long getSerialNumberForUser(UserHandle user) {
        VUserHandle vUserHandle = new VUserHandle(mirror.android.os.UserHandle.getIdentifier.call(user));
        return mUserManager.getSerialNumberForUser(vUserHandle);
    }

    @Override
    public UserHandle getUserForSerialNumber(long serialNumber) {
        VUserHandle vUser = mUserManager.getUserForSerialNumber(serialNumber);
        return mirror.android.os.UserHandle.of.call(vUser.getIdentifier());
    }

    @Override
    public CharSequence getBadgedLabelForUser(CharSequence label, UserHandle user) {
        return String.format(Locale.getDefault(), "%s[%d]", label, mirror.android.os.UserHandle.getIdentifier.call(user));
    }

    @Override
    public long getUserCreationTime(UserHandle user) {
        int userId = mirror.android.os.UserHandle.getIdentifier.call(user);
        VUserInfo userInfo = mUserManager.getUserInfo(userId);
        return userInfo.creationTime;
    }

    @Override
    public boolean isQuietModeEnabled(UserHandle user) {
        return false;
    }

    @Override
    public boolean isUserUnlocked(UserHandle user) {
        return false;
    }

    @Override
    public boolean isDemoUser() {
        return false;
    }
}
