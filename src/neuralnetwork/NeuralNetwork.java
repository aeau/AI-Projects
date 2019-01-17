package neuralnetwork;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import behaviortree.DataManager;
import behaviortree.DataSaverLoader;
import dataRecording.DataTuple;
import dataRecording.DataTupleManager;
import neuralnetwork.Neuron.NeuronTypes;
import pacman.game.Constants.MOVE;

public class NeuralNetwork 
{
	/*
	public double[][] XOR_DATASET = { 	{ 1.0, 0.0, 1.0, 1.0 }, 
										{ 0.0, 0.0, 0.0, 0.0 },
										{ 0.0, 1.0, 1.0, 1.0 }, 
										{ 1.0, 1.0, 0.0, 0.0 } };*/
	
	//public ArrayList<PacmanInfo> dataset = new ArrayList<PacmanInfo>();
	Random rnd = new Random();
	public ArrayList<ModelInformation> modelInfoEachStep = new ArrayList<ModelInformation>();
	
	public ArrayList<DataTuple> dataset = new ArrayList<DataTuple>();
	public ArrayList<DataTuple> trainingSet = new ArrayList<DataTuple>();
	public ArrayList<DataTuple> testSet = new ArrayList<DataTuple>();
	public ArrayList<DataTuple> validationSet = new ArrayList<DataTuple>();
	
	public float[] setDivision = {0.7f, 0.2f, 0.1f}; //Training, test, validation
	public int batchSize = 70;
	public int max_epochs = 200;
	
	public int input_neurons_quantity = 9;
	public int output_neurons_quantity = 4;
	public ArrayList<Layer> neuralLayers = new ArrayList<Layer>();
	
	//For Learning RATE - the lower-upper bounds are calculated through LRRange-test
	public double stepSize = 0.0;
//	public double lowerBoundLR = 0.003;
//	public double upperBoundLR = 0.00325;
//	public double lowerBoundLR = 0.00263;
//	public double upperBoundLR = 0.00371;
//	public double lowerBoundLR = 0.001;
//	public double upperBoundLR = 0.002;
	public double lowerBoundLR = 0.001;
	public double upperBoundLR = 0.001327273;
	
	private void fillSet(ArrayList<DataTuple> set, int amount, ArrayList<DataTuple> fromDataSet)
	{
		for(int tuple = 0; tuple < amount; tuple++)
		{
			set.add(fromDataSet.remove(rnd.nextInt(fromDataSet.size())));
		}
	}
	
	public NeuralNetwork(int[] hiddenLayers)
	{
//		dataset = DataManager.LoadNeuralNetworkDataset("average_player.csv");
		
		////////////////////// LOAD AND DIVIDE DATASET /////////////
		dataset = DataTupleManager.LoadPacManDataArrayList();	
		
		int trainingTuples = (int) (dataset.size() * setDivision[0]);
		int testingTuples = (int) (dataset.size() * setDivision[1]);
		int validationTuples = (int) (dataset.size() * setDivision[2]);
		
		fillSet(validationSet, validationTuples, dataset);
		fillSet(testSet, testingTuples, dataset);
		fillSet(trainingSet, trainingTuples, dataset);

		////////////// GENERATE ANN ////////////////////

		//Fill the input neurons
		this.neuralLayers.add(new Layer(input_neurons_quantity, NeuronTypes.INPUT));
		this.neuralLayers.get(0).populateLayer();
		
		//Fill the hidden neurons
		for(int hiddenLayer : hiddenLayers)
		{
			Layer layer = new Layer(hiddenLayer, NeuronTypes.HIDDEN);
			layer.populateLayer();
			this.neuralLayers.add(layer);
		}
		
		//Fill the output neurons
		this.neuralLayers.add(new Layer(output_neurons_quantity, NeuronTypes.OUTPUT));
		this.neuralLayers.get(this.neuralLayers.size() - 1).populateLayer();
		
		//Go through the layers (from the second one) and connect each neuron in that layer to all the neurons in the prev layer
		//As bonus, add the Bias too!
		//Disclaimmer! this only works for Multi layered perceptron fully connected :D --> but it will work without any hidden layer too
		for(int layerCount = 1; layerCount < this.neuralLayers.size(); layerCount++)
		{
			for(int currentNeuron = 0; currentNeuron < this.neuralLayers.get(layerCount).size; currentNeuron++)
			{
				Neuron neuron = this.neuralLayers.get(layerCount).getNeuron(currentNeuron);
				neuron.AddConnection(new Neuron(NeuronTypes.BIAS, 1.0));
				
				for(int prevLayerNeuron = 0; prevLayerNeuron < this.neuralLayers.get(layerCount - 1).size; prevLayerNeuron++)
				{
					neuron.AddConnection(this.neuralLayers.get(layerCount - 1).getNeuron(prevLayerNeuron));
				}
			}
		}
	
		NonBatchBackPropagation(1.0f);
	}
	
	public NeuralNetwork(boolean a ) //This method was for testing the XOR GATE
	{
//		//Generate neural network fully connected
//		for(int i = 0; i < input_neurons_quantity; i++)
//		{
//			input_neurons.add(new Neuron(NeuronTypes.INPUT));
//		}
//		
//		hidden_neurons.add(new Neuron(NeuronTypes.HIDDEN));
//		hidden_neurons.get(0).AddConnection(input_neurons.get(0), 0.2);
//		hidden_neurons.get(0).AddConnection(input_neurons.get(1), 0.4);
//		hidden_neurons.get(0).AddConnection(input_neurons.get(2), -0.5);
//		hidden_neurons.get(0).AddConnection(new Neuron(NeuronTypes.BIAS, 1.0), -0.4);
//		
//		hidden_neurons.add(new Neuron(NeuronTypes.HIDDEN));
//		hidden_neurons.get(1).AddConnection(input_neurons.get(0), -0.3);
//		hidden_neurons.get(1).AddConnection(input_neurons.get(1), 0.1);
//		hidden_neurons.get(1).AddConnection(input_neurons.get(2), 0.2);
//		hidden_neurons.get(1).AddConnection(new Neuron(NeuronTypes.BIAS, 1.0), 0.2);
//		
//		output_neurons.add(new Neuron(NeuronTypes.OUTPUT));
//		output_neurons.get(0).AddConnection(hidden_neurons.get(0), -0.3);
//		output_neurons.get(0).AddConnection(hidden_neurons.get(1), -0.2);
//		output_neurons.get(0).AddConnection(new Neuron(NeuronTypes.BIAS, 1.0), 0.1);
//		
//		NonBatchBackPropagation(0.1f);
	}
	
	public NeuralNetwork(NeuralNetwork toCopy)
	{
		//TODO: Update to current use of layers 
		dataset = DataTupleManager.LoadPacManDataArrayList();	
		
		int trainingTuples = (int) (dataset.size() * setDivision[0]);
		int testingTuples = (int) (dataset.size() * setDivision[1]);
		int validationTuples = (int) (dataset.size() * setDivision[2]);
		
		fillSet(validationSet, validationTuples, dataset);
		fillSet(testSet, testingTuples, dataset);
		fillSet(trainingSet, trainingTuples, dataset);
		
	}
	
	private void CopyFromNetwork(NeuralNetwork toCopy)
	{
		//TODO: Update to current use of layers --- Remember there is an extra neuron in the hidden and output (bias)
	}
	
	public float getContinuousOutput(MOVE expectedOutput)
	{
		switch(expectedOutput)
		{
		case UP:			
			return 0.0f + rnd.nextFloat() * (0.24f - 0.0f) + 0.0f;
		case RIGHT:
			return 0.25f + rnd.nextFloat() * (0.24f - 0.0f) + 0.0f;
		case DOWN:
			return 0.5f + rnd.nextFloat() * (0.24f - 0.0f) + 0.0f;
		case LEFT:
			return 0.75f + rnd.nextFloat() * (0.24f - 0.0f) + 0.0f;
		default:
			return -1.0f;
		}
		
	}
	
	/***
	 * We update connection weights after each tuple
	 * @param learning_ratee
	 */
	public void NonBatchBackPropagation(float learning_rate)
	{
		double deltaThreshold = 0.001f;
		double accuracyThreshold = 0.80;
		double learningRate = 0.005;
		int iterations = trainingSet.size() / batchSize;
		int currentBatch = 0;
		stepSize = 2.0 * iterations;
		
		for(int epoch = 0; epoch < max_epochs; epoch++)
		{
//			System.out.println("EPOCH " + epoch);
			double currentHighestDelta = 0.0;
			double currDelta = 0.0f;
			double currentAccuracy = 0.0;
			learningRate = findLearningRate(epoch);
			
			for(int iter = 0; iter < iterations; iter++ )
			{
				currentHighestDelta = 0.0; 
				currDelta = 0.0f;          
				currentAccuracy = 0.0;
				currentBatch = iter * batchSize;
				
				for(int tuple = currentBatch; tuple < currentBatch + batchSize; tuple++)
				{
					//step 1: feedforward
					FeedForward(tuple, trainingSet);
					int expectedOutput = trainingSet.get(tuple).DirectionChosen.ordinal();
					
					//This work unless you have connections within the same layer --> Then you need to handle it in a different way
					for(int layerCount = this.neuralLayers.size() - 1; layerCount > 1; layerCount--)
					{
						for(int currentNeuron = 0; currentNeuron < this.neuralLayers.get(layerCount).size; currentNeuron++)
						{
							Neuron neuron = this.neuralLayers.get(layerCount).getNeuron(currentNeuron);
							
							//Step 2: Calculate accumulated error (only important for hidden neurons)
							if(layerCount !=  this.neuralLayers.size() - 1)
								neuron.GetAccumulatedError(this.neuralLayers.get(layerCount + 1).getNeurons());
							
							//Step 3: Calculate error! 
							if(currentNeuron == expectedOutput)
							{
								neuron.CalculateError(0.9);
							}
							else
							{
								neuron.CalculateError(0.1);
							}
							
							//Step 4: Accumulate the delta weights
							currDelta = neuron.AccumulateWeights(learningRate);
							if(currDelta > currentHighestDelta)
								currentHighestDelta = currDelta;
						}
					}
					
					//Step 5: Correct all the weights (This is Online)
					for(int layerCount = this.neuralLayers.size() - 1; layerCount > 1; layerCount--)
					{
						for(int currentNeuron = 0; currentNeuron < this.neuralLayers.get(layerCount).size; currentNeuron++)
						{
							this.neuralLayers.get(layerCount).getNeuron(currentNeuron).ChangeWeights(learningRate);;
						}
					}
				}
				
//				if(currentHighestDelta <= deltaThreshold)
//				{
//					System.out.println("IS OVER, WEIGHT IS NOT MODIFIED ENOUGH");
//					return;
//				}
				
//				if(currentAccuracy >= accuracyThreshold)
//				{
//					System.out.println("IS OVER, ACCURATE ENOUGH");
//					return;
//				}
			}

			
			///////////////// DO FINAL TEST OF ONLY THE EPOCH //////////////////
			ModelInformation currentModel = new ModelInformation(learningRate, epoch, -1, batchSize, iterations, max_epochs);
			modelInfoEachStep.add(currentModel);
			
			TestNetwork(epoch, -1, validationSet, DatasetUses.VALIDATION, true, false, currentModel, DebugMode.EVERYTHING);
//			TestNetwork(epoch, -1, trainingSet, DatasetUses.TRAINING, true, false, currentModel, DebugMode.EVERYTHING);
			System.out.println("----------------------------------");

			if(currentModel.getAccuracy(DatasetUses.VALIDATION) >= accuracyThreshold)
			{
				System.out.println("IS OVER, ACCURATE ENOUGH");
				return;
			}
		
		}
		
		saveModelInformation("Execution-" + LocalDateTime.now().getNano());
		System.out.println("IS OVER!");
	}
	
	private void TestNetwork(int epoch, int iteration, ArrayList<DataTuple> set, 
							DatasetUses setName, boolean getLoss, boolean saveValues, 
							ModelInformation modelInfo, DebugMode debug)
	{
		float success = 0.0f;
		float failure = 0.0f;
		float setSize = set.size();
		double loss = 0.0;
		
		for(int k = 0; k < setSize; k++)
		{

			//step 1: feedforward
			FeedForward(k, set);
			
			//step 2: check if it was good classifications!
			if(Classify(k, set, false))
			{
				success++;
			}
			else
			{
				failure++;
			}
			
			if(getLoss)
				loss += defaultLossFunction(set.get(k).DirectionChosen.ordinal(), 0.9);
			
		}
		
		loss /= setSize;
		
		//////////////////////////////////////////////////////
		
		switch(debug)
		{
		case NONE:
			break;
		case ACCURACY:
			System.out.println("in " + setName + ", SUCCESS: " + (success * 100.0f)/setSize + "%; FAILURE: " + (failure * 100.0f)/setSize  + "%; TOTAL: " + setSize);
			break;
		case LOSS:
			System.out.println("Loss in " + setName + " at epoch.iteration " + epoch + "." + iteration + ": " + 
					loss + ", " + success/setSize);
			break;
		case EVERYTHING:
			System.out.println("Loss and accuracy in " + setName + " at epoch.iteration " + epoch + "." + iteration + ": " + 
								loss + ", " + success/setSize);
			break;
		default:
			break;
		}

		if(saveValues)
		{
			String inf = epoch + ";" + loss + ";" + success/setSize;
			DataSaverLoader.saveFile("loss-value" + setName + ".csv", inf, true);
		}
		
		modelInfo.setAccuracy(setName, success/setSize);
		modelInfo.setLoss(setName, loss);
	}
	
	private double defaultLossFunction(int expectedIndex, double expectedOutcome)
	{
		double squaredError = 0.0;
		ArrayList<Neuron> outputNeurons = neuralLayers.get(neuralLayers.size() - 1).getNeurons();
		for(int currentNeuron = 0; currentNeuron < outputNeurons.size(); currentNeuron++)
		{
			//Step 2: Calculate error! 
			if(currentNeuron == expectedIndex)
			{
				squaredError += Math.pow(0.9 - outputNeurons.get(currentNeuron).output, 2.0);
			}
			else
			{
				squaredError += Math.pow(0.1 - outputNeurons.get(currentNeuron).output, 2.0);
			}
		}
		
		return squaredError/(double)outputNeurons.size();
	}
	
	public double findLearningRate(double currentEpoch)
	{
		double localLR = 0.0;
		
		double localCycle = Math.floor(1.0 + currentEpoch/(2.0*stepSize));
		double localX = Math.abs(currentEpoch/stepSize - 2.0 * localCycle + 1.0);
		localLR = lowerBoundLR + (upperBoundLR - lowerBoundLR) * Math.max(0,  (1- localX));
		
		return localLR;
	}
	
	public void saveModelInformation(String filename)
	{
		String luAcc = "Epoch;Iteration;Learning Rate;Training-loss;Test-loss;Validation-loss;Training-accuracy;Test-accuracy;Validation-Accuracy";
		DataSaverLoader.saveFile(filename + ".csv", luAcc, true);
		
		for(ModelInformation mi : modelInfoEachStep)
		{
			luAcc = mi.epoch + ";";
			luAcc += mi.iteration + ";";
			luAcc += mi.learningRate + ";";
			luAcc += mi.getLoss(DatasetUses.TRAINING) + ";";
			luAcc += mi.getLoss(DatasetUses.TEST) + ";";
			luAcc += mi.getLoss(DatasetUses.VALIDATION) + ";";
			luAcc += mi.getAccuracy(DatasetUses.TRAINING) + ";";
			luAcc += mi.getAccuracy(DatasetUses.TEST) + ";";
			luAcc += mi.getAccuracy(DatasetUses.VALIDATION);
			 
			 DataSaverLoader.saveFile(filename + ".csv", luAcc, true);
		}
	}
	
	public MOVE moveOutput(double value)
	{
		if(value < 0.25f)
		{
			return MOVE.UP;
		}
		else if(value < 0.5f)
		{
			return MOVE.RIGHT;
		}
		else if(value < 0.75f)
		{
			return MOVE.DOWN;
		}
		else
		{
			return MOVE.LEFT;
		}
	}
	
	public boolean Classify(int index, ArrayList<DataTuple> set, boolean debug)
	{
		int selected_index = 0;
		double best_value = -99999.9;
		
		ArrayList<Neuron> outputNeurons = neuralLayers.get(neuralLayers.size() - 1).getNeurons();
		for(int currentNeuron = 0; currentNeuron < outputNeurons.size(); currentNeuron++)
		{
			if(outputNeurons.get(currentNeuron).output > best_value)
			{
				best_value = outputNeurons.get(currentNeuron).output;
				selected_index = currentNeuron;
			}
		}
		
		if(debug)
		{
			System.out.println("NEURAL OUTPUT: " + selected_index);
			System.out.println("EXPECTED OUTPUT: " + set.get(index).DirectionChosen.ordinal());
		}
		
		return set.get(index).DirectionChosen.ordinal() == selected_index;
	}
	
	public void FeedForward(int index, ArrayList<DataTuple> set)
	{
		neuralLayers.get(0).getNeuron(0).CalculateOutput(set.get(index).n_neighbor);
		neuralLayers.get(0).getNeuron(1).CalculateOutput(set.get(index).e_neighbor);
		neuralLayers.get(0).getNeuron(2).CalculateOutput(set.get(index).s_neighbor);
		neuralLayers.get(0).getNeuron(3).CalculateOutput(set.get(index).w_neighbor);
		neuralLayers.get(0).getNeuron(4).CalculateOutput(set.get(index).closest_ghost_dist);
		neuralLayers.get(0).getNeuron(5).CalculateOutput(set.get(index).closest_pill_dist);
		neuralLayers.get(0).getNeuron(6).CalculateOutput(set.get(index).closest_pp_dist);
		neuralLayers.get(0).getNeuron(7).CalculateOutput(set.get(index).num_pill_left_norm);
		neuralLayers.get(0).getNeuron(8).CalculateOutput(set.get(index).num_pp_left_norm);
		
		//Calculate the output of hidden and output neurons
		for(int layerCount = 1; layerCount < this.neuralLayers.size(); layerCount++)
		{
			for(Neuron neuron : this.neuralLayers.get(layerCount).getNeurons())
			{
				neuron.CalculateOutput(0.0);
			}
		}
	}
	
	public void FeedForward(/*ArrayList<Double> tuple*/ DataTuple tuple)
	{
		neuralLayers.get(0).getNeuron(0).CalculateOutput(tuple.n_neighbor);         
		neuralLayers.get(0).getNeuron(1).CalculateOutput(tuple.e_neighbor);         
		neuralLayers.get(0).getNeuron(2).CalculateOutput(tuple.s_neighbor);         
		neuralLayers.get(0).getNeuron(3).CalculateOutput(tuple.w_neighbor);         
		neuralLayers.get(0).getNeuron(4).CalculateOutput(tuple.closest_ghost_dist); 
		neuralLayers.get(0).getNeuron(5).CalculateOutput(tuple.closest_pill_dist);  
		neuralLayers.get(0).getNeuron(6).CalculateOutput(tuple.closest_pp_dist);    
		neuralLayers.get(0).getNeuron(7).CalculateOutput(tuple.num_pill_left_norm); 
		neuralLayers.get(0).getNeuron(8).CalculateOutput(tuple.num_pp_left_norm);   
		
		//Calculate the output of hidden and output neurons
		for(int layerCount = 1; layerCount < this.neuralLayers.size(); layerCount++)
		{
			for(Neuron neuron : this.neuralLayers.get(layerCount).getNeurons())
			{
				neuron.CalculateOutput(0.0);
			}
		}
	}
	
	/***
	 * We update connectiong weights after each epoch
	 * @param learning_ratee
	 */
	public void BatchBackPropagation(float learning_rate)
	{
		//TODO: Make this a batch back propagation -- updating the weights after each batch rather than after each delta calculation
	}
	
	
}
