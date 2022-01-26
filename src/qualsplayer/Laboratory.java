package qualsplayer;

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
        try {
            if (rc.canTransmute() && (currLead - rc.getTransmutationRate() >= RobotType.MINER.buildCostLead || comm.getSpawnCount() % 6 == 0)) {
                rc.transmute();
                return true;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }
}

