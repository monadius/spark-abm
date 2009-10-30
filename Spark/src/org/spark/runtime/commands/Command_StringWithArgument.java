package org.spark.runtime.commands;

/**
 * A string command with one string argument
 * @author Monad
 *
 */
@SuppressWarnings("serial")
public class Command_StringWithArgument extends Command_String {
	private String argument;
	
	
	public Command_StringWithArgument(String cmd, String arg) {
		super(cmd);
		this.argument = arg;
	}
	
	
	public String getArgument() {
		return argument;
	}
}
