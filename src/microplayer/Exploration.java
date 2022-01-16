package microplayer;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class Exploration {

    static final int NUM_INTERESTING = 9;

    RobotController rc;
    static Direction[] dirPath;
    Direction lastDirMoved = null;
    MapLocation exploreTarget = null;
    MapLocation[] pointsOfInterest;

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
        initalizePointsOfInterest();
    }

    void initTurn() {

    }

    void reset() {
        exploreTarget = null;
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

    //picks from pool of unexplored interesting targets. if fails, goes to random.
    MapLocation getExplore2Target(){
        if (exploreTarget != null && !hasVisited(exploreTarget)) return exploreTarget;
        int tries = 2; // dont want to many attemps, or we'll keep pathing to dangerous locations
        for (int i = tries; i-- > 0; ){
            int j = (int)(Math.random()*NUM_INTERESTING);
            exploreTarget = pointsOfInterest[j];
            if (!hasVisited(exploreTarget)) return exploreTarget;
        }
        return getExploreTarget();
    }

    MapLocation getExplore3Target(){
        if (exploreTarget != null && !hasVisited(exploreTarget)) return exploreTarget;
        exploreTarget = pointsOfInterest[4]; // map center
        if (!hasVisited(exploreTarget)) return exploreTarget;
        return getExploreTarget();
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

    // 4 corners, 4 midopints, and center
    void initalizePointsOfInterest() {
        pointsOfInterest = new MapLocation[NUM_INTERESTING];
        int midH = H/2;
        int midW = W/2;
        int H1 = H - 1;
        int W1 = W - 1;
        pointsOfInterest[0] = new MapLocation(0,0);
        pointsOfInterest[1] = new MapLocation(0, midH);
        pointsOfInterest[2] = new MapLocation(0, H1);
        pointsOfInterest[3] = new MapLocation(midW,0);
        pointsOfInterest[4] = new MapLocation(midW, midH);
        pointsOfInterest[5] = new MapLocation(midW, H1);
        pointsOfInterest[6] = new MapLocation(W1,0);
        pointsOfInterest[7] = new MapLocation(W1, midH);
        pointsOfInterest[8] = new MapLocation(W1, H1);


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
