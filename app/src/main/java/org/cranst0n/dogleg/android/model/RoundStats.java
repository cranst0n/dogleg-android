package org.cranst0n.dogleg.android.model;

public class RoundStats {

  public final int score;
  public final int frontScore;
  public final int backScore;
  public final int scoreToPar;
  public final int putts;
  public final int frontPutts;
  public final int backPutts;
  public final int penalties;
  public final int frontPenalties;
  public final int backPenalties;
  public final double puttAverage;
  public final double fairwayHitPercentage;
  public final double frontFairwayHitPercentage;
  public final double backFairwayHitPercentage;
  public final double girPercentage;
  public final double frontGirPercentage;
  public final double backGirPercentage;
  public final double par3ScoringAverage;
  public final double par4ScoringAverage;
  public final double par5ScoringAverage;

  public RoundStats(final Round round) {

    int holesPlayed = 0;
    int frontHolesPlayed = 0;
    int backHolesPlayed = 0;

    int scoreToPar = 0;

    int score = 0;
    int frontScore = 0;
    int backScore = 0;
    int putts = 0;
    int frontPutts = 0;
    int backPutts = 0;
    int penalties = 0;
    int frontPenalties = 0;
    int backPenalties = 0;

    int fairwayOpportunities = 0;
    int frontFairwayOpportunities = 0;
    int backFairwayOpportunities = 0;
    int fairwaysHit = 0;
    int frontFairwaysHit = 0;
    int backFairwaysHit = 0;
    int girs = 0;
    int frontGirs = 0;
    int backGirs = 0;

    int par3s = 0;
    int par3Score = 0;
    int par4s = 0;
    int par4Score = 0;
    int par5s = 0;
    int par5Score = 0;

    for (int ix = 0; ix < round.holeScores.length; ix++) {

      HoleScore holeScore = round.holeScores[ix];
      HoleRating holeRating = round.rating.holeRatings[holeScore.hole.number - 1];

      if (holeScore.score > 0 || holeScore.putts > 0) {

        holesPlayed++;
        if (holeRating.number < 10) {
          frontHolesPlayed++;
        } else {
          backHolesPlayed++;
        }

        if (holeRating.par == 3) {
          par3s++;
          par3Score += holeScore.score;
        } else if (holeRating.par == 4) {
          par4s++;
          par4Score += holeScore.score;
        } else if (holeRating.par == 5) {
          par5s++;
          par5Score += holeScore.score;
        }

        if (holeRating.par > 3) {
          fairwayOpportunities++;

          if (holeRating.number < 10) {
            frontFairwayOpportunities++;
          } else {
            backFairwayOpportunities++;
          }

          if (holeScore.fairwayHit) {
            fairwaysHit++;

            if (holeRating.number < 10) {
              frontFairwaysHit++;
            } else {
              backFairwaysHit++;
            }
          }
        }

        if (holeScore.gir) {
          girs++;

          if (holeRating.number < 10) {
            frontGirs++;
          } else {
            backGirs++;
          }
        }

        score += holeScore.score;
        scoreToPar += (holeScore.score - holeRating.par);
        putts += holeScore.putts;
        penalties += holeScore.penaltyStrokes;

        if (holeScore.hole.number < 10) {
          frontScore += holeScore.score;
          frontPutts += holeScore.putts;
          frontPenalties += holeScore.penaltyStrokes;
        } else {
          backScore += holeScore.score;
          backPutts += holeScore.putts;
          backPenalties += holeScore.penaltyStrokes;
        }
      }
    }

    this.scoreToPar = scoreToPar;
    this.score = score;
    this.frontScore = frontScore;
    this.backScore = backScore;
    this.putts = putts;
    this.frontPutts = frontPutts;
    this.backPutts = backPutts;
    this.penalties = penalties;
    this.frontPenalties = frontPenalties;
    this.backPenalties = backPenalties;

    this.puttAverage = holesPlayed > 0 ? (double) putts / holesPlayed : 0;
    this.fairwayHitPercentage = fairwayOpportunities > 0 ? (double) fairwaysHit / fairwayOpportunities : 0;
    this.frontFairwayHitPercentage = frontFairwayOpportunities > 0 ? (double) frontFairwaysHit / frontFairwayOpportunities : 0;
    this.backFairwayHitPercentage = backFairwayOpportunities > 0 ? (double) backFairwaysHit / backFairwayOpportunities : 0;
    this.girPercentage = holesPlayed > 0 ? (double) girs / holesPlayed : 0;
    this.frontGirPercentage = frontHolesPlayed > 0 ? (double) frontGirs / frontHolesPlayed : 0;
    this.backGirPercentage = backHolesPlayed > 0 ? (double) backGirs / backHolesPlayed : 0;

    this.par3ScoringAverage = par3s > 0 ? (double) par3Score / par3s : 0;
    this.par4ScoringAverage = par4s > 0 ? (double) par4Score / par4s : 0;
    this.par5ScoringAverage = par5s > 0 ? (double) par5Score / par5s : 0;

  }

  public String scoreToParString() {
    if (scoreToPar == 0) {
      return "E";
    } else if (scoreToPar > 0) {
      return String.format("+%d", scoreToPar);
    } else {
      return String.valueOf(scoreToPar);
    }
  }

}
