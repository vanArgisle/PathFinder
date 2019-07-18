import java.awt.Color;

/**
 * Data structure that holds a everything related to a path
 *
 */
public class Path
{
	private Coordinates startTile;
	private Coordinates endTile;
	
	public final Color startColor;
	public final Color endColor;
	
	public Path(String start, String end)
	{
		startColor = Color.decode(start);
		endColor = Color.decode(end);
	}
	
	public void setStart(int x, int y)
	{
		startTile = new Coordinates(x,y);
	}
	
	public void setEnd(int x, int y)
	{
		endTile = new Coordinates(x,y);
	}
	
	public void removeStart()
	{
		startTile = null;
	}
	
	public void removeEnd()
	{
		endTile = null;
	}
	
	public Coordinates getStart()
	{
		return startTile;
	}
	
	public Coordinates getEnd()
	{
		return endTile;
	}
	
}
