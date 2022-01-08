package newplayer;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class Exploration {

    RobotController rc;
    static Direction[] dirPath;
    Direction lastDirMoved = null;
    MapLocation exploreTarget = null;

    int H, W;
    boolean[][] visited;

    int initialX, initialY;
    int initRow = 0;
    int visionRadius;

    final int initBytecodeLeft = 300;
    final int visitedBytecodeLeft = 100;

    boolean initialized = false;

    Exploration(RobotController rc) {
        this.rc = rc;
        visionRadius = rc.getType().visionRadiusSquared;
        fillDirPath();
        H = rc.getMapHeight();
        W = rc.getMapWidth();
        visited = new boolean[W][];
        initialX = rc.getLocation().x;
        initialY = rc.getLocation().y;
    }

    void initTurn() {

    }

    void initialize() {
        if (initialized){
            return;
        }
        while(initRow < W){
            if (Clock.getBytecodesLeft() < initBytecodeLeft) return;
            visited[initRow] = new boolean[H];
            initRow++;
        }
        initialized = true;
    }

    MapLocation getExploreTarget(){
        if (exploreTarget != null && !hasVisited(exploreTarget)) return exploreTarget;
        int tries = 10;
        for (int i = tries; i-- > 0; ){
            int x = (int)(Math.random()*W);
            int y = (int)(Math.random()*H);
            exploreTarget = new MapLocation(x,y);
            if (!hasVisited(exploreTarget)) return exploreTarget;
        }
        return exploreTarget;
    }

    boolean hasVisited (MapLocation loc){
        if (!initialized) return false;
        return visited[loc.x][loc.y];
    }

    void markSeen(){
        if (!initialized) return;
        try{
            MapLocation loc = rc.getLocation();
            for (int i = dirPath.length; i-- > 0; ) {
                if (Clock.getBytecodesLeft() < visitedBytecodeLeft) return;
                loc = loc.add(dirPath[i]);
                if (rc.onTheMap(loc)) {
                    visited[loc.x][loc.y] = true; //encoded
                }
            }
        } catch (Throwable e){
            e.printStackTrace();
        }
    }
    void fillDirPath() {
        switch (visionRadius) {
            case 20:
                dirPath = new Direction[]{Direction.NORTHWEST, Direction.NORTHWEST, Direction.NORTH, Direction.NORTH, Direction.NORTH, Direction.NORTH, Direction.NORTHEAST, Direction.NORTHEAST, Direction.EAST, Direction.EAST, Direction.EAST, Direction.EAST, Direction.SOUTHEAST, Direction.SOUTHEAST, Direction.SOUTH, Direction.SOUTH, Direction.SOUTH, Direction.SOUTH, Direction.SOUTHWEST, Direction.SOUTHWEST, Direction.WEST, Direction.WEST, Direction.WEST, Direction.NORTHWEST, Direction.NORTHWEST, Direction.NORTH, Direction.NORTH, Direction.NORTH, Direction.NORTH, Direction.NORTHEAST, Direction.EAST, Direction.EAST, Direction.EAST, Direction.EAST, Direction.SOUTHEAST, Direction.SOUTH, Direction.SOUTH, Direction.SOUTH, Direction.SOUTH, Direction.SOUTHWEST, Direction.WEST, Direction.WEST, Direction.WEST, Direction.NORTHWEST, Direction.NORTH, Direction.NORTH, Direction.NORTH, Direction.NORTH, Direction.EAST, Direction.EAST, Direction.EAST, Direction.EAST, Direction.SOUTH, Direction.SOUTH, Direction.SOUTH, Direction.SOUTH, Direction.WEST, Direction.WEST, Direction.WEST, Direction.NORTH, Direction.NORTH, Direction.NORTH, Direction.EAST, Direction.EAST, Direction.SOUTH, Direction.SOUTH, Direction.WEST, Direction.NORTH, Direction.CENTER};
                break;
            case 34: //missing some directions, actually 30
                dirPath = new Direction[]{Direction.NORTHWEST, Direction.NORTHWEST, Direction.NORTHWEST, Direction.NORTH, Direction.NORTH, Direction.NORTH, Direction.NORTH, Direction.NORTHEAST, Direction.NORTHEAST, Direction.NORTHEAST, Direction.EAST, Direction.EAST, Direction.EAST, Direction.EAST, Direction.SOUTHEAST, Direction.SOUTHEAST, Direction.SOUTHEAST, Direction.SOUTH, Direction.SOUTH, Direction.SOUTH, Direction.SOUTH, Direction.SOUTHWEST, Direction.SOUTHWEST, Direction.SOUTHWEST, Direction.WEST, Direction.WEST, Direction.WEST, Direction.NORTHWEST, Direction.NORTHWEST, Direction.NORTHWEST, Direction.NORTH, Direction.NORTH, Direction.NORTH, Direction.NORTH, Direction.NORTHEAST, Direction.NORTHEAST, Direction.EAST, Direction.EAST, Direction.EAST, Direction.EAST, Direction.SOUTHEAST, Direction.SOUTHEAST, Direction.SOUTH, Direction.SOUTH, Direction.SOUTH, Direction.SOUTH, Direction.SOUTHWEST, Direction.SOUTHWEST, Direction.WEST, Direction.WEST, Direction.WEST, Direction.NORTHWEST, Direction.NORTHWEST, Direction.NORTH, Direction.NORTH, Direction.NORTH, Direction.NORTH, Direction.NORTHEAST, Direction.EAST, Direction.EAST, Direction.EAST, Direction.EAST, Direction.SOUTHEAST, Direction.SOUTH, Direction.SOUTH, Direction.SOUTH, Direction.SOUTH, Direction.SOUTHWEST, Direction.WEST, Direction.WEST, Direction.WEST, Direction.NORTHWEST, Direction.NORTH, Direction.NORTH, Direction.NORTH, Direction.NORTH, Direction.EAST, Direction.EAST, Direction.EAST, Direction.EAST, Direction.SOUTH, Direction.SOUTH, Direction.SOUTH, Direction.SOUTH, Direction.WEST, Direction.WEST, Direction.WEST, Direction.NORTH, Direction.NORTH, Direction.NORTH, Direction.EAST, Direction.EAST, Direction.SOUTH, Direction.SOUTH, Direction.WEST, Direction.NORTH, Direction.CENTER};
                break;
            default:
                dirPath = new Direction[0];
        }
    }
}
