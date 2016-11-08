

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import py4j.GatewayServer;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Stage;

import de.be4.classicalb.core.parser.exceptions.BException;
import de.prob.animator.domainobjects.AbstractEvalResult;
import de.prob.animator.domainobjects.ClassicalB;
import de.prob.animator.domainobjects.IEvalElement;
import de.prob.scripting.Api;
import de.prob.statespace.State;
import de.prob.statespace.StateSpace;
import de.prob.statespace.Trace;
import de.prob.statespace.Transition;
import example.MyGuiceConfig;

public class PyProBServer {

	private static Injector INJECTOR = Guice.createInjector(Stage.PRODUCTION, new MyGuiceConfig());
	private Api api;
	static GatewayServer server;
	public Trace t;
	public StateSpace stateSpace;

	
	@Inject
	public PyProBServer(Api api) {
		this.api = api;
	}
	

	public static void main(String[] args) throws BException, IOException, URISyntaxException {
		System.out.println("Server PyProB starting  (developed by Valerio Gutemberg and Diego Azevedo)");
		PyProBServer m = INJECTOR.getInstance(PyProBServer.class);
		server = new GatewayServer(m);
		server.start();
		System.out.println("Waiting the comunication.");
	
	}
	public boolean open_module(String path_in) throws IOException, BException, URISyntaxException{
		
		System.out.println("Load classical B Machine");
		Path path =  Paths.get(path_in);;
		//Path path = Paths.get(getClass().getResource("/ACounter.mch").toURI());
		System.out.println(path.toString());
		stateSpace = api.b_load(path.toAbsolutePath().toString());
		t = new Trace(stateSpace);
		t = t.anyEvent(null); //  t = t.anyEvent("initialisation"); (Unica operação habilitada)
		
		return true;
	}
	public String animate(String operName, String parameters_in){
		//t = t.anyEvent("inc");
		//t = t.anyEvent("inc");
		//t = t.anyEvent("res"); 

		//t = t.anyEvent(operName); // Ok!
		String [] arrayPar = parameters_in.split(";");
		ArrayList<String> parameters= new ArrayList<String>();
		Collections.addAll(parameters, arrayPar);
		//System.out.println(parameters.get(1));
		System.out.println(parameters);
		
		if ((parameters.size()== 1 ) &&  (parameters.get(0)=="") )
			t = t.anyEvent(operName);
		else
			t = t.execute(operName, parameters);
		System.out.println("Some human readable representation of a trace");
		System.out.println(t);
		System.out.println();

		// Fetch the last state
		//State state = t.getCurrentState();
		//State state = t.getCurrentState();
		
		/*
		 List<Transition> operations = state.getOutTransitions();
		
		System.out.println("There are " + operations.size() + " operations enabled.");
		for (Transition transition : operations) {
			System.out.println(transition.getPrettyRep());
		}
		System.out.println();
		*/
		
		return "OK";
	}
	
	public String getCurrenteValueVariable(String name_variable){
		State state = t.getCurrentState();
		String res = name_variable+"="+ state.eval(name_variable);
		System.out.println(res);
		return res;
	}
	
	public int addition(int first, int second) {
		return first + second;
	}
	public void shut1() throws InterruptedException {
		  server.shutdown();
		  Thread.sleep(1000);
		  System.exit(0);

	}

	public void exampleUsage() throws BException, IOException, URISyntaxException {
		System.out.println("ProB version: " + api.getVersion());
		System.out.println();
		System.out.println("Load classical B Machine");

		System.out.println(System.getProperty("user.dir"));
		Path path = Paths.get(getClass().getResource("/ACounter.mch").toURI());
		System.out.println(path.toString());
		StateSpace stateSpace = api.b_load(path.toAbsolutePath().toString());

		// Define some expressions of iterest
		ClassicalB jj = new ClassicalB("jj");
		ClassicalB sum = new ClassicalB("jj + ii");
		// Register the expressions for evaluation
		stateSpace.subscribe(this, jj);
		stateSpace.subscribe(this, sum);

		// A trace is a path throughout a state space
		System.out.println("Construct Trace");
		System.out.println();

		Trace t = new Trace(stateSpace);
		// Execute a couple of random steps

		//for (int i = 0; i < 10; i++) {
		//	t = t.anyEvent(null);
		//}

		t = t.anyEvent(null); //  t = t.anyEvent("initialisation"); (Unica operação habilitada)
		t = t.anyEvent("inc");
		t = t.anyEvent("inc");
		t = t.anyEvent("res"); 


		System.out.println("Some human readable representation of a trace");
		System.out.println(t);
		System.out.println();

		// Fetch the last state
		State state = t.getCurrentState();

		// Print the values of registered fomulas. Variables are automatically
		// registered, but you need the ClassicalB object that represents them
		// if you want to fetch a particular variable from the map.
		// The simplest way is to just register the variable as we did above

		// print them all
		Map<IEvalElement, AbstractEvalResult> values = state.getValues();
		Set<Entry<IEvalElement, AbstractEvalResult>> entrySet = values.entrySet();
		for (Entry<IEvalElement, AbstractEvalResult> entry : entrySet) {
			System.out.println(entry.getKey() + " -> " + entry.getValue());
		}
		System.out.println();
		System.out.println("The value of ii+jj is " + values.get(sum) + ". The value of jj is " + values.get(jj));
		System.out.println();

		List<Transition> operations = state.getOutTransitions();

		System.out.println("There are " + operations.size() + " operations enabled.");
		for (Transition transition : operations) {
			System.out.println(transition.getPrettyRep());
		}
		System.out.println();
	}


	
}