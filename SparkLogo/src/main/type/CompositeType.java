package main.type;

import main.AdhocImplementation;
import main.Command;
import main.Id;
import main.SparkModel;

/**
 * Represents a compostite type, such as an array type or a collection type.
 * Composite type can only be a Java type.
 * @author Monad
 *
 */
public class CompositeType extends BuiltinType {
	/* Derived types should know the original type */
	protected CompositeType type;
	/* Subtype of this type */
	protected Type subtype;
	
	/**
	 * Creates a prototype composite type
	 * @param id
	 */
	public CompositeType(Id id, Type parentType) {
		super(id, parentType, false);
		this.type = this;
	}
	
	
	/**
	 * Returns a sub-type
	 * @return
	 */
	// FIXME: we do not need this command
	public Type getSubtype() {
		return subtype;
	}
	
	
	/**
	 * Creates a fully specified composite type
	 * @param type
	 * @param subtype
	 */
	public CompositeType(CompositeType type, Type subtype) {
		super(type.id, type.getParentType(), false);
		
		this.type = type;
		this.subtype = subtype;
	}
	
	
	@Override
	public Command getCommand(String name) {
		Command cmd = super.getCommand(name);
		
		if (cmd == null) {
			if (type != this && type != null)
				cmd = type.getCommand(name);
		}
		
		return cmd;
	}
	

	
	@Override
	public String getTranslationString() throws Exception {
		// TODO: method should return id, not a translation string
		// Create a method for translation string
		String name = (type != null ? type.id.toJavaName() : id.toJavaName());
		if (subtype == null) {
			throw new Exception("Subtype is not defined for a composite type " + name);
		}

		String subtypeName = subtype.id.toJavaName();
		name = name.replaceAll("@subtype", subtypeName);
		return name;
	}
	
	
	@Override
	public boolean instanceOf(Type otherType) {
		if (otherType instanceof CompositeType) {
			CompositeType t = (CompositeType) otherType;
			
			// TODO: be sure that the base type is always the same
			boolean e1 = type == t.type;
			boolean e2 = false;
			
			if (t.subtype == null)
				e2 = true;
			else if (subtype != null)
				e2 = subtype.instanceOf(t.subtype);
			
			return e1 && e2;
		}
		
		// TODO: better solution is required
		AdhocImplementation.flag = true;
		if (otherType == SparkModel.getInstance().getType(new Id("$Object")))
			return true;
		
		return false;
	}
}
