package pacman.controllers.examples;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import dataRecording.HelperExtendedGame;
import pacman.controllers.Controller;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;
import pacman.game.GameView;

public class PlayoutPacman extends Controller<MOVE>
{

	HelperExtendedGame helper = new HelperExtendedGame();
	int past_selection = -1;
	int target_ghost = -1;
	MOVE current_selection = MOVE.NEUTRAL;
	MOVE chosen_move;
	int[] current_pacman_path = new int[0];
	int next_dest = 0;
	boolean reverse = false;
	Random random = new Random();
	MOVE move = MOVE.NEUTRAL;
	int index_pos = 0;	
	int[] path = new int[0];
	
	@Override
	public MOVE getMove(Game game, long timeDue) {
		
		helper.SetState(game);
		int pacman_pos = game.getPacmanCurrentNodeIndex();
		index_pos++;
		
		if(game.wasPacManEaten() || game.getCurrentLevelTime() == 0)
		{
			past_selection = pacman_pos;
			index_pos = 0;
			current_selection = PacmanPlayoutJunction(game);
			next_dest = helper.NextJunctionTowardMovement(pacman_pos, current_selection);
//			current_pacman_path = helper.GetPathFromMove(current_selection);
//			System.out.println("SELECTED MOVE: " + current_selection + "PACMAN PATH LENGTH: " + current_pacman_path.length);
			reverse = false;
			
			return current_selection;
		}
		GameView.addPoints(game, Color.MAGENTA, current_pacman_path);
		//AT JUNCTION
		if(helper.IsJunction(pacman_pos))
		{
			index_pos = 0;
			past_selection = pacman_pos;
			current_selection = PacmanPlayoutJunction(game);
			next_dest = helper.NextJunctionTowardMovement(pacman_pos, current_selection);
			int start = game.getNeighbour(past_selection, current_selection);
//			System.out.println(start);
//			System.out.println(next_dest);
			path = game.getShortestPath(start, next_dest);
			
//			System.out.println("ENTERED JUNCTION AT: " + game.getCurrentLevelTime() + "\tMOVING TO: " + current_selection + "\t" + helper.IsJunction(pacman_pos));
//			current_pacman_path = helper.GetPathFromMove(current_selection);
			reverse = false;
			
			return current_selection;
		}
		else//IN THE PATH
		{
			int[] updated_path = game.getShortestPath(pacman_pos, next_dest);
//			int[] updated_path = new int[current_pacman_path.length - index_pos];
//			for(int i = index_pos, k = 0; i < current_pacman_path.length; i++, k++)
//			{
//				updated_path[k] = current_pacman_path[i];
//				
//			}
			GameView.addPoints(game, Color.GREEN, updated_path);
			int index = PacmanPlayoutPath(game, updated_path);
			
			if(index != -1)
			{
				index_pos = 0;
				past_selection = pacman_pos;
				current_selection = game.getNextMoveTowardsTarget(pacman_pos, index, DM.PATH);
				next_dest = index;
//				System.out.println("ENTERED TO PATH PLAYOUT AT: " + game.getCurrentLevelTime() + "\tMOVING TO: " + current_selection);
//				current_pacman_path = game.getShortestPath(pacman_pos, index);
				
				return current_selection;
			}
		}
		System.out.println(path.length);
		GameView.addPoints(game, Color.WHITE, path);
//		move = game.getNextMoveTowardsTarget(game.getPacmanCurrentNodeIndex(),current_pacman_path[index_pos], DM.PATH);
		move = game.getPossibleMoves(game.getPacmanCurrentNodeIndex(), game.getPacmanLastMoveMade())[0];
		// TODO Auto-generated method stub
		return move;
	}

	//TODO: CHECK THE VALUES AFTER ISPATHSAFE
	public int PacmanPlayoutPath(Game st, int... path)
	{
		boolean just_ate = false;
		
		if(!reverse)
		{
			if(!helper.IsPathSafePowerPill(st, path))
			{
//				System.out.println("I'M SCARED PLEAE DONT KILL ME GHOST");
				reverse = true;
				return past_selection;
			}
		
			int go = -1;
			int min_dist = Integer.MAX_VALUE;
			GHOST selected_ghost = null;
			
			if(st.wasPowerPillEaten())
			{
				for(GHOST ghost : GHOST.values())
				{
					if(st.isGhostEdible(ghost) && st.getGhostLairTime(ghost)==0)
					{
						int dist = st.getShortestPathDistance(st.getPacmanCurrentNodeIndex(),st.getGhostCurrentNodeIndex(ghost));
						if(dist < min_dist)
						{
//							g = ghost;
							selected_ghost = ghost;
							go = st.getGhostCurrentNodeIndex(ghost);
							min_dist = dist;
						}
					}
				}
				
				MOVE m = st.getNextMoveTowardsTarget(st.getPacmanCurrentNodeIndex(), st.getGhostCurrentNodeIndex(selected_ghost), DM.PATH);
				
				if(m == st.getPacmanLastMoveMade().opposite() || m.opposite() == st.getPacmanLastMoveMade())
				{
					reverse = true;
				}
				
				return go;
			}

		}
		
		return -1;
	}
	
	
	//Return junction to move to
	public MOVE PacmanPlayoutJunction(Game st)
	{
		//First we set the possible safe moves
		//i.e. • Has no non-edible ghost on it moving in Pac-Man’s direction.
//		• Next junction is safe, i.e. in any case Pac-Man will reach
//		the next junction before a non-edible ghost.
		//TODO:CHECK THIS FOR NOT CONSIDER REVERSE
		MOVE[] possibleMoves=st.getPossibleMoves(st.getPacmanCurrentNodeIndex(), st.getPacmanLastMoveMade());
		ArrayList<MOVE> safe_moves = new ArrayList<MOVE>();
		ArrayList<MOVE> moves = new ArrayList<MOVE>();
		MOVE selected_move = null;
		int path_pills = 0;
		
		moves:
		for(MOVE move : possibleMoves)
		{
			int junc = helper.NextJunctionTowardMovement(st.getPacmanCurrentNodeIndex(), move);
			if(junc == -1)
				continue moves;
			
			int[] path = helper.GetPathFromMove(move);
			moves.add(move);
			
			if(helper.IsPathSafePowerPill(st, path) && !helper.WillGhostsArriveFirst(st, path[path.length - 1]))
			{
				if(helper.EdibleGhostInPath(st, path))
				{
					return move;
				}
				
				safe_moves.add(move);
				
				int pi = helper.PillsInPath(st, path);
				
				if(pi >= path_pills)
				{
					selected_move = move;
					path_pills = pi;
				}
			}
		}
		
		if(selected_move != null)
		{
			return selected_move;
		}
			
		
		if(safe_moves.isEmpty())
		{
			if(!moves.isEmpty())
				return moves.get(random.nextInt(moves.size()));
			else
			{
				return possibleMoves[0];
			}
		}
		
		return safe_moves.get(random.nextInt(safe_moves.size()));
	}

}
