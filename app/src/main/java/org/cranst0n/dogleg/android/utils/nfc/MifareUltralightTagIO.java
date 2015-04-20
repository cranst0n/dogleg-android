package org.cranst0n.dogleg.android.utils.nfc;

import android.nfc.Tag;
import android.nfc.tech.MifareUltralight;
import android.support.annotation.NonNull;
import android.util.Log;

import org.cranst0n.dogleg.android.model.Club;

import java.io.IOException;
import java.nio.ByteBuffer;

public class MifareUltralightTagIO implements NfcTagIO<MifareUltralight> {

  private final String TAG = getClass().getSimpleName();

  @Override
  public Club readClub(@NonNull final Tag tag) {

    MifareUltralight mifare = MifareUltralight.get(tag);

    try {

      mifare.connect();

      return Club.forId(ByteBuffer.wrap(mifare.readPages(4)).getInt());

    } catch (final IOException e) {
      Log.e(TAG, "IOException while reading MifareUltralight message.", e);
    } finally {
      if (mifare != null) {
        try {
          mifare.close();
        } catch (final IOException e) {
          Log.e(TAG, "Error closing tag.", e);
        }
      }
    }

    return Club.Unknown;
  }

  @Override
  public boolean writeClubTag(@NonNull final Tag tag, @NonNull final Club club) {

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

    } catch (final IOException e) {
      Log.e(TAG, "IOException while writing to MifareUltralight.", e);
    } finally {
      if (ultralight != null) {
        try {
          ultralight.close();
        } catch (final IOException e) {
          Log.e(TAG, "IOException while closing MifareUltralight.", e);
        }
      }
    }

    return false;
  }

}
