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
