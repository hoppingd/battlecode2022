package queueplayer;

import battlecode.common.*;

import java.util.HashSet;

public class Pathfinding {

    RobotController rc;
    MapLocation target = null;
    double avgRubble = 100;

    BugNav bugNav = new BugNav();
    Exploration explore;

    Team myTeam, enemyTeam;

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

    boolean[] impassable = null;

    void setImpassable(boolean[] impassable){
        this.impassable = impassable;
    }

    void initTurn(){
        impassable = new boolean[directions.length];
    }

    boolean canMove(Direction dir){
        if (!rc.canMove(dir)) return false;
        if (impassable[dir.ordinal()]) return false;
        return true;
    }

    boolean doMicro() {
        return bugNav.doMicro();
    }

    Pathfinding(RobotController rc, Exploration explore){
        this.rc = rc;
        this.explore = explore;
        myTeam = rc.getTeam();
        enemyTeam = myTeam.opponent();
    }

    double getEstimation (MapLocation loc){
        try {
            if (loc.distanceSquaredTo(target) == 0) return 0;
            int d = Util.distance(target, loc);
            double r = 10 + rc.senseRubble(loc);
            return r + (d - 1)*avgRubble;
        } catch (Throwable e){
            e.printStackTrace();
        }
        return 1e9;
    }

    public void move(MapLocation loc){
        if (rc.getMovementCooldownTurns() >= 1) return;
        target = loc;

        //rc.setIndicatorLine(rc.getLocation(), target, 255, 0, 0);

        if (!bugNav.move()) greedyPath();
        bugNav.move();
    }

    final double eps = 1e-5;

    void greedyPath(){
        try {
            MapLocation myLoc = rc.getLocation();
            Direction bestDir = null;
            double bestEstimation = 0;
            double firstStep = rc.senseRubble(myLoc);
            int contRubble = 0;
            int bestEstimationDist = 0;
            double avgR = 0;
            for (Direction dir : directions) {
                MapLocation newLoc = myLoc.add(dir);
                if (!rc.onTheMap(newLoc)) continue;

                //pass
                avgR += rc.senseRubble(newLoc);
                ++contRubble;


                if (!canMove(dir)) continue;
                if (!strictlyCloser(newLoc, myLoc, target)) continue;

                int newDist = newLoc.distanceSquaredTo(target);

                double estimation = firstStep + getEstimation(newLoc);
                if (bestDir == null || estimation < bestEstimation - eps || (Math.abs(estimation - bestEstimation) <= 2*eps && newDist < bestEstimationDist)) {
                    bestEstimation = estimation;
                    bestDir = dir;
                    bestEstimationDist = newDist;
                }
            }
            if (contRubble != 0) {
                avgRubble = avgR / contRubble;
            }
            if (bestDir != null) rc.move(bestDir);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    boolean strictlyCloser(MapLocation newLoc, MapLocation oldLoc, MapLocation target){
        int dOld = Util.distance(target, oldLoc), dNew = Util.distance(target, newLoc);
        if (dOld < dNew) return false;
        if (dNew < dOld) return true;
        return target.distanceSquaredTo(newLoc) < target.distanceSquaredTo(oldLoc);

    }

    class BugNav{

        BugNav(){}

        final int INF = 1000000;

        boolean rotateRight = true; //if I should rotate right or left
        MapLocation lastObstacleFound = null; //latest obstacle I've found in my way
        int minDistToEnemy = INF; //minimum distance I've been to the enemy while going around an obstacle
        MapLocation prevTarget = null; //previous target
        HashSet<Integer> visited = new HashSet<>();

        boolean move() {
            try{

                //different target? ==> previous data does not help!
                if (prevTarget == null || target.distanceSquaredTo(prevTarget) > 0) resetPathfinding();

                //If I'm at a minimum distance to the target, I'm free!
                MapLocation myLoc = rc.getLocation();
                int d = myLoc.distanceSquaredTo(target);
                if (d <= minDistToEnemy) resetPathfinding();

                int code = getCode();

                if (visited.contains(code)) resetPathfinding();
                visited.add(code);

                //Update data
                prevTarget = target;
                minDistToEnemy = Math.min(d, minDistToEnemy);

                //If there's an obstacle I try to go around it [until I'm free] instead of going to the target directly
                Direction dir = myLoc.directionTo(target);
                if (lastObstacleFound != null) dir = myLoc.directionTo(lastObstacleFound);
                if (canMove(dir)){
                    resetPathfinding();
                }

                //I rotate clockwise or counterclockwise (depends on 'rotateRight'). If I try to go out of the map I change the orientation
                //Note that we have to try at most 16 times since we can switch orientation in the middle of the loop. (It can be done more efficiently)
                for (int i = 8; i-- > 0;) {
                    if (canMove(dir)) {
                        rc.move(dir);
                        return true;
                    }
                    MapLocation newLoc = myLoc.add(dir);
                    if (!rc.onTheMap(newLoc)) rotateRight = !rotateRight;
                        //If I could not go in that direction, and it was not outside the map, then this is the latest obstacle found
                    else lastObstacleFound = myLoc.add(dir);
                    if (rotateRight) dir = dir.rotateRight();
                    else dir = dir.rotateLeft();
                }

                if (canMove(dir)) rc.move(dir);
            } catch (Exception e){
                e.printStackTrace();
            }
            return true;
        }

        //clear some of previous data
        void resetPathfinding(){
            lastObstacleFound = null;
            minDistToEnemy = INF;
            visited.clear();
        }

        int getCode(){
            int x = rc.getLocation().x;
            int y = rc.getLocation().y;
            Direction obstacleDir = rc.getLocation().directionTo(target);
            if (lastObstacleFound != null) obstacleDir = rc.getLocation().directionTo(lastObstacleFound);
            int bit = rotateRight ? 1 : 0;
            return (((((x << 6) | y) << 4) | obstacleDir.ordinal()) << 1) | bit;
        }

        boolean doMicro() {
            MicroInfo[] microInfo = new MicroInfo[9];
            for (int i = 0; i < 9; i++) microInfo[i] = new MicroInfo(rc.getLocation().add(directions[i]));

            RobotInfo[] enemies = rc.senseNearbyRobots(rc.getType().visionRadiusSquared, enemyTeam);

            for (RobotInfo enemy : enemies) {
                for (int i = 0; i < 9; i++) {
                    microInfo[i].update(enemy);
                }
            }

            int bestIndex = -1;
            for (int i = 8; i >= 0; i--) {
                if (!rc.canMove(directions[i])) continue;
                if (bestIndex < 0 || !microInfo[bestIndex].isBetter(microInfo[i])) bestIndex = i;
            }

            if (bestIndex != -1) {
                if (enemies.length > 0) {
                    try {
                        //try attacking if fleeing all combat
                        if(rc.isActionReady() && microInfo[bestIndex].numEnemies == 0) {
                            RobotInfo[] enemiesToAttack = rc.senseNearbyRobots(rc.getType().actionRadiusSquared, enemyTeam);
                            MapLocation bestLoc = null;
                            boolean attackerInRange = false;
                            // don't attack miners if soldiers in view
                            for (RobotInfo r : enemies) {
                                if (r.type.canAttack()) {
                                    attackerInRange = true;
                                }
                            }
                            int bestHealth = 10000;
                            int bestRubble = GameConstants.MAX_RUBBLE;
                            for (RobotInfo r : enemiesToAttack) {
                                MapLocation enemyLoc = r.getLocation();
                                boolean isAttacker = r.type.canAttack();
                                // if there are attackers, ignore all non-attackers and reset variables
                                if (!isAttacker && attackerInRange) continue;
                                int rubble = GameConstants.MAX_RUBBLE;
                                try {
                                    rubble = rc.senseRubble(r.location);
                                } catch (Throwable t) {
                                    t.printStackTrace();
                                }
                                if (isAttacker && !attackerInRange) {
                                    bestHealth = 10000;
                                    bestRubble = rubble;
                                    attackerInRange = true;
                                }
                                // shoot lowest health with rubble as tiebreaker
                                if (r.health < bestHealth) {
                                    bestHealth = r.health;
                                    bestRubble = rubble;
                                    bestLoc = enemyLoc;
                                }
                                else if (r.health == bestHealth && rubble < bestRubble) {
                                    bestRubble = rubble;
                                    bestLoc = enemyLoc;
                                }
                            }
                            try {
                                if (bestLoc != null) {
                                    rc.attack(bestLoc);
                                }
                            } catch (Throwable t) {
                                t.printStackTrace();
                            }
                        }
                        //System.err.println("microing to " + rc.getLocation().add(directions[bestIndex]));
                        rc.move(directions[bestIndex]);
                    } catch (Throwable e){
                        e.printStackTrace();
                    }
                    return true;
                }
            }
            return false;
        }

        class MicroInfo{
            int numEnemies;
            int minDistToEnemy = INF;
            int rubble = GameConstants.MAX_RUBBLE + 1;
            MapLocation loc;

            public MicroInfo(MapLocation loc) {
                this.loc = loc;
                numEnemies = 0;
                try {
                    if (rc.onTheMap(loc)) rubble = rc.senseRubble(loc);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }

            void update(RobotInfo robot) {
                int d = robot.location.distanceSquaredTo(loc);
                if (d <= robot.type.actionRadiusSquared && robot.getType().canAttack()) {
                    numEnemies++;
                }
                if (d < minDistToEnemy) minDistToEnemy = d;
            }

            boolean canAttack() {
                return rc.getType().actionRadiusSquared >= minDistToEnemy;
            }

            //TODO: improve
            boolean isBetter (MicroInfo m) {
                if (rubble < m.rubble) {
                    return true;
                }
                if (rubble > m.rubble) return false;
                if (numEnemies < m.numEnemies) return true;
                if (numEnemies > m.numEnemies) return false;
                if (canAttack()) {
                    if (!m.canAttack()) return true;
                    return minDistToEnemy >= m.minDistToEnemy;
                }
                if (m.canAttack()) return false;
                return minDistToEnemy <= m.minDistToEnemy;
            }
        }
    }
}