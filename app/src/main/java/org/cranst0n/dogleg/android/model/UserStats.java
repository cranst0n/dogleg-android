package org.cranst0n.dogleg.android.model;

import java.util.List;

public class UserStats {

  public final User user;
  public final List<CourseSummary> frequentCourses;
  public final int autoHandicap;
  public final int totalRounds;
  public final int lowGross9Hole;
  public final int lowGross18Hole;
  public final double averageGross18Hole;
  public final int lowNet9Hole;
  public final int lowNet18Hole;
  public final double averageNet18Hole;
  public final double fairwayHitPercentage;
  public final double girPercentage;
  public final double grossScrambling;
  public final double netScrambling;
  public final int grossAces;
  public final int grossBirdieStreak;
  public final int grossParStreak;
  public final int netAces;
  public final int netBirdieStreak;
  public final int netParStreak;
  public final int fewestPutts18Hole;
  public final int grossMostBirdies18Hole;
  public final int grossMostPars18Hole;
  public final int netMostBirdies18Hole;
  public final int netMostPars18Hole;
  public final double averagePuttPerHole;
  public final double averagePenaltiesPerRound;
  public final double grossAverageEaglesPerRound;
  public final int grossEagles;
  public final double grossAverageBirdiesPerRound;
  public final int grossBirdies;
  public final double grossAverageParsPerRound;
  public final int grossPars;
  public final double netAverageEaglesPerRound;
  public final int netEagles;
  public final double netAverageBirdiesPerRound;
  public final int netBirdies;
  public final double netAverageParsPerRound;
  public final int netPars;
  public final double grossPar3Average;
  public final double grossPar4Average;
  public final double grossPar5Average;
  public final double netPar3Average;
  public final double netPar4Average;
  public final double netPar5Average;

  public UserStats(final User user,
                   final List<CourseSummary> frequentCourses,
                   final int autoHandicap,
                   final int totalRounds,
                   final int lowGross9Hole,
                   final int lowGross18Hole,
                   final double averageGross18Hole,
                   final int lowNet9Hole,
                   final int lowNet18Hole,
                   final double averageNet18Hole,
                   final double fairwayHitPercentage,
                   final double girPercentage,
                   final double grossScrambling,
                   final double netScrambling,
                   final int grossAces,
                   final int grossBirdieStreak,
                   final int grossParStreak,
                   final int netAces,
                   final int netBirdieStreak,
                   final int netParStreak,
                   final int fewestPutts18Hole,
                   final int grossMostBirdies18Hole,
                   final int grossMostPars18Hole,
                   final int netMostBirdies18Hole,
                   final int netMostPars18Hole,
                   final double averagePuttPerHole,
                   final double averagePenaltiesPerRound,
                   final double grossAverageEaglesPerRound,
                   final int grossEagles,
                   final double grossAverageBirdiesPerRound,
                   final int grossBirdies,
                   final double grossAverageParsPerRound,
                   final int grossPars,
                   final double netAverageEaglesPerRound,
                   final int netEagles,
                   final double netAverageBirdiesPerRound,
                   final int netBirdies,
                   final double netAverageParsPerRound,
                   final int netPars,
                   final double grossPar3Average,
                   final double grossPar4Average,
                   final double grossPar5Average,
                   final double netPar3Average,
                   final double netPar4Average,
                   final double netPar5Average) {

    this.user = user;
    this.frequentCourses = frequentCourses;
    this.autoHandicap = autoHandicap;
    this.totalRounds = totalRounds;
    this.lowGross9Hole = lowGross9Hole;
    this.lowGross18Hole = lowGross18Hole;
    this.averageGross18Hole = averageGross18Hole;
    this.lowNet9Hole = lowNet9Hole;
    this.lowNet18Hole = lowNet18Hole;
    this.averageNet18Hole = averageNet18Hole;
    this.fairwayHitPercentage = fairwayHitPercentage;
    this.girPercentage = girPercentage;
    this.grossScrambling = grossScrambling;
    this.netScrambling = netScrambling;
    this.grossAces = grossAces;
    this.grossBirdieStreak = grossBirdieStreak;
    this.grossParStreak = grossParStreak;
    this.netAces = netAces;
    this.netBirdieStreak = netBirdieStreak;
    this.netParStreak = netParStreak;
    this.fewestPutts18Hole = fewestPutts18Hole;
    this.grossMostBirdies18Hole = grossMostBirdies18Hole;
    this.grossMostPars18Hole = grossMostPars18Hole;
    this.netMostBirdies18Hole = netMostBirdies18Hole;
    this.netMostPars18Hole = netMostPars18Hole;
    this.averagePuttPerHole = averagePuttPerHole;
    this.averagePenaltiesPerRound = averagePenaltiesPerRound;
    this.grossAverageEaglesPerRound = grossAverageEaglesPerRound;
    this.grossEagles = grossEagles;
    this.grossAverageBirdiesPerRound = grossAverageBirdiesPerRound;
    this.grossBirdies = grossBirdies;
    this.grossAverageParsPerRound = grossAverageParsPerRound;
    this.grossPars = grossPars;
    this.netAverageEaglesPerRound = netAverageEaglesPerRound;
    this.netEagles = netEagles;
    this.netAverageBirdiesPerRound = netAverageBirdiesPerRound;
    this.netBirdies = netBirdies;
    this.netAverageParsPerRound = netAverageParsPerRound;
    this.netPars = netPars;
    this.grossPar3Average = grossPar3Average;
    this.grossPar4Average = grossPar4Average;
    this.grossPar5Average = grossPar5Average;
    this.netPar3Average = netPar3Average;
    this.netPar4Average = netPar4Average;
    this.netPar5Average = netPar5Average;
  }
}
