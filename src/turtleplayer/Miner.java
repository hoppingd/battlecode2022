package turtleplayer;

import battlecode.common.*;

import java.awt.*;

public class Miner extends MyRobot {

    static final Direction[] moveDirections = {
            Direction.NORTH,
            Direction.NORTHEAST,
            Direction.EAST,
            Direction.SOUTHEAST,
            Direction.SOUTH,
            Direction.SOUTHWEST,
            Direction.WEST,
            Direction.NORTHWEST,
    };

    static final int MIN_LEAD_TO_MINE = 6;
    static final int DISINTEGRATE_HEALTH = 21;
    Direction[] dirs = Direction.allDirections();
    int H, W;
    Team myTeam, enemyTeam;

    RobotInfo[] nearbyEnemies;
    double mapLeadScore;
    double turtleRadius;

    MapLocation nearestCorner = null;
    boolean isBaseMiner;

    public Miner(RobotController rc){
        super(rc);
        H = rc.getMapHeight();
        W = rc.getMapWidth();
        myTeam = rc.getTeam();
        enemyTeam = myTeam.opponent();
        comm.readHQloc();
        mapLeadScore = (comm.leadScore / (double)comm.numArchons) * (400.0/(H*W));
        turtleRadius = Math.pow(H*W, .6);
        if (comm.HQloc != null) getNearestCorner();
        isBaseMiner = comm.getMinerFlag() == 1;
        if (isBaseMiner) comm.setMinerFlag(2);
    }

    public void play(){
        if (comm.HQloc == null) {
            if (comm.readHQloc()) {
                getNearestCorner();
            }
        }
        nearbyEnemies = rc.senseNearbyRobots(RobotType.MINER.visionRadiusSquared, enemyTeam);
        tryDisintegrate(); //will only disintegrate if valid location and health too low
        tryMine();
        if (rc.isMovementReady()) {
            if (!isBaseMiner) {
                tryMove();
            }
            else {
                //System.err.println("The little miner that could.");
                moveWithinBase();
            }
            tryMine();
        }
    }

    //TODO: improve
    MapLocation moveInCombat() {
        for (RobotInfo enemy : nearbyEnemies) {
            //sense enemyArchons
            if (enemy.type == RobotType.ARCHON) {
                comm.writeEnemyArchonLocation(enemy);
                try {
                    if ((mapLeadScore < comm.HIGH_LEAD_THRESHOLD && rc.getRoundNum() <= 200 && rc.senseNearbyLocationsWithLead(RobotType.MINER.visionRadiusSquared).length > 9)) { // sense not rush
                        comm.setTask(4); // RUSH!
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
            // only consider offensive units
            if (!enemy.type.canAttack()) continue;
            //TODO: only consider combat units, with more weight given to watchtowers
            int myForcesCount = 0;
            RobotInfo[] myForces = rc.senseNearbyRobots(enemy.location, RobotType.SOLDIER.visionRadiusSquared, myTeam);
            for (RobotInfo r : myForces) {
                if (r.type.canAttack()) {
                    myForcesCount += r.health;
                }
            }
            int enemyForcesCount = 0;
            RobotInfo[] enemyForces = rc.senseNearbyRobots(enemy.location, RobotType.SOLDIER.visionRadiusSquared, enemyTeam);
            for (RobotInfo r : enemyForces) {
                if (r.type.canAttack()) {
                    enemyForcesCount += r.health;
                }
            }

            if (myForcesCount < enemyForcesCount * 2) { // arbitrary modifier to be a bit safer TODO: flee from highest enemyforcescount
                explore.reset();
                //return flee(enemy.location);
                return comm.HQloc;
            }
        }
        return null;
    }

    // TODO: improve
    void tryMine(){
        MapLocation myLoc = rc.getLocation();
        int leadToLeave = 1;
        if (comm.HQloc != null && myLoc.distanceSquaredTo(comm.HQloc) > turtleRadius*1.25 && myLoc.distanceSquaredTo(nearestCorner) > turtleRadius*1.25) { // outside archon turtle radius or corner turtle radius
            leadToLeave = 0;
        }
        try {
            for (Direction dir : dirs) {
                MapLocation prospect = myLoc.add(dir);
                if (!(rc.onTheMap(prospect))) continue; // reduce bytecode?
                int lead = rc.senseLead(prospect);
                int gold = rc.senseGold(prospect); //adds max of 45 bytecode

                while (lead > leadToLeave) {
                    if (rc.isActionReady()) {
                        rc.mineLead(prospect);
                        lead--;
                    }
                    else {
                        return;
                    }
                }
                while (gold > 0) {
                    if (rc.isActionReady()) {
                        rc.mineGold(prospect);
                        gold--;
                    }
                    else {
                        return;
                    }
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    void tryMove(){
        MapLocation loc = moveInCombat();
        if (rc.getHealth() < DISINTEGRATE_HEALTH) loc = getMineProspect(); // if too far from HQ, don't bother
        if (loc == null) loc = getClosestMine();
        if (loc != null){
            bfs.move(loc);
            return;
        }
        loc = explore.getExplore2Target(); // use alternate function to find points of interest
        bfs.move(loc);
        return;
    }

    void moveWithinBase() {
        MapLocation loc = null;
        if (loc == null) loc = getClosestMineInBase();
        if (loc != null){
            bfs.move(loc);
            return;
        }
        loc = patrol();
        greedyMove(loc);
        return;
    }

    boolean isInBase(MapLocation loc) {
        int d1 = loc.distanceSquaredTo(nearestCorner);
        int d2 = loc.distanceSquaredTo(comm.HQloc);
        return d1 <= turtleRadius || d2 <= turtleRadius;
    }

    void greedyMove(MapLocation target) {
        MapLocation myLoc = rc.getLocation();
        int bestRubble = GameConstants.MAX_RUBBLE;
        MapLocation bestLoc = null;
        int d1 = myLoc.distanceSquaredTo(target);
        try {
            for (Direction dir: moveDirections) {
                MapLocation prospect = myLoc.add(dir);
                if (!isInBase(prospect) || !rc.canMove(dir)) continue;
                int r = rc.senseRubble(prospect);
                if (prospect.distanceSquaredTo(target) < d1 && r < bestRubble) {
                    bestLoc = prospect;
                    bestRubble = r;
                }
            }
            if (bestLoc != null) rc.move(myLoc.directionTo(bestLoc));

        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    MapLocation getClosestMine(){
        MapLocation myLoc = rc.getLocation();
        MapLocation bestMine = null;
        int bestDist = 10000;
        try {
            MapLocation leadMines[] = rc.senseNearbyLocationsWithLead(RobotType.MINER.visionRadiusSquared, MIN_LEAD_TO_MINE);
            for (MapLocation mine : leadMines) { // interlinked
                int dist = myLoc.distanceSquaredTo(mine);
                if (bestMine == null) {
                    bestMine = mine;
                    bestDist = dist;
                }
                if (dist < bestDist) {
                    bestMine = mine;
                    bestDist = dist;
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return bestMine;
    }

    MapLocation getClosestMineInBase(){
        MapLocation myLoc = rc.getLocation();
        MapLocation bestMine = null;
        int bestDist = 10000;
        try {
            MapLocation leadMines[] = rc.senseNearbyLocationsWithLead(RobotType.MINER.visionRadiusSquared, MIN_LEAD_TO_MINE);
            for (MapLocation mine : leadMines) { // interlinked
                int dist = myLoc.distanceSquaredTo(mine);
                if (mine.distanceSquaredTo(comm.HQloc) > turtleRadius && mine.distanceSquaredTo(nearestCorner) > turtleRadius) continue; // stay safe
                if (bestMine == null) {
                    bestMine = mine;
                    bestDist = dist;
                }
                if (dist < bestDist) {
                    bestMine = mine;
                    bestDist = dist;
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return bestMine;
    }

    //todo: improve patrol
    MapLocation patrol() {
        MapLocation myLoc = rc.getLocation();
        try {
            int tries = 4; // dont want to many attemps, or we'll keep pathing to dangerous locations
            MapLocation[] cells = rc.getAllLocationsWithinRadiusSquared(myLoc, RobotType.MINER.visionRadiusSquared);
            for (int i = tries; i-- > 0; ){
                int j = (int)(Math.random()*cells.length);
                MapLocation target = cells[j];
                if (target.distanceSquaredTo(comm.HQloc) <= turtleRadius || target.distanceSquaredTo(nearestCorner) <= turtleRadius) return target;
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return myLoc;
    }


    MapLocation getMineProspect() {
        MapLocation myLoc = rc.getLocation();
        // consider giving up if too far away
        MapLocation target = null;
        int bestDist = 10000;
        try {
            MapLocation cells[] = rc.getAllLocationsWithinRadiusSquared(myLoc, RobotType.MINER.visionRadiusSquared);
            for (MapLocation cell : cells){
                if (rc.senseLead(cell) > 0 || (cell.distanceSquaredTo(comm.HQloc) > turtleRadius && cell.distanceSquaredTo(nearestCorner) > turtleRadius)) continue;
                int dist = myLoc.distanceSquaredTo(cell);
                if (dist < bestDist) { // closest spot with no lead within turtle radius
                    bestDist = dist;
                    target = cell;
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        if (target == null) return comm.HQloc;
        return target;
    }

    void tryDisintegrate() {
        if (rc.getHealth() >= DISINTEGRATE_HEALTH || isBaseMiner) return;
        if (!rc.isActionReady()) return;
        MapLocation myLoc = rc.getLocation();
        try {
            if ((myLoc.distanceSquaredTo(comm.HQloc) <= turtleRadius || myLoc.distanceSquaredTo(nearestCorner) <= turtleRadius) && rc.senseLead(myLoc) == 0) {
                rc.disintegrate();
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    void getNearestCorner() {
        int x;
        int y;
        int W1 = W - 1;
        int H1 = H - 1;
        if(W1 - comm.HQloc.x > comm.HQloc.x) {
            x = 0;
        }
        else {
            x = W1;
        }
        if(H1 - comm.HQloc.y > comm.HQloc.y) {
            y = 0;
        }
        else {
            y = H1;
        }
        nearestCorner = new MapLocation(x,y);
        int d1 = comm.HQloc.distanceSquaredTo(nearestCorner);
        // if not near corner, build around HQ TODO: if close to wall, builder should build near wall, not HQ
        if (comm.HQloc.distanceSquaredTo(new MapLocation(x, H1/2)) < d1) {
            nearestCorner = new MapLocation(x, comm.HQloc.y);
        }
        else if (comm.HQloc.distanceSquaredTo(new MapLocation(W1/2, y)) < d1) {
            nearestCorner = new MapLocation(comm.HQloc.x, y);
        }
    }
}