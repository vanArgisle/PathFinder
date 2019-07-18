/**
 * Data structure for holding an ordered pair of numbers. 
 * These number represent the x-y coordinates on the grid.
 *
 */
public class Coordinates
{
	private int x;
	private int y;
	
	/**
	 * Constructor that sets the initial values.
	 * @param x
	 * @param y
	 */
	public Coordinates(int x, int y)
	{
		this.x = x;
		this.y = y;
	}
	
	/**
	 * Sets new values to the pair.
	 * @param x
	 * @param y
	 */
	public void setNew(int x, int y)
	{
		this.x = x;
		this.y = y;
	}
	
	/**
	 * Gets x value.
	 * @return int
	 */
	public int getX()
	{
		return x;
	}
	
	/**
	 * Gets y value.
	 * @return int
	 */
	public int getY()
	{
		return y;
	}
}
