package advancedplayer;

import battlecode.common.*;

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
        if (currLead - rc.getTransmutationRate() > RobotType.WATCHTOWER.buildCostLead + RobotType.BUILDER.buildCostLead || (rc.getRoundNum() > 1900 && rc.getTeamGoldAmount(myTeam) <= rc.getTeamGoldAmount(enemyTeam))) { // <- try to win on gold
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

