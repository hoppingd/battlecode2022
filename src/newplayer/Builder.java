package newplayer;

import battlecode.common.*;

public class Builder extends MyRobot {

    //easiest way is each miner simply builds a set number of watchtowers... right now ill just do one
    int watchCount = 0;
    Team myTeam, enemyTeam;
    RobotController rc;
    boolean moved = false;

    public Builder(RobotController rc){
        super(rc);
        myTeam = rc.getTeam();
        enemyTeam = myTeam.opponent();
    }



    void play()
    {
        //just need to build a watchtower lol I guess for now we will just set it north
        //eventually we should do most towards the center of the map
        //how to scout for that tho idk
        moved = false;
        tryBuild();
        tryMove();
        //TODO tryRepair(); soon!
    }

    void tryMove()
    {
        //rc.setIndicatorDot(loc, 0, 0, 255);

    }

    void tryBuild()
    {
        /*
            if(rc.getTeamLeadAmount(myTeam) >= RobotType.WATCHTOWER.buildCostLead && watchCount < 1) {
                //just trying building in one of these four directions I figure atleast one will work
                try {
                    if (rc.canBuildRobot(RobotType.WATCHTOWER, Direction.NORTH) && watchCount < 1) {
                        watchCount++;
                        rc.buildRobot(RobotType.WATCHTOWER, Direction.NORTH);
                    } else if (rc.canBuildRobot(RobotType.WATCHTOWER, Direction.SOUTH) && watchCount < 1) {
                        watchCount++;
                        rc.buildRobot(RobotType.WATCHTOWER, Direction.SOUTH);
                    } else if (rc.canBuildRobot(RobotType.WATCHTOWER, Direction.EAST) && watchCount < 1) {
                        watchCount++;
                        rc.buildRobot(RobotType.WATCHTOWER, Direction.EAST);
                    } else {
                        if (rc.canBuildRobot(RobotType.WATCHTOWER, Direction.WEST) && watchCount < 1) {
                            watchCount++;
                            rc.buildRobot(RobotType.WATCHTOWER, Direction.WEST);
                        }
                    }
                    watchCount++;
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }*/

    }


}

