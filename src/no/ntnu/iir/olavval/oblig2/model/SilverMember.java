package no.ntnu.iir.olavval.oblig2.model;

import java.time.LocalDate;

/**
 * Represents a Silver level Bonus member.
 * @author mort
 */
public class SilverMember extends no.ntnu.iir.olavval.oblig2.model.BonusMember {

  static final double FACTOR_SILVER = 1.2;

  /**
   * Silver level member constructor.
   *
   * @param memberNo     the member number.
   * @param personals    the personal details of the member.
   * @param enrolledDate date member was first enrolled.
   * @param points       current points held by member.
   */
  public SilverMember(int memberNo, Personals personals, LocalDate enrolledDate, int points) {
    super(memberNo, personals, enrolledDate);

    // Transfers existing points from old member to the new member.
    // Direct call to super.registerPoints avoids factoring by FACTOR_SILVER
    super.point = points;

  }


  @Override
  public String getMembershipLevel() {
    return "Silver";
  }

  /**
   * Adds points to this Silver level Bonus member, factoring in the Silver level bonus multiplier.
   *
   * @param points Number of points to be added.
   */
  @Override
  public void registerPoints(int points) {
    super.point += ((int) (FACTOR_SILVER * points));
  }
}
