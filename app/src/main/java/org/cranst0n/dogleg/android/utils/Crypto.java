package org.cranst0n.dogleg.android.utils;

import android.util.Log;

import org.bouncycastle.jcajce.provider.digest.SHA3;

import java.math.BigInteger;

public class Crypto {

  public static final String hashPassword(final String password) {

    String hashed = null;

    try {

      SHA3.DigestSHA3 sha3 = new SHA3.Digest512();
      sha3.update(password.getBytes("UTF-8"));

      byte[] bytes = sha3.digest();
      BigInteger bi = new BigInteger(1, bytes);

      hashed = String.format("%0" + (bytes.length << 1) + "X", bi).toLowerCase();

    } catch (Exception ex) {
      Log.e(Crypto.class.getSimpleName(), "Failed to hash password.", ex);
    }

    return hashed;
  }

}
