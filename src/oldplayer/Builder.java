package oldplayer;

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

    static final int ALLY_FORCES_RANGE = 29;

    int watchCount = 0;
    int watchRepairedCount = 0;
    int H, W;
    Team myTeam, enemyTeam;
    int birthday;
    int task = 0;
    MapLocation nearestCorner;
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
        nearestCorner = getNearestCorner();
        comm.readLeadScore(); // we also get lead score to determine how much we build before moving, if we should make mines, build towers, etc
        mapLeadScore = (comm.leadScore / (double)comm.numArchons) * (400.0/(H*W));
        HIGH_LEAD_MAP = mapLeadScore > comm.HIGH_LEAD_THRESHOLD;
        LOW_LEAD_MAP = mapLeadScore < comm.LOW_LEAD_THRESHOLD;
    }

    public void play(){
        currRound = rc.getRoundNum();
        task = comm.getTask();
        moved = false;
        if(!tryRepairPrototype()) { //for finishing tower
            tryBuild();
        }
        tryMove();
        tryDisintegrate();
    }

    // TODO: cleanup
    boolean tryRepairPrototype() {
        RobotInfo[] allies = rc.senseNearbyRobots(rc.getType().actionRadiusSquared, myTeam);
        MapLocation bestRepair = null;
        int bestHealth = 0;
        for (RobotInfo r : allies){
            MapLocation allyLoc = r.getLocation();
            if (r.getMode() == RobotMode.PROTOTYPE && rc.canRepair(allyLoc)){
                int health = r.getHealth();
                if (health < r.getType().health && health > bestHealth) {
                    bestHealth = health;
                    bestRepair = allyLoc;
                }
            }
        }
        try {
            if (bestRepair != null) {
                rc.repair(bestRepair);
                RobotInfo repairTarget = rc.senseRobotAtLocation(bestRepair);
                if (repairTarget.getHealth() == repairTarget.getType().health) {
                    watchRepairedCount++;
                }
                return true;
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return false;
    }

    // TODO: disintegrate if crowded and been around long
    void tryDisintegrate() {
        if (watchRepairedCount > 0) {
            MapLocation myLoc = rc.getLocation();
            try {
                if (rc.senseLead(myLoc) == 0) {
                    rc.disintegrate();
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    // TODO: should be way smarter with where we build watchtowers
    void tryBuild(){
        if(!comm.labIsBuilt()) {
            if (rc.getLocation().isAdjacentTo(nearestCorner) && rc.getTeamLeadAmount(myTeam) > RobotType.LABORATORY.buildCostLead) {
                MapLocation bestLoc = null;
                int bestRubble = 10000;
                int bestDist = 10000;
                for (Direction dir : spawnDirections) {
                    MapLocation cell = rc.getLocation().add(dir);
                    try {
                        if (!rc.canSenseLocation(cell) || rc.isLocationOccupied(cell)) continue;
                        int rubble = rc.senseRubble(cell);
                        if (bestLoc == null) {
                            bestLoc = cell;
                            bestRubble = rubble;
                            bestDist = cell.distanceSquaredTo(nearestCorner);
                        }
                        else if (rubble < bestRubble) {
                            bestLoc = cell;
                            bestRubble = rubble;
                            bestDist = cell.distanceSquaredTo(nearestCorner);
                        }
                        else if (rubble == bestRubble && cell.distanceSquaredTo(nearestCorner) < bestDist) {
                            bestLoc = cell;
                            bestRubble = rubble;
                            bestDist = cell.distanceSquaredTo(nearestCorner);
                        }

                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
                try {
                    if (bestLoc != null && rc.canBuildRobot(RobotType.LABORATORY, rc.getLocation().directionTo(bestLoc))) {
                        rc.buildRobot(RobotType.LABORATORY, rc.getLocation().directionTo(bestLoc));
                        comm.setLabBuilt();
                        return;
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        }
    }

    void tryMove(){
        if (!rc.isMovementReady()) return;
        MapLocation target = null;

        if (watchRepairedCount == 0) {
            target = getLabBuildLoc();
            //System.err.println("labBuildLoc: " + target);
        }

        if (target == null) {
            target = getMineProspect();
        }

        if (target != null) {
            MapLocation myLoc = rc.getLocation();
            bfs.move(target);
            if (myLoc == rc.getLocation()) bfs.path.move(myLoc);
        }
        else {
            bfs.move(explore.getExploreTarget());
        }
    }

    // TODO: lowest rubble adjacent to corner
    MapLocation getLabBuildLoc() {
        MapLocation target = nearestCorner;
        int bestRubble = 10000;
        try {
            MapLocation[] cells = rc.getAllLocationsWithinRadiusSquared(nearestCorner, 2);
            for (MapLocation cell : cells) {
                if (!rc.canSenseLocation(cell)) continue;
                if (cell.equals(nearestCorner)) continue;
                if (rc.isLocationOccupied(cell) && !rc.getLocation().equals(cell)) continue;
                int rubble = rc.senseRubble(cell);
                if (rubble < bestRubble) {
                    target = cell;
                    bestRubble = rubble;
                }
                else if (rubble == bestRubble && rc.getLocation().distanceSquaredTo(cell) < rc.getLocation().distanceSquaredTo(target)) {
                    target = cell;
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return target;
    }

    MapLocation getMineProspect() {
        MapLocation myLoc = rc.getLocation();
        MapLocation target = null;
        int bestDist = 10000;
        try {
            MapLocation cells[] = rc.getAllLocationsWithinRadiusSquared(myLoc, rc.getType().visionRadiusSquared);
            for (MapLocation cell : cells){
                if (rc.senseLead(cell) > 0 || cell.distanceSquaredTo(nearestCorner) > Math.sqrt(Math.sqrt(H*W))) break;
                int dist = myLoc.distanceSquaredTo(cell);
                if (dist < bestDist) { // closest spot with no lead and not too close to corner
                    bestDist = dist;
                    target = cell;
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return target;
    }

    MapLocation getNearestCorner() {
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
        MapLocation nearestCorner = new MapLocation(x,y);
        int d1 = comm.HQloc.distanceSquaredTo(nearestCorner);
        // if not near corner, build around HQ TODO: if close to wall, builder should build near wall, not HQ
        if (comm.HQloc.distanceSquaredTo(new MapLocation(x, H1/2)) < d1) {
            nearestCorner = new MapLocation(x, comm.HQloc.y);
        }
        else if (comm.HQloc.distanceSquaredTo(new MapLocation(W1/2, y)) < d1) {
            nearestCorner = new MapLocation(comm.HQloc.x, y);
        }
        return nearestCorner;
    }
}