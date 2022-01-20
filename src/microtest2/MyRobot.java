package microtest2;

import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public abstract class MyRobot {

    static final Direction[] directions = {
            Direction.NORTH,
            Direction.NORTHEAST,
            Direction.EAST,
            Direction.SOUTHEAST,
            Direction.SOUTH,
            Direction.SOUTHWEST,
            Direction.WEST,
            Direction.NORTHWEST,
            Direction.CENTER
    };

    RobotController rc;
    BFS bfs;
    Exploration explore;
    Communication comm;


    public MyRobot(RobotController rc){
        this.rc = rc;
        comm = new Communication(rc);
        explore = new Exploration(rc);
        bfs = new BFSDroid(rc, explore); //if need to move units with visions other than 20, switch here
    }

    abstract void play();

    void initTurn(){
        comm.init();
        bfs.initTurn();
        explore.initTurn();
        //comm.readMessages();
        //comm.debugDraw();
    }

    void endTurn(){
        //comm.run();
        explore.initialize();
        explore.markSeen();
    }

    /*boolean surroundEnemyHQ(){
        MapLocation loc = comm.getClosestEnemyEC();
        return moveSafely(loc, Util.SAFETY_DISTANCE_ENEMY_EC);
    }*/

    /*boolean surroundOurHQ(int rad){
        MapLocation loc = comm.getClosestEC();
        return moveSafely(loc, rad);
    }*/

    boolean moveSafely(MapLocation loc, int rad){
        if (loc == null) return false;
        int d = rc.getLocation().distanceSquaredTo(loc);
        d = Math.min(d, rad);
        boolean[] imp = new boolean[directions.length];
        boolean greedy = false;
        for (int i = directions.length; i-- > 0; ){
            MapLocation newLoc = rc.getLocation().add(directions[i]);
            if (newLoc.distanceSquaredTo(loc) <= d){
                imp[i] = true;
                greedy = true;
            }
        }
        bfs.path.setImpassable(imp);
        bfs.move(loc, greedy);
        return true;
    }


}