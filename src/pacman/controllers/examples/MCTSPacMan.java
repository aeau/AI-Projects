package pacman.controllers.examples;

import java.awt.Color;

import mcts.UCT;
import pacman.controllers.Controller;
import pacman.game.Constants.DM;
import pacman.game.Constants.MOVE;
import pacman.game.Game;
import pacman.game.GameView;

public class MCTSPacMan extends Controller<MOVE>
{
	public UCT mcts = new UCT();
	public int previous_action = 0;
	boolean reverse = false;
	
	public MCTSPacMan()
	{
		
	}
	
	@Override
	public MOVE getMove(Game game, long timeDue) 
	{
//		System.out.println("WTF");
		mcts.SetGame(game);
		MOVE move = MOVE.NEUTRAL;
		
		if(mcts.helper.IsIntersection(game.getPacmanCurrentNodeIndex()) ||
				mcts.helper.IsJunction(game.getPacmanCurrentNodeIndex()) 	)
		{
			if(!game.wasPacManEaten())
			{
				try {
					move = mcts.runUCT(previous_action, timeDue);
					move = game.getNextMoveTowardsTarget(game.getPacmanCurrentNodeIndex(), mcts.target, DM.PATH);
					reverse = false;
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else
			{
				move = MOVE.NEUTRAL;
			}
		}
		else
		{
			mcts.current_pacman_path = game.getShortestPath(game.getPacmanCurrentNodeIndex(), mcts.target);
			
			if(!reverse)
			{
				if(!mcts.helper.IsPathSafe(game, mcts.current_pacman_path))
				{
					reverse = true;
					int aux = mcts.target;
					mcts.target = mcts.previous_target;
					mcts.previous_target = game.getPacmanCurrentNodeIndex();
					mcts.current_pacman_path = game.getShortestPath(mcts.previous_target, mcts.target);
				}
			}
			//move = MOVE.values()[previous_action];
			move = game.getNextMoveTowardsTarget(game.getPacmanCurrentNodeIndex(), mcts.target, DM.PATH);
		}
		
		previous_action = move.ordinal();
		
		if(move == MOVE.NEUTRAL)
		{
			System.out.println("NO TIME FOR MCTS");
		}
		
		GameView.addPoints(game,Color.CYAN, mcts.GetSelectedJuncs());
		GameView.addPoints(game,Color.BLUE, mcts.target);
//		GameView.addPoints(game,Color.CYAN, mcts.helper.GetIntersections());
		// TODO Auto-generated method stub
		return move;
	}

}
