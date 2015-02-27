package org.cranst0n.dogleg.android.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import org.cranst0n.dogleg.android.DoglegApplication;

public class PreferencesEditor {

  private static SharedPreferences mSingletonPreferences;

  private static SharedPreferences getSharedPreferences(final Context context) {
    if (mSingletonPreferences == null) {
      synchronized (PreferencesEditor.class) {
        if (mSingletonPreferences == null) {
          mSingletonPreferences = PreferenceManager.getDefaultSharedPreferences(DoglegApplication.context());
        }
      }
    }
    return mSingletonPreferences;
  }

  public static void shutdown() {
    mSingletonPreferences.edit().commit();
    mSingletonPreferences = null;
  }

  public static void clearPreferences(final Context context) {
    Editor editor = getSharedPreferences(context).edit();
    editor.clear();
    editor.apply();
    shutdown();
  }

  public static boolean getBooleanPreference(final Context context, final int resId, final boolean defaultValue) {
    return getSharedPreferences(context).getBoolean(context.getString(resId), defaultValue);
  }

  public static boolean getBooleanPreference(final Context context, final String prefKey, final boolean defaultValue) {
    return getSharedPreferences(context).getBoolean(prefKey, defaultValue);
  }

  public static int getIntPreference(final Context context, final int resId, final int defaultValue) {
    return getSharedPreferences(context).getInt(context.getString(resId), defaultValue);
  }

  public static int getIntPreference(final Context context, final String prefKey, final int defaultValue) {
    return getSharedPreferences(context).getInt(prefKey, defaultValue);
  }

  public static String getStringPreference(final Context context, final int resId, final String defaultValue) {
    return getSharedPreferences(context).getString(context.getString(resId), defaultValue);
  }

  public static String getStringPreference(final Context context, final String prefKey, final String defaultValue) {
    return getSharedPreferences(context).getString(prefKey, defaultValue);
  }

  public static void savePreference(final Context context, final int resId, final int newValue) {
    Editor editor = getSharedPreferences(context).edit();
    editor.putInt(context.getString(resId), newValue);
    editor.apply();
  }

  public static void savePreference(final Context context, final String prefKey, final int newValue) {
    Editor editor = getSharedPreferences(context).edit();
    editor.putInt(prefKey, newValue);
    editor.apply();
  }

  public static void savePreference(final Context context, final int resId, final String newValue) {
    Editor editor = getSharedPreferences(context).edit();
    editor.putString(context.getString(resId), newValue);
    editor.apply();
  }

  public static void savePreference(final Context context, final String prefKey, final String newValue) {
    Editor editor = getSharedPreferences(context).edit();
    editor.putString(prefKey, newValue);
    editor.apply();
  }

  public static void savePreference(final Context context, final int resId, final Boolean newValue) {
    Editor editor = getSharedPreferences(context).edit();
    editor.putBoolean(context.getString(resId), newValue);
    editor.apply();
  }

  public static void savePreference(final Context context, final String prefKey, final Boolean newValue) {
    Editor editor = getSharedPreferences(context).edit();
    editor.putBoolean(prefKey, newValue);
    editor.apply();
  }

  public static void removePreference(final Context context, final int resId) {
    Editor editor = getSharedPreferences(context).edit();
    editor.remove(context.getString(resId));
    editor.apply();
  }

  public static void removePreference(final Context context, final String prefKey) {
    Editor editor = getSharedPreferences(context).edit();
    editor.remove(prefKey);
    editor.apply();
  }

}
