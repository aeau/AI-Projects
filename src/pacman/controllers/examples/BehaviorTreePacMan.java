package pacman.controllers.examples;

import behaviortree.DataManager;
import behaviortree.Tree;
import pacman.controllers.Controller;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

public class BehaviorTreePacMan extends Controller<MOVE>{

	Tree my_tree = DataManager.LoadTree("simple-tree.txt");
	
	public BehaviorTreePacMan(Tree t)
	{
		super();
		my_tree = t;
	}
	
	@Override
	public MOVE getMove(Game game, long timeDue) 
	{
		my_tree.UpdateGameState(game);
		my_tree.Execute(); //execute tree to the end
		my_tree.my_blackboard.target = -1;
		return my_tree.GetMove();
	}

}
