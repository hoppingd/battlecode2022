package newplayer;

import battlecode.common.Clock;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.Team;

public class Laboratory extends MyRobot {

    static final int P4_SAVINGS = 1000;

    Team myTeam, enemyTeam;

    Laboratory(RobotController rc){
        super(rc);
        myTeam = rc.getTeam();
        enemyTeam = myTeam.opponent();
    }

    void play(){
        transmute();

    }

    boolean transmute() {
        int currLead = rc.getTeamLeadAmount(myTeam);
        // need to win
        if (rc.getTeamGoldAmount(myTeam) <= rc.getTeamGoldAmount(enemyTeam) && currLead > P4_SAVINGS) {
            try {
                if (rc.canTransmute()) {
                    rc.transmute();
                    return true;
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        // want more gold
        return false;
    }
}

