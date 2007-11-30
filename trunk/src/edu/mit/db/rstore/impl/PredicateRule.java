package edu.mit.db.rstore.impl;

/**
 * This class contains information necessary to specify what to do with a predicate in the DB populator.  It can be thought of
 * as an arc template and the direction of the arc.  It will contain a predicate name, the subject and object types, and the arc
 * direction.
 * 
 * @author Angelika
 *
 */

public class PredicateRule 
{
	public enum Direction
	{
		FORWARD,
		BACKWARD
	};
	
	private String predicate, subject, object;
	private Direction direction;
	
	public PredicateRule(String pred, String sub, String obj, Direction dir)
	{
		predicate = pred;
		subject = sub;
		object = obj;
		direction = dir;
	}
	
	public String getPredicate()
	{
		return new String(predicate);
	}
	
	public String getSubject()
	{
		return new String(subject);
	}
	
	public String getObject()
	{
		return new String(object);
	}
	
	public Direction getDirection()
	{
		return direction;
	}
	
	/**
	 * Allow arc reversal
	 */
	public void setDirection(Direction dir)
	{
		direction = dir;
	}
	
	/**
	 * Does not take directionality of the arc into account, may change this later if necessary, I think not, since the primary
	 * expected use is in the DBPopulator
	 */
	public boolean equals(Object o)
	{
		if(o instanceof PredicateRule)
			return ((PredicateRule)o).subject.equals(subject) && ((PredicateRule)o).object.equals(object) && ((PredicateRule)o).predicate.equals(predicate);
		return false;
	}
	
	/**
	 * Ugh... HashMaps use hashCode!  I dont know anything about hash codes, so relying on a not ridiculous number of these things
	 */
	public int hashCode()
	{
		return (subject.hashCode() + predicate.hashCode() + object.hashCode()) % 37;
	}
	
	public void print()
	{
		System.out.println("< " + subject + ", " + predicate + ", " + object + " >  " + direction.toString());
	}

}
