package org.lab.osm.connector.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.commons.lang3.StringUtils;

/**
 * Oracle stored procedure / function definition.
 * 
 * This notation applies to an interface extending from {@link org.lab.osm.connector.handler.StoredProcedureExecutor} to
 * setup the invocation information as follows:
 * 
 * <pre>
 * &#64;OracleStoredProcedure(name = "MY_STORED_PROCEDURE_NAME",
 * 	oraclePackage = "MY_PACKAGE_NAME",
 * 	owner = "MY_USER",
 * 	isFunction = false,
 * 	parameters = { @OracleParameter(name = "P_01", type = Types.NUMERIC, mode = ParameterType.IN),
 * 		&#64;OracleParameter(name = "P_02", type = Types.NVARCHAR, mode = ParameterType.IN),
 * 		&#64;OracleParameter(name = "P_03",
 * 			typeName = "SOME_ORACLE_TYPE",
 * 			type = Types.ARRAY,
 * 			mode = ParameterType.OUT,
 * 			returnStructClass = MyResultEntity.class) })
 * public interface MyStoredProcedureExecutor extends StoredProcedureExecutor {
 * }
 * </pre>
 * 
 * @author lab.cabrera@gmail.com
 * @since 1.0.0
 *
 * @see org.lab.osm.connector.handler.StoredProcedureExecutor
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface OracleStoredProcedure {

	/**
	 * Stored procedure name.
	 * @return
	 */
	String name();

	/**
	 * Stored procedure owner.
	 * @return
	 */
	String owner() default StringUtils.EMPTY;

	/**
	 * Oracle package name.
	 * @return
	 */
	String oraclePackage() default StringUtils.EMPTY;

	/**
	 * Stored procedure / function discriminator.
	 * @return
	 */
	boolean isFunction() default false;

	/**
	 * Stored procedure parameter list.
	 * @return
	 */
	OracleParameter[] parameters() default {};

}
