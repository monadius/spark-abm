package main.type;

import javasrc.Translation;
import main.Command;
import main.Variable;

/**
 * A command derived from a method
 * @author Monad
 *
 */
public class CommandFromMethod extends Command {
	/* Base method */
	Method method;

	/**
	 * Creates a command from a method
	 * @param method
	 */
	protected CommandFromMethod(Method method) {
		super(method.id.name);
		this.method = method;

		
		for (int i = 0; i < method.arguments.size(); i++) {
			Variable var = method.arguments.get(i);
			addArgument(var);
		}
		
		if (method.translation == null)
			setTranslation(getDefaultTranslation());
		else
			setTranslation(method.translation);
		
		setReturnType(method.returnType);
		setReturnSubtype(method.returnSubtype);
	}
	

	/**
	 * Returns a default translation
	 * @return
	 */
	private Translation getDefaultTranslation() {
		String translationString = "@@object." + method.id.toJavaName() + "(";

		for (int i = 0; i < method.arguments.size(); i++) {
			Variable var = method.arguments.get(i);
			translationString += "@" + var.id.name;
			
			if (i != method.arguments.size() - 1)
				translationString += ", ";
		}
		
		translationString += ")";

		return new Translation(translationString);
	}

}
