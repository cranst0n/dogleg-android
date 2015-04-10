package org.cranst0n.dogleg.android.utils.nfc;

import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.util.Log;

import org.cranst0n.dogleg.android.model.Club;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class NdefTagIO implements NfcTagIO<Ndef> {

  private final String TAG = getClass().getSimpleName();

  @Override
  public Club readClub(final Tag tag) {
    
    Ndef ndefTag = Ndef.get(tag);

    try {

      ndefTag.connect();
      NdefMessage ndefMessage = ndefTag.getNdefMessage();

      if(ndefMessage != null) {
        NdefRecord[] ndefRecords = ndefMessage.getRecords();
        if(ndefRecords.length > 0) {

          int clubId = ByteBuffer.wrap(ndefRecords[0].getPayload()).getInt();
          return Club.forId(clubId);

        } else {
          return Club.Unknown;
        }
      } else {
        return Club.Unknown;
      }

    } catch (final IOException e) {
      e.printStackTrace();
    } catch (final FormatException e) {
      e.printStackTrace();
    } finally {
      if(ndefTag != null) {
        try {
          ndefTag.close();
        } catch (IOException e) {
          Log.e(TAG, "IOException while closing Ndef tag.", e);
        }
      }
    }

    return Club.Unknown;
  }

  @Override
  public boolean writeClubTag(final Tag tag, final Club club) {

    Ndef ndefTag = Ndef.get(tag);

    NdefRecord ndefRecord = new NdefRecord(
        NdefRecord.TNF_MIME_MEDIA ,
        "application/org.cranst0n.dogleg.android".getBytes(Charset.forName("US-ASCII")),
        new byte[0], ByteBuffer.allocate(4).putInt(club.id).array());

    NdefMessage ndefMessage = new NdefMessage(new NdefRecord[] {
        ndefRecord,
        NdefRecord.createApplicationRecord("org.cranst0n.dogleg.android")
    });

    try {

      ndefTag.connect();
      ndefTag.writeNdefMessage(ndefMessage);

      return true;

    } catch (final IOException e) {
      e.printStackTrace();
      Log.e(TAG, "IOException while writing to Ndef tag.", e);
    } catch (final FormatException e) {
      Log.e(TAG, "FormatException while writing to Ndef tag.", e);
    } finally {
      if(ndefTag != null) {
        try {
          ndefTag.close();
        } catch (IOException e) {
          Log.e(TAG, "IOException while closing Ndef tag.", e);
        }
      }
    }

    return false;
  }
}
