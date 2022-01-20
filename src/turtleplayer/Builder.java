package turtleplayer;

import battlecode.common.*;

import java.awt.*;

public class Builder extends MyRobot {
    static final Direction[] spawnDirections = {
            Direction.NORTH,
            Direction.NORTHEAST,
            Direction.EAST,
            Direction.SOUTHEAST,
            Direction.SOUTH,
            Direction.SOUTHWEST,
            Direction.WEST,
            Direction.NORTHWEST,
    };

    int H, W;
    Team myTeam, enemyTeam;
    int birthday;
    int task = 0;
    double mapLeadScore;
    boolean HIGH_LEAD_MAP = false;
    boolean LOW_LEAD_MAP = false;

    boolean moved = false;
    int currRound = 0;

    public Builder(RobotController rc){
        super(rc);
        H = rc.getMapHeight();
        W = rc.getMapWidth();
        myTeam = rc.getTeam();
        enemyTeam = myTeam.opponent();
        birthday = rc.getRoundNum();
        comm.readHQloc();
        comm.readLeadScore(); // we also get lead score to determine how much we build before moving, if we should make mines, build towers, etc
        mapLeadScore = (comm.leadScore / (double)comm.numArchons) * (400.0/(H*W));
        HIGH_LEAD_MAP = mapLeadScore > comm.HIGH_LEAD_THRESHOLD;
        LOW_LEAD_MAP = mapLeadScore < comm.LOW_LEAD_THRESHOLD;
    }

    public void play() {
        currRound = rc.getRoundNum();
        task = comm.getTask();
        moved = false;
    }

}