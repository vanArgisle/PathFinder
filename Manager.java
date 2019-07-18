import java.util.concurrent.locks.ReentrantLock;

public class Manager {

	public Interface iface;
	public Data[][] dArray;
	private Path coord;

	public Manager(Interface i, Path c) {
		iface = i;
		coord = c;
		dArray = new Data[iface.getGridSizeX()][iface.getGridSizeY()];

		for (int x = 0; x < iface.getGridSizeX(); x++) {
			for (int y = 0; y < iface.getGridSizeY(); y++) {
				clone(iface.getTile(x, y), x, y);
			}
		}
	}

	private void clone(Tile t, int x, int y) {
		Data d = new Data();

		d.currentProp = iface.getPropertyInt(x, y);
		if (d.currentProp == 4 && (coord.getEnd().getX() != x || coord.getEnd().getY() != y))
			d.currentProp = 2; // Replace unwanted endings
		if (d.currentProp == 3 && (coord.getStart().getX() != x || coord.getStart().getY() != y))
			d.currentProp = 2; // Replace unwanted starts
		d.xVal = x;
		d.yVal = y;

		dArray[x][y] = d;
	}

	public void runAlgorithm() {
		if (iface.getCurrentAlgorithm().equals("Dijkstra")) {
			DijkstraAlgorithm dAlgorithm = new DijkstraAlgorithm(this);
			dAlgorithm.setStarts(coord.getStart());
		} else if (iface.getCurrentAlgorithm().equals("A*")) {
			AStarAlgorithm aAlgorithm = new AStarAlgorithm(this);
			aAlgorithm.setStarts(coord.getStart());
		} else if (iface.getCurrentAlgorithm().equals("A* Diagonal")) {
			AStarDiagonal aAlgorithm = new AStarDiagonal(this);
			aAlgorithm.setStarts(coord.getStart());
		} else if (iface.getCurrentAlgorithm().equals("Concurrent")) {
			ConcurrentAlgorithm cAlgorithm = new ConcurrentAlgorithm(this);
			cAlgorithm.setStarts(coord.getStart());
		}
	}

	public int getHeuristic(int x, int y, int hType) {
		if (hType == 0) {
			return dArray[x][y].startHeuristic;
		} else if (hType == 1) {
			return dArray[x][y].endHeuristic;
		}
		// returns the combined heuristic if htype is not 0 or 1
		else
			return dArray[x][y].endHeuristic + dArray[x][y].startHeuristic;
	}

	public void setHeuristic(int x, int y, int l, int hType) {
		if (hType == 0) {
			dArray[x][y].startHeuristic = l;
		}
		if (hType == 1) {
			dArray[x][y].endHeuristic = l;
		}
	}

}

class Data {
	public final ReentrantLock l = new ReentrantLock();
	public int currentProp;
	public String parent = "Stop";
	public int tileLength = 9999;
	public int endHeuristic = 99999;
	public int startHeuristic = 99999;;

	// X and Y coordinated where it is on the grid.
	public int xVal;
	public int yVal;
}
