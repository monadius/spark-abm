package org.spark.runtime.internal.data;

/**
 * This exception is raised whenever data source is missing
 * or does not work properly.
 * @author Monad
 *
 */
@SuppressWarnings("serial")
public class BadDataSourceException extends Exception {
	public BadDataSourceException(String message) {
		super(message);
	}
}
