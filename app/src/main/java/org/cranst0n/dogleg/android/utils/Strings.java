package org.cranst0n.dogleg.android.utils;

import org.passay.CharacterCharacteristicsRule;
import org.passay.DigitCharacterRule;
import org.passay.LengthRule;
import org.passay.PasswordData;
import org.passay.PasswordValidator;
import org.passay.Rule;
import org.passay.UppercaseCharacterRule;
import org.passay.WhitespaceRule;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Strings {

  private Strings() {

  }

  public static int levensteinDistance(final String x, final String y) {

    String a = x.toLowerCase();
    String b = y.toLowerCase();

    // i == 0
    int[] costs = new int[b.length() + 1];

    for (int j = 0; j < costs.length; j++) {
      costs[j] = j;
    }

    for (int i = 1; i <= a.length(); i++) {
      // j == 0; nw = lev(i - 1, j)
      costs[0] = i;
      int nw = i - 1;
      for (int j = 1; j <= b.length(); j++) {
        int cj = Math.min(1 + Math.min(costs[j], costs[j - 1]), a.charAt(i - 1) == b.charAt(j - 1) ? nw : nw + 1);
        nw = costs[j];
        costs[j] = cj;
      }
    }
    return costs[b.length()];
  }

  public static String numberSuffix(final int number) {

    if (number >= 11 && number <= 13) {
      return "th";
    }

    switch (number % 10) {
      case 1:
        return "st";
      case 2:
        return "nd";
      case 3:
        return "rd";
      default:
        return "th";
    }
  }

  public static boolean isEmailValid(final String email) {
    String regExpn =
        "^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]{1}|[\\w-]{2,}))@"
            + "((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
            + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
            + "([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
            + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])){1}|"
            + "([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$";

    CharSequence inputStr = email;

    Pattern pattern = Pattern.compile(regExpn, Pattern.CASE_INSENSITIVE);
    Matcher matcher = pattern.matcher(inputStr);

    if (matcher.matches()) {
      return true;
    } else {
      return false;
    }
  }

  public static List<String> isPasswordStrong(final String password) {
    LengthRule lengthRule = new LengthRule(8, 1000);
    WhitespaceRule noWhitespaceRule = new WhitespaceRule();

    CharacterCharacteristicsRule charRule = new CharacterCharacteristicsRule();
    charRule.getRules().add(new DigitCharacterRule(1));
    charRule.getRules().add(new UppercaseCharacterRule(1));

    List<Rule> ruleList = new ArrayList<Rule>();
    ruleList.add(lengthRule);
    ruleList.add(noWhitespaceRule);
    ruleList.add(charRule);

    PasswordValidator validator = new PasswordValidator(ruleList);
    PasswordData passwordData = new PasswordData(password);

    return validator.getMessages(validator.validate(passwordData));
  }
}
