package pacman.controllers.examples;

import java.awt.Color;

import mcts.UCT;
import pacman.controllers.Controller;
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
		mcts.SetGame(game);
		MOVE move = MOVE.NEUTRAL;
		
		if(!game.wasPacManEaten())
		{
			try {
				move = mcts.runUCT(previous_action, timeDue);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else
		{
			move = MOVE.NEUTRAL;
		}
		
		previous_action = move.ordinal();
		
		if(move == MOVE.NEUTRAL)
		{
			System.out.println("NO TIME FOR MCTS");
		}
		
		GameView.addPoints(game,Color.CYAN, mcts.GetSelectedJuncs());
		
		// TODO Auto-generated method stub
		return move;
	}

}
