package pacman.controllers.examples;

import dataRecording.DataTuple;
import neuralnetwork.NeuralNetwork;
import neuralnetwork.Neuron;
import neuralnetwork.PacmanInfo;
import pacman.controllers.Controller;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

public class NeuralNetworkPacMan extends Controller<MOVE>{

	NeuralNetwork nn;
	MOVE next_move;
	
	public NeuralNetworkPacMan(NeuralNetwork nn)
	{
		super();
		this.nn = nn;
	}
	
	@Override
	public MOVE getMove(Game game, long timeDue) 
	{
		/*
		String line = game.GetNeuralNetworkInfo(false);
		String[] arr = line.split(";"); 
		PacmanInfo pi = new PacmanInfo(arr);
		
		nn.FeedForward(pi.info);
		*/
		
		DataTuple tuple = new DataTuple(game);
		nn.FeedForward(tuple);
		
		//Select correct output
		int selected_index = 0;
		double best_value = Double.MIN_VALUE;
		
		for(int i = 0; i < nn.output_neurons_quantity; i++)
		{
			if(nn.output_neurons.get(i).output > best_value)
			{
				best_value = nn.output_neurons.get(i).output;
				selected_index = i;
			}
		}
		
		next_move = MOVE.values()[selected_index];
		
		return next_move;
	}

}
