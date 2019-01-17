package pacman.controllers.examples;

import java.util.ArrayList;
import java.util.List;

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
	boolean usingPossibleMoves = false;
	
	public NeuralNetworkPacMan(NeuralNetwork nn, boolean possibleMoves)
	{
		super();
		this.nn = nn;
		usingPossibleMoves = possibleMoves;
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
		MOVE[] possibleMoves = game.getPossibleMoves(game.getPacmanCurrentNodeIndex(), game.getPacmanLastMoveMade());
		
		//Select correct output
		int selected_index = 0;
		double best_value = Double.MIN_VALUE;
		ArrayList<Neuron> outputNeurons = nn.neuralLayers.get(nn.neuralLayers.size() - 1).getNeurons();
		
		
		if(usingPossibleMoves)
		{
			for (MOVE move : possibleMoves) {
				int i = move.ordinal();
				double val = outputNeurons.get(i).output;
				if (val > best_value) {
					selected_index = i;
					best_value = val;
				}
			}
		}
		else
		{
			for(int currentNeuron = 0; currentNeuron < outputNeurons.size(); currentNeuron++)
			{
				System.out.println("OUTPUT " + currentNeuron + ": " + outputNeurons.get(currentNeuron).output);
				if(outputNeurons.get(currentNeuron).output > best_value)
				{
					best_value = outputNeurons.get(currentNeuron).output;
					selected_index = currentNeuron;
				}
			}
		}

		next_move = MOVE.values()[selected_index];
		System.out.println(next_move);
		return next_move;
	}

}
