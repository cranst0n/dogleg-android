package org.cranst0n.dogleg.android.utils.nfc;

import android.nfc.Tag;
import android.nfc.tech.MifareUltralight;
import android.util.Log;

import org.cranst0n.dogleg.android.model.Club;

import java.io.IOException;
import java.nio.ByteBuffer;

public class MifareUltralightTagIO implements NfcTagIO<MifareUltralight> {

  private final String TAG = getClass().getSimpleName();

  public Club readClub(final Tag tag) {

    MifareUltralight mifare = MifareUltralight.get(tag);

    try {

      mifare.connect();
      return Club.forId(ByteBuffer.wrap(mifare.readPages(4)).getInt());

    } catch (IOException e) {
      Log.e(TAG, "IOException while reading MifareUltralight message.", e);
    } finally {
      if (mifare != null) {
        try {
          mifare.close();
        } catch (IOException e) {
          Log.e(TAG, "Error closing tag.", e);
        }
      }
    }

    return Club.Unknown;
  }

  public boolean writeClubTag(final Tag tag, final Club club) {

    MifareUltralight ultralight = MifareUltralight.get(tag);

    try {

      ultralight.connect();

      ByteBuffer idBuffer = ByteBuffer.allocate(4);
      idBuffer.putInt(club.id);

      ultralight.writePage(4, idBuffer.array());
      ultralight.writePage(5, new byte[4]);
      ultralight.writePage(6, new byte[4]);
      ultralight.writePage(7, new byte[4]);

      return true;

    } catch (IOException e) {
      Log.e(TAG, "IOException while closing MifareUltralight...", e);
    } finally {
      try {
        ultralight.close();
      } catch (IOException e) {
        Log.e(TAG, "IOException while closing MifareUltralight...", e);
      }
    }

    return false;
  }

}
