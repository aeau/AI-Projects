package pacman.controllers.examples;

import java.awt.Color;

import mcts.MCTSNode;
import mcts.UCT;
import pacman.controllers.Controller;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;
import pacman.game.GameView;

public class MCTSPacMan extends Controller<MOVE>
{
	public UCT 	mcts 			= new UCT();
	public int 	previous_action = 0;
	int 		next_dest 		= 0;
	int 		previous_pos	= 0;
	boolean 	reverse 		= false;
	int i = 0;
	
	public MCTSPacMan()
	{
		
	}
	
	/**
	 * MCTS is requested a move each time we are in a junction, meanwhile the agent is in the path between junctions
	 * it can select to reverse is path getss compromised.
	 */
	@Override
	public MOVE getMove(Game game, long timeDue) 
	{
		i++;
		mcts.SetGame(game);
		MOVE move = MOVE.NEUTRAL;
		int pacman_pos = game.getPacmanCurrentNodeIndex();
		
		//first time executing the controller, just being careful 
		if(game.wasPacManEaten() || game.getCurrentLevelTime() == 0)
		{
			try
			{
				previous_pos = pacman_pos;
				move = mcts.runUCT(previous_action, timeDue); //request movement
				next_dest = mcts.target;
				reverse = false;
				return move;
			}
			catch(Exception e)
			{
				System.out.println("FIRST TIME: " + e.getMessage());
			}
		}
		
		if(mcts.helper.IsJunction(pacman_pos))
		{
			try {
				previous_pos = pacman_pos;
				move = mcts.runUCT(game.getPacmanLastMoveMade().ordinal(), timeDue); //request movement
				next_dest = mcts.target;
				reverse = false;
				return move;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				System.out.println("junction: " + e.getMessage());
			}
		}
		else if(next_dest <= 1296 && pacman_pos <= 1296 && next_dest != -1 ) // in path
		{
			int[] updated_path = game.getShortestPath(pacman_pos, next_dest); //--> error of 1297 index
			//Maybe is when changing level the index already changes
//			GameView.addPoints(game, Color.GREEN, updated_path);
			if(!reverse)
			{
				if(!mcts.helper.IsPathSafePowerPill(game, updated_path)  && mcts.helper.WillGhostsArriveFirst(game, updated_path[updated_path.length - 1]))
				{
//					reverse = true;
					next_dest = previous_pos;
					previous_pos = pacman_pos;
					MOVE m = game.getNextMoveTowardsTarget(previous_pos, next_dest, DM.PATH);
					
					if(m == game.getPacmanLastMoveMade().opposite() || m.opposite() == game.getPacmanLastMoveMade())
					{
						reverse = true;
					}
					
					return  m;//TODO: maybe here it could improve
				}
			}
		}
		
		//if not in a junction and not need to reverse in path (or can't) we just do the next movement pacman is able to do
		//which will allow us to follow the correct path.
		move = game.getPossibleMoves(game.getPacmanCurrentNodeIndex(), game.getPacmanLastMoveMade())[0];
		previous_action = move.ordinal();

		return move;
	}

}
