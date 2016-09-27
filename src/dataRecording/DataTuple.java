package dataRecording;

import java.util.ArrayList;

import pacman.game.Constants;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

public class DataTuple {
	
	public Game my_game;
	
	public MOVE DirectionChosen;
	
	//General game state info - not normalized!
	public int mazeIndex;
	public int currentLevel;
	public int pacmanPosition;
	public int pacmanLivesLeft;
	public int currentScore;
	public int totalGameTime;
	public int currentLevelTime;
	public int numOfPillsLeft;
	public int numOfPowerPillsLeft;
	
	//Normalized values & extra values
	public double num_pill_left_norm;
	public double num_pp_left_norm;
	public double closest_ghost_dist;
	public MOVE closest_ghost_dir;
	public double closest_pill_dist;
	public double closest_pp_dist;
	
	//neighbors works like
	//0.25 for for empty
	//0.5 for pill
	//0.75 for power pill
	//1.0 for ghost
	//-1 for wall
	public double n_neighbor;
	public double e_neighbor;
	public double s_neighbor;
	public double w_neighbor;

	
	//We can try to save states only when pacman is in junctions
	//and neural network output would be to select to which next junction to go (nearest 3 - ... n)
	
	//Ghost this, dir, dist, edible - BLINKY, INKY, PINKY, SUE
	public boolean isBlinkyEdible = false;
	public boolean isInkyEdible = false;
	public boolean isPinkyEdible = false;
	public boolean isSueEdible = false;
	
	public int blinkyDist = -1;
	public int inkyDist = -1;
	public int pinkyDist = -1;
	public int sueDist = -1;
	
	public MOVE blinkyDir;
	public MOVE inkyDir;
	public MOVE pinkyDir;
	public MOVE sueDir;
	
	//Util data - useful for normalization
	public int numberOfNodesInLevel;
	public int numberOfTotalPillsInLevel;
	public int numberOfTotalPowerPillsInLevel;
	
	public DataTuple(Game game)
	{
		my_game = game;
		
		this.mazeIndex = game.getMazeIndex();
		this.currentLevel = game.getCurrentLevel();
		this.pacmanPosition = game.getPacmanCurrentNodeIndex();
		this.pacmanLivesLeft = game.getPacmanNumberOfLivesRemaining();
		this.currentScore = game.getScore();
		this.totalGameTime = game.getTotalTime();
		this.currentLevelTime = game.getCurrentLevelTime();
		this.numOfPillsLeft = game.getNumberOfActivePills();
		this.numOfPowerPillsLeft = game.getNumberOfActivePowerPills();
		
		if (game.getGhostLairTime(GHOST.BLINKY) == 0) {
			this.isBlinkyEdible = game.isGhostEdible(GHOST.BLINKY);
			this.blinkyDist = game.getShortestPathDistance(game.getPacmanCurrentNodeIndex(),game.getGhostCurrentNodeIndex(GHOST.BLINKY));
		}
		
		if (game.getGhostLairTime(GHOST.INKY) == 0) {
		this.isInkyEdible = game.isGhostEdible(GHOST.INKY);
		this.inkyDist = game.getShortestPathDistance(game.getPacmanCurrentNodeIndex(),game.getGhostCurrentNodeIndex(GHOST.INKY));
		}
		
		if (game.getGhostLairTime(GHOST.PINKY) == 0) {
		this.isPinkyEdible = game.isGhostEdible(GHOST.PINKY);
		this.pinkyDist = game.getShortestPathDistance(game.getPacmanCurrentNodeIndex(),game.getGhostCurrentNodeIndex(GHOST.PINKY));
		}
		
		if (game.getGhostLairTime(GHOST.SUE) == 0) {
		this.isSueEdible = game.isGhostEdible(GHOST.SUE);
		this.sueDist = game.getShortestPathDistance(game.getPacmanCurrentNodeIndex(),game.getGhostCurrentNodeIndex(GHOST.SUE));
		}
		
		this.blinkyDir = game.getNextMoveTowardsTarget(game.getPacmanCurrentNodeIndex(),game.getGhostCurrentNodeIndex(GHOST.BLINKY), DM.PATH);
		this.inkyDir = game.getNextMoveTowardsTarget(game.getPacmanCurrentNodeIndex(),game.getGhostCurrentNodeIndex(GHOST.INKY), DM.PATH);
		this.pinkyDir = game.getNextMoveTowardsTarget(game.getPacmanCurrentNodeIndex(),game.getGhostCurrentNodeIndex(GHOST.PINKY), DM.PATH);
		this.sueDir = game.getNextMoveTowardsTarget(game.getPacmanCurrentNodeIndex(),game.getGhostCurrentNodeIndex(GHOST.SUE), DM.PATH);
		
		this.numberOfNodesInLevel = game.getNumberOfNodes();
		this.numberOfTotalPillsInLevel = game.getNumberOfPills();
		this.numberOfTotalPowerPillsInLevel = game.getNumberOfPowerPills();
		
		
		//Set my individual values
		this.n_neighbor = SetNeighbor(game.getNeighbour(pacmanPosition, MOVE.UP));
		this.e_neighbor = SetNeighbor(game.getNeighbour(pacmanPosition, MOVE.RIGHT));
		this.s_neighbor = SetNeighbor(game.getNeighbour(pacmanPosition, MOVE.DOWN));
		this.w_neighbor = SetNeighbor(game.getNeighbour(pacmanPosition, MOVE.LEFT));
		
		switch(ClosestGhost())
		{
		case 0:
			closest_ghost_dir = this.blinkyDir;
			closest_ghost_dist = normalizeDistance(this.blinkyDist);
			break;
		case 1:
			closest_ghost_dir = this.inkyDir;
			closest_ghost_dist = normalizeDistance(this.inkyDist);
			break;
		case 2:
			closest_ghost_dir = this.pinkyDir;
			closest_ghost_dist = normalizeDistance(this.pinkyDist);
			break;
		case 3:
			closest_ghost_dir = this.sueDir;
			closest_ghost_dist = normalizeDistance(this.sueDist);
			break;
		default:
			break;
		}
		
		num_pill_left_norm = normalizeNumberOfPills(this.numOfPillsLeft);
		num_pp_left_norm = normalizeNumberOfPowerPills(this.numOfPowerPillsLeft);
		
		//CLOSEST POWER PILL
		int[] activePowerPills = game.getActivePowerPillsIndices();
		if(activePowerPills.length != 0)
		{
			int[] targetNodeIndices = new int[activePowerPills.length];
	
			
			for(int i=0;i<activePowerPills.length;i++)
				targetNodeIndices[i]=activePowerPills[i];		
			
			closest_pp_dist = normalizeDistance(game.getShortestPathDistance(this.pacmanPosition, 
									game.getClosestNodeIndexFromNodeIndex (this.pacmanPosition ,targetNodeIndices,DM.PATH)));
		}
		else
		{
			closest_pp_dist = 0.8;
		}
		
		//CLOSEST NORMAL PILL
		int[] pills=game.getPillIndices();	
		
		ArrayList<Integer> targets=new ArrayList<Integer>();
		
		for(int i=0;i<pills.length;i++)					//check which pills are available			
			if(game.isPillStillAvailable(i))
				targets.add(pills[i]);	
		
		int[] targetsArray=new int[targets.size()];		//convert from ArrayList to array
		
		for(int i=0;i<targetsArray.length;i++)
			targetsArray[i]=targets.get(i);
		
		closest_pill_dist = normalizeDistance(game.getShortestPathDistance(this.pacmanPosition, 
				game.getClosestNodeIndexFromNodeIndex (this.pacmanPosition ,targetsArray,DM.PATH)));

	}
	
	public DataTuple(Game game, MOVE move)
	{
		my_game = game;
		
		if(move == MOVE.NEUTRAL)
		{
			move = game.getPacmanLastMoveMade();
		}
		
		this.DirectionChosen = move;
		
		this.mazeIndex = game.getMazeIndex();
		this.currentLevel = game.getCurrentLevel();
		this.pacmanPosition = game.getPacmanCurrentNodeIndex();
		this.pacmanLivesLeft = game.getPacmanNumberOfLivesRemaining();
		this.currentScore = game.getScore();
		this.totalGameTime = game.getTotalTime();
		this.currentLevelTime = game.getCurrentLevelTime();
		this.numOfPillsLeft = game.getNumberOfActivePills();
		this.numOfPowerPillsLeft = game.getNumberOfActivePowerPills();
		
		if (game.getGhostLairTime(GHOST.BLINKY) == 0) {
			this.isBlinkyEdible = game.isGhostEdible(GHOST.BLINKY);
			this.blinkyDist = game.getShortestPathDistance(game.getPacmanCurrentNodeIndex(),game.getGhostCurrentNodeIndex(GHOST.BLINKY));
		}
		
		if (game.getGhostLairTime(GHOST.INKY) == 0) {
		this.isInkyEdible = game.isGhostEdible(GHOST.INKY);
		this.inkyDist = game.getShortestPathDistance(game.getPacmanCurrentNodeIndex(),game.getGhostCurrentNodeIndex(GHOST.INKY));
		}
		
		if (game.getGhostLairTime(GHOST.PINKY) == 0) {
		this.isPinkyEdible = game.isGhostEdible(GHOST.PINKY);
		this.pinkyDist = game.getShortestPathDistance(game.getPacmanCurrentNodeIndex(),game.getGhostCurrentNodeIndex(GHOST.PINKY));
		}
		
		if (game.getGhostLairTime(GHOST.SUE) == 0) {
		this.isSueEdible = game.isGhostEdible(GHOST.SUE);
		this.sueDist = game.getShortestPathDistance(game.getPacmanCurrentNodeIndex(),game.getGhostCurrentNodeIndex(GHOST.SUE));
		}
		
		this.blinkyDir = game.getNextMoveTowardsTarget(game.getPacmanCurrentNodeIndex(),game.getGhostCurrentNodeIndex(GHOST.BLINKY), DM.PATH);
		this.inkyDir = game.getNextMoveTowardsTarget(game.getPacmanCurrentNodeIndex(),game.getGhostCurrentNodeIndex(GHOST.INKY), DM.PATH);
		this.pinkyDir = game.getNextMoveTowardsTarget(game.getPacmanCurrentNodeIndex(),game.getGhostCurrentNodeIndex(GHOST.PINKY), DM.PATH);
		this.sueDir = game.getNextMoveTowardsTarget(game.getPacmanCurrentNodeIndex(),game.getGhostCurrentNodeIndex(GHOST.SUE), DM.PATH);
		
		this.numberOfNodesInLevel = game.getNumberOfNodes();
		this.numberOfTotalPillsInLevel = game.getNumberOfPills();
		this.numberOfTotalPowerPillsInLevel = game.getNumberOfPowerPills();
		
		
		//Set my individual values
		this.n_neighbor = SetNeighbor(game.getNeighbour(pacmanPosition, MOVE.UP));
		this.e_neighbor = SetNeighbor(game.getNeighbour(pacmanPosition, MOVE.RIGHT));
		this.s_neighbor = SetNeighbor(game.getNeighbour(pacmanPosition, MOVE.DOWN));
		this.w_neighbor = SetNeighbor(game.getNeighbour(pacmanPosition, MOVE.LEFT));
		
		switch(ClosestGhost())
		{
		case 0:
			closest_ghost_dir = this.blinkyDir;
			closest_ghost_dist = normalizeDistance(this.blinkyDist);
			break;
		case 1:
			closest_ghost_dir = this.inkyDir;
			closest_ghost_dist = normalizeDistance(this.inkyDist);
			break;
		case 2:
			closest_ghost_dir = this.pinkyDir;
			closest_ghost_dist = normalizeDistance(this.pinkyDist);
			break;
		case 3:
			closest_ghost_dir = this.sueDir;
			closest_ghost_dist = normalizeDistance(this.sueDist);
			break;
		default:
			break;
		}
		
		num_pill_left_norm = normalizeNumberOfPills(this.numOfPillsLeft);
		num_pp_left_norm = normalizeNumberOfPowerPills(this.numOfPowerPillsLeft);
		
		//CLOSEST POWER PILL
		int[] activePowerPills = game.getActivePowerPillsIndices();
		if(activePowerPills.length != 0)
		{
			int[] targetNodeIndices = new int[activePowerPills.length];
	
			
			for(int i=0;i<activePowerPills.length;i++)
				targetNodeIndices[i]=activePowerPills[i];		
			
			closest_pp_dist = normalizeDistance(game.getShortestPathDistance(this.pacmanPosition, 
									game.getClosestNodeIndexFromNodeIndex (this.pacmanPosition ,targetNodeIndices,DM.PATH)));
		}
		else
		{
			closest_pp_dist = 0.8;
		}
		
		//CLOSEST NORMAL PILL
		int[] pills=game.getPillIndices();	
		
		ArrayList<Integer> targets=new ArrayList<Integer>();
		
		for(int i=0;i<pills.length;i++)					//check which pills are available			
			if(game.isPillStillAvailable(i))
				targets.add(pills[i]);	
		
		int[] targetsArray=new int[targets.size()];		//convert from ArrayList to array
		
		for(int i=0;i<targetsArray.length;i++)
			targetsArray[i]=targets.get(i);
		
		closest_pill_dist = normalizeDistance(game.getShortestPathDistance(this.pacmanPosition, 
				game.getClosestNodeIndexFromNodeIndex (this.pacmanPosition ,targetsArray,DM.PATH)));

	}
	
	public int ClosestGhost()
	{
		int min_dist = Integer.MAX_VALUE;
		int selected_one = 0;
		//0 = blinky
		//1 = inky
		//2 = pinky
		//3 = sue
		
		if(this.blinkyDist < min_dist)
		{
			selected_one = 0;
			min_dist = this.blinkyDist;
		}
		if(this.inkyDist < min_dist)
		{
			selected_one = 1;
			min_dist = this.inkyDist;
		}
		if(this.pinkyDist < min_dist)
		{
			selected_one = 2;
			min_dist = this.pinkyDist;
		}
		if(this.sueDist < min_dist)
		{
			selected_one = 3;
			min_dist = this.sueDist;
		}
		
		return selected_one;
	}
	
	public double SetNeighbor(int index)
	{
		double result = 0.33;
		
		//Check if wall
		if(index == -1)
			return -1.0;
		
		//check if pill or power pill or ghost
		if(my_game.getPillIndex(index) != -1 &&
				my_game.isPillStillAvailable(my_game.getPillIndex(index)))
			return 0.66;
		else if(my_game.getPowerPillIndex(index) != -1 &&
				my_game.isPowerPillStillAvailable(my_game.getPowerPillIndex(index)))
			return 0.99;
		else if(my_game.getGhostCurrentNodeIndex(GHOST.BLINKY) == index ||
				my_game.getGhostCurrentNodeIndex(GHOST.INKY) == index ||
				my_game.getGhostCurrentNodeIndex(GHOST.PINKY) == index ||
				my_game.getGhostCurrentNodeIndex(GHOST.SUE) == index)
			return 0.0;
		
		//theres nothing there, return empty space
		return result;
	}
	
	public DataTuple(String data)
	{
		String[] dataSplit = data.split(";");
		
		this.DirectionChosen = MOVE.valueOf(dataSplit[0]); //class
		
		this.mazeIndex = Integer.parseInt(dataSplit[1]);
		this.currentLevel = Integer.parseInt(dataSplit[2]);
		this.pacmanPosition = Integer.parseInt(dataSplit[3]);
		this.pacmanLivesLeft = Integer.parseInt(dataSplit[4]);
		this.currentScore = Integer.parseInt(dataSplit[5]);
		this.totalGameTime = Integer.parseInt(dataSplit[6]);
		this.currentLevelTime = Integer.parseInt(dataSplit[7]);
		this.numOfPillsLeft = Integer.parseInt(dataSplit[8]);
		this.numOfPowerPillsLeft = Integer.parseInt(dataSplit[9]);
		this.isBlinkyEdible = Boolean.parseBoolean(dataSplit[10]);
		this.isInkyEdible = Boolean.parseBoolean(dataSplit[11]);
		this.isPinkyEdible = Boolean.parseBoolean(dataSplit[12]);
		this.isSueEdible = Boolean.parseBoolean(dataSplit[13]);
		this.blinkyDist = Integer.parseInt(dataSplit[14]);
		this.inkyDist = Integer.parseInt(dataSplit[15]);
		this.pinkyDist = Integer.parseInt(dataSplit[16]);
		this.sueDist = Integer.parseInt(dataSplit[17]);
		this.blinkyDir = MOVE.valueOf(dataSplit[18]);
		this.inkyDir = MOVE.valueOf(dataSplit[19]);
		this.pinkyDir = MOVE.valueOf(dataSplit[20]);
		this.sueDir = MOVE.valueOf(dataSplit[21]);
		this.numberOfNodesInLevel = Integer.parseInt(dataSplit[22]);
		this.numberOfTotalPillsInLevel = Integer.parseInt(dataSplit[23]);
		this.numberOfTotalPowerPillsInLevel = Integer.parseInt(dataSplit[24]);
		//my extra values
		this.n_neighbor = Double.parseDouble(dataSplit[25]);
		this.e_neighbor = Double.parseDouble(dataSplit[26]);
		this.s_neighbor = Double.parseDouble(dataSplit[27]);
		this.w_neighbor = Double.parseDouble(dataSplit[28]);
		this.num_pill_left_norm = Double.valueOf(dataSplit[29]);
		this.num_pp_left_norm = Double.valueOf(dataSplit[30]);
		this.closest_ghost_dist = Double.valueOf(dataSplit[31]);
		this.closest_ghost_dir = MOVE.valueOf(dataSplit[32]);
		this.closest_pill_dist = Double.valueOf(dataSplit[33]);
		this.closest_pp_dist = Double.valueOf(dataSplit[34]);
	}
	
	public String getSaveString()
	{
		StringBuilder stringbuilder = new StringBuilder();
		
		stringbuilder.append(this.DirectionChosen+";");
		stringbuilder.append(this.mazeIndex+";");
		stringbuilder.append(this.currentLevel+";");
		stringbuilder.append(this.pacmanPosition+";");
		stringbuilder.append(this.pacmanLivesLeft+";");
		stringbuilder.append(this.currentScore+";");
		stringbuilder.append(this.totalGameTime+";");
		stringbuilder.append(this.currentLevelTime+";");
		stringbuilder.append(this.numOfPillsLeft+";");
		stringbuilder.append(this.numOfPowerPillsLeft+";");
		stringbuilder.append(this.isBlinkyEdible+";");
		stringbuilder.append(this.isInkyEdible+";");
		stringbuilder.append(this.isPinkyEdible+";");
		stringbuilder.append(this.isSueEdible+";");
		stringbuilder.append(this.blinkyDist+";");
		stringbuilder.append(this.inkyDist+";");
		stringbuilder.append(this.pinkyDist+";");
		stringbuilder.append(this.sueDist+";");
		stringbuilder.append(this.blinkyDir+";");
		stringbuilder.append(this.inkyDir+";");
		stringbuilder.append(this.pinkyDir+";");
		stringbuilder.append(this.sueDir+";");
		stringbuilder.append(this.numberOfNodesInLevel+";");
		stringbuilder.append(this.numberOfTotalPillsInLevel+";");
		stringbuilder.append(this.numberOfTotalPowerPillsInLevel+";");
		stringbuilder.append(this.n_neighbor+";");
		stringbuilder.append(this.e_neighbor+";");
		stringbuilder.append(this.s_neighbor+";");
		stringbuilder.append(this.w_neighbor+";");
		stringbuilder.append(this.num_pill_left_norm +";");
		stringbuilder.append(this.num_pp_left_norm +";");
		stringbuilder.append(this.closest_ghost_dist+";");
		stringbuilder.append(this.closest_ghost_dir+";");
		stringbuilder.append(this.closest_pill_dist+";");
		stringbuilder.append(this.closest_pp_dist+";");
		return stringbuilder.toString();
	}

	/**
	 * Used to normalize distances. Done via min-max normalization.
	 * Assumes that minimum possible distance is 0. Assumes that
	 * the maximum possible distance is the total number of nodes in
	 * the current level.
	 * @param dist Distance to be normalized
	 * @return Normalized distance
	 */
	public double normalizeDistance(int dist)
	{
		return (double)((double)(dist-0)/(double)(this.numberOfNodesInLevel-0));
	}
	
	public double normalizeLevel(int level)
	{
		return (double)((double)(level-0)/(double)(Constants.NUM_MAZES-0));
	}
	
	public double normalizePosition(int position)
	{
		return (double)((double)(position-0)/(double)(this.numberOfNodesInLevel-0));
	}
	
	public double normalizeBoolean(boolean bool)
	{
		if(bool)
		{
			return 1.0;
		}
		else
		{
			return 0.0;
		}
	}
	
	public double normalizeNumberOfPills(int numOfPills)
	{
		return (double)((double)(numOfPills-0)/(double)(this.numberOfTotalPillsInLevel-0));
	}
	
	public double normalizeNumberOfPowerPills(int numOfPowerPills)
	{
		return (double)((double)(numOfPowerPills-0)/(double)(this.numberOfTotalPowerPillsInLevel-0));
	}
	
	public double normalizeTotalGameTime(int time)
	{
		return (double)((double)(time-0)/(double)(Constants.MAX_TIME-0));
	}
	
	public double normalizeCurrentLevelTime(int time)
	{
		return (double)((double)(time-0)/(double)(Constants.LEVEL_LIMIT-0));
	}
	
	/**
	 * 
	 * Max score value lifted from highest ranking PacMan controller on PacMan vs Ghosts
	 * website: http://pacman-vs-ghosts.net/controllers/1104
	 * @param score
	 * @return
	 */
	public double normalizeCurrentScore(int score)
	{
		return (double)((double)(score-0)/(double)(82180-0));
	}

	public String ToString() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
