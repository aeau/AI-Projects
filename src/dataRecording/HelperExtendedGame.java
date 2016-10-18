package dataRecording;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;

import pacman.game.Game;
import pacman.game.GameView;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;

public class HelperExtendedGame 
{
	int[] intersections = null;
	HashMap<MOVE, int[]> correlated_path_to_move = new HashMap<MOVE, int[]>(); 
	public int maze_index = -1;
	Game current_state;
	
	public HelperExtendedGame()
	{
		
	}
	
	public void SetState(Game current)
	{
		current_state = current;
		if(maze_index != current.getMazeIndex())
		{
			maze_index = current.getMazeIndex();
			RecalculateIntersections();
		}
	}
	
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
	
	public int[] GetIntersections()
	{
		if(this.intersections == null || this.intersections.length == 0)
		{
			RecalculateIntersections();
		}
		
		return intersections;
	}
	
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
			
//			if(move == MOVE.LEFT)
//			{
//				GameView.addPoints(current_state, Color.PINK, next_movement);
//			}
			
			
			next_movement = current_state.getNeighbour(next_movement, movement);
			path.add(next_movement);
			
//			if(IsIntersection(next_movement))
//			{
//				intersection:
//				for(MOVE m : MOVE.values())
//				{
//					if(m == movement || m == movement.opposite())
//					{
//						continue intersection;
//					}
//					else if(current_state.getNeighbour(next_movement, m) != -1)
//					{
//						movement = m;
//						break intersection;
//					}
//				}
//			}
//			
//			if(next_movement == -1)
//			{
//				return -1;
//			}
			
		}
		
		int[] _path = new int[path.size()];
		for(int i = 0; i < _path.length; i++)
		{
			_path[i] = path.get(i);
		}
		
		correlated_path_to_move.put(move, _path);
		
		return next_movement;
	}
	
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
	
	//MAYBE CHECK HEADING OF GHOST
	//TODO: DELETE COMMENT & CHECK -- 12/10/2016
	public boolean WillGhostsArriveFirst(Game st, int target)
	{
		int min_dist = st.getShortestPathDistance(st.getPacmanCurrentNodeIndex(), target);
		
//		System.out.println("PACMAN DISTANCE TO TARGET: " + min_dist);
		for(GHOST ghost : GHOST.values())
		{
						
			if(	st.getGhostLairTime(ghost) == 0 &&
				!st.isGhostEdible(ghost) &&
				st.getShortestPathDistance(st.getGhostCurrentNodeIndex(ghost), target) <= min_dist + 2)
			{
//				System.out.println("GHOST " + ghost + " DISTANCE TO TARGET: " + st.getShortestPathDistance(st.getGhostCurrentNodeIndex(ghost), target));
				return true;
			}
		}
		
		return false;
	}
	
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
	 * Get closest junction EXCLUDING previous junction i was and current (if i was)
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
