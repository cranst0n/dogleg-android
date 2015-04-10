package org.cranst0n.dogleg.android.utils.nfc;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;
import android.nfc.tech.TagTechnology;
import android.util.Log;

import org.cranst0n.dogleg.android.model.Club;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Nfc {

  private static final String TAG = Nfc.class.getSimpleName();

  private static final Map<Class<? extends TagTechnology>, NfcTagIO<?>> tagIO = new HashMap<>();

  static {
    tagIO.put(Ndef.class, new NdefTagIO());
    tagIO.put(MifareUltralight.class, new MifareUltralightTagIO());
  }

  private Nfc() {

  }

  public static void enableForegroundDispatch(final NfcAdapter nfcAdapter, final Activity activity) {

    if (nfcAdapter != null) {

      PendingIntent pendingIntent = PendingIntent.getActivity(
          activity, 0, new Intent(activity, activity.getClass()).addFlags(Intent
              .FLAG_ACTIVITY_SINGLE_TOP),
          0);

      // Setup an intent filter for all MIME based dispatches
      IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);

      try {
        ndef.addDataType("*/*");
      } catch (IntentFilter.MalformedMimeTypeException e) {
        throw new RuntimeException("fail", e);
      }

      IntentFilter td = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);

      IntentFilter[] mFilters = new IntentFilter[]{
          ndef, td
      };

      String[][] mTechLists = new String[1][tagIO.size()];
      List<Class> techs = Arrays.asList(tagIO.keySet().toArray(new
          Class[tagIO.size()]));

      for (int ix = 0; ix < tagIO.size(); ix++) {
        mTechLists[0][ix] = techs.get(ix).getName();
      }

      nfcAdapter.enableForegroundDispatch(activity, pendingIntent, mFilters, mTechLists);
    }
  }

  public static Club readClubTag(final Tag tag) {

    Log.d(TAG, String.format("Reading club from tag: [%s]", tag));

    Class<? extends TagTechnology> tagTech = getTagTech(tag);

    Log.d(TAG, String.format("Tag tech found: %s", tagTech.getName()));

    if (tagIO.containsKey(tagTech)) {
      return tagIO.get(tagTech).readClub(tag);
    } else {
      Log.d(TAG, String.format("Unsupported tag tech: %s", tagTech.getName()));
    }

    return Club.Unknown;
  }

  public static boolean writeClubTag(final Tag tag, final Club club) {

    Log.d(TAG, String.format("Writing club to tag: [%s]", tag));

    Class<? extends TagTechnology> tagTech = getTagTech(tag);

    Log.d(TAG, String.format("Tag tech found: %s", tagTech.getName()));

    if (tagIO.containsKey(tagTech)) {
      return tagIO.get(tagTech).writeClubTag(tag, club);
    } else {
      Log.d(TAG, String.format("Unsupported tag tech: %s", tagTech.getName()));
    }

    return false;
  }

  public static Class<? extends TagTechnology> getTagTech(final Tag tag) {

    String[] techList = tag.getTechList();

    for (Class<? extends TagTechnology> tech : tagIO.keySet()) {
      for (String tagTech : tag.getTechList()) {
        if (tagTech.equals(tech.getClass().getName())) {
          return tech;
        }
      }
    }

    return tagIO.keySet().iterator().next();
  }

}
