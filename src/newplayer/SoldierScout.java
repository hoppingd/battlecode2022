package newplayer;

import battlecode.common.*;

public class SoldierScout {

    RobotController rc;
    Communication comm;
    int H, W;
    boolean checkedVertical;
    boolean checkedHorizontal;
    Team myTeam, enemyTeam;

    SoldierScout(RobotController rc, Communication comm) {
        this.rc = rc;
        this.comm = comm;
        H = rc.getMapHeight();
        W = rc.getMapWidth();
        myTeam = rc.getTeam();
        enemyTeam = myTeam.opponent();
    }

    MapLocation getProspect() {
        if (comm.HQloc == null) { // we start scouting a bit late since the HQ only writes its location after building miners
            return null;
        }
        if (!checkedVertical) {
            int d1 = comm.HQloc.x - W / 2;
            int d2 = Math.abs(d1);
            if (d1 == 0) { // HQ is on line of vertical symmetry
                checkedVertical = true; // could still be vertically symmetrical, but this is an edge case
            } else if (d1 > 0) {
                return new MapLocation(W / 2 - d2, comm.HQloc.y);
            } else {
                return new MapLocation(W / 2 + d2, comm.HQloc.y);
            }
        }
        else if (!checkedHorizontal) {
            int d1 = comm.HQloc.y - H / 2;
            int d2 = Math.abs(d1);
            if (d1 == 0) { // HQ is on line of horizontal symmetry
                checkedHorizontal = true; // could still be horizontally symmetrical, but this is an edge case
            } else if (d1 > 0) {
                return new MapLocation(comm.HQloc.x, H / 2 - d2);
            } else {
                return new MapLocation(comm.HQloc.x, H / 2 + d2);
            }
        }
        // we've checked both, so we should change to defensive lattice, or something
        return comm.HQloc;
    }

    boolean checkProspect(MapLocation prospect) { // if we find an abandoned archon, we might want to pause scouting and shoot it
        if (!rc.canSenseLocation(prospect)) return false; // we only affirm symmetry and enemy HQ location if they don't move. if scout dies, we won't affirm
        if (!checkedVertical) {
            checkedVertical = true;
        }
        else if (!checkedHorizontal) {
            checkedHorizontal = true;
        }
        if (rc.canSenseRobotAtLocation(prospect)) {
            try {
                RobotInfo r = rc.senseRobotAtLocation(prospect);
                if (r.getType() == RobotType.ARCHON) {
                    if (r.getTeam() == enemyTeam) {
                        comm.setEnemyHQloc(prospect);
                        // if both symmetries are false at this point, must be rotational symmetry
                    }
                    else {
                        comm.setSymmetry(checkedHorizontal);
                    }
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        return checkedHorizontal;
    }
}
