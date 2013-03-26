package kz.bsbnb.usci.eav.model.type;

/**
 * Holds complex keys (array or entity) types
 * 
 * @author a.tkachenko
 * @version 1.0, 17.01.2013
 */
public enum ComplexKeyTypes
{
	/**
	 * AND strategy
	 * For array:
	 * All elements must match to find entity.
	 * Example: all documents must match to find a person
	 * 
	 * For entity:
	 * All key attributes must match to find entity.
	 */
	ALL,
	/**
	 * OR strategy
	 * Any element of an array must match to find entity. 
	 * Example: One matching document is enough to find a person
	 * 
	 * For entity:
	 * Any key attribute must match to find entity.
	 */
	ANY
}
