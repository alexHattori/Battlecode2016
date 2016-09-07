package turrets;

import battlecode.common.*;

public class RobotPlayer {
	public static void run(RobotController rc){
		try {
			if (rc.getType() == RobotType.ARCHON) {
				runArchon(rc);
			} else if (rc.getType() == RobotType.TURRET || rc.getType() == RobotType.TTM) {
				runTurretTTM(rc);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void runArchon(RobotController rc) throws GameActionException {
		while (true) {
			if (rc.isCoreReady()) {
				for (Direction dir : Direction.values()) {
					if (rc.canBuild(dir, RobotType.TURRET)) {
						rc.build(dir, RobotType.TURRET);
						break;
					}
				}
			}
			Clock.yield();
		}
	}
	
	private static MapLocation attackingLocation = null;
	private static int sameAttackingCounter = 0;
	private static final int SAME_ATTACKING_LIMIT = 20;
	private static void runTurretTTM(RobotController rc) throws GameActionException {
		while (true) {
			if (rc.getType() == RobotType.TURRET) {
				
			} else {
				
			}
			
			
			if (rc.getType() == RobotType.TURRET) {
				if (rc.isWeaponReady()) {
					boolean updatedLocation = false;
					for (Signal s : rc.emptySignalQueue()) {
						MapLocation loc = s.getLocation();
						if (s.getTeam() != rc.getTeam() && rc.canAttackLocation(loc)) {
							attackingLocation = loc;
							updatedLocation = true;
							break;
						}
					}
					
					RobotInfo[] robotInfos = rc.senseHostileRobots(rc.getLocation(), -1);
					for (RobotInfo ri : robotInfos) {
						if (rc.canAttackLocation(ri.location)) {
							attackingLocation = ri.location;
							updatedLocation = true;
							break;
						}
					}
					
					if (attackingLocation != null) {
						rc.attackLocation(attackingLocation);
					}
					if (!updatedLocation && sameAttackingCounter++ > SAME_ATTACKING_LIMIT) {
						rc.pack();
					}
				}
			} else {
				if (rc.isCoreReady()) {
					
				}
			}
			
			Clock.yield();
		}
	}
}
