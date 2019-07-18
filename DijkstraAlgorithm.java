import java.util.ArrayList;
import java.util.List;

public class DijkstraAlgorithm {

	private List<Integer> endList;
	private List<Integer> searchList;
	private List<Integer> startList;
	Manager inter;

	public DijkstraAlgorithm(Manager iface) {
		System.out.println("interface ready");
		this.inter = iface;
	}

	public void setStarts(Coordinates c) {
		System.out.println("setStarts started");

		startList = new<Integer> ArrayList();
		searchList = new<Integer> ArrayList();
		endList = new<Integer> ArrayList();

		int x = c.getX();
		int y = c.getY();

		startList.add(x);
		startList.add(y);

		for (int i = 0; i < startList.size(); i = i + 2) {
			runDij(startList.get(i), startList.get(i + 1));
			// loading runDij with the start tile fills the search list
			// now it continues
			while (searchList.size() != 0 && endList.size() == 0) {
				runDij(searchList.get(0), searchList.get(1));
				searchList.remove(0);
				searchList.remove(0);
			}

			if (endList.size() == 0) {
				System.out.println("no path from start to end");
			}
			while (endList.size() != 0) {
				printPath(endList.get(0), endList.get(1), 0);
				endList.remove(0);
				endList.remove(0);
			}

		}
		// go to end tiles and now traverse back through their parents to get
		// shorts path, and print it out

	}

	private void runDij(int x, int y) {
		// search tiles above,right,below,and left of given node
		// once this is done it sets the node to explored unless it is a start
		// or end tile
		// can't search obstacles
		// clength represents the current path length from the start node
		int cLength = inter.dArray[x][y].tileLength;

		if (inter.dArray[x][y].currentProp == 3) {
			// for the starting tile we keep its parent as itself but change the
			// path length to 0 to indicate that it is the start of the path
			inter.dArray[x][y].tileLength = 0;
		}
		if (x != 0) {
			checkSide(x - 1, y, "Up", cLength);
		}

		if (y != inter.iface.getGridSizeY() - 1) {
			checkSide(x, y + 1, "Right", cLength);
		}

		if (x != inter.iface.getGridSizeX() - 1) {
			checkSide(x + 1, y, "Down", cLength);
		}

		if (y != 0) {
			checkSide(x, y - 1, "Left", cLength);
		}

		System.out.println("ran D");
	}

	private void checkSide(int x, int y, String direction, int cLength) {

		if (inter.dArray[x][y].currentProp == 1 || inter.dArray[x][y].currentProp == 4) {
			// to expand perhaps also allow PATH properties to be checked put
			// dont add them into the list unless they have a longer path length
			searchList.add(x);
			searchList.add(y);
			// set current tile to CURRENT
			// once search is done set it to explored
			if (inter.dArray[x][y].tileLength == 9999) {
				inter.dArray[x][y].tileLength = cLength + 1;
			}
			// inter.setTileParent(x, y, inter.getTile(x, y)); if setting
			// parents as tiles is more practical than string directions
			if (inter.dArray[x][y].parent == "Stop") {
				inter.dArray[x][y].parent = direction;
			}

			if (inter.dArray[x][y].currentProp == 4) {
				foundEnd(x, y);
			}

			// if its current path length is larger than our current tile lenght
			// +1 then change it to that and set this tile as its parent
			if (inter.dArray[x][y].currentProp == 1) {
				inter.dArray[x][y].currentProp = 5;
				if (inter.iface.getPropertyInt(x, y) != 7)
					inter.iface.setToProperty(x, y, TileProperties.Property.PATH);
			}
		}
	}

	// this method stores the end tiles in an array list as they are found by
	// the algorithm
	private void foundEnd(int x, int y) {

		endList.add(x);
		endList.add(y);
	}

	private void printPath(int x, int y, int length) {

		if (inter.dArray[x][y].currentProp == 5) {
			inter.iface.setToProperty(x, y, TileProperties.Property.SHORT);
		}

		switch (inter.dArray[x][y].parent) {
		case "Left":
			System.out.println("--> Left");
			printPath(x, y + 1, length + 1);
			break;
		case "Up":
			System.out.println("--> Up");
			printPath(x + 1, y, length + 1);
			break;
		case "Right":
			System.out.println("--> Right");
			printPath(x, y - 1, length + 1);
			break;
		case "Down":
			System.out.println("--> Down");
			printPath(x - 1, y, length + 1);
			break;
		case "Stop":
			System.out.println("Start");
			System.out.println("The length of the path is: " + length);
			break;
		default:
			System.out.println("Their is an unknown parent");
		}
	}

}