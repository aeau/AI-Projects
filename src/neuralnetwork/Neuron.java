package neuralnetwork;

import java.util.ArrayList;
import java.util.Random;

public class Neuron 
{
	public ArrayList<Connection> connections = new ArrayList<Connection>();
	
	public double output = 0.0;
	//public double desired_output;
	public double error = 0.0;
	
	public enum NeuronTypes
	{
		INPUT,
		HIDDEN,
		OUTPUT,
		BIAS
	};
	
	public NeuronTypes neuron_type;
	
	public Neuron(NeuronTypes nt)
	{
		neuron_type = nt;
		
		
		if(neuron_type != NeuronTypes.INPUT)
		{
			AddConnection(new Neuron(NeuronTypes.BIAS, 1.0));
		}
	}
	
	public Neuron(NeuronTypes nt, double value)
	{
		neuron_type = nt;
		output = value;
	}
	
	public void AddConnection(Neuron n)
	{
		Random rnd = new Random();
		for(Connection c : connections)
		{
			if(c.from == n)
			{
				return;
			}
		}	
		connections.add(new Connection(n, this, (rnd.nextDouble() * 2.0f) - 1.0f));
	}
	
	public void AddConnection(Neuron n, double weight)
	{
		for(Connection c : connections)
		{
			if(c.from == n)
			{
				return;
			}
		}	
		connections.add(new Connection(n, this, weight));
	}
	
	public void CalculateOutput(double value)
	{
		output = 0.0;
		
		switch(neuron_type)
		{
		case INPUT:
			output = value;
			break;
		case HIDDEN:
		case OUTPUT:
			for(Connection c : connections)
			{
				output += (c.weight * c.from.output);
			}
			
			output = ApplyActivationFunction(output);
			break;
			
		default:
			System.out.println("ERROR NO VALID NEURON TYPE");
			break;
		}
	}
	
	public void CalculateError(double value)
	{
		switch(neuron_type)
		{
		case HIDDEN:

			//ONLY FOR SIGMOID
			error *= (output * (1 - output));
			
			break;
			
		case OUTPUT:
			error = 0.0;
			if(value == output)
			{
				error = 0;
				return;
			}
			
			error = (value - output) * (output * (1 - output));
			break;
			
		default:
			System.out.println("ERROR NO VALID NEURON TYPE");
			break;
		}
	}
	
	public void GetAccumulatedError(ArrayList<Neuron> output_neurons)
	{
		error = 0.0;
		
		for(Neuron o : output_neurons)
		{
			for(Connection c : o.connections)
			{
				if(c.from == this)
				{
					error += (c.weight * o.error);
				}
			}
		}
	}
	
	public void CorrectWeights(double learning_rate)
	{
		for(Connection c : connections)
		{
			c.delta_weight = learning_rate * error * c.from.output;
			c.weight += c.delta_weight;
		}
	}
	
	public double ApplyActivationFunction(double output)
	{
		//FOR NOW ONLY SIGMOID
		double return_value = (1.0/(1.0 + Math.pow(Math.E, (-1.0*output))));
		return return_value;
	}
}
