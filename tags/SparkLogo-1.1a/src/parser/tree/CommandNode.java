package parser.tree;

import parser.Symbol;
import javasrc.JavaEmitter;
import javasrc.Translation;
import main.Command;
import main.OverloadedCommand;
import main.Variable;
import main.type.ArgumentType;
import main.type.CompositeType;
import main.type.MethodType;
import main.type.MyselfType;
import main.type.ParentType;
import main.type.SelfType;
import main.type.ThisType;
import main.type.Type;
import main.type.UnknownType;



public class CommandNode extends MultiNode {
	protected Command	cmd;
	
	
	public Command getCommand() {
		return cmd;
	}
	
	
	public CommandNode(Symbol symbol, Command cmd) {
		super(symbol);
		this.cmd = cmd;
	}
	
	
	public void debugPrint(java.io.PrintStream out) throws Exception {
		String name = cmd.getName();
		name = name.replaceAll("<", "LT");
		name = name.replaceAll(">", "GT");
		
		
		out.print("<command name = \"" + name + "\">\n");
		
		for (int i = 0; i < this.children.size(); i++) {
			if (children.get(i) == null) continue;
			out.print("<argument>\n");
			children.get(i).debugPrint(out);
			out.print("</argument>\n");
		}
		
		out.print("</command>\n");
	}
	
	
	
	
	@Override
	public void translate(JavaEmitter java, int flag) throws Exception {
		java.addBuffer("@@self");
		java.setActiveBuffer("@@self");
		java.print(currentBlock().getSelfVariable().id.toJavaName());
		java.endBuffer();

		java.addBuffer("@@myself");
		java.setActiveBuffer("@@myself");
		java.print(currentBlock().getMyselfVariable().id.toJavaName());
		java.endBuffer();
		
		
		// FIXME: find a better solution (a universal one)
		if (cmd.getName().equals("ask")) {
			BlockNode askBlock = (BlockNode) children.get(1);
			Variable selfVariable = askBlock.getCodeBlock().getSelfVariable();

			java.addBuffer("@@declaration");
			java.setActiveBuffer("@@declaration");
			java.print(selfVariable.type.getTranslationString());
			java.print(" ");
			java.print(selfVariable.id.toJavaName());
			java.endBuffer();
		}
		
		for (int i = 0; i < children.size(); i++) {
			String argName = "@" + cmd.getArgument(i).id.name;
			java.addBuffer(argName);
			
			java.setActiveBuffer(argName);
			children.get(i).translate(java, flag | GET_VALUE);
			java.endBuffer();
		}
		
		Translation translation = cmd.getTranslation();
		if (translation != null)
			translation.doTranslation(java, flag);
		else
			java.println("// " + cmd.getName());
		
		java.clearBuffers();
	}
	
	
	@Override
	public Type getType() throws Exception {
		return cmd.getReturnType();
	}
	
	
	@Override
	public Type resolveType(Type expectedType, int flag) throws Exception {
		Type[] argTypes = new Type[cmd.getArgumentsNumber()];
		
		for (int i = 0; i < cmd.getArgumentsNumber(); i++) {
			Variable arg = cmd.getArgument(i);
			argTypes[i] = getNode(i).resolveType(arg.type, flag);
			// TODO: is it correct?
			if (!(cmd instanceof OverloadedCommand) && 
					arg.type instanceof UnknownType)
				arg.type = argTypes[i];
		}
		
		if (cmd instanceof OverloadedCommand) {
			OverloadedCommand cmd2 = (OverloadedCommand) cmd;
			this.cmd = cmd2.findCommand(argTypes);
			
			if (cmd == null || cmd instanceof OverloadedCommand)
				throw new Exception("Overloaded command not found: " + symbol);
			
			// TODO: be sure that cmd is not of the type OverloadedCommand again
			return resolveType(expectedType, flag);
		}
		

		Type myType = cmd.getReturnType();

		try {
			// Resolve dependence on argument types
			if (myType instanceof ArgumentType) {
				myType = ((ArgumentType) myType).deriveType(cmd.getArguments(), argTypes);
			}
		
			// Resolve composite type
			if (myType instanceof CompositeType) {
				Type mySubtype = cmd.getReturnSubtype();

				if (mySubtype instanceof ArgumentType) {
					mySubtype = ((ArgumentType) mySubtype).deriveType(cmd.getArguments(), argTypes);
				}
			
				myType = new CompositeType((CompositeType) myType, mySubtype);
			}
		}
		catch (Exception e) {
			throw new Exception(e.getMessage() + " " + symbol);
		}
		
		// Resolve SelfType
		if (myType instanceof SelfType) {
			myType = currentBlock().getSelfType();
		}
		
		// Resolve MyselfType
		if (myType instanceof MyselfType) {
			myType = currentBlock().getMyselfType();
		}
		
		// Resolve ParentType
		if (myType instanceof ParentType) {
			myType = currentBlock().getMethod().getParentType().getParentType();
		}
		
		// Resolve ThisType
		if (myType instanceof ThisType) {
			myType = currentBlock().getMethod().getParentType();
		}
		
		
		if (expectedType != null && !(expectedType instanceof UnknownType)) {
			if (myType == null)
				throw new Exception("Command " + cmd.getName() + " does not return any value: " + symbol);

			if (expectedType == MethodType.getInstance()) {
				expectedType = currentBlock().getMethod().getReturnType();
				if (expectedType == null)
					throw new Exception("Method " + currentBlock().getMethod() + " does not have return type: " + symbol);
			}

			
			// TODO: should types be unique?
			if (myType instanceof UnknownType) {
				myType = expectedType;
			}
	
			
			if (!myType.instanceOf(expectedType))
				throw new Exception("Types mismatch: myType = " + myType + "; expectedType = " + expectedType + " for command " + cmd.getName() + ": " + symbol);
		}
		
		return myType;
	}



}
