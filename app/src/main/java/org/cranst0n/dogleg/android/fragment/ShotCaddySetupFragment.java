package org.cranst0n.dogleg.android.fragment;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;

import com.afollestad.materialdialogs.MaterialDialog;

import org.cranst0n.dogleg.android.R;
import org.cranst0n.dogleg.android.model.Club;
import org.cranst0n.dogleg.android.utils.SnackBars;
import org.cranst0n.dogleg.android.utils.nfc.Nfc;

public class ShotCaddySetupFragment extends BaseFragment {

  private final String TAG = getClass().getSimpleName();

  private NfcAdapter nfcAdapter;

  private View setupView;

  private RadioButton readModeButton;
  private RadioButton writeModeButton;
  private Button clubSelectionButton;

  private enum NfcMode {Read, Write}

  private NfcMode nfcMode = NfcMode.Write;
  private Club clubToWrite = Club.Unknown;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    initNfc();
  }

  @Override
  public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle
      savedInstanceState) {

    setupView = inflater.inflate(R.layout.fragment_shot_caddy_setup, container, false);

    readModeButton = (RadioButton) setupView.findViewById(R.id.nfc_read_mode_button);
    writeModeButton = (RadioButton) setupView.findViewById(R.id.nfc_write_mode_button);
    clubSelectionButton = (Button) setupView.findViewById(R.id.club_selection_button);

    readModeButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(final View view) {
        writeModeButton.setChecked(false);
        nfcMode = NfcMode.Read;
        clubSelectionButton.setEnabled(false);
      }
    });

    writeModeButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(final View view) {
        readModeButton.setChecked(false);
        nfcMode = NfcMode.Write;
        clubSelectionButton.setEnabled(true);
      }
    });

    clubSelectionButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(final View view) {

        final String[] clubNames = new String[Club.values().length];
        for (int ix = 0; ix < Club.values().length; ix++) {
          clubNames[ix] = Club.values()[ix].name;
        }

        new MaterialDialog.Builder(activity)
            .title("Select Club")
            .items(clubNames)
            .itemsCallbackSingleChoice(-1, new MaterialDialog.ListCallbackSingleChoice() {
              @Override
              public boolean onSelection(final MaterialDialog materialDialog, final View view,
                                         final int i, final CharSequence charSequence) {

                clubToWrite = Club.values()[i];
                clubSelectionButton.setText(clubToWrite.name);

                return true;
              }
            })
            .show();
      }
    });

    return setupView;
  }

  @Override
  public void onPause() {
    super.onPause();

    if (nfcAdapter != null) {
      nfcAdapter.disableForegroundDispatch(activity);
    }
  }

  @Override
  public void onResume() {
    super.onResume();

    if (nfcAdapter != null) {
      Nfc.enableForegroundDispatch(nfcAdapter, activity);
    }
  }

  private void initNfc() {

    nfcAdapter = NfcAdapter.getDefaultAdapter(activity);

    if (nfcAdapter == null) {
      SnackBars.showSimple(activity, "This device doesn't seem to support NFC.");
    } else {

      if (!nfcAdapter.isEnabled()) {
        SnackBars.showSimple(activity, "NFC is disabled!");
      }
    }
  }

  public void onNewIntent(final Intent intent) {

    Tag tag = (Tag) intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

    Log.d(TAG, "New NFC tag: [" + tag + "]");

    switch (nfcMode) {
      case Read: {
        Club club = Nfc.readClubTag(tag);
        SnackBars.showSimple(activity, "Club tag found: " + club.name);
        break;
      }
      case Write: {
        if (Nfc.writeClubTag(tag, clubToWrite)) {
          SnackBars.showSimple(activity, String.format("Wrote '%s' to tag.", clubToWrite.name));
        } else {
          SnackBars.showSimple(activity, String.format("'%s' write failed.", clubToWrite));
        }
        break;
      }
    }
  }

}
