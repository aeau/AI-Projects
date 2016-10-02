package dataRecording;

import java.util.ArrayList;

import pacman.game.Game;
import pacman.game.Constants.MOVE;

public class HelperExtendedGame 
{
	int[] intersections = null;
	int maze_index = -1;
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
	
	public void RecalculateIntersections()
	{
		if(this.intersections == null || this.intersections.length == 0)
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
