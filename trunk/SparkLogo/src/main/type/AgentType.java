package main.type;

import java.util.ArrayList;

import main.Id;
import main.SparkModel;
import main.Variable;
import main.annotation.AgentAnnotation;
import main.annotation.InterfaceAnnotation;

public class AgentType extends Type {
	protected final ArrayList<AgentAnnotation> annotations;

	/**
	 * Creates an agent type
	 * @param id
	 * @param parent
	 */
	protected AgentType(Id id, Type parent) {
		super(id, parent);
		annotations = new ArrayList<AgentAnnotation>();
		// TODO: parent should be specific: Agent, SpaceAgent, etc.
	}
	
	
	/**
	 * Adds an annotation to the agent type
	 * @param annotation
	 * @throws Exception
	 */
	public void addAnnotation(InterfaceAnnotation annotation) throws Exception {
		if (!(annotation instanceof AgentAnnotation))
			throw new Exception("Annotation " + annotation.getId() + " cannot be associated with " + this);
		
		AgentAnnotation a = (AgentAnnotation) annotation;
		annotations.add(a);
		a.associateAgentType(this);
	}
	
	
	/**
	 * Returns annotations associated with the agent type
	 * @return
	 */
	public ArrayList<AgentAnnotation> getAnnotations() {
		return annotations;
	}
	
	
	/**
	 * Parses all methods.
	 * Prepares step method.
	 */
	@Override
	public void parseMethods() throws Exception {
		// void step(long tick) method
		createStepMethod();
		
		// Parse methods
		super.parseMethods();
	}
	
	
	/**
	 * Creates the step method
	 * @throws Exception
	 */
	private void createStepMethod() throws Exception {
		Method step = getMethod(new Id("step"), false);
		if (step == null) {
			// We do not need to create step method
			return;
//			step = new Method(new Id("step"));
//			addMethod(step);
		}
		
		if (step.arguments.size() >= 2)
			throw new Exception("Wrong number of argument for the step method");
		
		// TODO: verify first argument type
		Id timeId = new Id("time");
		if (step.arguments.size() == 1) {
			timeId = step.arguments.get(0).id;
		}
		
		// Argument should be of type 'long'
		step.arguments.clear();		
		step.addArgument( new Variable(timeId, SparkModel.getInstance().getType(new Id("$time"))) );
		
		// TODO: check return type: should be void
	}


}
