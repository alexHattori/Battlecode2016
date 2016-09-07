package team138;

import battlecode.common.*;

import java.util.ArrayList;
import java.util.Random;

public class RobotPlayer {

	private static void runArchon() throws GameActionException{
		if(rc.isCoreReady()){
			MapLocation myLoc = rc.getLocation();
			RobotInfo[] poops = rc.senseHostileRobots(myLoc,14);
			if(poops.length>=3){
				curState = STATES.FLEEING;
			}
			switch(curState){
			case INIT:
				curState = STATES.SEARCHING;
				break;
			case SEARCHING:
				Direction goalDir;
				if(initArchon.length>0){
					goalDir = myLoc.directionTo(initArchon[0]).opposite();
				}
				else{
					goalDir = directions[rand.nextInt(8)];
				}
				if(rc.senseHostileRobots(myLoc, -1).length>=3){
					curState = STATES.FLEEING;
				}
				if(rand.nextInt(10)<=3){
					if(rc.isCoreReady()){
						rc.broadcastSignal(4);
					}
				}
				int timesThrough = 0;
				while(!rc.canMove(goalDir)){
					if(timesThrough ==4){
						curState = STATES.WAITING;
						break;
					}
					timesThrough++;
					Direction left = goalDir.rotateLeft().rotateLeft();
					Direction right = goalDir.rotateRight().rotateRight();
					if(rc.isLocationOccupied(myLoc.add(left))||rc.isLocationOccupied(myLoc.add(right))||rc.isLocationOccupied(myLoc.add(goalDir))){
						break;
					}
					if(!rc.canMove(left)&&!rc.canMove(right)){
						goalDir = goalDir.opposite();
						inCorner = true;
						break;
					}
					goalDir = goalDir.rotateRight();
				}
				if(rc.canMove(goalDir)){
					if(rc.isCoreReady()){
						rc.move(goalDir);
					}
				}
				if(inCorner){
					curState = STATES.SIGNALING;
				}
				break;
			case SIGNALING:
				rc.broadcastSignal(4);
				if(signalPeriod>0){
					signalPeriod--;
				}
				else{
					curState = STATES.WAITING;
					signalPeriod = 5;
				}
				break;
			case FLEEING:
				if(poops.length<=1){
					curState = STATES.SEARCHING;
					break;
				}
				MapLocation optimalMove;

				int avgX = 0;
				int avgY = 0;

				for(int i = poops.length-1;i>=0;--i){
					RobotInfo curButt = poops[i];
					if(myLoc.distanceSquaredTo(curButt.location)<curButt.type.attackRadiusSquared){
						optimalMove = myLoc.add(myLoc.directionTo(poops[i].location).opposite());
						avgX+=optimalMove.x;
						avgY+=optimalMove.y;
					}
				}
				avgX/=poops.length;
				avgY/=poops.length;
				Direction optimalDir = myLoc.directionTo(new MapLocation(avgX,avgY));
				if(rc.canMove(optimalDir)&&rc.isCoreReady()){
					rc.move(optimalDir);
				}
				else{
					while(!rc.canMove(optimalDir)){
						optimalDir = optimalDir.rotateLeft();
					}
					if(rc.isCoreReady()){
						rc.move(optimalDir);
					}
				}
				break;
			case WAITING:
				if(rc.getHealth()<RobotType.ARCHON.maxHealth/2){
					curState = STATES.FLEEING;
				}
				break;
			}
			RobotInfo[] localNeutrs = rc.senseNearbyRobots(2, Team.NEUTRAL);
			if(localNeutrs.length>0){
				if(rc.isCoreReady()){
					rc.activate(localNeutrs[0].location);
				}
			}
			Direction dir = directions[rand.nextInt(8)];
			RobotType typeToBuild = types[curIndex];
			if(curIndex<types.length-1){
				curIndex++;
			}
			else{
				curIndex = 0;
			}
			RobotInfo[] peeps = rc.senseNearbyRobots(2,rc.getTeam());
			boolean hasTurret = false;
			RobotInfo weakestPeep;
			Double minHealth = 100000.0;
			if(peeps.length>0){
				weakestPeep = peeps[0];
				for(int i = peeps.length-1;i>=0;--i){
					RobotInfo curPeep = peeps[i];
					if(curPeep.type==RobotType.TTM||curPeep.type==RobotType.TURRET){
						hasTurret = true;
					}
					if(curPeep.type==RobotType.ARCHON){
						continue;
					}
					if(curPeep.health<=minHealth){
						minHealth = curPeep.health;
						weakestPeep = curPeep;
					}

				}
				if(rc.isCoreReady()){
					if(weakestPeep != null&&weakestPeep.type!=RobotType.ARCHON){
						rc.repair(weakestPeep.location);
					}
				}
			}
			if(!hasTurret && rc.getRoundNum()>10&&curState!=STATES.FLEEING){
				typeToBuild = RobotType.TURRET;
			}
			if(rc.senseHostileRobots(myLoc, 13).length>3){
				typeToBuild = RobotType.GUARD;
			}
			if(rc.isCoreReady()){
				if(peeps.length<=6 || rc.getTeamParts()>100){
					if(rc.hasBuildRequirements(typeToBuild)){
						int timesThrough = 0;
						while(!rc.canBuild(dir, typeToBuild)){
							if(timesThrough==8){
								break;
							}
							dir = dir.rotateRight();
							timesThrough++;
						}
						if(timesThrough<8){
							rc.build(dir, typeToBuild);
						}
					}
					else{
						if(rc.hasBuildRequirements(RobotType.GUARD)){
							int timesThrough = 0;
							while(!rc.canBuild(dir, typeToBuild)){
								if(timesThrough==8){
									break;
								}
								dir = dir.rotateRight();
								timesThrough++;
							}
							if(timesThrough<8){
								rc.build(dir, typeToBuild);
							}
						}
					}
				}
			}
		}
	}
	private static void runViper() throws GameActionException{
	}
	private static void runTurret() throws GameActionException{
		if(rc.isCoreReady()){
			MapLocation myLoc = rc.getLocation();
			Signal curSignal = rc.readSignal();
			MapLocation signalLoc;
			if(curSignal!=null){
				if(curSignal.getID()==creatorID){
					signalLoc = curSignal.getLocation();
					creatorLoc = signalLoc;
					if(myLoc.distanceSquaredTo(signalLoc)>=2&&rc.getType()!=RobotType.TTM){
						rc.pack();
					}
				}
			}
			if(myLoc.distanceSquaredTo(creatorLoc)>=2&&rc.getType()!=RobotType.TTM){
				rc.pack();
			}
			switch(rc.getType()){
			case TURRET:
				RobotInfo[] enemies = rc.senseHostileRobots(rc.getLocation(),rc.getType().attackRadiusSquared);
				if(enemies.length>0){
					if(rc.isWeaponReady()){
						RobotInfo weakestEnemy = enemies[0];
						boolean foundTurret = false;
						boolean foundArchon = false;
						boolean foundBigZombie = false;
						for(RobotInfo a:enemies){
							if(foundBigZombie){
								if(a.type==RobotType.BIGZOMBIE && a.health<weakestEnemy.health){
									weakestEnemy = a;
								}
							}
							else if(foundTurret){
								if(a.type==RobotType.BIGZOMBIE){
									foundBigZombie = true;
									weakestEnemy = a;
								}
								else if(a.type==RobotType.TURRET && a.health<weakestEnemy.health){
									weakestEnemy = a;
								}

							}
							else if(foundArchon){
								if(a.type==RobotType.BIGZOMBIE){
									foundBigZombie = true;
									weakestEnemy = a;
								}
								else if(a.type==RobotType.TURRET){
									foundTurret = true;
									weakestEnemy = a;
								}
								else if(a.type == RobotType.ARCHON && a.health<weakestEnemy.health){
									weakestEnemy = a;
								}
							}
							else{
								if(a.type == RobotType.BIGZOMBIE){
									foundBigZombie = true;
									weakestEnemy = a;
								}
								else if(a.type == RobotType.TURRET){
									foundTurret = true;
									weakestEnemy = a;
								}
								else if(a.type == RobotType.ARCHON){
									foundArchon = true;
									weakestEnemy = a;
								}
								else if(a.health<weakestEnemy.health){
									weakestEnemy = a;
								}
							}
						}
						if(rc.canAttackLocation(weakestEnemy.location)){
							rc.attackLocation(weakestEnemy.location);
						}
					}
				}
				break;
			case TTM:
				if(myLoc.distanceSquaredTo(creatorLoc)<=2){
					rc.unpack();
				}
				else{
					Direction goalDir = myLoc.directionTo(creatorLoc);
					while(!rc.canMove(goalDir)){
						goalDir = goalDir.rotateRight();
					}
					if(rc.isCoreReady()){
						rc.move(goalDir);
					}
				}
				break;

			}


		}
	}
	private static void runGuard() throws GameActionException{
		MapLocation myLoc = rc.getLocation();
		if(rc.isCoreReady()){

			Direction goalDir = myLoc.directionTo(creatorLoc);

			RobotInfo[] enemies = rc.senseHostileRobots(rc.getLocation(),rc.getType().attackRadiusSquared);
			if(enemies.length>0){
				if(rc.isWeaponReady()){
					RobotInfo weakestEnemy = enemies[0];
					boolean foundTurret = false;
					boolean foundArchon = false;
					boolean foundBigZombie = false;
					for(RobotInfo a:enemies){
						if(foundBigZombie){
							if(a.type==RobotType.BIGZOMBIE && a.health<weakestEnemy.health){
								weakestEnemy = a;
							}
						}
						else if(foundTurret){
							if(a.type==RobotType.BIGZOMBIE){
								foundBigZombie = true;
								weakestEnemy = a;
							}
							else if(a.type==RobotType.TURRET && a.health<weakestEnemy.health){
								weakestEnemy = a;
							}

						}
						else if(foundArchon){
							if(a.type==RobotType.BIGZOMBIE){
								foundBigZombie = true;
								weakestEnemy = a;
							}
							else if(a.type==RobotType.TURRET){
								foundTurret = true;
								weakestEnemy = a;
							}
							else if(a.type == RobotType.ARCHON && a.health<weakestEnemy.health){
								weakestEnemy = a;
							}
						}
						else{
							if(a.type == RobotType.BIGZOMBIE){
								foundBigZombie = true;
								weakestEnemy = a;
							}
							else if(a.type == RobotType.TURRET){
								foundTurret = true;
								weakestEnemy = a;
							}
							else if(a.type == RobotType.ARCHON){
								foundArchon = true;
								weakestEnemy = a;
							}
							else if(a.health<weakestEnemy.health){
								weakestEnemy = a;
							}
						}
					}
					if(rc.canAttackLocation(weakestEnemy.location)){
						rc.attackLocation(weakestEnemy.location);
					}
				}
			}
			else{
				Signal curSignal = rc.readSignal();
				if(curSignal!=null){
					if(curSignal.getID()==creatorID){
						creatorLoc = curSignal.getLocation();
					}
					if(curSignal.getTeam()==rc.getTeam()){
						int[] message = curSignal.getMessage();
						if(message!=null){
							MapLocation targLoc = new MapLocation(message[0],message[1]);
							if(myLoc.distanceSquaredTo(targLoc)<20){
								goalDir = myLoc.directionTo(targLoc);
							}
						}
					}
				}
				RobotInfo[] localFriends = rc.senseNearbyRobots(1, rc.getTeam());
				for(RobotInfo lf:localFriends){
					if(lf.type==RobotType.GUARD)
						if(lf.location.isAdjacentTo(myLoc)){
							goalDir = myLoc.directionTo(lf.location).opposite().rotateLeft();
							//							break;
						}
					if(lf.type==RobotType.ARCHON){
						goalDir = myLoc.directionTo(lf.location).opposite();
					}
				}
				if(goalDir == null){
					goalDir = directions[rand.nextInt(8)];
				}
				double rb = rc.senseRubble(myLoc.add(goalDir));
				if(rb>0 && rb<200){
					if(goalDir!=Direction.NONE&&goalDir!=Direction.OMNI){
						rc.clearRubble(goalDir);
					}
				}
				if(rc.isCoreReady()){
					while(!rc.canMove(goalDir)){
						goalDir = goalDir.rotateLeft();
					}
					rc.move(goalDir);
				}
			}
		}

	}
	private static void runSoldier() throws GameActionException{
		MapLocation myLoc = rc.getLocation();
		if(rc.isCoreReady()){
			Signal curSignal = rc.readSignal();
			Direction goalDir;
			if(curSignal!=null){
				if(curSignal.getID()==creatorID){
					creatorLoc = curSignal.getLocation();
				}
				if(curSignal.getTeam()==rc.getTeam()){
					int[] msg = curSignal.getMessage();
					if(msg!=null){
						usefulLocs[0] = new MapLocation(msg[0],msg[1]);
						goalDir = myLoc.directionTo(usefulLocs[0]);
					}
					else{
						goalDir = directions[rand.nextInt(8)];
					}
				}
				else{
					usefulLocs[0] = curSignal.getLocation();
					goalDir = myLoc.directionTo(usefulLocs[0]);
				}
			}
			else if(usefulLocs[0]!=null){
				goalDir = myLoc.directionTo(usefulLocs[0]);
			}
			else{
				goalDir = myLoc.directionTo(creatorLoc);
				if(goalDir == null){
					goalDir = directions[rand.nextInt(8)];
				}
			}
			if(goalDir == null){
				goalDir = directions[rand.nextInt(8)];
			}
			RobotInfo[] enemies = rc.senseHostileRobots(rc.getLocation(),rc.getType().attackRadiusSquared);
			if(enemies.length>0){
				if(rc.isWeaponReady()){
					RobotInfo weakestEnemy = enemies[0];
					boolean foundTurret = false;
					boolean foundArchon = false;
					boolean foundBigZombie = false;
					for(RobotInfo a:enemies){
						if(foundBigZombie){
							if(a.type==RobotType.BIGZOMBIE && a.health<weakestEnemy.health){
								weakestEnemy = a;
							}
						}
						else if(foundTurret){
							if(a.type==RobotType.BIGZOMBIE){
								foundBigZombie = true;
								weakestEnemy = a;
							}
							else if(a.type==RobotType.TURRET && a.health<weakestEnemy.health){
								weakestEnemy = a;
							}

						}
						else if(foundArchon){
							if(a.type==RobotType.BIGZOMBIE){
								foundBigZombie = true;
								weakestEnemy = a;
							}
							else if(a.type==RobotType.TURRET){
								foundTurret = true;
								weakestEnemy = a;
							}
							else if(a.type == RobotType.ARCHON && a.health<weakestEnemy.health){
								weakestEnemy = a;
							}
						}
						else{
							if(a.type == RobotType.BIGZOMBIE){
								foundBigZombie = true;
								weakestEnemy = a;
							}
							else if(a.type == RobotType.TURRET){
								foundTurret = true;
								weakestEnemy = a;
							}
							else if(a.type == RobotType.ARCHON){
								foundArchon = true;
								weakestEnemy = a;
							}
							else if(a.health<weakestEnemy.health){
								weakestEnemy = a;
							}
						}
					}
					if(rc.canAttackLocation(weakestEnemy.location)){
						rc.attackLocation(weakestEnemy.location);
					}
				}
			}
			else{
				double rb = rc.senseRubble(myLoc.add(goalDir));
				if(rb>0 && rb<200){
					rc.clearRubble(goalDir);
				}
				if(rc.isCoreReady()){
					while(!rc.canMove(goalDir)){
						goalDir = goalDir.rotateLeft();
					}
					rc.move(goalDir);
				}

			}
		}

	}
	private static void runScout() throws GameActionException{
		MapLocation myLoc = rc.getLocation();
		switch(curState){
		case INIT:
			curState = STATES.SEARCHING;
		case SEARCHING:
			RobotInfo[] nearEnemies = rc.senseHostileRobots(myLoc,-1);
			boolean foundEnemy = false;
			for(RobotInfo a:nearEnemies){
				switch(a.type){
				case ARCHON:
					usefulLocs[0] = a.location;
					eArchonID = a.ID;
					curState = STATES.SIGNALING;
					foundEnemy = true;
					break;
				case ZOMBIEDEN:
					usefulLocs[0] = a.location;
					curState = STATES.LURING;
					foundEnemy = true;
					break;
				}	
			}
			if(rc.isCoreReady()){
				if(!foundEnemy){
					Direction targetDir;
					Signal curSignal = rc.readSignal();
					if(curSignal!=null){
						if(curSignal.getTeam()!=myTeam){
							targetDir = myLoc.directionTo(curSignal.getLocation());
						}
						else{
							targetDir = myLoc.directionTo(creatorLoc).opposite();
						}
					}
					else{
						targetDir = myLoc.directionTo(creatorLoc).opposite();
					}
					if(rc.canMove(targetDir)){
						visitedLocs.add(myLoc.add(targetDir));
						rc.move(targetDir);
					}
					else{
						Direction randomDir = directions[rand.nextInt(8)];
						while(visitedLocs.contains(myLoc.add(randomDir))||!rc.canMove(randomDir)){
							randomDir = directions[rand.nextInt(8)];
						}
						visitedLocs.add(myLoc.add(randomDir));
						rc.move(randomDir);
					}
				}
			}

			break;
		case SIGNALING:
		case LURING:
			signalPeriod--;
			
			RobotInfo[] nearScouts = rc.senseNearbyRobots(-1, rc.getTeam());
			for(RobotInfo a:nearScouts){
				switch(a.type){
				case SCOUT:
					curState = STATES.SEARCHING;
					break;
				}	
			}
			rc.broadcastMessageSignal(usefulLocs[0].x, usefulLocs[0].y, Integer.MAX_VALUE);
			RobotInfo[] dangers = rc.senseHostileRobots(myLoc, -1);
			if(signalPeriod<0 && dangers.length>3){
				rc.disintegrate();
			}
			if(dangers!=null && dangers.length>0){
				MapLocation optimalMove;

				int avgX = 0;
				int avgY = 0;

				for(int i = dangers.length-1;i>=0;--i){
					RobotInfo curButt = dangers[i];
					if(myLoc.distanceSquaredTo(curButt.location)<curButt.type.attackRadiusSquared){
						optimalMove = myLoc.add(myLoc.directionTo(dangers[i].location).opposite());
						avgX+=optimalMove.x;
						avgY+=optimalMove.y;
					}
				}
				avgX/=dangers.length;
				avgY/=dangers.length;
				Direction optimalDir = myLoc.directionTo(new MapLocation(avgX,avgY));
				if(rc.canMove(optimalDir)&&rc.isCoreReady()){
					rc.move(optimalDir);
				}
				else{
					while(!rc.canMove(optimalDir)){
						optimalDir = optimalDir.rotateLeft();
					}
					if(rc.isCoreReady()){
						rc.move(optimalDir);
					}
				}

			}

			break;


		}


	}

	static Direction[] directions = {Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST,
			Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};
	static RobotType[] robotTypes = {RobotType.SCOUT, RobotType.SOLDIER, RobotType.GUARD, RobotType.VIPER, RobotType.TURRET};
	static Random rand;
	static int myAttackRange = 0;
	static Team myTeam;
	static Team enemyTeam;
	static RobotController rc;

	static enum STATES {INIT,SEARCHING,SIGNALING,LURING,WAITING,FLEEING};
	static RobotType[] types = {RobotType.SCOUT, RobotType.SOLDIER, RobotType.GUARD, RobotType.GUARD, RobotType.GUARD,RobotType.GUARD,RobotType.TURRET,RobotType.SOLDIER,RobotType.GUARD, RobotType.SOLDIER,RobotType.GUARD,RobotType.GUARD, RobotType.SOLDIER,RobotType.GUARD,RobotType.GUARD};
	static STATES curState;
	static MapLocation[] usefulLocs;
	static RobotInfo creator;
	static MapLocation creatorLoc;
	static int suicideThresh = 10;
	static ArrayList<MapLocation> visitedLocs;
	static int eArchonID;
	static MapLocation[] initArchon;
	static boolean inCorner;
	static int signalPeriod;
	static int creatorID;
	static int curIndex = 0;

	public static void run(RobotController rc) {
		RobotPlayer.rc = rc;
		rand = new Random(rc.getID());
		myTeam = rc.getTeam();
		enemyTeam= myTeam.opponent();
		myAttackRange = rc.getType().attackRadiusSquared;

		curState = STATES.INIT;
		usefulLocs = new MapLocation[5];
		RobotInfo[] initFriends = rc.senseNearbyRobots(3,rc.getTeam());
		visitedLocs = new ArrayList<MapLocation>();
		initArchon = rc.getInitialArchonLocations(enemyTeam);
		inCorner = false;
		signalPeriod = 5;

		for(int i = initFriends.length-1;i>=0;--i){
			RobotInfo curFriend = initFriends[i];
			if(curFriend.type == RobotType.ARCHON){
				creator = curFriend;
				creatorLoc = creator.location;
				creatorID = creator.ID;
			}

		}
		if(creatorLoc == null){
			creatorLoc = rc.getLocation();
		}
		//Insert Any Initialization Methods Here If Needed
		switch(rc.getType()){

		}
		while(true){
			if(rc.getHealth()<=suicideThresh && rc.isInfected()){ //Rather than turn into Zombie, die
				rc.disintegrate();
				continue;
			}
			try{
				switch(rc.getType()){
				case ARCHON:
					runArchon();
					break;
				case SCOUT: 
					runScout();
					break; 
				case SOLDIER: 
					runSoldier(); 
					break; 
				case GUARD:
					runGuard(); 
					break;
				case VIPER:
					runViper();
					break;
				case TURRET:
				case TTM:
					runTurret();
					break;
				}

			}catch (Exception e) {
				// Throwing an uncaught exception makes the robot die, so we need to catch exceptions.
				// Caught exceptions will result in a bytecode penalty.
				System.out.println(e.getMessage());
				e.printStackTrace();
			}
			Clock.yield();
		}

	}

}
