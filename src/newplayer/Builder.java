package newplayer;

import battlecode.common.*;

public class Builder extends MyRobot {

    //easiest way is each miner simply builds a set number of watchtowers... right now ill just do one
    int watchCount = 0;
    Team myTeam, enemyTeam;
    RobotController rc;

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

        tryBuild();
        //TODO tryRepair(); soon!
    }

    void tryBuild()
    {
        for (Direction dir : Direction.allDirections())
        {
            if(rc.getTeamLeadAmount(myTeam) > RobotType.WATCHTOWER.buildCostLead) {
                //just trying building in one of these four directions I figure atleast one will work
                try {
                    if (rc.canBuildRobot(RobotType.WATCHTOWER, Direction.NORTH) && watchCount < 1) {
                        watchCount++;
                        rc.buildRobot(RobotType.WATCHTOWER, Direction.NORTH);
                        break;
                    } else if (rc.canBuildRobot(RobotType.WATCHTOWER, Direction.SOUTH) && watchCount < 1) {
                        watchCount++;
                        rc.buildRobot(RobotType.WATCHTOWER, Direction.SOUTH);
                        break;
                    } else if (rc.canBuildRobot(RobotType.WATCHTOWER, Direction.EAST) && watchCount < 1) {
                        watchCount++;
                        rc.buildRobot(RobotType.WATCHTOWER, Direction.EAST);
                        break;
                    } else {
                        if (rc.canBuildRobot(RobotType.WATCHTOWER, Direction.WEST) && watchCount < 1) {
                            watchCount++;
                            rc.buildRobot(RobotType.WATCHTOWER, Direction.WEST);
                            break;
                        }
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        }
    }


}

