package org.spark.core;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface AgentAnnotation {
	public boolean Static() default false;
}
