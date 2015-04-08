package org.cranst0n.dogleg.android.utils.nfc;

import android.nfc.Tag;
import android.nfc.tech.TagTechnology;

import org.cranst0n.dogleg.android.model.Club;

public interface NfcTagIO<T extends TagTechnology> {

  Club readClub(final Tag tag);

  boolean writeClubTag(final Tag tag, final Club club);
}
