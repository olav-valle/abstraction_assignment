package no.ntnu.iir.olavval.oblig2;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class MemberArchive {
    public static final int SILVER_LIMIT = 25000;
    public static final int GOLD_LIMIT = 75000;
    private static final Random RANDOM_NUMBER = new Random();

    private HashMap<Integer, BonusMember> members;
    //TODO should this be a HashMap<int memberNo, BonusMember member>?
    // The memberNo's should all be unique, and upgrading a member simply means storing the current memberNo,
    // and reusing it as the key for the upgraded member object.

    /**
     * Member archive registry.
     */
    public MemberArchive()
    {
        members = new HashMap<>();
    }

    /**
     * Imitates a member logging in to check their point balance.
     * Requires a valid user number and password.
     * @param memberNo The users membership number.
     * @param passwd The users password.
     * @return Number of points held by the member, or -1 if password or memberNo was invalid.
     */
    public int findPoints(int memberNo, String passwd)
    {
        return members.get(memberNo).getPoints();
        //TODO add tests
        // + return right amount when given valid memberNo && passwd
        // - return -1 for invalid memberNo || passwd || (memberNo && passwd)
    }

    /**
     * Adds the specified number of points to a member.
     * Any bonus multipliers are factored automatically.
     * @param memberNo the membership number of the member.
     * @param points the number of points to be added to the member.
     * @return True if points were succesfully added to member account, false if memberNo was invalid.
     */
    public boolean registerPoints(int memberNo, int points)
    {
        boolean success = false;

        //sanity check: does member exist in collection, or is points value negative?
        if(members.get(memberNo) == null || points < 0) return false;

        //TODO: 03/02/2020 Either add a removePoints method, or allow negative point values for when
        // subtracting points from a balance becomes necessary.

        else{ // member exists and value is positive
                members.get(memberNo).registerPoints(points); //calls registerPoints method of member object
            success = true;
        }

        return success;


        //TODO add tests
        // + return true for valid memberNo
        // + amount increases correctly
        // - false for invalid memberNo
        // - false for negative points?

    }

    /**
     * Finds the member with the specified member number.
     * @param memberNo the member number of the desired member
     * @return the member with specified member number, or null if no member with that number exists.
     */
    public BonusMember findMember(int memberNo)
    {
        return members.get(memberNo);

    }

    /**
     * Creates a new member account using the personal details and date provided.
     * Returns the unique membership number generated for the member.
     * @param person The personal details of the member.
     * @param dateEnrolled The date of first enrollment into the program.
     * @return the unique membership number of the member, or -1 if creation failed.
     */
    public int addMember(Personals person, LocalDate dateEnrolled)
    {
        BonusMember newMember = null;

        if(person != null && dateEnrolled != null) { // requires person and date to be non-null.

            newMember = new BasicMember(findAvailableNo(), person, dateEnrolled); // inst. new BasicMember object

            putMember(newMember); //put member object into collection.
        }//if

        int newMemberNo = -1; // we assume that member creation failed
        if (newMember != null) newMemberNo = newMember.getMemberNo(); // if a new member was instantiated
        return newMemberNo; // newMember was still null, ergo BasicMember inst. failed and we return -1.
        // TODO: 03/02/2020 how to notify caller of failed process, i.e. null parameters or otherwise invalid data?
        //  return -1?
        //TODO add tests:
        // + possible failure if
    }

    /**
     * Puts a member into the members collection, using its member number as key.
     * Do NOT use this method to replace a member already in the archive,
     * e.g. during membership upgrade. This will fail if the membership number is the same
     * for both the old and the upgraded member objects (which they should be, even after an upgrade)
     *
     * see replaceUpgradedMember for interactions with members already in collection.
     * @param newMember the member to add to the list.
     */
    protected void putMember(BonusMember newMember)
    {
        // TODO: 03/02/2020 check if collection already has this specific key-value pair.
        if(newMember != null && !(members.containsKey(newMember.getMemberNo()))) {
            members.put( // add newly created member to collection
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
     * that is to be replaced. It will automatically fetch
     *
     * See putMember to add a new member.
     *
     * @param replacementMember the member object that will replace the old one.
     */
    protected void replaceUpgradedMember(BonusMember replacementMember)
    {
        if(replacementMember != null // non-null check
            && members.containsKey( // check that this member is currently present in some form
                    replacementMember.getMemberNo()))
        {
            members.replace(
                    replacementMember.getMemberNo(), //memberNo of replacement should be same as original
                    replacementMember);
        }
    }

    /**
     * Checks member registry for members that are eligible for upgrade to silver or gold level.
     * @param testDate Date to test member enrollment date with.
     */
    public void checkMembers(LocalDate testDate)
    {
        for(BonusMember member : members.values()) {

            // member cannot be upgraded past gold, so we check for that first.
            if (!(member instanceof GoldMember)) {

                // check for gold level qualification
                if (checkGoldQualification(member, testDate)) {
                    members.replace( //replaces value of original member object with upgraded member object
                            member.getMemberNo(), //keeps the same member number as original member object
                            upgradeMemberToGold(member)); // updates member to gold level
                }// gold upgrade

                //check for silver level qualification
                else if (checkSilverQualification(member, testDate)) {
                    members.replace( //replaces value of original member object with upgraded member object
                            member.getMemberNo(), //keeps the same member number as original member object
                            upgradeMemberToSilver(member)); // updates member to silver level
                }// silver upgrade

            }// if !gold member
        }// for each
        //TODO implement instanceof check for basic, silver and gold members,
        // and call methods accordingly.
    }

    /**
     * Generates a unique membership number.
     * @return a unique membership number.
     */
    private int findAvailableNo()
    {
        int newNo = RANDOM_NUMBER.nextInt();
        if(!members.containsKey(newNo)) { //check if newNo is used as memberNo already
            return newNo; //newNo was unused
        }
        else return findAvailableNo(); //newNo was already used, run trying again.
        // TODO: 03/02/2020 Is this dangerously recursive?

    }

    //TODO Is this a good way to handle this upgrading members?
    // Perhaps checks of point limits should be performed before sending member to be upgraded.

    /**
     * Checks if a member is eligible for upgrade to silver level.
     * @param member the member to be checked.
     * @param testDate the date to test the members enrollment date with.
     * @return true if member is eligible for silver level membership.
     */
    private boolean checkSilverQualification(BonusMember member, LocalDate testDate)
    {
        return (member.findQualificationPoints(testDate) >= SILVER_LIMIT);
        // should return true if member has enough valid points to be upgraded to silver.
        //TODO: 03/02/2020 add test:
        // + return true for member with >= 25000 points
        // - return false for null parameters, and members with < 25000 points

    }
    /**
     * Checks if a member is eligible for upgrade to gold level.
     * @param member the member to be checked.
     * @param testDate the date to test the members enrollment date with.
     * @return true if member is eligible for gold level membership.
     */
    private boolean checkGoldQualification(BonusMember member, LocalDate testDate) {
        return (member.findQualificationPoints(testDate) >= GOLD_LIMIT);
        // should return true if member has enough valid points to be upgraded to gold.
        //TODO: 03/02/2020 add test:
        // + return true for member with >= 75000 points
        // - return false for null parameters, and members with < 75000 points
    }
    /**
     * Checks if a member is eligible for upgrade to silver level.
     * @param memberNo the number of the member to be checked.
     * @param testDate the date to test the members enrollment date with.
     * @return New silver level member object if member was upgradeable,
     * or the unchanged member if it was not.
     */
    private BonusMember checkSilverQualification(int memberNo, LocalDate testDate)
    {
        return null;
        //TODO Basically just check if member.findQualificationPoints returns more than SILVER_LIMIT?
    }

    /**
     * Checks if a member is eligible for upgrade to gold level.
     * @param memberNo the number of the member to be checked.
     * @param testDate the date to test the members enrollment date with.
     * @return New gold level member object if member was upgradeable,
     * or the unchanged member if it was not.
     */
    private BonusMember checkGoldQualification(int memberNo, LocalDate testDate)
    {
        return null;
        //TODO Basically just check if member.findQualificationPoints returns more than GOLD_LIMIT?

    }

    /**
     * Upgrades the parameter member to silver member status.
     * @param currentMember The member who is to be upgraded to silver level.
     * @return the newly upgraded silver level member.
     */
    private BonusMember upgradeMemberToSilver(BonusMember currentMember){

        return new SilverMember( //recycles details from old BasicMember object.
                currentMember.getMemberNo(),
                currentMember.getPersonals(),
                currentMember.getEnrolledDate(),
                currentMember.getPoints());
    }

    /**
     * Upgrades the parameter member to gold member status.
     * @param currentMember The member who is to be upgraded to gold level.
     * @return the newly upgraded gold level member.
     */
    private BonusMember upgradeMemberToGold(BonusMember currentMember){

        return new GoldMember( //recycles details from old SilverMember or BasicMember object.
                currentMember.getMemberNo(),
                currentMember.getPersonals(),
                currentMember.getEnrolledDate(),
                currentMember.getPoints());
    }
    /**
     * Main method of BonusMember program app.
     * @param args
     */
    public static void main(String[] args)
    {

    }
}
