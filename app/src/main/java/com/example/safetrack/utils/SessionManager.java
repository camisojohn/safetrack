package com.example.safetrack.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        prefs  = context.getSharedPreferences(
                Constants.PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public void saveSession(String userId,
                            String username,
                            String role) {
        editor.putString(Constants.KEY_USER_ID,  userId);
        editor.putString(Constants.KEY_USERNAME, username);
        editor.putString(Constants.KEY_ROLE,     role);
        editor.putBoolean("isLoggedIn",          true);
        editor.apply();
    }

    public String  getUserId()   {
        return prefs.getString(Constants.KEY_USER_ID, null);
    }
    public String  getUsername() {
        return prefs.getString(Constants.KEY_USERNAME, null);
    }
    public String  getRole()     {
        return prefs.getString(Constants.KEY_ROLE, "user");
    }
    public boolean isLoggedIn()  {
        return prefs.getBoolean("isLoggedIn", false);
    }
    public boolean isAdmin()     {
        return "admin".equals(getRole());
    }

    public void logout() {
        editor.clear();
        editor.apply();
    }
}