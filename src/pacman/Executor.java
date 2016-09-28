package pacman;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.time.temporal.JulianFields;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Random;

import behaviortree.DataManager;
import behaviortree.DataSaverLoader;
import behaviortree.Tree;
import behaviortree.ga.EvolutionProcess;
import behaviortree.ga.Genome;
import dataRecording.DataCollectorController;
import neuralnetwork.NeuralNetwork;
import pacman.controllers.Controller;
import pacman.controllers.HumanController;
import pacman.controllers.KeyBoardInput;
import pacman.controllers.examples.AggressiveGhosts;
import pacman.controllers.examples.BehaviorTreePacMan;
import pacman.controllers.examples.Legacy;
import pacman.controllers.examples.Legacy2TheReckoning;
import pacman.controllers.examples.NearestPillPacMan;
import pacman.controllers.examples.NearestPillPacManVS;
import pacman.controllers.examples.NeuralNetworkPacMan;
import pacman.controllers.examples.QLearningPacMan;
import pacman.controllers.examples.QLearningPacMan2;
import pacman.controllers.examples.RandomGhosts;
import pacman.controllers.examples.RandomNonRevPacMan;
import pacman.controllers.examples.RandomPacMan;
import pacman.controllers.examples.StarterGhosts;
import pacman.controllers.examples.StarterPacMan;
import pacman.game.Game;
import pacman.game.GameView;
import pacman.game.Constants.GHOST;
import reinforcementlearning.QPacmanState;
import reinforcementlearning.QTable;

import static pacman.game.Constants.*;

/**
 * This class may be used to execute the game in timed or un-timed modes, with or without
 * visuals. Competitors should implement their controllers in game.entries.ghosts and 
 * game.entries.pacman respectively. The skeleton classes are already provided. The package
 * structure should not be changed (although you may create sub-packages in these packages).
 */
@SuppressWarnings("unused")
public class Executor
{	
	/**
	 * The main method. Several options are listed - simply remove comments to use the option you want.
	 *
	 * @param args the command line arguments
	 */
	public static void main(String[] args)
	{
		Executor exec=new Executor();
		boolean visual = true;
		int numTrials=500;
		
		//HUMAN TEST
		//exec.runGameTimed(new HumanController(new KeyBoardInput()),new StarterGhosts(),visual);
		
		//FOR REINFORCEMENT LEARNING
		
		QTable q = new QTable(4);
		
		exec.runExperimentReinforcementLearning(new QLearningPacMan(q),new StarterGhosts(),numTrials, q);
		q.explorationChance = 0.0f;
//		exec.runExperimentReinforcementLearning(new QLearningPacMan(q),new StarterGhosts(),numTrials, q);
//		q.explorationChance = 0.7f;
//		exec.runExperimentReinforcementLearning(new QLearningPacMan(q),new StarterGhosts(),numTrials, q);
//		q.explorationChance = 0.0f;
//		exec.runExperimentReinforcementLearning(new QLearningPacMan(q),new StarterGhosts(),numTrials, q);
//		DataManager.SaveQTable(q, "first_try.txt");
		exec.runGameTimedRLTest(new QLearningPacMan2(q),new StarterGhosts(),visual, q);	
		
		//LOAD Q TABLE
//		q = DataManager.LoadQTable("first_try.txt");
//		q.explorationChance = 0.0f;
//		exec.runGameTimedRLTest(new QLearningPacMan2(q),new StarterGhosts(),visual, q);	
		
		//FOR NEURAL NETWORKS

		//exec.runGameTimed(new DataCollectorController(new KeyBoardInput()),new StarterGhosts(),visual);	
		//NeuralNetwork nn = new NeuralNetwork();
		//exec.runGameTimed(new NeuralNetworkPacMan(nn),new StarterGhosts(),visual);	
		//exec.runGame(new NeuralNetworkPacMan(nn),new StarterGhosts(),visual,5);
		

		//FOR BEHAVIOR TREES
		
		//EvolutionProcess evo = new EvolutionProcess(40);
		//evo.ExecuteEvolution();
		//DataManager.SaveTree(evo.champion.Deserialize(),evo.champion.PrintGenome(), "Champion.txt");
		
		

		
		//run multiple games in batch mode - good for testing.
		//int numTrials=10;
		//exec.runExperiment(new RandomPacMan(),new RandomGhosts(),numTrials);
		
		//Load tree from txt file
//		Tree t = DataManager.LoadTree("Champion_copy.txt");
//		exec.runGameTimed(new BehaviorTreePacMan(t),new StarterGhosts(),visual);
		//exec.runGame(new BehaviorTreePacMan(t),new StarterGhosts(),visual,5);
		//exec.runExperiment(new BehaviorTreePacMan(t),new StarterGhosts(),100);
		//exec.runGameTimed(new HumanController(new KeyBoardInput()),new StarterGhosts(),visual);
		
		/*
		Tree a = null;
		
		double sco = 0;
		for(int i = 0; i < 30; i++)
		{
			//Load tree from gen
			Genome genome = new Genome();
			Tree t = genome.Deserialize();
			double avg_score = 0;
			//run a game in synchronous mode: game waits until controllers respond.
			int delay=5;
			boolean visual=true;
			//exec.runGame(new BehaviorTreePacMan(t),new StarterGhosts(),visual,delay);
			int numTrials=10;
			avg_score = exec.runExperiment(new BehaviorTreePacMan(t),new StarterGhosts(),numTrials);
			//exec.runGame(new StarterPacMan(),new RandomGhosts(),visual,delay);
			
			System.out.println("OLD SCORE: " + sco + "; EXECUTION " + i + ", SCORE: " + avg_score);
			
			if(avg_score > sco)
			{
				sco = avg_score;
				a = t;
			}
			
			System.out.println(genome.PrintGenome());
		}
		
		DataManager.SaveTree(a, "Champion.txt");
		*/
/*
		//run the game in asynchronous mode.
		boolean visual=true;
//		exec.runGameTimed(new NearestPillPacMan(),new AggressiveGhosts(),visual);
		//exec.runGameTimed(new StarterPacMan(),new StarterGhosts(),visual);
		exec.runGameTimed(new BehaviorTreePacMan(),new StarterGhosts(),visual);
//		exec.runGameTimed(new HumanController(new KeyBoardInput()),new StarterGhosts(),visual);	
		//*/
		
		/*
		//run the game in asynchronous mode but advance as soon as both controllers are ready  - this is the mode of the competition.
		//time limit of DELAY ms still applies.
		boolean visual=true;
		boolean fixedTime=false;
		exec.runGameTimedSpeedOptimised(new RandomPacMan(),new RandomGhosts(),fixedTime,visual);
		*/
		
		/*
		//run game in asynchronous mode and record it to file for replay at a later stage.
		boolean visual=true;
		String fileName="replay.txt";
		exec.runGameTimedRecorded(new HumanController(new KeyBoardInput()),new RandomGhosts(),visual,fileName);
		//exec.replayGame(fileName,visual);
		 */
	}
	
	public int[] ClosestKJunctions(Game game, int k, int previous_target)
	{
		int[] closest_juncs = new int[k];
		int[] inters = game.GetIntersections();
		int[] juncs = game.getJunctionIndices();
		
		int[] mix = new int[inters.length + juncs.length];
		int i = 0;
		for(i = 0; i < inters.length; i++)
		{
			mix[i] = inters[i];
		}
		for(int j = 0; j < juncs.length; j++, i++)
		{
			mix[i] = juncs[j];
		}
		
		ArrayList<Integer> targetjuncs=new ArrayList<Integer>();
		int pacm = game.getPacmanCurrentNodeIndex();

		for(int j = -1; j < k; j++)
		{
			int min_dist = Integer.MAX_VALUE;
			int selected_index = -1;
			for(int index : inters)
			{
				int dist = game.getShortestPathDistance(pacm, index);
				if(dist < min_dist && !targetjuncs.contains(index) && index != pacm && index != previous_target)
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
	
	/**
     * For running multiple games without visuals. This is useful to get a good idea of how well a controller plays
     * against a chosen opponent: the random nature of the game means that performance can vary from game to game. 
     * Running many games and looking at the average score (and standard deviation/error) helps to get a better
     * idea of how well the controller is likely to do in the competition.
     *
     * @param pacManController The Pac-Man controller
     * @param ghostController The Ghosts controller
     * @param trials The number of trials to be executed
     */
    public double runExperimentReinforcementLearning(Controller<MOVE> pacManController,Controller<EnumMap<GHOST,MOVE>> ghostController,int trials, QTable q)
    {
    	double avgScore=0;
    	double pills_ate=0;
    	double cherry_ate=0;
    	
    	int min_away_dist = -25;
    	int min_close_dist = 50;
    	
    	Random rnd=new Random(0);
		Game game;
		
		
		
		for(int i=0;i<trials;i++)
		{
			q.first_time = true;
			game=new Game(rnd.nextLong());
			int pacman_lives = 3;
			int num_pills = game.getNumberOfPills();
			int num_power_pills = game.getNumberOfPowerPills();
			int streak = 0;
			MOVE last_move = MOVE.NEUTRAL;
			int accumulated_reward = 0;
			int same_pos = -1;
			boolean check_path = true;
			
			while(!game.gameOver())
			{
		        game.advanceGame(pacManController.getMove(game.copy(),System.currentTimeMillis()+DELAY),
		        		ghostController.getMove(game.copy(),System.currentTimeMillis()+DELAY));
		        
		        if(check_path)
		        {
		        	int[] path = game.getShortestPath(game.getPacmanCurrentNodeIndex(), q.target);
		        	 
		        	 for(int p : path)
		        	 {
		        		 //We check first for ghost in the position
		        		 if((game.getGhostCurrentNodeIndex(GHOST.BLINKY) == p && !game.isGhostEdible(GHOST.BLINKY))||
        					(game.getGhostCurrentNodeIndex(GHOST.INKY) == p && !game.isGhostEdible(GHOST.INKY))||
        					(game.getGhostCurrentNodeIndex(GHOST.PINKY) == p && !game.isGhostEdible(GHOST.PINKY))||
        					(game.getGhostCurrentNodeIndex(GHOST.SUE) == p && !game.isGhostEdible(GHOST.SUE)))
		        		 {
		        			 accumulated_reward -= 25; 
		        			 continue;
		        		 }
		        		 
		        		 //EDIBLE GHOST
		        		 if((game.getGhostCurrentNodeIndex(GHOST.BLINKY) == p && !game.isGhostEdible(GHOST.BLINKY))||
        					(game.getGhostCurrentNodeIndex(GHOST.INKY) == p && !game.isGhostEdible(GHOST.INKY))||
        					(game.getGhostCurrentNodeIndex(GHOST.PINKY) == p && !game.isGhostEdible(GHOST.PINKY))||
        					(game.getGhostCurrentNodeIndex(GHOST.SUE) == p && !game.isGhostEdible(GHOST.SUE)))
		        		 {
		        			 accumulated_reward += 25; 
		        			 continue;
		        		 }
		        		 
		        		 int index = game.getPillIndex(p);
		        		 
		        		 if(index != -1)
		        		 {
		        			 if(game.isPillStillAvailable(index))
		        			 {
		        				 accumulated_reward += 2;
		        				 continue;
		        			 }
		        			 else
		        			 {
		        				 accumulated_reward += 1;
		        				 continue;
		        			 }
		        		 }
		        		 
		        		 index = game.getPowerPillIndex(p);
		        		 
		        		 if(index != -1)
		        		 {
		        			 if(game.isPowerPillStillAvailable(index))
		        			 {
		        				 accumulated_reward += 10;
		        				 continue;
		        			 }
		        			 else
		        			 {
		        				 accumulated_reward += 1;
		        				 continue;
		        			 }
		        		 }
		        		 
		        		 
		        	 }
		        	 
		        	 check_path = false;
		        	 q.reward = accumulated_reward;
		        }
		        
		        if(game.getPacmanCurrentNodeIndex() == q.target)
		        {
		        	//System.out.println("REWARD: " + q.reward);
		        	accumulated_reward = 0;
		        	check_path = true;
		        }
		        
//		        for(GHOST ghost : GHOST.values())
//				{
//		        	if(game.getGhostLairTime(ghost) == 0 && game.getGhostEdibleTime(ghost)==0)
//		        	{
//			        	int dist = game.getShortestPathDistance(game.getPacmanCurrentNodeIndex(),
//								game.getGhostCurrentNodeIndex(ghost));
//			        	
//						if(dist<=25)
//						{
//							accumulated_reward = 0;
//							q.reward = -15;
//							q.target = -1;
//							check_path = true;
//						}
//		        	}
//				}

		        
//		        if(same_pos != game.getPacmanCurrentNodeIndex())
//		        {
//			        //We die
//			        if(game.getPacmanNumberOfLivesRemaining() != pacman_lives)
//			        {
//			        	pacman_lives--;
//			        	accumulated_reward += -50;
//			        	QPacmanState ps = new QPacmanState(game, last_move);
//			        	q.updateQvalue(accumulated_reward, ps.toString());
//			        	streak = 0;
//			        	//q.reward = -50;
//			        }
//			        
//			        for(GHOST ghost : GHOST.values())
//					{
//			        	if(game.getGhostLairTime(ghost) != 0)
//			        	{
//				        	int dist = game.getShortestPathDistance(game.getPacmanCurrentNodeIndex(),
//									game.getGhostCurrentNodeIndex(ghost));
//				        	
//							if(game.getGhostEdibleTime(ghost)!=0)
//							{
//								if(dist<=2)
//								{
//									accumulated_reward += 25;
//								}
//							}
//							else
//							{
//								if(dist <= 2)
//								{
//									accumulated_reward -= 25;
//								}
//							}
//			        	}
//					}
//			        
//	//		        
//	//		        
//	//		        if(game.getGhostLairTime(GHOST.BLINKY) == 0 && 
//	//		        		!game.isGhostEdible(GHOST.BLINKY))
//	//		        {
//	//		        	q.reward += min_away_dist + game.getShortestPathDistance(game.getPacmanCurrentNodeIndex(),game.getGhostCurrentNodeIndex(GHOST.BLINKY));
//	//		        }
//	//		        else if(game.isGhostEdible(GHOST.BLINKY))
//	//		        {
//	//		        	q.reward += min_close_dist - game.getShortestPathDistance(game.getPacmanCurrentNodeIndex(),game.getGhostCurrentNodeIndex(GHOST.BLINKY));
//	//		        }
//	//		        if(game.getGhostLairTime(GHOST.INKY) == 0 && 
//	//		        		!game.isGhostEdible(GHOST.INKY))
//	//		        {
//	//		        	q.reward += min_away_dist + game.getShortestPathDistance(game.getPacmanCurrentNodeIndex(),game.getGhostCurrentNodeIndex(GHOST.INKY));
//	//		        }
//	//		        else if(game.isGhostEdible(GHOST.INKY))
//	//		        {
//	//		        	q.reward += min_close_dist - game.getShortestPathDistance(game.getPacmanCurrentNodeIndex(),game.getGhostCurrentNodeIndex(GHOST.BLINKY));
//	//		        }
//	//		        if(game.getGhostLairTime(GHOST.PINKY) == 0 && 
//	//		        		!game.isGhostEdible(GHOST.PINKY))
//	//		        {
//	//		        	q.reward += min_away_dist + game.getShortestPathDistance(game.getPacmanCurrentNodeIndex(),game.getGhostCurrentNodeIndex(GHOST.PINKY));
//	//		        }
//	//		        else if(game.isGhostEdible(GHOST.PINKY))
//	//		        {
//	//		        	q.reward += min_close_dist - game.getShortestPathDistance(game.getPacmanCurrentNodeIndex(),game.getGhostCurrentNodeIndex(GHOST.BLINKY));
//	//		        }
//	//		        if(game.getGhostLairTime(GHOST.SUE) == 0 && 
//	//		        		!game.isGhostEdible(GHOST.SUE))
//	//		        {
//	//		        	
//	//		        	q.reward += min_away_dist + game.getShortestPathDistance(game.getPacmanCurrentNodeIndex(),game.getGhostCurrentNodeIndex(GHOST.SUE));
//	//		        }
//	//		        else if(game.isGhostEdible(GHOST.SUE))
//	//		        {
//	//		        	q.reward += min_close_dist - game.getShortestPathDistance(game.getPacmanCurrentNodeIndex(),game.getGhostCurrentNodeIndex(GHOST.BLINKY));
//	//		        }
//			        
//			        if(num_power_pills != game.getNumberOfActivePowerPills())
//			        {
//			        	num_power_pills = game.getNumberOfActivePowerPills();
//			        	accumulated_reward += 10;
//			        }
//			        
//			        if(num_pills != game.getNumberOfActivePills())
//			        {
//			        	streak++; 
//			        	num_pills = game.getNumberOfActivePills();
//			        	accumulated_reward += 5 * streak;
//			        }
//			        else
//			        {
//			        	streak = 0;
//			        	accumulated_reward += 2;
//			        	//q.reward = 0;
//			        }
//			        
//			        if(game.getPacmanCurrentNodeIndex() == q.target)
//			        {
//			        	q.reward = accumulated_reward;
//			        	System.out.println("REWARD: " + q.reward);
//			        	accumulated_reward = 0;
//			        }
//		        }
//		        
//		        same_pos = game.getPacmanCurrentNodeIndex();
		       //q.printQtable();
		       // System.out.println(q.reward);
			}

			q.ResetVars();
			
			avgScore+=game.getScore();
			System.out.println("FINISHED GAME " + i + "; TIME: " + game.getCurrentLevelTime() + "; SCORE: " + game.getScore());
		}
		
		//System.out.println(avgScore/trials);
		return avgScore/trials;
    }
	
	public void runGameTimedForNeuralNetwork(Controller<MOVE> pacManController,Controller<EnumMap<GHOST,MOVE>> ghostController,boolean visual)
	{
		StringBuilder replay=new StringBuilder();
		
		Game game=new Game(0);
		
		GameView gv=null;
		
		if(visual)
			gv=new GameView(game).showGame();
		
		if(pacManController instanceof HumanController)
			gv.getFrame().addKeyListener(((HumanController)pacManController).getKeyboardInput());
				
		new Thread(pacManController).start();
		new Thread(ghostController).start();
		
		while(!game.gameOver())
		{
			pacManController.update(game.copy(),System.currentTimeMillis()+DELAY);
			ghostController.update(game.copy(),System.currentTimeMillis()+DELAY);

			try
			{
				Thread.sleep(DELAY);
			}
			catch(InterruptedException e)
			{
				e.printStackTrace();
			}

	        game.advanceGame(pacManController.getMove(),ghostController.getMove());	   
	        
	        if(visual)
	        	gv.repaint();
	        
	        String tuple = game.GetNeuralNetworkInfo(true);
	        
	        //dont allow neutral movement
	        if(!tuple.equals("null"))
	        {
	        	replay.append(tuple + "\n");
	        }
		}
		
		pacManController.terminate();
		ghostController.terminate();
		
		DataSaverLoader.saveFile("average_player.csv", replay.toString(), true);
	}
	
    /**
     * For running multiple games without visuals. This is useful to get a good idea of how well a controller plays
     * against a chosen opponent: the random nature of the game means that performance can vary from game to game. 
     * Running many games and looking at the average score (and standard deviation/error) helps to get a better
     * idea of how well the controller is likely to do in the competition.
     *
     * @param pacManController The Pac-Man controller
     * @param ghostController The Ghosts controller
     * @param trials The number of trials to be executed
     */
    public double runExperiment(Controller<MOVE> pacManController,Controller<EnumMap<GHOST,MOVE>> ghostController,int trials)
    {
    	double avgScore=0;
    	double pills_ate=0;
    	double cherry_ate=0;
    	
    	Random rnd=new Random(0);
		Game game;
		
		for(int i=0;i<trials;i++)
		{
			game=new Game(rnd.nextLong());
			int pacman_lives = 3;
			//int num_pills
			while(!game.gameOver())
			{
		        game.advanceGame(pacManController.getMove(game.copy(),System.currentTimeMillis()+DELAY),
		        		ghostController.getMove(game.copy(),System.currentTimeMillis()+DELAY));
		        
		        if(game.getPacmanNumberOfLivesRemaining() != pacman_lives)
		        {
		        	pacman_lives--;
		        }

			}
			
			//CHECK THIS FOR BEHAVIOR TREES
			avgScore+=game.getScore();
			pills_ate += (game.getNumberOfPills() * game.getCurrentLevel()) - game.getNumberOfActivePills();
			cherry_ate += (game.getNumberOfPowerPills() * game.getCurrentLevel()) - game.getNumberOfActivePowerPills();
			//System.out.println(i+"\t"+game.getScore());
		}
		
		//System.out.println(avgScore/trials);
		return avgScore/trials;
    }
	
    /**
     * For running multiple games without visuals. This is useful to get a good idea of how well a controller plays
     * against a chosen opponent: the random nature of the game means that performance can vary from game to game. 
     * Running many games and looking at the average score (and standard deviation/error) helps to get a better
     * idea of how well the controller is likely to do in the competition.
     *
     * @param pacManController The Pac-Man controller
     * @param ghostController The Ghosts controller
     * @param trials The number of trials to be executed
     */
    public ArrayList<Double> runExperimentEvolution(Controller<MOVE> pacManController,Controller<EnumMap<GHOST,MOVE>> ghostController,int trials)
    {
    	ArrayList<Double> result = new ArrayList<Double>();
    	double avgScore=0;
    	double pills_ate=0;
    	double cherry_ate=0;
    	double avg_time = 0;
    	
    	Random rnd=new Random(0);
		Game game;
		
		for(int i=0;i<trials;i++)
		{
			game=new Game(rnd.nextLong());
			
			while(!game.gameOver())
			{
		        game.advanceGame(pacManController.getMove(game.copy(),System.currentTimeMillis()+DELAY),
		        		ghostController.getMove(game.copy(),System.currentTimeMillis()+DELAY));
			}
			
			avgScore+=game.getScore();
			pills_ate += (game.getNumberOfPills() * game.getCurrentLevel()) - game.getNumberOfActivePills();
			cherry_ate += (game.getNumberOfPowerPills() * game.getCurrentLevel()) - game.getNumberOfActivePowerPills();
			avg_time += game.getTotalTime();
			//System.out.println(i+"\t"+game.getScore());
		}
		
		//System.out.println(avgScore/trials);
		result.add(avgScore/trials);
		result.add(pills_ate/trials);
		result.add(cherry_ate/trials);
		result.add(avg_time/trials);
		
		return result;
    }
    
	/**
	 * Run a game in asynchronous mode: the game waits until a move is returned. In order to slow thing down in case
	 * the controllers return very quickly, a time limit can be used. If fasted gameplay is required, this delay
	 * should be put as 0.
	 *
	 * @param pacManController The Pac-Man controller
	 * @param ghostController The Ghosts controller
	 * @param visual Indicates whether or not to use visuals
	 * @param delay The delay between time-steps
	 */
	public void runGame(Controller<MOVE> pacManController,Controller<EnumMap<GHOST,MOVE>> ghostController,boolean visual,int delay)
	{
		Game game=new Game(0);

		GameView gv=null;
		
		if(visual)
			gv=new GameView(game).showGame();
		
		while(!game.gameOver())
		{
	        game.advanceGame(pacManController.getMove(game.copy(),-1),ghostController.getMove(game.copy(),-1));
	        
	        try{Thread.sleep(delay);}catch(Exception e){}
	        
	        if(visual)
	        	gv.repaint();
		}
	}
	
	/**
     * Run the game with time limit (asynchronous mode). This is how it will be done in the competition. 
     * Can be played with and without visual display of game states.
     *
     * @param pacManController The Pac-Man controller
     * @param ghostController The Ghosts controller
	 * @param visual Indicates whether or not to use visuals
     */
    public void runGameTimed(Controller<MOVE> pacManController,Controller<EnumMap<GHOST,MOVE>> ghostController,boolean visual)
	{
		Game game=new Game(0);
		
		GameView gv=null;
		
		if(visual)
			gv=new GameView(game).showGame();
		
		if(pacManController instanceof HumanController)
			gv.getFrame().addKeyListener(((HumanController)pacManController).getKeyboardInput());
				
		new Thread(pacManController).start();
		new Thread(ghostController).start();
		
		while(!game.gameOver())
		{
			pacManController.update(game.copy(),System.currentTimeMillis()+DELAY);
			ghostController.update(game.copy(),System.currentTimeMillis()+DELAY);

			try
			{
				Thread.sleep(DELAY);
			}
			catch(InterruptedException e)
			{
				e.printStackTrace();
			}

	        game.advanceGame(pacManController.getMove(),ghostController.getMove());	   
	        
	        if(visual)
	        {
	        	//if(game.isJunction(game.getPacmanCurrentNodeIndex()))
	        		//GameView.addPoints(game, Color.CYAN, ClosestKJunctions(game, 4));
	        	gv.repaint();
	        	
	        }
		}
		
		pacManController.terminate();
		ghostController.terminate();
	}
    
    public void runGameTimedRLTest(Controller<MOVE> pacManController,Controller<EnumMap<GHOST,MOVE>> ghostController,boolean visual, QTable q)
	{
		Game game=new Game(0);
		
		GameView gv=null;
		
		if(visual)
			gv=new GameView(game).showGame();
		
		if(pacManController instanceof HumanController)
			gv.getFrame().addKeyListener(((HumanController)pacManController).getKeyboardInput());
				
		new Thread(pacManController).start();
		new Thread(ghostController).start();
		
		while(!game.gameOver())
		{
			pacManController.update(game.copy(),System.currentTimeMillis()+DELAY);
			ghostController.update(game.copy(),System.currentTimeMillis()+DELAY);

			try
			{
				Thread.sleep(DELAY);
			}
			catch(InterruptedException e)
			{
				e.printStackTrace();
			}

	        game.advanceGame(pacManController.getMove(),ghostController.getMove());	   
	        
	        if(visual)
	        {
	        	//if(game.isJunction(game.getPacmanCurrentNodeIndex()))
	        		//GameView.addPoints(game, Color.CYAN, ClosestKJunctions(game, 4, q.previous_target));
	        		
	        		//GameView.addLines(game, Color.MAGENTA, q.previous_target, q.target);
	        		int[] path = game.getShortestPath(q.previous_target, q.target);
	        		
	        		for(int i= 0; i < path.length -1; i++)
	        		{
	        			GameView.addLines(game, Color.MAGENTA, path[i], path[i + 1]);
	        		}
//	        		if(q.target != -1)
//	        			GameView.addPoints(game, Color.GREEN,q.target);
	        	
	        	GameView.addPoints(game, Color.MAGENTA, game.GetPillIntersection());
	        	GameView.addPoints(game, Color.CYAN, game.GetJunctionPills());
	        	//GameView.addPoints(game, Color.CYAN, game.getJunctionIndices());
	        	gv.repaint();
	        	
	        }
		}
		
		pacManController.terminate();
		ghostController.terminate();
	}
	
    /**
     * Run the game in asynchronous mode but proceed as soon as both controllers replied. The time limit still applies so 
     * so the game will proceed after 40ms regardless of whether the controllers managed to calculate a turn.
     *     
     * @param pacManController The Pac-Man controller
     * @param ghostController The Ghosts controller
     * @param fixedTime Whether or not to wait until 40ms are up even if both controllers already responded
	 * @param visual Indicates whether or not to use visuals
     */
    public void runGameTimedSpeedOptimised(Controller<MOVE> pacManController,Controller<EnumMap<GHOST,MOVE>> ghostController,boolean fixedTime,boolean visual)
 	{
 		Game game=new Game(0);
 		
 		GameView gv=null;
 		
 		if(visual)
 			gv=new GameView(game).showGame();
 		
 		if(pacManController instanceof HumanController)
 			gv.getFrame().addKeyListener(((HumanController)pacManController).getKeyboardInput());
 				
 		new Thread(pacManController).start();
 		new Thread(ghostController).start();
 		
 		while(!game.gameOver())
 		{
 			pacManController.update(game.copy(),System.currentTimeMillis()+DELAY);
 			ghostController.update(game.copy(),System.currentTimeMillis()+DELAY);

 			try
			{
				int waited=DELAY/INTERVAL_WAIT;
				
				for(int j=0;j<DELAY/INTERVAL_WAIT;j++)
				{
					Thread.sleep(INTERVAL_WAIT);
					
					if(pacManController.hasComputed() && ghostController.hasComputed())
					{
						waited=j;
						break;
					}
				}
				
				if(fixedTime)
					Thread.sleep(((DELAY/INTERVAL_WAIT)-waited)*INTERVAL_WAIT);
				
				game.advanceGame(pacManController.getMove(),ghostController.getMove());	
			}
			catch(InterruptedException e)
			{
				e.printStackTrace();
			}
 	        
 	        if(visual)
 	        	gv.repaint();
 		}
 		
 		pacManController.terminate();
 		ghostController.terminate();
 	}
    
	/**
	 * Run a game in asynchronous mode and recorded.
	 *
     * @param pacManController The Pac-Man controller
     * @param ghostController The Ghosts controller
     * @param visual Whether to run the game with visuals
	 * @param fileName The file name of the file that saves the replay
	 */
	public void runGameTimedRecorded(Controller<MOVE> pacManController,Controller<EnumMap<GHOST,MOVE>> ghostController,boolean visual,String fileName)
	{
		StringBuilder replay=new StringBuilder();
		
		Game game=new Game(0);
		
		GameView gv=null;
		
		if(visual)
		{
			gv=new GameView(game).showGame();
			
			if(pacManController instanceof HumanController)
				gv.getFrame().addKeyListener(((HumanController)pacManController).getKeyboardInput());
		}		
		
		new Thread(pacManController).start();
		new Thread(ghostController).start();
		
		while(!game.gameOver())
		{
			pacManController.update(game.copy(),System.currentTimeMillis()+DELAY);
			ghostController.update(game.copy(),System.currentTimeMillis()+DELAY);

			try
			{
				Thread.sleep(DELAY);
			}
			catch(InterruptedException e)
			{
				e.printStackTrace();
			}

	        game.advanceGame(pacManController.getMove(),ghostController.getMove());	        
	        
	        if(visual)
	        	gv.repaint();
	        
	        replay.append(game.getGameState()+"\n");
		}
		
		pacManController.terminate();
		ghostController.terminate();
		
		saveToFile(replay.toString(),fileName,false);
	}
	
	/**
	 * Replay a previously saved game.
	 *
	 * @param fileName The file name of the game to be played
	 * @param visual Indicates whether or not to use visuals
	 */
	public void replayGame(String fileName,boolean visual)
	{
		ArrayList<String> timeSteps=loadReplay(fileName);
		
		Game game=new Game(0);
		
		GameView gv=null;
		
		if(visual)
			gv=new GameView(game).showGame();
		
		for(int j=0;j<timeSteps.size();j++)
		{			
			game.setGameState(timeSteps.get(j));

			try
			{
				Thread.sleep(DELAY);
			}
			catch(InterruptedException e)
			{
				e.printStackTrace();
			}
	        if(visual)
	        	gv.repaint();
		}
	}
	
	//save file for replays
    public static void saveToFile(String data,String name,boolean append)
    {
        try 
        {
            FileOutputStream outS=new FileOutputStream(name,append);
            PrintWriter pw=new PrintWriter(outS);

            pw.println(data);
            pw.flush();
            outS.close();

        } 
        catch (IOException e)
        {
            System.out.println("Could not save data!");	
        }
    }  

    //load a replay
    private static ArrayList<String> loadReplay(String fileName)
	{
    	ArrayList<String> replay=new ArrayList<String>();
		
        try
        {         	
        	BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));	 
            String input=br.readLine();		
            
            while(input!=null)
            {
            	if(!input.equals(""))
            		replay.add(input);

            	input=br.readLine();	
            }
        }
        catch(IOException ioe)
        {
            ioe.printStackTrace();
        }
        
        return replay;
	}
}