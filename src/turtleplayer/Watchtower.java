package turtleplayer;

import battlecode.common.*;

import java.awt.*;

public class Watchtower extends MyRobot {

    Team myTeam, enemyTeam;
    int H, W;
    int task = 0;

    Watchtower(RobotController rc) {
        super(rc);
        myTeam = rc.getTeam();
        enemyTeam = myTeam.opponent();
        H = rc.getMapHeight();
        W = rc.getMapWidth();
        comm.readHQloc();
    }

    void play() {

    }

}

