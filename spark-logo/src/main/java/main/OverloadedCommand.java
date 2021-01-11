package main;

import java.util.ArrayList;

import main.type.BlockType;
import main.type.Type;
import main.type.UnknownType;

/**
 * Represents an overloaded command
 * @author Monad
 *
 */
public class OverloadedCommand extends Command {
	/* All possible commands */
	protected ArrayList<Command> possibleCommands;
	
	/* Number of arguments cannot be overloaded */
	protected int argsNumber;
	
	/**
	 * Creates an overloaded command
	 * @param cmd
	 */
	public OverloadedCommand(Command cmd) {
		super(cmd.getName());
		
		possibleCommands = new ArrayList<Command>(5);
		possibleCommands.add(cmd);
		
		argsNumber = cmd.getArgumentsNumber();
		for (int i = 0; i < argsNumber; i++) {
			Type type = new UnknownType();
			if (cmd.getArgument(i).type instanceof BlockType)
				type = cmd.getArgument(i).type;
			addArgument( new Variable(cmd.getArgument(i).id, type) );
		}
		
		setInfix(cmd.isInfix());
		try {
			setPrecedence(cmd.getPrecedence());
			setReturnType(cmd.getReturnType());
			setReturnSubtype(cmd.getReturnSubtype());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Adds another command
	 * @param cmd
	 * @throws Exception
	 */
	public void addCommand(Command cmd) throws Exception {
		if (cmd.getArgumentsNumber() != argsNumber)
			throw new Exception("Incorrect number of arguments");
		
		// TODO: check conflicts: block types, precedence, return type
		possibleCommands.add(cmd);
	}
	
	
	/**
	 * Returns a command with the given argument signature
	 * @return null if no suitable command found
	 */
	public Command findCommand(Type ... argTypes) {
		// TODO: accurately deal with multiple choices
		
		if (argTypes.length != argsNumber)
			return null;
		
		for (int i = 0; i < possibleCommands.size(); i++) {
			Command cmd = possibleCommands.get(i);
			
			int matches = 0;
			for (int k = 0; k < argsNumber; k++) {
				Type type = cmd.getArgument(k).type;
				
				if (argTypes[k] instanceof UnknownType) {
					matches++;
					continue;
				}
				
				// TODO: be sure that there are no copies of the same type
				if (argTypes[k].instanceOf(type))
					matches++;
			}
			
			if (matches == argsNumber) {
				return cmd;
			}
		}
		
		return null;
	}

	
	
}
