package turtleplayer;

import battlecode.common.*;

public abstract class BFS {

    final int GREEDY_TURNS = 4;

    Pathfinding path;
    Exploration explore;
    static RobotController rc;
    MapTracker mapTracker = new MapTracker();

    int turnsGreedy = 0;

    MapLocation currentTarget = null;




    BFS(RobotController rc, Exploration explore){
        this.rc = rc;
        this.explore = explore;
        this.path = new Pathfinding(rc, explore);
    }

    void reset(){
        turnsGreedy = 0;
        mapTracker.reset();
    }

    void update(MapLocation target){
        if (currentTarget == null || target.distanceSquaredTo(currentTarget) > 0){
            reset();
        } else --turnsGreedy;
        currentTarget = target;
        mapTracker.add(rc.getLocation());
    }

    void activateGreedy(){
        turnsGreedy = GREEDY_TURNS;
    }

    void initTurn(){
        path.initTurn();
    }

    void move(MapLocation target){
        move(target, false);
    }

    void move(MapLocation target, boolean greedy){
        if (target == null) return;
        if (rc.getMovementCooldownTurns() >= 1) return;
        if (rc.getLocation().distanceSquaredTo(target) == 0) return;

        update(target);

        if (!greedy && turnsGreedy <= 0){

            //System.err.println("Using bfs");
            Direction dir = getBestDir(target);
            if (dir != null && !mapTracker.check(rc.getLocation().add(dir))){
                try{
                    if (!rc.canMove(dir)) return;
                    rc.move(dir);
                } catch (Exception e){
                    e.printStackTrace();
                }
                // can we get stuck here?
                return;
            } else activateGreedy();
        }

        if (Clock.getBytecodesLeft() >= 2500) {
            //System.err.println("Using greedy");
            path.move(target);
            --turnsGreedy;
        }
    }


    abstract Direction getBestDir(MapLocation target);


}
