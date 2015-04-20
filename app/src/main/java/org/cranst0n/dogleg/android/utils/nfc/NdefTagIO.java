package org.cranst0n.dogleg.android.utils.nfc;

import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.support.annotation.NonNull;
import android.util.Log;

import org.cranst0n.dogleg.android.model.Club;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class NdefTagIO implements NfcTagIO<Ndef> {

  private final String TAG = getClass().getSimpleName();

  @Override
  public Club readClub(@NonNull final Tag tag) {

    Ndef ndefTag = Ndef.get(tag);

    try {

      ndefTag.connect();
      NdefMessage ndefMessage = ndefTag.getNdefMessage();

      if (ndefMessage != null) {
        NdefRecord[] ndefRecords = ndefMessage.getRecords();
        if (ndefRecords.length > 0) {

          int clubId = ByteBuffer.wrap(ndefRecords[0].getPayload()).getInt();
          return Club.forId(clubId);

        } else {
          return Club.Unknown;
        }
      } else {
        return Club.Unknown;
      }

    } catch (final IOException | FormatException e) {
      Log.e(TAG, "Exception while reading Ndef tag.", e);
    } finally {
      if (ndefTag != null) {
        try {
          ndefTag.close();
        } catch (final IOException e) {
          Log.e(TAG, "IOException while closing Ndef tag.", e);
        }
      }
    }

    return Club.Unknown;
  }

  @Override
  public boolean writeClubTag(@NonNull final Tag tag, @NonNull final Club club) {

    Ndef ndefTag = Ndef.get(tag);

    NdefRecord ndefRecord = new NdefRecord(
        NdefRecord.TNF_MIME_MEDIA,
        "application/org.cranst0n.dogleg.android".getBytes(Charset.forName("US-ASCII")),
        new byte[0], ByteBuffer.allocate(4).putInt(club.id).array());

    NdefMessage ndefMessage = new NdefMessage(new NdefRecord[]{
        ndefRecord
    });

    try {

      ndefTag.connect();
      ndefTag.writeNdefMessage(ndefMessage);

      return true;

    } catch (final IOException | FormatException e) {
      Log.e(TAG, "Exception while writing to Ndef tag.", e);
    } finally {
      if (ndefTag != null) {
        try {
          ndefTag.close();
        } catch (final IOException e) {
          Log.e(TAG, "IOException while closing Ndef tag.", e);
        }
      }
    }

    return false;
  }
}
