package dataRecording;

import java.util.ArrayList;
import java.util.HashMap;

import pacman.game.Game;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;

public class HelperExtendedGame 
{
	//intersections in the game
	int[] 					intersections 			= null;
	HashMap<MOVE, int[]> 	correlated_path_to_move = new HashMap<MOVE, int[]>(); //path selected when moving through junctions
	public int 				maze_index 				= -1;
	Game 					current_state;
	
	public HelperExtendedGame()
	{
		
	}
	
	/**
	 * Set the current state of the game and check if agent is still in the same level
	 * aalways do this first than anything else
	 * @param current
	 */
	public void SetState(Game current)
	{
		current_state = current;
		if(maze_index != current.getMazeIndex())
		{
			maze_index = current.getMazeIndex();
			RecalculateIntersections();
		}
	}
	
	/**
	 * path movement between junctions of a selected move
	 * NextJunctionTowardMovement needs to be called first to find the path
	 * @param move
	 * @return
	 */
	public int[] GetPathFromMove(MOVE move)
	{
		if(correlated_path_to_move.containsKey(move))
		{
			return correlated_path_to_move.get(move);
		}
		else
		{
			return null;
		}
	}
	
	/**
	 * Recalculate all the intersections of the maze by testing all nodes in the maze
	 */
	public void RecalculateIntersections()
	{
		int[] intersections = null;
		ArrayList<Integer> inters = new ArrayList<Integer>();
		
		int max_nodes = current_state.getNumberOfNodes();
		
		for(int i = 0; i < max_nodes; i++)
		{
			int neighbor_n = current_state.getNeighbour(i, MOVE.UP);
			int neighbor_e = current_state.getNeighbour(i, MOVE.RIGHT);
			int neighbor_s = current_state.getNeighbour(i, MOVE.DOWN);
			int neighbor_w = current_state.getNeighbour(i, MOVE.LEFT);
		
			//We have a possible intersection
			if(neighbor_n == -1 && (neighbor_e == -1 || neighbor_w == -1)) //wall
			{
				inters.add(i);
			}
			else if(neighbor_s == -1 && (neighbor_e == -1 || neighbor_w == -1))
			{
				inters.add(i);
			}
		}
		
		intersections = new int[inters.size()];
		
		for(int i=0;i<intersections.length;i++)
			intersections[i]=inters.get(i);
		
		this.intersections = intersections;
		
	}
	
	/**
	 * Get all the junctions that contains pills
	 * @return
	 */
	public int[] GetJunctionPills()
	{
		int[] result;
		int[] juncs = current_state.getJunctionIndices();
		ArrayList<Integer> pills = new ArrayList<Integer>();
		
		for(int index : juncs)
		{
			int pill = current_state.getPillIndex(index);
			if(pill != -1)
			{
				if(current_state.isPillStillAvailable(pill))
				{
					pills.add(index);
					continue;
				}
			}
			pill = current_state.getPowerPillIndex(index);
			if(pill != -1)
			{
				if(current_state.isPowerPillStillAvailable(pill))
				{
					pills.add(index);
					continue;
				}
			}
		}
		
		result = new int[pills.size()];
		
		for(int i=0;i<result.length;i++)
			result[i]=pills.get(i);
		
		return result;
	}
	
	/**
	 * Get all the intersections that contains pills
	 * @return
	 */
	public int[] GetPillIntersection()
	{
		int[] result;
		int[] inters = GetIntersections();
		ArrayList<Integer> pills = new ArrayList<Integer>();
		
		for(int index : inters)
		{
			int pill = current_state.getPillIndex(index);
			if(pill != -1)
			{
				if(current_state.isPillStillAvailable(pill))
				{
					pills.add(index);
					continue;
				}
			}
			pill = current_state.getPowerPillIndex(index);
			if(pill != -1)
			{
				if(current_state.isPowerPillStillAvailable(pill))
				{
					pills.add(index);
					continue;
				}
			}
		}
		
		result = new int[pills.size()];
		
		for(int i=0;i<result.length;i++)
			result[i]=pills.get(i);
		
		return result;
	}
	
	/**
	 * Get all junctions and intersections of the mazes combined
	 * @return
	 */
	public int[] GetMixIndices()
	{
		int[] juncs = current_state.getJunctionIndices();
		int[] inters = GetIntersections();
		
		int[] result = new int[inters.length + juncs.length];
		
		int i = 0;
		for(i = 0; i < inters.length; i++)
		{
			result[i] = inters[i];
		}
		for(int j = 0; j < juncs.length; j++, i++)
		{
			result[i] = juncs[j];
		}
		
		return result;
	}
	
	/**
	 * Get all the combined junction and intersections that contains pills
	 * @return
	 */
	public int[] GetMixPillIndices()
	{
		int[] juncs = GetJunctionPills();
		int[] inters = GetPillIntersection();
		
		int[] result = new int[inters.length + juncs.length];
		
		int i = 0;
		for(i = 0; i < inters.length; i++)
		{
			result[i] = inters[i];
		}
		for(int j = 0; j < juncs.length; j++, i++)
		{
			result[i] = juncs[j];
		}
		
		return result;
	}
	
	/**
	 * if the intersections are not set they are recalculated and sent
	 * @return
	 */
	public int[] GetIntersections()
	{
		if(this.intersections == null || this.intersections.length == 0)
		{
			RecalculateIntersections();
		}
		
		return intersections;
	}
	
	/**
	 * Is the current node a junction ?
	 * Is better to use the one in the game class
	 * @param node
	 * @return
	 */
	public boolean IsJunction(int node)
	{
		int[] juncs = current_state.getJunctionIndices();
		
		for(int j : juncs)
		{
			if(node == j)
				return true;
		}
		
		return false;
	}
	
	/**
	 * Check if current node is an intersection
	 * @param node
	 * @return
	 */
	public boolean IsIntersection(int node)
	{
		int[] inters = GetIntersections();
		
		for(int i : inters)
		{
			if(node == i)
				return true;
		}
		
		return false;
	}
	
	/**
	 * Get the junction connected to the desired movement, only if movement is a valid one
	 * @param junction, origin node can be any node actually
	 * @param move
	 * @return junction by moving from origin
	 */
	public int NextJunctionTowardMovement(int junction, MOVE move)
	{
		MOVE movement = move;
		int next_movement = current_state.getNeighbour(junction, movement);
		
		if(next_movement == -1)
		{
			return -1;
		}
		
		ArrayList<Integer> path = new ArrayList<Integer>();
		path.add(next_movement);
		while(!IsJunction(next_movement))
		{
			
			movement = current_state.getPossibleMoves(next_movement, movement)[0];

			next_movement = current_state.getNeighbour(next_movement, movement);
			path.add(next_movement);
			
		}
		
		int[] _path = new int[path.size()];
		for(int i = 0; i < _path.length; i++)
		{
			_path[i] = path.get(i);
		}
		
		correlated_path_to_move.put(move, _path);
		
		return next_movement;
	}
	
	/**
	 * Get the junction or intersection connected to the desired movement, only if movement is a valid one
	 * @param junction, origin node can be any node actually
	 * @param move
	 * @return junction or intersection by moving from origin
	 */
	public int NextJunctionORIntersectionTowardMovement(int junction, MOVE move)
	{
		MOVE movement = move;
		int next_movement = current_state.getNeighbour(junction, movement);
		
		if(next_movement == -1)
		{
			return junction;
		}
		
		while(!IsJunction(next_movement) && !IsIntersection(next_movement))
		{
			next_movement = current_state.getNeighbour(next_movement, movement);
		}
		
		return next_movement;
	}
	
	/**
	 * Used by ghosts to decide next movement to perform in a junction,
	 * checks which move from the junction is connected to the path pacman is currently following
	 * @param st, current state
	 * @param junction, position of ghost
	 * @param path of pacmaan
	 * @return movement to perform to reach the path of pacman or null if no move will directly reach
	 */
	public MOVE JunctionConnectedToPath(Game st, int junction, int... path)
	{
		
		for(int i = 0; i < 4; i++)
		{
			MOVE probable_move = MOVE.values()[i];
			int next_index = st.getNeighbour(junction, probable_move);
			
			if(next_index == -1)
				continue;
			
			while(next_index != -1)
			{
				if(PointInPath(next_index))
				{
					return probable_move;
				}
				next_index = st.getNeighbour(next_index, probable_move);
			}
			
		}
		
		return null;
	}
	
	/**
	 * use by pacman controller to know if the current path is safe (no non-edible ghost in the way)
	 * if theres a power pill in the path, it checks until the power pill
	 * @param st, current staate
	 * @param current path of pacman
	 * @return if is safe or not
	 */
	public boolean IsPathSafePowerPill(Game st, int... path)
	{
		int pill;
		for(int p : path)
		{
			for(GHOST ghost : GHOST.values())
			{
				if(!st.isGhostEdible(ghost) && st.getGhostCurrentNodeIndex(ghost) == p)
				{
					return false;
				}
			}
			
			pill = st.getPowerPillIndex(p);
			if(pill != -1)
			{
				if(st.isPowerPillStillAvailable(pill))
				{
					return true;
				}
			}
		}
		
		return true;
	}
	
	/**
	 * Used by ghost controller to know if theress aalready a ghost
	 * in the same path that the current checking ghost is about to take
	 * @param st, actual state
	 * @param my_ghost, current ghost checking path
	 * @param path, path going to be traversed by the ghost
	 * @return true or false depending if another ghost is already in path
	 */
	public boolean GhostInThePathOfGhost(Game st, GHOST my_ghost, int... path )
	{
		for(int p : path)
		{
			for(GHOST ghost : GHOST.values())
			{
				if(ghost != my_ghost && st.getGhostCurrentNodeIndex(ghost) == p)
				{
					return true;
				}
			}
		}
		
		return false;
	}
	
	/**
	 * use by pacman controller to know if the current path is safe (no non-edible ghost in the way)
	 * @param st, current staate
	 * @param current path of pacman
	 * @return if is safe or not
	 */
	public boolean IsPathSafe(Game st, int... path)
	{
		for(int p : path)
		{
			for(GHOST ghost : GHOST.values())
			{
				if(!st.isGhostEdible(ghost) && st.getGhostCurrentNodeIndex(ghost) == p)
				{
					return false;
				}
			}
		}
		
		return true;
	}
	
	/**
	 * use by pacman controller to know if the current path have an edible ghost
	 * if true, pacman will choose the move leading to this path always.
	 * @param st, current staate
	 * @param current path of pacman
	 * @return if there is food or no
	 */
	public boolean EdibleGhostInPath(Game st, int... path)
	{
		for(int p : path)
		{
			for(GHOST ghost : GHOST.values())
			{
				if(st.isGhostEdible(ghost) && st.getGhostCurrentNodeIndex(ghost) == p)
				{
					return true;
				}
			}
		}
		
		return false;
	}
	
	/**
	 * Narest pill index to pacman
	 * @param st
	 * @return
	 */
	public int NearestPill(Game st)
	{
		int value = -1;
		int[] pills=st.getActivePillsIndices();	
		
		if(pills.length != 0)
		{
			value = st.getClosestNodeIndexFromNodeIndex(st.getPacmanCurrentNodeIndex(),pills,DM.PATH);
		}
		
		return value;
	}
	
	/**
	 * Narest power pill index to pacman
	 * @param st
	 * @return
	 */
	public int NearestPowerPill(Game st)
	{
		int value = -1;
		int[] pills=st.getActivePowerPillsIndices();	
		
		if(pills.length != 0)
		{
			value = st.getClosestNodeIndexFromNodeIndex(st.getPacmanCurrentNodeIndex(),pills,DM.PATH);
		}
		
		return value;
	}
	
	/**
	 *  Narest edible ghost index to pacman if in range
	 * @param st
	 * @param range
	 * @return
	 */
	public int NearestEdibleGhost(Game st, int range)
	{
		int value = -1;
		int min_dist = range;
		for(GHOST ghost : GHOST.values())
		{
			if(st.isGhostEdible(ghost) && st.getGhostLairTime(ghost) == 0)
			{
				int dist = st.getShortestPathDistance(st.getPacmanCurrentNodeIndex(),
						st.getGhostCurrentNodeIndex(ghost));
				if(dist < min_dist)
				{
					min_dist = dist;
					value = st.getGhostCurrentNodeIndex(ghost);
				}
			}	
		}
		
		return value;
	}
	
	//TODO:MAYBE CHECK HEADING OF GHOST
	/**
	 * Used by pacman controller to check if a ghost is closer to the agent target than her.
	 * @param st, current state
	 * @param target, destination of the agent
	 * @return
	 */
	public boolean WillGhostsArriveFirst(Game st, int target)
	{
		int min_dist = st.getShortestPathDistance(st.getPacmanCurrentNodeIndex(), target);
		
		for(GHOST ghost : GHOST.values())
		{
						
			if(	st.getGhostLairTime(ghost) == 0 &&
				!st.isGhostEdible(ghost) &&
				st.getShortestPathDistance(st.getGhostCurrentNodeIndex(ghost), target) <= min_dist + 3)
			{
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Used by pacman controller, check how many pills (if any) 
	 * are situated in the possible path the agent is about to take
	 * @param st
	 * @param path
	 * @return
	 */
	public int PillsInPath(Game st, int...path)
	{
		int pills = 0;
		for(int p : path)
		{
			int pill = st.getPillIndex(p);
			if(pill != -1)
			{
				if(st.isPillStillAvailable(pill))
				{
					pills++;
					continue;
				}
			}
			pill = st.getPowerPillIndex(p);
			if(pill != -1)
			{
				if(st.isPowerPillStillAvailable(pill))
				{
					pills++;
					continue;
				}
			}
		}
		
		return pills;
	}
	
	/**
	 * Check in index node is in passed path, internal use to know if
	 * next simulated move of the ghost reached the path
	 * @param index
	 * @param path
	 * @return
	 */
	private boolean PointInPath(int index, int... path)
	{
		for(int p : path)
		{
			if(index == p)
				return true;
		}
		
		return false;
	}
	
	/**
	 * Get closest junction EXCLUDING previous junction agent was and current (if it is in one)
	 * @param game
	 * @param previous_index
	 * @param actual_pos
	 * @return
	 */
	public int ClosestJunction(Game game, int previous_index, int actual_pos)
	{
		int[] juncs = game.getJunctionIndices();
		int selected_junc = -1;
		
		//System.out.println("PACMAN POS: " + pacm + "; PREVIOUS PACMAN POS: " + previous_index);
		for(int j = 0; j < 1; j++)
		{
			int min_dist = Integer.MAX_VALUE;
			int selected_index = -1;
			for(int index : juncs)
			{
				int dist = game.getShortestPathDistance(actual_pos, index);
				if(dist < min_dist && 
					index != actual_pos &&
					previous_index != index)
				{
					min_dist = dist;
					selected_index = index;
				}
			}
			
			selected_junc = selected_index;
		}
		
		return selected_junc;
	}
}
