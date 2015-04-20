package org.cranst0n.dogleg.android.utils.nfc;

import android.nfc.Tag;
import android.nfc.tech.TagTechnology;
import android.support.annotation.NonNull;

import org.cranst0n.dogleg.android.model.Club;

public interface NfcTagIO<T extends TagTechnology> {

  Club readClub(@NonNull final Tag tag);

  boolean writeClubTag(@NonNull final Tag tag, @NonNull final Club club);
}
