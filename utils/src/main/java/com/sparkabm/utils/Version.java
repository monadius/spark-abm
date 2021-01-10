package com.sparkabm.utils;

/**
 * Represents a version in the format major.minor
 */
public class Version {
	public final int major;
	public final int minor;
	
	/**
	 * Constructor
	 */
	public Version(int major, int minor) {
		this.major = major;
		this.minor = minor;
	}
	
	/**
	 * Copy constructor
	 */
	public Version(Version v) {
		this.major = v.major;
		this.minor = v.minor;
	}
	
	/**
	 * Returns -1 if this < v2
	 * Returns 0 if this == v2
	 * Returns 1 if this > v2
	 */
	public int compare(Version v2) {
		int major2 = v2.major, minor2 = v2.minor;
		
		if (major < major2)
			return -1;
		
		if (major > major2)
			return 1;
		
		// major1 == major2
		if (minor < minor2)
			return -1;
		
		if (minor > minor2)
			return 1;
		
		return 0;
	}
	
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		
		if (!(obj instanceof Version))
			return false;
		
		Version v2 = (Version) obj;
		return major == v2.major && minor == v2.minor;
	}
	
	@Override
	public int hashCode() {
		return major * 1023 + minor;
	}
	
	@Override
	public String toString() {
		return "" + major + "." + minor;
	}
}
