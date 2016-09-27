package neuralnetwork;

import java.util.ArrayList;
import java.util.Random;

import behaviortree.DataManager;
import behaviortree.DataSaverLoader;
import dataRecording.DataTuple;
import dataRecording.DataTupleManager;
import neuralnetwork.Neuron.NeuronTypes;

public class NeuralNetwork 
{
	/*
	public double[][] XOR_DATASET = { 	{ 1.0, 0.0, 1.0, 1.0 }, 
										{ 0.0, 0.0, 0.0, 0.0 },
										{ 0.0, 1.0, 1.0, 1.0 }, 
										{ 1.0, 1.0, 0.0, 0.0 } };*/
	
	//public ArrayList<PacmanInfo> dataset = new ArrayList<PacmanInfo>();
	public ArrayList<DataTuple> dataset = new ArrayList<DataTuple>();
	
	public int input_neurons_quantity = 9;
	public int output_neurons_quantity = 4;
	
	//kolmogorov theorem
	public int hidden_neurons_quantity = 19;
	public int max_epochs = 2500;
	
	public ArrayList<Neuron> input_neurons = new ArrayList<Neuron>();
	public ArrayList<Neuron> hidden_neurons = new ArrayList<Neuron>();
	public ArrayList<Neuron> output_neurons = new ArrayList<Neuron>();
	
	public NeuralNetwork()
	{
		//dataset = DataManager.LoadNeuralNetworkDataset("average_player.csv");
		dataset = DataTupleManager.LoadPacManDataArrayList();
		
		//Generate neural network fully connected
		for(int i = 0; i < input_neurons_quantity; i++)
		{
			input_neurons.add(new Neuron(NeuronTypes.INPUT));
		}
		
		for(int i = 0; i < hidden_neurons_quantity; i++)
		{
			hidden_neurons.add(new Neuron(NeuronTypes.HIDDEN));
			for(int j = 0; j < input_neurons_quantity; j++)
			{
				hidden_neurons.get(i).AddConnection(input_neurons.get(j));
			}
		}
		
		for(int i = 0; i < output_neurons_quantity; i++)
		{
			output_neurons.add(new Neuron(NeuronTypes.OUTPUT));
			for(int j = 0; j < hidden_neurons_quantity; j++)
			{
				output_neurons.get(i).AddConnection(hidden_neurons.get(j));
			}
			
			//Highway network
			/*
			for(int j = 0; j < input_neurons_quantity; j++)
			{
				output_neurons.get(i).AddConnection(input_neurons.get(j));
			}*/
		}
		
		NonBatchBackPropagation(0.1f);
	}
	
	public NeuralNetwork(boolean a )
	{
		//Generate neural network fully connected
		for(int i = 0; i < input_neurons_quantity; i++)
		{
			input_neurons.add(new Neuron(NeuronTypes.INPUT));
		}
		
		hidden_neurons.add(new Neuron(NeuronTypes.HIDDEN));
		hidden_neurons.get(0).AddConnection(input_neurons.get(0), 0.2);
		hidden_neurons.get(0).AddConnection(input_neurons.get(1), 0.4);
		hidden_neurons.get(0).AddConnection(input_neurons.get(2), -0.5);
		hidden_neurons.get(0).AddConnection(new Neuron(NeuronTypes.BIAS, 1.0), -0.4);
		
		hidden_neurons.add(new Neuron(NeuronTypes.HIDDEN));
		hidden_neurons.get(1).AddConnection(input_neurons.get(0), -0.3);
		hidden_neurons.get(1).AddConnection(input_neurons.get(1), 0.1);
		hidden_neurons.get(1).AddConnection(input_neurons.get(2), 0.2);
		hidden_neurons.get(1).AddConnection(new Neuron(NeuronTypes.BIAS, 1.0), 0.2);
		
		output_neurons.add(new Neuron(NeuronTypes.OUTPUT));
		output_neurons.get(0).AddConnection(hidden_neurons.get(0), -0.3);
		output_neurons.get(0).AddConnection(hidden_neurons.get(1), -0.2);
		output_neurons.get(0).AddConnection(new Neuron(NeuronTypes.BIAS, 1.0), 0.1);
		
		NonBatchBackPropagation(0.1f);
	}
	
	/***
	 * We update connection weights after each tuple
	 * @param learning_ratee
	 */
	public void NonBatchBackPropagation(float learning_rate)
	{
		Random rnd = new Random();
		int next_test = 300;
		int test_neural_network = next_test;
		
		for(int i = 0; i < max_epochs; i++)
		{
			System.out.println("EPOCH " + i);
			
			learning_rate = 0.5f;
			for(int k = 0; k < dataset.size(); k++)
			{
				//step 1: feedforward
				FeedForward(k);
				
				//step 2: calculate error
				for(int j = 0; j < output_neurons_quantity; j++)
				{
					if(dataset.get(k).DirectionChosen.ordinal() == j)
					{
						output_neurons.get(j).CalculateError(1.0);
					}
					else
					{
						output_neurons.get(j).CalculateError(0.0);
					}
					
				}
				for(Neuron n : hidden_neurons)
				{
					n.GetAccumulatedError(output_neurons);
					n.CalculateError(0.0);
				}
				
				//step 3: correct all weights
				for(Neuron n : output_neurons)
				{
					n.CorrectWeights(learning_rate);
				}
				for(Neuron n : hidden_neurons)
				{
					n.CorrectWeights(learning_rate);
				}
				
			}
			
			//Test 
			if(test_neural_network == i)
			{
				System.out.println("EPOCH " + i + ":");
				int success = 0;
				int failure = 0;
				int tries = (dataset.size() * 10)/100;
				for(int k = 0; k < tries; k++)
				{
					int test = rnd.nextInt(dataset.size());
					//step 1: feedforward
					FeedForward(test);
					
					if(DebugOutput(test))
					{
						success++;
					}
					else
					{
						failure++;
					}
					
					System.out.println();
				}
				
				System.out.println();
				String inf = "EPOCH " + i + ":" + System.getProperty("line.separator"); 
				inf = "SUCCESS: " + success + "; FAILURE: " + failure + "; TOTAL: " + tries + System.getProperty("line.separator");
				inf += "SUCCESS: " + (success * 100)/tries + "%; FAILURE: " + (failure * 100)/tries  + "%; TOTAL: " + tries + System.getProperty("line.separator");
				System.out.println("SUCCESS: " + success + "; FAILURE: " + failure + "; TOTAL: " + tries);
				System.out.println("SUCCESS: " + (success * 100)/tries + "%; FAILURE: " + (failure * 100)/tries  + "%; TOTAL: " + tries);
				test_neural_network += next_test;
				
				DataSaverLoader.saveFile("Information.txt", inf, true);
			}
			
			//step 2: calculate error
			
			//step 3: check if 0 error, or minimum threshold reached
			//if not: repeat  from step 1.
			
		}
	}
	
	public boolean DebugOutput(int first_pos)
	{
		/*
		System.out.print("INPUTS: ");
		
		for(int j = 0; j < input_neurons_quantity; j++)
		{
			System.out.print(dataset.get(first_pos).info.get(j) + "; ");
		}
		*/
		System.out.println();
		System.out.println("EXPECTED OUTPUT: " + dataset.get(first_pos).DirectionChosen);
		System.out.println("EXPECTED OUTPUT: " + dataset.get(first_pos).DirectionChosen.ordinal());
		int selected_index = 0;
		double best_value = Double.MIN_VALUE;
		
		for(int i = 0; i < output_neurons_quantity; i++)
		{
			if(output_neurons.get(i).output > best_value)
			{
				best_value = output_neurons.get(i).output;
				selected_index = i;
			}
		}
		System.out.println("NEURAL OUTPUT: " + selected_index);
		
		if(dataset.get(first_pos).DirectionChosen.ordinal() == selected_index)
		{
			return true;
		}
		else
		{
			return false;
		}
		
	}
	
	public void FeedForward(int first_pos)
	{
		//With this method we cannot use loop for input neurons
		input_neurons.get(0).CalculateOutput(dataset.get(first_pos).n_neighbor);
		input_neurons.get(1).CalculateOutput(dataset.get(first_pos).e_neighbor);
		input_neurons.get(2).CalculateOutput(dataset.get(first_pos).s_neighbor);
		input_neurons.get(3).CalculateOutput(dataset.get(first_pos).w_neighbor);
		input_neurons.get(4).CalculateOutput(dataset.get(first_pos).closest_ghost_dist);
		input_neurons.get(5).CalculateOutput(dataset.get(first_pos).closest_pill_dist);
		input_neurons.get(6).CalculateOutput(dataset.get(first_pos).closest_pp_dist);
		input_neurons.get(7).CalculateOutput(dataset.get(first_pos).num_pill_left_norm);
		input_neurons.get(8).CalculateOutput(dataset.get(first_pos).num_pp_left_norm);
		
		/*
		for(int j = 0; j < input_neurons_quantity; j++)
		{
			input_neurons.get(j).CalculateOutput(dataset.get(first_pos).info.get(j));
		}
		*/
		for(Neuron n : hidden_neurons)
		{
			n.CalculateOutput(0.0);
		}
		
		for(Neuron n : output_neurons)
		{
			n.CalculateOutput(0.0);
		}
	}
	
	public void FeedForward(/*ArrayList<Double> tuple*/ DataTuple tuple)
	{
		//With this method we cannot use loop for input neurons
		input_neurons.get(0).CalculateOutput(tuple.n_neighbor);
		input_neurons.get(1).CalculateOutput(tuple.e_neighbor);
		input_neurons.get(2).CalculateOutput(tuple.s_neighbor);
		input_neurons.get(3).CalculateOutput(tuple.w_neighbor);
		input_neurons.get(4).CalculateOutput(tuple.closest_ghost_dist);
		input_neurons.get(5).CalculateOutput(tuple.closest_pill_dist);
		input_neurons.get(6).CalculateOutput(tuple.closest_pp_dist);
		input_neurons.get(7).CalculateOutput(tuple.num_pill_left_norm);
		input_neurons.get(8).CalculateOutput(tuple.num_pp_left_norm);
		
		/*
		for(int j = 0; j < input_neurons_quantity; j++)
		{
			input_neurons.get(j).CalculateOutput(tuple.get(j));
		}*/
		for(Neuron n : hidden_neurons)
		{
			n.CalculateOutput(0.0);
		}
		
		for(Neuron n : output_neurons)
		{
			n.CalculateOutput(0.0);
		}
	}
	
	/***
	 * We update connectiong weights after each epoch
	 * @param learning_ratee
	 */
	public void BatchBackPropagation(float learning_ratee)
	{
		
	}
	
	
}
