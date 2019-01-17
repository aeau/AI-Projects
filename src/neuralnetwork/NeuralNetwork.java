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
	
	//kolmogorov theorem
	public int hidden_neurons_quantity = 60;
	
	public ArrayList<Neuron> input_neurons = new ArrayList<Neuron>();
	public ArrayList<Neuron> hidden_neurons = new ArrayList<Neuron>();
	public ArrayList<Neuron> output_neurons = new ArrayList<Neuron>();
	public ArrayList<Layer> neuralLayers = new ArrayList<Layer>();
	
	//For Learning RATE
	public double stepSize = 0.0;
//	public double lowerBoundLR = 0.003;
//	public double upperBoundLR = 0.00325;
//	public double lowerBoundLR = 0.00263;
//	public double upperBoundLR = 0.00371;
//	public double lowerBoundLR = 0.001;
//	public double upperBoundLR = 0.002;
	public double lowerBoundLR = 0.001;
	public double upperBoundLR = 0.001327273;
	
	//For testing
	double Acc = 0.0;
	
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
		
//////////////////////// PREV WAY OF DOING IT! //////////////////////////////
		
		//Create all the input neurons
//		for(int i = 0; i < input_neurons_quantity; i++)
//		{
//			input_neurons.add(new Neuron(NeuronTypes.INPUT));
//		}
//		
//		for(int i = 0; i < hidden_neurons_quantity; i++)
//		{
//			hidden_neurons.add(new Neuron(NeuronTypes.HIDDEN));
//			hidden_neurons.get(i).AddConnection(new Neuron(NeuronTypes.BIAS, 1.0));
//			for(int j = 0; j < input_neurons_quantity; j++)
//			{
//				hidden_neurons.get(i).AddConnection(input_neurons.get(j));
//			}
//		}
//		
//		for(int i = 0; i < output_neurons_quantity; i++)
//		{
//			output_neurons.add(new Neuron(NeuronTypes.OUTPUT));
//			output_neurons.get(i).AddConnection(new Neuron(NeuronTypes.BIAS, 1.0));
//			for(int j = 0; j < hidden_neurons_quantity; j++)
//			{
//				output_neurons.get(i).AddConnection(hidden_neurons.get(j));
//			}
//			
//			//Highway network
//			/*
//			for(int j = 0; j < input_neurons_quantity; j++)
//			{
//				output_neurons.get(i).AddConnection(input_neurons.get(j));
//			}*/
//		}
		
////////////////////////PREV WAY OF DOING IT! //////////////////////////////
		
		NonBatchBackPropagation(1.0f);
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
	
	public NeuralNetwork(NeuralNetwork toCopy)
	{
		dataset = DataTupleManager.LoadPacManDataArrayList();	
		
		int trainingTuples = (int) (dataset.size() * setDivision[0]);
		int testingTuples = (int) (dataset.size() * setDivision[1]);
		int validationTuples = (int) (dataset.size() * setDivision[2]);
		
		fillSet(validationSet, validationTuples, dataset);
		fillSet(testSet, testingTuples, dataset);
		fillSet(trainingSet, trainingTuples, dataset);

		//Generate neural network fully connected
		for(int i = 0; i < input_neurons_quantity; i++)
		{
			input_neurons.add(new Neuron(NeuronTypes.INPUT));
		}
		
		for(int i = 0; i < hidden_neurons_quantity; i++)
		{
			hidden_neurons.add(new Neuron(NeuronTypes.HIDDEN));
			hidden_neurons.get(i).AddConnection(new Neuron(NeuronTypes.BIAS, 1.0), toCopy.hidden_neurons.get(i).connections.get(0).weight);
			for(int j = 0; j < input_neurons_quantity; j++)
			{
				hidden_neurons.get(i).AddConnection(input_neurons.get(j), toCopy.hidden_neurons.get(i).connections.get(j+1).weight);
			}
		}
		
		for(int i = 0; i < output_neurons_quantity; i++)
		{
			output_neurons.add(new Neuron(NeuronTypes.OUTPUT));
			output_neurons.get(i).AddConnection(new Neuron(NeuronTypes.BIAS, 1.0), toCopy.output_neurons.get(i).connections.get(0).weight);
			for(int j = 0; j < hidden_neurons_quantity; j++)
			{
				output_neurons.get(i).AddConnection(hidden_neurons.get(j),  toCopy.output_neurons.get(i).connections.get(j+1).weight);
			}
			
		}
	}
	
	private NeuralNetwork getCopyState()
	{
		NeuralNetwork copy = new NeuralNetwork(this);
		return copy;
	}
	
	private void CopyFromNetwork(NeuralNetwork toCopy)
	{
		for(int i = 0; i < hidden_neurons_quantity; i++)
		{
			for(int j = 0; j < input_neurons_quantity+1; j++) //+1 for bias
			{
				hidden_neurons.get(i).connections.get(j).weight = toCopy.hidden_neurons.get(i).connections.get(j).weight;
			}
		}
		
		for(int i = 0; i < output_neurons_quantity; i++)
		{
			for(int j = 0; j < hidden_neurons_quantity+1; j++) //+1 for bias
			{
				output_neurons.get(i).connections.get(j).weight = toCopy.output_neurons.get(i).connections.get(j).weight;
			}
			
		}
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
		int maxWorstSteps = 10;
		int worseningSteps = 0;
		int iterationCounter = 0;
		
//		NeuralNetwork bestSoFar = new NeuralNetwork(this);
//		
//		String luAcc = "Learning Rate;loss;accuracy";
//		DataSaverLoader.saveFile("LRRangeTest-loss.csv", luAcc, true);

		for(int epoch = 0; epoch < max_epochs; epoch++)
		{
//			System.out.println("EPOCH " + epoch);
			double currentHighestDelta = 0.0;
			double currDelta = 0.0f;
			double currentAccuracy = 0.0;
			learningRate = findLearningRate(epoch);
			
			for(int iter = 0; iter < iterations; iter++ )
			{
//				learningRate = findLearningRate(iterationCounter++);
//				learningRate = calculateLocalLearningRate(iter);
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
							
							//Step 1: Calculate accumulated error (only important for hidden neurons)
							if(layerCount !=  this.neuralLayers.size() - 1)
								neuron.GetAccumulatedError(this.neuralLayers.get(layerCount + 1).getNeurons());
							
							//Step 2: Calculate error! 
							if(currentNeuron == expectedOutput)
							{
								neuron.CalculateError(0.9);
							}
							else
							{
								neuron.CalculateError(0.1);
							}
							
							//Step 3: Accumulate the delta weights
							currDelta = neuron.AccumulateWeights(learningRate);
							if(currDelta > currentHighestDelta)
								currentHighestDelta = currDelta;
						}
					}
					
					//Change all the weights
					for(int layerCount = this.neuralLayers.size() - 1; layerCount > 1; layerCount--)
					{
						for(int currentNeuron = 0; currentNeuron < this.neuralLayers.get(layerCount).size; currentNeuron++)
						{
							this.neuralLayers.get(layerCount).getNeuron(currentNeuron).ChangeWeights(learningRate);;
						}
					}
					

					
					//TESTING NEW OUTPUT
					
//					for(Neuron output : output_neurons)
//					{
//						output.CalculateError(getContinuousOutput(trainingSet.get(tuple).DirectionChosen));
//					}
//					
					
					//step 2: Did I classify correctly? --> This needs more refinement
					if(Classify(tuple, trainingSet, false))
					{
						

					}
					else
					{

					}
					

//					
//					//step 2: calculate error
//					for(int j = 0; j < output_neurons_quantity; j++)
//					{
//						if(expectedOutput == j)
//						{
//							output_neurons.get(j).CalculateError(1.0);
//						}
//						else
//						{
//							output_neurons.get(j).CalculateError(0.0);
//						}
//						
//					}
//					
//					//Calculate the delta weight of the output neurons
//					for(Neuron n : output_neurons)
//					{
//						currDelta = n.AccumulateWeights(learningRate);
//						if(currDelta > currentHighestDelta)
//							currentHighestDelta = currDelta;
//					}
//					
//					//Calcualte the error of the hidden neurons
//					for(Neuron n : hidden_neurons)
//					{
//						n.GetAccumulatedError(output_neurons);
//						n.CalculateError(0.0);
//					}
//					
//					for(Neuron n : hidden_neurons)
//					{
//						currDelta = n.AccumulateWeights(learningRate);
//						if(currDelta > currentHighestDelta)
//							currentHighestDelta = currDelta;
//					}
//					/////////// CHANGE THE WEIGHT OF ALL THE NEURONS //////////////
//					
//					for(Neuron n : output_neurons)
//					{
//						n.ChangeWeights(learningRate);
//					}
//					
//					//step 3: correct all weights
//					for(Neuron n : hidden_neurons)
//					{
//						n.ChangeWeights(learningRate);
//					}

				}
				
				/////////BATCH IS DONE - NOW TEST //////////
				
//				//Change all the weights
//				for(int layerCount = this.neuralLayers.size() - 1; layerCount > 1; layerCount--)
//				{
//					for(int currentNeuron = 0; currentNeuron < this.neuralLayers.get(layerCount).size; currentNeuron++)
//					{
//						this.neuralLayers.get(layerCount).getNeuron(currentNeuron).ChangeWeights(learningRate);;
//					}
//				}
//				
				//Test the accuracy with the validation set
//				currentAccuracy = TestNetwork(epoch, iter, validationSet);
//				Acc = currentAccuracy;
//				
				
				/////////////// SAVING AND LOADING OF THE BEST NETWORKS -- ALLOWED TO GET WORSE BY 5 TIMES ///////////////
				
				
//				if(currentAccuracy >= bestSoFar.Acc)
//				{
//					bestSoFar = new NeuralNetwork(this);
//					bestSoFar.Acc = currentAccuracy;
//					worseningSteps = 0;
//				}
//				else
//				{
//					worseningSteps++;
//				}
//			
//				if(worseningSteps > maxWorstSteps)
//				{
//					CopyFromNetwork(bestSoFar);
//					Acc = bestSoFar.Acc;
//					
//					System.out.println("Copying new network!");
//					TestNetwork(epoch, iter);
//					
//					worseningSteps = 0;
//				}
				
				//step 3: check if 0 error, or minimum threshold reached
//				//if not: repeat  from step 1.
//				if(currentHighestDelta <= deltaThreshold)
//				{
//					System.out.println("IS OVER, WEIGHT IS NOT MODIFIED ENOUGH");
//					return;
//				}
				
//				LowerUpperBounds.put(learningRate, currentAccuracy);
//				

//				System.out.println();
//				System.out.println("CURRENT HIGHEST DELTA = " + currentHighestDelta);
//				System.out.println();
////		
				if(currentAccuracy >= accuracyThreshold)
				{
					System.out.println("IS OVER, ACCURATE ENOUGH");
					return;
				}
			}
			
//			CopyFromNetwork(bestSoFar);
			
			///////////////// DO FINAL TEST OF ONLY THE EPOCH //////////////////
			ModelInformation currentModel = new ModelInformation(learningRate, epoch, -1, batchSize, iterations, max_epochs);
			modelInfoEachStep.add(currentModel);
//			currentAccuracy = TestNetwork(epoch, testSet);
			TestNetwork(epoch, -1, validationSet, DatasetUses.VALIDATION, true, false, currentModel, DebugMode.EVERYTHING);
//			TestNetwork(epoch, -1, trainingSet, DatasetUses.TRAINING, true, false, currentModel, DebugMode.EVERYTHING);
			System.out.println("----------------------------------");
//			
//			luAcc = learningRate + ";" + currentAccuracy;
//			DataSaverLoader.saveFile("LRRangeTest-loss.csv", luAcc, true);
			
			//step 3: check if 0 error, or minimum threshold reached
			//if not: repeat  from step 1.
//			if(currentHighestDelta <= deltaThreshold)
//			{
//				System.out.println("IS OVER, WEIGHT IS NOT MODIFIED ENOUGH");
//				return;
//			}
	
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
	
	
	
	
	
	
	
	public float TestNetwork(int epoch, int iteration, ArrayList<DataTuple> set)
	{
		System.out.println("EPOCH " + epoch + ", ITERATION " + iteration + ":");
		float success = 0.0f;
		float failure = 0.0f;
		float setSize = set.size();
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
			
//			System.out.println();
		}
		
		System.out.println();
		String inf = "EPOCH " + epoch + ", ITERATION " + iteration + ":" + System.getProperty("line.separator"); 
		inf += "SUCCESS: " + success + "; FAILURE: " + failure + "; TOTAL: " + setSize + System.getProperty("line.separator");
		inf += "SUCCESS: " + (success * 100.0f)/setSize + "%; FAILURE: " + (failure * 100.0f)/setSize  + "%; TOTAL: " + setSize + System.getProperty("line.separator");
		System.out.println("SUCCESS: " + success + "; FAILURE: " + failure + "; TOTAL: " + setSize);
		System.out.println("SUCCESS: " + (success * 100.0f)/setSize + "%; FAILURE: " + (failure * 100.0f)/setSize  + "%; TOTAL: " + setSize);
		
		
		DataSaverLoader.saveFile("Information.txt", inf, true);
		return success/setSize;
	}
	
	public float TestNetwork(int epoch, ArrayList<DataTuple> set)
	{
		System.out.println("EPOCH " + epoch + ":");
		float success = 0;
		float failure = 0;
		float setSize = set.size();
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
			
//			System.out.println();
		}
		
		System.out.println();
		String inf = "EPOCH " + epoch + ":" + System.getProperty("line.separator"); 
		inf += "SUCCESS: " + success + "; FAILURE: " + failure + "; TOTAL: " + setSize + System.getProperty("line.separator");
		inf += "SUCCESS: " + (success * 100.0f)/setSize + "%; FAILURE: " + (failure * 100.0f)/setSize  + "%; TOTAL: " + setSize + System.getProperty("line.separator");
		System.out.println("SUCCESS: " + success + "; FAILURE: " + failure + "; TOTAL: " + setSize);
		System.out.println("SUCCESS: " + (success * 100.0f)/setSize + "%; FAILURE: " + (failure * 100.0f)/setSize  + "%; TOTAL: " + setSize);
		
		
		DataSaverLoader.saveFile("Information.txt", inf, true);
		return success/setSize;
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
		
//		if(!cacheValues.containsKey(index))
//		{
//			
//		}
		
//		for(Neuron output : output_neurons)
//		{
//			output.CalculateError();
//		}
//		
//		float expectedOutput = getContinuousOutput(set.get(index).DirectionChosen);
//		MOVE actualOutput = moveOutput(output_neurons.get(0).output);
//		selected_index = actualOutput.ordinal();
		
		ArrayList<Neuron> outputNeurons = neuralLayers.get(neuralLayers.size() - 1).getNeurons();
		for(int currentNeuron = 0; currentNeuron < outputNeurons.size(); currentNeuron++)
		{
			if(outputNeurons.get(currentNeuron).output > best_value)
			{
				best_value = outputNeurons.get(currentNeuron).output;
				selected_index = currentNeuron;
			}
		}
		
//		for(int i = 0; i < output_neurons_quantity; i++)
//		{
//			if(output_neurons.get(i).output > best_value)
//			{
//				best_value = output_neurons.get(i).output;
//				selected_index = i;
//			}
//		}
//		
		if(debug)
		{
			System.out.println("NEURAL OUTPUT: " + selected_index);
			System.out.println("EXPECTED OUTPUT: " + set.get(index).DirectionChosen.ordinal());
		}
		
		return set.get(index).DirectionChosen.ordinal() == selected_index;
	}
	
	public boolean DebugOutput(int index, ArrayList<DataTuple> set)
	{
		/*
		System.out.print("INPUTS: ");
		
		for(int j = 0; j < input_neurons_quantity; j++)
		{
			System.out.print(dataset.get(first_pos).info.get(j) + "; ");
		}
		*/
		System.out.println();
		System.out.println("EXPECTED OUTPUT: " + set.get(index).DirectionChosen);
		System.out.println("EXPECTED OUTPUT: " + set.get(index).DirectionChosen.ordinal());
		return Classify(index, set, true);
		
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
		
		//With this method we cannot use loop for input neurons --> Soon to be changed
//		input_neurons.get(0).CalculateOutput(set.get(index).n_neighbor);
//		input_neurons.get(1).CalculateOutput(set.get(index).e_neighbor);
//		input_neurons.get(2).CalculateOutput(set.get(index).s_neighbor);
//		input_neurons.get(3).CalculateOutput(set.get(index).w_neighbor);
//		input_neurons.get(4).CalculateOutput(set.get(index).closest_ghost_dist);
//		input_neurons.get(5).CalculateOutput(set.get(index).closest_pill_dist);
//		input_neurons.get(6).CalculateOutput(set.get(index).closest_pp_dist);
//		input_neurons.get(7).CalculateOutput(set.get(index).num_pill_left_norm);
//		input_neurons.get(8).CalculateOutput(set.get(index).num_pp_left_norm);
//		input_neurons.get(9).CalculateOutput(set.get(index).normalizedPosition);
		
		/*
		for(int j = 0; j < input_neurons_quantity; j++)
		{
			input_neurons.get(j).CalculateOutput(dataset.get(first_pos).info.get(j));
		}
		*/
//		for(Neuron n : hidden_neurons)
//		{
//			n.CalculateOutput(0.0);
//		}
//		
//		for(Neuron n : output_neurons)
//		{
//			n.CalculateOutput(0.0);
//		}
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
		
//		//With this method we cannot use loop for input neurons
//		input_neurons.get(0).CalculateOutput(tuple.n_neighbor);
//		input_neurons.get(1).CalculateOutput(tuple.e_neighbor);
//		input_neurons.get(2).CalculateOutput(tuple.s_neighbor);
//		input_neurons.get(3).CalculateOutput(tuple.w_neighbor);
//		input_neurons.get(4).CalculateOutput(tuple.closest_ghost_dist);
//		input_neurons.get(5).CalculateOutput(tuple.closest_pill_dist);
//		input_neurons.get(6).CalculateOutput(tuple.closest_pp_dist);
//		input_neurons.get(7).CalculateOutput(tuple.num_pill_left_norm);
//		input_neurons.get(8).CalculateOutput(tuple.num_pp_left_norm);
////		input_neurons.get(9).CalculateOutput(tuple.normalizedPosition);
//		
//		/*
//		for(int j = 0; j < input_neurons_quantity; j++)
//		{
//			input_neurons.get(j).CalculateOutput(tuple.get(j));
//		}*/
//		for(Neuron n : hidden_neurons)
//		{
//			n.CalculateOutput(0.0);
//		}
//		
//		for(Neuron n : output_neurons)
//		{
//			n.CalculateOutput(0.0);
//		}
	}
	
	public void DEPRECATEDNonBatchBackPropagation(float learning_rate)
	{
		int next_test = 300;
		int test_neural_network = next_test;
		double deltaThreshold = 0.001f;
		double accuracyThreshold = 0.80;
		double learningRate = learning_rate;
		int iterations = trainingSet.size() / batchSize;
		int currentBatch = 0;

		for(int i = 1; i < max_epochs; i++)
		{
			System.out.println("EPOCH " + i);
			int incorrectClassify = 0;
			int correct = 0;
			int tup = 0;
			double currentHighestDelta = 0.0;
			double currDelta = 0.0f;
			double currentAccuracy = 0.0;
			
			for(int iter = 0; iter < iterations; iter++ )
			{
				currentBatch = iter * batchSize;
				for(int tuple = currentBatch; tuple < currentBatch + batchSize; tuple++)
				{
					
				}
			}
			
			
//			learningRate = learning_rate/i;
			learningRate = 0.01;
			for(int k = 0; k < trainingSet.size(); k++)
			{
				tup++;
				//step 1: feedforward
				FeedForward(k, trainingSet);
				
				//step 2: Did I classify correctly? --> This needs more refinement
				if(!Classify(k, trainingSet, false))
				{
					incorrectClassify++;

				}
				else
				{
					correct++;
				}
				
				//step 2: calculate error
				for(int j = 0; j < output_neurons_quantity; j++)
				{
					int mov = trainingSet.get(k).DirectionChosen.ordinal();
					if(mov == j)
					{
						output_neurons.get(j).CalculateError(1.0);
					}
					else
					{
						output_neurons.get(j).CalculateError(0.0);
					}
					
				}
				
				//Calculate the delta weight of the output neurons
				for(Neuron n : output_neurons)
				{
					currDelta = n.AccumulateWeights(learningRate);
					if(currDelta > currentHighestDelta)
						currentHighestDelta = currDelta;
				}
				
				//Calcualte the error of the hidden neurons
				for(Neuron n : hidden_neurons)
				{
					n.GetAccumulatedError(output_neurons);
					n.CalculateError(0.0);
				}
				
				for(Neuron n : hidden_neurons)
				{
					currDelta = n.AccumulateWeights(learningRate);
					if(currDelta > currentHighestDelta)
						currentHighestDelta = currDelta;
				}
				/////////// CHANGE THE WEIGHT OF ALL THE NEURONS //////////////
				
				for(Neuron n : output_neurons)
				{
					n.ChangeWeights(learningRate);
				}
				
				//step 3: correct all weights
				for(Neuron n : hidden_neurons)
				{
					n.ChangeWeights(learningRate);
				}

			}
			
//			System.out.println("Training Accuracy:");
//			System.out.println("Correctly classified: " + (correct) + ", " + (correct * 100)/tup + "%");
//			System.out.println("Incorrectly classified: "+ (incorrectClassify) + ", " + (incorrectClassify * 100)/tup + "%");

			//Test 
			if(test_neural_network == i) //(mAYBE IT IS INTERESTING TO TEST THE NETWORK IN GAME!!)
			{
				currentAccuracy = TestNetwork(i, testSet);
				currentAccuracy = currentAccuracy/(double) testSet.size();
				test_neural_network += next_test;
			}

			
			//step 3: check if 0 error, or minimum threshold reached
			//if not: repeat  from step 1.
			if(currentHighestDelta <= deltaThreshold)
			{
				System.out.println("IS OVER, WEIGHT IS NOT MODIFIED ENOUGH");
				return;
			}
	
			if(currentAccuracy >= accuracyThreshold)
			{
				System.out.println("IS OVER, ACCURATE ENOUGH");
				return;
			}
			
		}
		
		System.out.println("IS OVER!");
	}
	
	
	/***
	 * We update connectiong weights after each epoch
	 * @param learning_ratee
	 */
	public void BatchBackPropagation(float learning_rate)
	{
		Random rnd = new Random();
		int next_test = 100;
		int test_neural_network = next_test;
		
		for(int i = 0; i < max_epochs; i++)
		{
			System.out.println("EPOCH " + i);
			int incorrectClassify = 0;
			int correct = 0;
//			learning_rate = 0.5f;
			for(int k = 0; k < trainingSet.size(); k++)
			{
				//step 1: feedforward
				FeedForward(k, trainingSet);
				
				//step 2: Did I classify correctly?
				if(!Classify(k, trainingSet, false))
				{
				
					incorrectClassify++;
					//step 2: calculate error
					for(int j = 0; j < output_neurons_quantity; j++)
					{
						int mov = trainingSet.get(k).DirectionChosen.ordinal();
						if(mov == j)
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
						n.AccumulateWeights(learning_rate);
					}
					for(Neuron n : hidden_neurons)
					{
						n.AccumulateWeights(learning_rate);
					}
				}
				else
				{
					correct++;
					
					for(int j = 0; j < output_neurons_quantity; j++)
					{
						int mov = trainingSet.get(k).DirectionChosen.ordinal();
						if(mov == j)
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
						n.AccumulateWeights(learning_rate);
					}
					for(Neuron n : hidden_neurons)
					{
						n.AccumulateWeights(learning_rate);
					}
				}
				
			}
			
			System.out.println("Incorrectly classified: " + (incorrectClassify * 100)/trainingSet.size() + "%");

			System.out.println("Incorrectly classified: " + (incorrectClassify));
			System.out.println("Correctly classified: " + (correct));
			
			for(Neuron n : output_neurons)
			{
				n.ChangeWeights(learning_rate);
			}
			for(Neuron n : hidden_neurons)
			{
				n.ChangeWeights(learning_rate);
			}
			
			//Test 
			if(test_neural_network == i)
			{
				incorrectClassify =0;
				correct = 0;
				TestNetwork(i, testSet);
				for(int k = 0; k < trainingSet.size(); k++)
				{
					//step 1: feedforward
					FeedForward(k, trainingSet);
					
					//step 2: Did I classify correctly?
					if(!Classify(k, trainingSet, false))
					{
					
						incorrectClassify++;
					}
					else
					{
						correct++;
					}
					
				}
				System.out.println("Incorrectly classified: " + (incorrectClassify * 100)/trainingSet.size() + "%");

				System.out.println("Incorrectly classified: " + (incorrectClassify));
				System.out.println("Correctly classified: " + (correct));
				test_neural_network += next_test;
			}
			
			//step 2: calculate error
			
			//step 3: check if 0 error, or minimum threshold reached
			//if not: repeat  from step 1.
			
		}
		
		System.out.println("IS OVER!");
	}
	
	
}
