package de.tum.in.scheduler.annotations.timeinterval;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Interval {

	int SECOND = 1;
	int MINUTE = 60 * SECOND;
	int HOUR = 60 * MINUTE;

	int value();

	int period() default SECOND;

}
