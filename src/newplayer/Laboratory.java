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
        if (rc.getTeamGoldAmount(myTeam) <= rc.getTeamGoldAmount(enemyTeam) && rc.getTeamLeadAmount(myTeam) > P4_SAVINGS) {
            try{
                if (rc.canTransmute()) {
                    rc.transmute();
                }
            } catch (Throwable e){
                e.printStackTrace();
            }
        }
    }

}

