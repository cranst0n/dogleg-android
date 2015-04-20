package org.cranst0n.dogleg.android.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import org.cranst0n.dogleg.android.DoglegApplication;

public class PreferencesEditor {

  private static SharedPreferences mSingletonPreferences;

  public static SharedPreferences getSharedPreferences() {
    if (mSingletonPreferences == null) {
      synchronized (PreferencesEditor.class) {
        if (mSingletonPreferences == null) {
          mSingletonPreferences = PreferenceManager.getDefaultSharedPreferences
              (DoglegApplication.application().context());
        }
      }
    }
    return mSingletonPreferences;
  }

  public static void shutdown() {
    mSingletonPreferences.edit().apply();
    mSingletonPreferences = null;
  }

  public static void clearPreferences() {
    Editor editor = getSharedPreferences().edit();
    editor.clear();
    editor.apply();
    shutdown();
  }

  public static boolean getBooleanPreference(final Context context, final int resId, final boolean defaultValue) {
    return getSharedPreferences().getBoolean(context.getString(resId), defaultValue);
  }

  public static boolean getBooleanPreference(final String prefKey, final boolean defaultValue) {
    return getSharedPreferences().getBoolean(prefKey, defaultValue);
  }

  public static int getIntPreference(final Context context, final int resId, final int defaultValue) {
    return getSharedPreferences().getInt(context.getString(resId), defaultValue);
  }

  public static int getIntPreference(final String prefKey, final int defaultValue) {
    return getSharedPreferences().getInt(prefKey, defaultValue);
  }

  @Nullable
  public static String getStringPreference(final Context context, final int resId, final String defaultValue) {
    return getSharedPreferences().getString(context.getString(resId), defaultValue);
  }

  @Nullable
  public static String getStringPreference(final String prefKey, final String defaultValue) {
    return getSharedPreferences().getString(prefKey, defaultValue);
  }

  public static void savePreference(final Context context, final int resId, final int newValue) {
    Editor editor = getSharedPreferences().edit();
    editor.putInt(context.getString(resId), newValue);
    editor.apply();
  }

  public static void savePreference(final String prefKey, final int newValue) {
    Editor editor = getSharedPreferences().edit();
    editor.putInt(prefKey, newValue);
    editor.apply();
  }

  public static void savePreference(final Context context, final int resId, final String newValue) {
    Editor editor = getSharedPreferences().edit();
    editor.putString(context.getString(resId), newValue);
    editor.apply();
  }

  public static void savePreference(final String prefKey, final String newValue) {
    Editor editor = getSharedPreferences().edit();
    editor.putString(prefKey, newValue);
    editor.apply();
  }

  public static void savePreference(final Context context, final int resId, final Boolean newValue) {
    Editor editor = getSharedPreferences().edit();
    editor.putBoolean(context.getString(resId), newValue);
    editor.apply();
  }

  public static void savePreference(final String prefKey, final Boolean newValue) {
    Editor editor = getSharedPreferences().edit();
    editor.putBoolean(prefKey, newValue);
    editor.apply();
  }

  public static void removePreference(final Context context, final int resId) {
    Editor editor = getSharedPreferences().edit();
    editor.remove(context.getString(resId));
    editor.apply();
  }

  public static void removePreference(final String prefKey) {
    Editor editor = getSharedPreferences().edit();
    editor.remove(prefKey);
    editor.apply();
  }

}
