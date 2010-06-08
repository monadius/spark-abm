package main;


/**
 * Identificator, or name
 * @author Monad
 *
 */
public class Id {
	public final String name;
	/* Custom java name */
	private String javaName;

	/**
	 * Creates a simple id
	 * @param name
	 */
	public Id(String name) {
		this.name = name;
	}
	

	/**
	 * Sets a custom java name
	 * @param javaName
	 */
	public void setJavaName(String javaName) {
		this.javaName = javaName;
	}
	
	
	/**
	 * Converts SparkLogo id into correct java id
	 * @return
	 */
	public String toJavaName() {
		if (javaName != null)
			return javaName;
		
		char[] chars = name.toCharArray();
		StringBuilder str = new StringBuilder(chars.length);
		
		for (int i = 0; i < chars.length; i++) {
//			char ch = Character.toLowerCase(chars[i]);
			char ch = chars[i];
			
			if (ch == '-' && i < chars.length - 1) {
				chars[i + 1] = Character.toUpperCase(chars[i + 1]);
			}
			else if (ch == ' ') {
				str.append('_');
			}
			else {
				str.append(ch);
			}
		}

		// name field is final so it cannot be changed later
		javaName = str.toString();
		return javaName;
	}
	
	
	@Override
	public int hashCode() {
		return name.hashCode();
	}
	
	
	@Override
	public boolean equals(Object object) {
		if (object instanceof Id) {
			return ((Id) object).name.equals(name);
		}
		
		return false;
	}
	
	
	@Override
	public String toString() {
		return "Id(name = '" + name + "')";
	}
}
