package no.ntnu.iir.olavval.oblig2;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

// TODO: 09/02/2020 Refactor upgrade related methods to new UpgradeMembers class?
// TODO: 09/02/2020 Refactor member creation to new class?

public class MemberArchive {
  public static final int SILVER_LIMIT = 25000;
  public static final int GOLD_LIMIT = 75000;
  private static final Random RANDOM_NUMBER = new Random();
  private HashMap<Integer, BonusMember> members;

  /**
   * Member archive registry.
   */
  public MemberArchive() {
    members = new HashMap<>();
  }

  /**
   * Returns the number of elements in the archive.
   *
   * @return the number of elements in the archive.
   */
  public int getArchiveSize() {
    return members.size();
  }

  /**
   * Returns an iterator of all member objects in archive.
   * @return Iterator of all members in archive.
   */
  public Iterator<BonusMember> getArchiveIterator() {
    return members.values().iterator();
  }

  /**
   * Imitates a member logging in to check their point balance.
   * Requires a valid user number and password.
   *
   * @param memberNo The users membership number.
   * @param passwd   The users password.
   * @return Number of points held by the member, or -1 if password or memberNo was invalid.
   */
  public int findPoints(int memberNo, String passwd) {
    BonusMember member = members.get(memberNo);
    return member.okPassword(passwd) ? member.getPoints() : -1;

  }

  /**
   * Adds the specified number of points to a member.
   * Any bonus multipliers are factored automatically.
   *
   * @param memberNo the membership number of the member.
   * @param points   the number of points to be added to the member.
   * @return True if points were successfully added to member account,
   *        false if memberNo was invalid.
   */
  public boolean registerPoints(int memberNo, int points) {

    //sanity check: does member exist in collection, or is points a negative number?
    if (members.get(memberNo) == null || points < 0) {
      return false;
    } else { // member exists and value is positive
      members.get(memberNo).registerPoints(points); //calls registerPoints method of member object
    }
    return true;
  }

  /**
   * Finds the member with the specified member number.
   *
   * @param memberNo the member number of the desired member
   * @return the member with specified member number, or null if no member with that number exists.
   */
  public BonusMember findMember(int memberNo) {
    return members.get(memberNo);

  }

  /**
   * Creates a new member account using the personal details and date provided.
   * Returns the unique membership number generated for the member.
   *
   * @param person       The personal details of the member.
   * @param dateEnrolled The date of first enrollment into the program.
   * @return the unique membership number of the member, or -1 if creation failed.
   */
  public int addMember(Personals person, LocalDate dateEnrolled) {

    int newMemberNo = -1; // we assume that member creation failed

    if (person != null && dateEnrolled != null) { // requires person and date to be non-null.
      // fetch a member number
      newMemberNo = findAvailableNo();

      // inst. new BasicMember object
      BonusMember newMember = new BasicMember(newMemberNo, person, dateEnrolled);

      //put member object into collection.
      putMember(newMember);

    } //if

    // returns the member number of the new member, or -1 if creation failed.
    return newMemberNo;
  }

  /**
   * Puts a member into the members collection, using its member number as key.
   * Do NOT use this method to replace a member already in the archive,
   * e.g. during membership upgrade. This will fail if the membership number is the same
   * for both the old and the upgraded member objects (which they should be, even after an upgrade)
   *
   * <p>see replaceUpgradedMember for interactions with members already in collection.</p>
   *
   * @param newMember the member to add to the list.
   */
  protected void putMember(BonusMember newMember) {
    if (newMember != null && !(members.containsKey(newMember.getMemberNo()))) {
      members.put(// add newly created member to collection
          newMember.getMemberNo(), // member number is key
          newMember); // member object is value
    }
  }

  /**
   * Replaces a member already in collection, with the parameter member object.
   * Do NOT use this method when adding a new member to the archive, e.g. during
   * membership creation. Should ONLY be used when upgrading a member, as it requires
   * that an object with the same member number key is already present in the collection.
   * Method does not allow caller to directly specify the member number of the member
   * that is to be replaced. It will automatically fetch and reuse the member number
   * from the old member.
   *
   * <p>See putMember to add a new member.</p>
   *
   * @param oldMember         the current member object, that will be replaced.
   * @param replacementMember the new member object that will replace the old one.
   */
  protected void replaceUpgradedMember(BonusMember oldMember, BonusMember replacementMember) {
    if (replacementMember != null     // confirm that this key-value pair is NOT new
        && (oldMember.getMemberNo() == replacementMember.getMemberNo())
        && members.containsKey(replacementMember.getMemberNo())) {
      members.replace(
          oldMember.getMemberNo(), //memberNo of replacement and old are the same
          replacementMember);
    }
  }

  /**
   * Checks member registry for members that are eligible for upgrade to silver or gold level.
   *
   * @param testDate Date to test member enrollment date with.
   */
  public void checkAndUpgradeMembers(LocalDate testDate) {
    if (testDate == null) { // sanity check
      return;
    }

    for (BonusMember member : members.values()) {
      //iterates over all values objects (i.e. members) in collection

      // member cannot be upgraded past gold, so we check for that first.
      if (!(member instanceof GoldMember)) { // if not already gold

        // check for gold level qualification first,
        // since gold level eligibility overrides silver level.
        // this saves us checking for, and then upgrading to, silver only to
        // realise later that the member should have been upgraded straight to gold.

        if (checkGoldQualification(member, testDate)) { //if qualified for gold
          replaceUpgradedMember(member, upgradeMemberToGold(member));
          // replace member in HashMap with upgraded member.
        } else if (checkSilverQualification(member, testDate)) {
          //check for silver level qualification
          replaceUpgradedMember(member, upgradeMemberToSilver(member));
        }

      } //if !gold member

    } //for each

  }

  /**
   * Generates a unique membership number.
   *
   * @return a unique membership number.
   */
  private int findAvailableNo() {
    boolean unique = false;
    int newNo = -1;
    while (!unique) {
      newNo = RANDOM_NUMBER.nextInt((1000000));

      if (!members.containsKey(newNo)) {
        unique = true;

      }
    }

    return newNo;
  }

  /**
   * Checks if a member is eligible for upgrade to silver level.
   *
   * @param member   the member to be checked.
   * @param testDate the date to test the members enrollment date with.
   * @return true if member is eligible for silver level membership.
   */
  private boolean checkSilverQualification(BonusMember member, LocalDate testDate) {
    return (member.findQualificationPoints(testDate) >= SILVER_LIMIT);
    // should return true if member has enough valid points to be upgraded to silver.
  }

  /**
   * Checks if a member is eligible for upgrade to gold level.
   *
   * @param member   the member to be checked.
   * @param testDate the date to test the members enrollment date with.
   * @return true if member is eligible for gold level membership.
   */
  private boolean checkGoldQualification(BonusMember member, LocalDate testDate) {
    return (member.findQualificationPoints(testDate) >= GOLD_LIMIT);
    // should return true if member has enough valid points to be upgraded to gold.
  }

  /**
   * Upgrades the parameter member to silver member status.
   *
   * @param currentMember The member who is to be upgraded to silver level.
   * @return the newly upgraded silver level member.
   */
  private BonusMember upgradeMemberToSilver(BonusMember currentMember) {

    return new SilverMember(//recycles details from old BasicMember object.
        currentMember.getMemberNo(),
        currentMember.getPersonals(),
        currentMember.getEnrolledDate(),
        currentMember.getPoints());
  }

  /**
   * Upgrades the parameter member to gold member status.
   *
   * @param currentMember The member who is to be upgraded to gold level.
   * @return the newly upgraded gold level member.
   */
  private BonusMember upgradeMemberToGold(BonusMember currentMember) {

    return new GoldMember(//recycles details from old SilverMember or BasicMember object.
        currentMember.getMemberNo(),
        currentMember.getPersonals(),
        currentMember.getEnrolledDate(),
        currentMember.getPoints());
  }
}
