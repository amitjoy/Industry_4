package de.tum.in.scheduler.annotations.timeinterval;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface StartingDailyAt {

	int hour();

	int minute() default 0;

	int second() default 0;

}
