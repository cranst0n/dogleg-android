package org.cranst0n.dogleg.android.fragment;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import org.cranst0n.dogleg.android.DoglegApplication;
import org.cranst0n.dogleg.android.R;
import org.cranst0n.dogleg.android.backend.BackendResponse;
import org.cranst0n.dogleg.android.backend.Users;
import org.cranst0n.dogleg.android.model.User;
import org.cranst0n.dogleg.android.model.UserStats;
import org.cranst0n.dogleg.android.utils.BusProvider;
import org.cranst0n.dogleg.android.utils.Colors;
import org.cranst0n.dogleg.android.utils.SnackBars;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends BaseFragment {

  private Bus bus;
  private User currentUser = DoglegApplication.application().user();

  private Users users;

  @Nullable
  private UserStats userStats;

  private boolean grossScoringDisplayed = true;
  private boolean grossRoundRecordsDisplayed = true;
  private boolean grossScoringRecordsDisplayed = true;

  private ImageButton customBackButton;
  private CircleImageView avatarView;
  private TextView usernameView;

  private TextView autoHandicap;
  private TextView totalRounds;
  private TextView memberSince;

  private Button scoringGrossButton;
  private Button scoringNetButton;
  private Button roundRecordsGrossButton;
  private Button roundRecordsNetButton;
  private Button scoringRecordsGrossButton;
  private Button scoringRecordsNetButton;

  private TextView averageScore18Holes;
  private TextView averagePuttsPerHole;
  private TextView lowScore18Holes;
  private TextView lowScore9Holes;

  private TextView fairwayHitPercentage;
  private TextView girPercentage;
  private TextView par3Average;
  private TextView par4Average;
  private TextView par5Average;
  private TextView averageEaglesPerRound;
  private TextView averageBirdiesPerRound;
  private TextView averageParsPerRound;

  private TextView aces;
  private TextView birdieStreak;
  private TextView parStreak;
  private TextView fewestPutts18Holes;
  private TextView mostBirdies18Holes;
  private TextView mostPars18Holes;

  @Override
  public void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    users = new Users(context);

    bus = BusProvider.Instance.bus;
    bus.register(this);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();

    bus.unregister(this);
  }

  @Override
  public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                           final Bundle savedInstanceState) {

    View profileView = inflater.inflate(R.layout.fragment_profile, container, false);

    findViews(profileView);
    attachListeners();

    updateView();

    return profileView;
  }

  @Subscribe
  public void newUser(@NonNull final User user) {

    currentUser = user;

    if (currentUser.isValid()) {
      refresh();
    } else {
      activity.finish();
    }
  }

  private void refresh() {
    users.stats(currentUser.id)
        .onSuccess(new BackendResponse.BackendSuccessListener<UserStats>() {
          @Override
          public void onSuccess(@NonNull final UserStats value) {
            userStats = value;
            updateView();
          }
        })
        .onError(SnackBars.showBackendError(activity))
        .onException(SnackBars.showBackendException(activity));
  }

  private void updateView() {
    if (isAdded() && userStats != null) {

      Ion.with(this)
          .load(users.avatarUrl(currentUser))
          .noCache()
          .asBitmap()
          .setCallback(new FutureCallback<Bitmap>() {
            @Override
            public void onCompleted(final Exception e, final Bitmap result) {
              if (e == null) {
                avatarView.setImageBitmap(result);
              }
            }
          });

      usernameView.setText(String.format("%s's Profile", userStats.user.name));

      autoHandicap.setText(String.valueOf(userStats.autoHandicap));
      totalRounds.setText(String.valueOf(userStats.totalRounds));
      memberSince.setText(userStats.user.created.toString("MMMM d, y"));

      if (grossScoringDisplayed) {
        averageScore18Holes.setText(String.format("%.2f", userStats.averageGross18Hole));
        par3Average.setText(String.format("%.2f", userStats.grossPar3Average));
        par4Average.setText(String.format("%.2f", userStats.grossPar4Average));
        par5Average.setText(String.format("%.2f", userStats.grossPar5Average));
        averageEaglesPerRound.setText(String.format("%.2f", userStats.grossAverageEaglesPerRound));
        averageBirdiesPerRound.setText(String.format("%.2f", userStats.grossAverageBirdiesPerRound));
        averageParsPerRound.setText(String.format("%.2f", userStats.grossAverageParsPerRound));
      } else {
        averageScore18Holes.setText(String.format("%.2f", userStats.averageNet18Hole));
        par3Average.setText(String.format("%.2f", userStats.netPar3Average));
        par4Average.setText(String.format("%.2f", userStats.netPar4Average));
        par5Average.setText(String.format("%.2f", userStats.netPar5Average));
        averageEaglesPerRound.setText(String.format("%.2f", userStats.netAverageEaglesPerRound));
        averageBirdiesPerRound.setText(String.format("%.2f", userStats.netAverageBirdiesPerRound));
        averageParsPerRound.setText(String.format("%.2f", userStats.netAverageParsPerRound));
      }

      averagePuttsPerHole.setText(String.format("%.2f", userStats.averagePuttPerHole));

      if (grossRoundRecordsDisplayed) {
        lowScore18Holes.setText(String.valueOf(userStats.lowGross18Hole));
        lowScore9Holes.setText(String.valueOf(userStats.lowGross9Hole));
        mostBirdies18Holes.setText(String.valueOf(userStats.grossMostBirdies18Hole));
        mostPars18Holes.setText(String.valueOf(userStats.grossMostPars18Hole));
      } else {
        lowScore18Holes.setText(String.valueOf(userStats.lowNet18Hole));
        lowScore9Holes.setText(String.valueOf(userStats.lowNet9Hole));
        mostBirdies18Holes.setText(String.valueOf(userStats.netMostBirdies18Hole));
        mostPars18Holes.setText(String.valueOf(userStats.netMostPars18Hole));
      }

      if (grossScoringRecordsDisplayed) {
        aces.setText(String.valueOf(userStats.grossAces));
        birdieStreak.setText(String.valueOf(userStats.grossBirdieStreak));
        parStreak.setText(String.valueOf(userStats.grossParStreak));
      } else {
        aces.setText(String.valueOf(userStats.netAces));
        birdieStreak.setText(String.valueOf(userStats.netBirdieStreak));
        parStreak.setText(String.valueOf(userStats.netParStreak));
      }

      fairwayHitPercentage.setText(String.format("%.2f%%", userStats.fairwayHitPercentage * 100));
      girPercentage.setText(String.format("%.2f%%", userStats.girPercentage * 100));
      fewestPutts18Holes.setText(String.valueOf(userStats.fewestPutts18Hole));
    }
  }

  private void attachListeners() {

    customBackButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(final View view) {
        activity.onBackPressed();
      }
    });

    scoringGrossButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(final View view) {
        grossScoringDisplayed = true;
        buttonToggled(scoringGrossButton, scoringNetButton);
      }
    });

    scoringNetButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(final View view) {
        grossScoringDisplayed = false;
        buttonToggled(scoringNetButton, scoringGrossButton);
      }
    });

    roundRecordsGrossButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(final View view) {
        grossRoundRecordsDisplayed = true;
        buttonToggled(roundRecordsGrossButton, roundRecordsNetButton);
      }
    });

    roundRecordsNetButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(final View view) {
        grossRoundRecordsDisplayed = false;
        buttonToggled(roundRecordsNetButton, roundRecordsGrossButton);
      }
    });

    scoringRecordsGrossButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(final View view) {
        grossScoringRecordsDisplayed = true;
        buttonToggled(scoringRecordsGrossButton, scoringRecordsNetButton);
      }
    });

    scoringRecordsNetButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(final View view) {
        grossScoringRecordsDisplayed = false;
        buttonToggled(scoringRecordsNetButton, scoringRecordsGrossButton);
      }
    });
  }

  private void buttonToggled(final Button selectedButton, final Button deselectedButton) {
    selectButton(selectedButton);
    deselectButton(deselectedButton);
    updateView();
  }

  private void selectButton(final Button button) {
    button.setBackgroundResource(R.color.accent);
    button.setTextColor(getResources().getColor(android.R.color.white));
  }

  private void deselectButton(final Button button) {
    button.setBackgroundResource(R.drawable.border);
    button.setTextColor(getResources().getColor(R.color.text_grey));
  }

  private void findViews(@NonNull final View profileView) {

    customBackButton = (ImageButton) profileView.findViewById(R.id.custom_back_button);
    customBackButton.setImageDrawable(
        Colors.colorize(customBackButton.getDrawable(), R.color.text_grey, context));

    avatarView = (CircleImageView) profileView.findViewById(R.id.user_avatar);
    usernameView = (TextView) profileView.findViewById(R.id.username);

    autoHandicap = (TextView) profileView.findViewById(R.id.auto_handicap);
    totalRounds = (TextView) profileView.findViewById(R.id.total_rounds);
    memberSince = (TextView) profileView.findViewById(R.id.member_since);

    scoringGrossButton = (Button) profileView.findViewById(R.id.scoring_gross_button);
    scoringNetButton = (Button) profileView.findViewById(R.id.scoring_net_button);
    roundRecordsGrossButton = (Button) profileView.findViewById(R.id.round_records_gross_button);
    roundRecordsNetButton = (Button) profileView.findViewById(R.id.round_records_net_button);
    scoringRecordsGrossButton = (Button) profileView.findViewById(R.id.scoring_records_gross_button);
    scoringRecordsNetButton = (Button) profileView.findViewById(R.id.scoring_records_net_button);

    averageScore18Holes = (TextView) profileView.findViewById(R.id.average_score_18_holes);
    averagePuttsPerHole = (TextView) profileView.findViewById(R.id.average_putts_per_hole);

    lowScore18Holes = (TextView) profileView.findViewById(R.id.low_score_18_holes);
    lowScore9Holes = (TextView) profileView.findViewById(R.id.low_score_9_holes);
    fairwayHitPercentage = (TextView) profileView.findViewById(R.id.fairway_hit_percentage);
    girPercentage = (TextView) profileView.findViewById(R.id.gir_percentage);

    par3Average = (TextView) profileView.findViewById(R.id.par_3_average);
    par4Average = (TextView) profileView.findViewById(R.id.par_4_average);
    par5Average = (TextView) profileView.findViewById(R.id.par_5_average);
    averageEaglesPerRound = (TextView) profileView.findViewById(R.id.average_eagles_per_round);
    averageBirdiesPerRound = (TextView) profileView.findViewById(R.id.average_birdies_per_round);
    averageParsPerRound = (TextView) profileView.findViewById(R.id.average_pars_per_round);

    aces = (TextView) profileView.findViewById(R.id.aces);
    birdieStreak = (TextView) profileView.findViewById(R.id.birdie_streak);
    parStreak = (TextView) profileView.findViewById(R.id.par_streak);
    fewestPutts18Holes = (TextView) profileView.findViewById(R.id.fewestPutts18Holes);
    mostBirdies18Holes = (TextView) profileView.findViewById(R.id.mostBirdies18Holes);
    mostPars18Holes = (TextView) profileView.findViewById(R.id.mostPars18Holes);
  }
}
