package advancedplayer;

import battlecode.common.Clock;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.Team;

public class Laboratory extends MyRobot {

    static final int OBSCENELY_RICH = 2000;
    Team myTeam, enemyTeam;
    int task = 0;

    Laboratory(RobotController rc){
        super(rc);
        myTeam = rc.getTeam();
        enemyTeam = myTeam.opponent();
    }

    void play(){
        task = comm.getTask();
        transmute();
    }

    boolean transmute() {
        int currLead = rc.getTeamLeadAmount(myTeam);
        // need to win
        if (currLead > comm.P4_SAVINGS) {
            try {
                if (rc.canTransmute()) {
                    rc.transmute();
                    return true;
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}

