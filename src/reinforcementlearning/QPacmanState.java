package reinforcementlearning;

import java.util.ArrayList;

import dataRecording.DataTuple;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

public class QPacmanState{

	String s_value = "";
	
	Game my_game;
	MOVE DirectionChosen;
	
	public int pacmanPosition;
	
	//Normalized values & extra values
	public int numOfPillsLeft;
	public int numOfPowerPillsLeft;
	public int closest_pill_dist;
	public int closest_pill_index;
	public int closest_pp_dist;
	public int closest_pp_index;
	public MOVE closest_pill_dir;
	public MOVE closest_pp_dir;
	
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
	
	public QPacmanState(Game game, MOVE move) {
		
		my_game = game;
		
		if(move == MOVE.NEUTRAL)
		{
			move = game.getPacmanLastMoveMade();
		}
		
		this.DirectionChosen = move;
		this.pacmanPosition = game.getPacmanCurrentNodeIndex();
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
		
		//CLOSEST POWER PILL
		int[] activePowerPills = game.getActivePowerPillsIndices();
		if(activePowerPills.length != 0)
		{
			int[] targetNodeIndices = new int[activePowerPills.length];
	
			
			for(int i=0;i<activePowerPills.length;i++)
				targetNodeIndices[i]=activePowerPills[i];		
			
			closest_pp_index = game.getClosestNodeIndexFromNodeIndex (this.pacmanPosition ,targetNodeIndices,DM.PATH);
			closest_pp_dist = game.getShortestPathDistance(this.pacmanPosition, this.closest_pp_index);
			closest_pp_dir = game.getNextMoveTowardsTarget(this.pacmanPosition, this.closest_pp_index, DM.PATH);				
		}
		else
		{
			closest_pp_dist = -1;
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
		
		this.closest_pill_index = game.getClosestNodeIndexFromNodeIndex (this.pacmanPosition ,targetsArray,DM.PATH);
		this.closest_pill_dist = game.getShortestPathDistance(this.pacmanPosition, this.closest_pill_index);
		this.closest_pill_dir = game.getNextMoveTowardsTarget(this.pacmanPosition, this.closest_pill_index, DM.PATH);

	}
	
	
	//TODO: fix
	public QPacmanState(String data)
	{
		String[] dataSplit = data.split(";");
		
		this.DirectionChosen = MOVE.valueOf(dataSplit[0]); //class
		
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
		this.closest_pill_dist = Integer.parseInt(dataSplit[33]);
		this.closest_pp_dist = Integer.parseInt(dataSplit[34]);
	}
	
	public QPacmanState(QPacmanState clone)
	{
		this(clone.my_game, clone.DirectionChosen);
	}
	
	public int[] ClosestKJunctions(Game game, int k, int previous_index)
	{
		int[] closest_juncs = new int[k];
		int[] inters = game.GetPillIntersection();
		int[] juncs = game.GetJunctionPills();
		
//		if(inters.length < 5)
//		{
//			inters = game.GetIntersections();
//		}
		
		int[] mix = new int[inters.length + juncs.length];
		
		if(mix.length < 5)
		{
			mix = game.getJunctionIndices();
		}
		else
		{
		
			int i = 0;
			for(i = 0; i < inters.length; i++)
			{
				mix[i] = inters[i];
			}
			for(int j = 0; j < juncs.length; j++, i++)
			{
				mix[i] = juncs[j];
			}
		}
		
		
		ArrayList<Integer> targetjuncs=new ArrayList<Integer>();
		int pacm = game.getPacmanCurrentNodeIndex();

		
		//System.out.println("PACMAN POS: " + pacm + "; PREVIOUS PACMAN POS: " + previous_index);
		for(int j = -1; j < k; j++)
		{
			int min_dist = Integer.MAX_VALUE;
			int selected_index = -1;
			for(int index : mix)
			{
				int dist = game.getShortestPathDistance(pacm, index);
				if(dist < min_dist && 
					!targetjuncs.contains(index) && 
					index != pacm &&
					previous_index != index)
				{
					min_dist = dist;
					selected_index = index;
				}
			}
			targetjuncs.add(selected_index);
			if(j >= 0)
				closest_juncs[j] = selected_index;
		}
		
		return closest_juncs;
	}
	
	@Override
	public String toString()
	{
		if(s_value.equals(""))
		{
			/*
			 s_value += 
//					 	this.pacmanPosition + " " +
//						this.numOfPillsLeft + " " +
//						this.numOfPowerPillsLeft + " " +
						this.blinkyDist + " " +
						this.inkyDist + " " +
						this.pinkyDist + " " +
						this.sueDist + " " +
//						this.blinkyDir + " " +
//						this.pinkyDir + " " +
//						this.inkyDir + " " +
//						this.sueDir + " " +
//						this.closest_pill_dir + " " +
//						this.closest_pp_dir + " " +
						this.closest_pill_dist + " " +
						this.closest_pp_dist + " " +
						this.isBlinkyEdible + " " +
						this.isInkyEdible + " " +
						this.isPinkyEdible + " " +
						this.isSueEdible;
			
			*/
			
			
			 s_value += 
//					 	this.pacmanPosition + " " +
//						this.numOfPillsLeft + " " +
//						this.numOfPowerPillsLeft + " " +
						this.blinkyDist + " " +
						this.inkyDist + " " +
						this.pinkyDist + " " +
						this.sueDist + " " +
//						this.blinkyDir + " " +
//						this.pinkyDir + " " +
//						this.inkyDir + " " +
//						this.sueDir + " " +
						this.closest_pill_dir + " " +
						this.closest_pp_dir + " " +
						this.closest_pill_dist + " " +
						this.closest_pp_dist + " " ;
//						this.isBlinkyEdible + " " +
//						this.isInkyEdible + " " +
//						this.isPinkyEdible + " " +
//						this.isSueEdible;
			 			
			return s_value;
		}
		else
		{
			return s_value;
		}
		
	}
	
	public boolean compareTo(QPacmanState other)
	{
		/*
		if(	this.pacmanPosition == other.pacmanPosition &&
			this.numOfPillsLeft == other.numOfPillsLeft &&

			this.numOfPowerPillsLeft == other.numOfPowerPillsLeft &&
			this.blinkyDist == other.blinkyDist &&
			this.inkyDist == other.inkyDist &&
			this.pinkyDist == other.pinkyDist &&
			this.sueDist == other.sueDist &&
			this.isBlinkyEdible == other.isBlinkyEdible &&
			this.isInkyEdible == other.isInkyEdible &&
			this.isPinkyEdible == other.isPinkyEdible &&
			this.isSueEdible == other.isSueEdible
				)
		{
			return true;
		}
		else
		{
			return false;
		}*/
		
		return this.toString().equals(other.toString());
	}
}
