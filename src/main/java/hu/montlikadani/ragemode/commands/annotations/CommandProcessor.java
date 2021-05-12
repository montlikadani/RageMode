package hu.montlikadani.ragemode.commands.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CommandProcessor {

	/**
	 * @return the name of this command
	 */
	String name() default "";

	/**
	 * @return command parameters/arguments
	 */
	String params() default "";

	/**
	 * @return the description of this command
	 */
	String desc() default "";

	/**
	 * @return the permission of this command
	 */
	String[] permission() default "";

	/**
	 * @return Whenever this command should only be performed by player or not
	 */
	boolean playerOnly() default false;

}
