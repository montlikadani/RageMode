package hu.montlikadani.ragemode.database;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DB {

	/**
	 * @return the database type {@link DBType}
	 */
	DBType type() default DBType.YAML;

	/**
	 * @return true if the database is file-based otherwise false
	 */
	boolean fileBased() default true;
}
