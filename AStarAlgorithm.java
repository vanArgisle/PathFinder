import java.util.ArrayList;
import java.util.ArrayList;
import java.util.List;

public class AStarAlgorithm {

	private List<Integer> endList;
	private List<Integer> searchList;
	private List<Integer> startList;
	private List<Integer> searchPoints;// used for setting the heuristics
	Manager inter;

	public AStarAlgorithm(Manager iface) {
		System.out.println("interface ready");
		this.inter = iface;
	}

	public void setStarts(Coordinates c) {
		System.out.println("setStarts started");

		int x = c.getX();
		int y = c.getY();

		prepareA(inter.iface.getGridSizeX(), inter.iface.getGridSizeY());

		System.out.println("A* Diagonal prepared");

		// int example = inter.getPropertyInt(3, 1);
		// System.out.println(example);

		// System.out.println(x+"_ and for y:"+ y);
		startList = new<Integer> ArrayList();
		searchList = new<Integer> ArrayList();
		endList = new<Integer> ArrayList();

		System.out.println("added x:" + x + " and y:" + y);
		startList.add(x);
		startList.add(y);

		for (int i = 0; i < startList.size(); i = i + 2) {
			runAStar(startList.get(i), startList.get(i + 1));
			sortSearchListHeuristic();

			// loading runAStar with the start tile fills the search list
			// now it continues
			while (searchList.size() != 0 && endList.size() == 0) {
				runAStar(searchList.get(0), searchList.get(1));
				searchList.remove(0);
				searchList.remove(0);
				sortSearchListHeuristic();
			}

			if (endList.size() == 0) {
				System.out.println("no path from start to end");
			}
			while (endList.size() != 0) {
				// System.out.println("The path length is: " +
				// inter.getTileLength(endList.get(0),endList.get(1)));
				printPath(endList.get(0), endList.get(1), 0);
				endList.remove(0);
				endList.remove(0);
			}

		}
		// go to end tiles and now traverse back through their parents to get
		// shortest path, and print it out

	}

	// this method gets the start & end points and gets calls a method to set
	// the heuristics from those positions
	private void prepareA(int x, int y) {
		System.out.println("setHuristic started");

		ArrayList startPoints = new<Integer> ArrayList();
		ArrayList endPoints = new<Integer> ArrayList();
		searchPoints = new<Integer> ArrayList();

		startPoints = findPoints(x, y, 3);
		if (startPoints.size() != 0) {
			inter.setHeuristic((int) startPoints.get(0), (int) startPoints.get(1), 0, 0);
			setHeuristics((int) startPoints.get(0), (int) startPoints.get(1), 0);
		}
		while (searchPoints.size() != 0) {
			setHeuristics(searchPoints.get(0), searchPoints.get(1), 0);
			searchPoints.remove(0);
			searchPoints.remove(0);
		}

		endPoints = findPoints(x, y, 4);
		if (endPoints.size() != 0) {
			inter.setHeuristic((int) endPoints.get(0), (int) endPoints.get(1), 0, 1);
			setHeuristics((int) endPoints.get(0), (int) endPoints.get(1), 1);
		}
		while (searchPoints.size() != 0) {
			setHeuristics(searchPoints.get(0), searchPoints.get(1), 1);
			searchPoints.remove(0);
			searchPoints.remove(0);
		}
	}

	// this finds either the start or end points and returns them in a list
	private ArrayList findPoints(int x, int y, int property) {

		ArrayList points = new<Integer> ArrayList();

		for (int i = 0; i < x; i++) {
			for (int j = 0; j < y; j++) {

				if (inter.dArray[i][j].currentProp == property) {
					System.out.println("added x:" + i + " and y:" + j);
					points.add(i);
					points.add(j);
				}
			}
		}
		return points;
	}

	private void setHeuristics(int x, int y, int hType) {

		int length = inter.getHeuristic(x, y, hType);

		if (x != 0) {
			checkHeuristic(x - 1, y, length + 1, hType);
		}

		if (y != inter.iface.getGridSizeY() - 1) {
			checkHeuristic(x, y + 1, length + 1, hType);
		}

		if (x != inter.iface.getGridSizeX() - 1) {
			checkHeuristic(x + 1, y, length + 1, hType);
		}

		if (y != 0) {
			checkHeuristic(x, y - 1, length + 1, hType);
		}

	}

	private void checkHeuristic(int x, int y, int length, int hType) {
		if (inter.getHeuristic(x, y, hType) == 99999) {
			// if(length> inter.getHeuristic(x, y, hType)){
			// if(hType == 0){
			// length = length +1;
			// }
			inter.setHeuristic(x, y, length, hType);
			searchPoints.add(x);
			searchPoints.add(y);

		}
	}

	private void runAStar(int x, int y) {

		int cLength = inter.dArray[x][y].tileLength;

		if (inter.dArray[x][y].currentProp == 3) {
			// for the starting tile we keep its parent as itself but change the
			// path length to 0 to indicate that it is the start of the path
			inter.dArray[x][y].tileLength = 0;
		}
		if (x != 0) {
			checkSide(x - 1, y, "Up", cLength + 1);
		}

		if (y != inter.iface.getGridSizeY() - 1) {
			checkSide(x, y + 1, "Right", cLength + 1);
		}

		if (x != inter.iface.getGridSizeX() - 1) {
			checkSide(x + 1, y, "Down", cLength + 1);
		}

		if (y != 0) {
			checkSide(x, y - 1, "Left", cLength + 1);
		}

		System.out.println("ran aStar");
	}

	private void checkSide(int x, int y, String direction, int cLength) {

		if (inter.dArray[x][y].currentProp == 5 && inter.dArray[x][y].tileLength > cLength) {
			inter.dArray[x][y].parent = direction;
			inter.dArray[x][y].tileLength = cLength;
		}

		if (inter.dArray[x][y].currentProp == 1 || inter.dArray[x][y].currentProp == 4) {

			// to expand perhaps also allow PATH properties to be checked put
			// dont add them into the list unless they have a longer path length
			searchList.add(x);
			searchList.add(y);
			// set current tile to CURRENT
			// once search is done set it to explored
			inter.dArray[x][y].tileLength = cLength;
			// if (inter.getTileLength(x, y) == 9999){ inter.setTileLength(x, y,
			// cLength+1);}

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

	private void sortSearchListHeuristic() {
		// insertion sort
		int i, j, valI, valJ, valH, valEH;
		for (i = 2; i < searchList.size(); i = i + 2)// i cycles through the
														// second position of
														// the array to the end
		{
			valI = searchList.get(i);// val is set to the heuristic value of the
										// current i positions tile
			valJ = searchList.get(i + 1);
			valH = inter.getHeuristic(valI, valJ, 2);
			valEH = inter.getHeuristic(valI, valJ, 1);// end heuristic

			for (j = i; j > 0
					&& valH < (inter.getHeuristic(searchList.get(j - 2), searchList.get(j - 1), 2)); j = j - 2) {// j
																													// cycles
																													// to
																													// the
																													// left
																													// of
																													// i,the
																													// loop
																													// only
																													// executes
				// as long as the value to the left of j is smaller than val and
				// j is not in the first position
				// integerArray[j] = integerArray[j -1];//the value at position
				// j is replaced if the value to its left is bigger than it

				if (valH == (inter.getHeuristic(searchList.get(j - 2), searchList.get(j - 1), 2))) {
					if (valEH < (inter.getHeuristic(searchList.get(j - 2), searchList.get(j - 1), 1))) {
						searchList.set(j, searchList.get(j - 2));
						searchList.set(j + 1, searchList.get(j - 1));
					}
				}
				if (valH != (inter.getHeuristic(searchList.get(j - 2), searchList.get(j - 1), 2))) {
					searchList.set(j, searchList.get(j - 2));
					searchList.set(j + 1, searchList.get(j - 1));
				}

				// searchList.set(j, searchList.get(j-2));
				// searchList.set(j+1, searchList.get(j-1));

			}
			searchList.set(j, valI);
			searchList.set(j + 1, valJ);
		}
	}

	private void sortSearchListLength() {
		// insertion sort
		int i, j, valI, valJ, valH;
		for (i = 2; i < searchList.size(); i = i + 2)// i cycles through the
														// second position of
														// the array to the end
		{
			valI = searchList.get(i);// val is set to the heuristic value of the
										// current i positions tile
			valJ = searchList.get(i + 1);
			valH = (inter.dArray[valI][valJ].tileLength);

			for (j = i; j > 0 && valH < (inter.dArray[j - 2][j - 1].tileLength); j = j - 2) {// j
																								// cycles
																								// to
																								// the
																								// left
																								// of
																								// i,the
																								// loop
																								// only
																								// executes
				// as long as the value to the left of j is smaller than val and
				// j is not in the first position
				// integerArray[j] = integerArray[j -1];//the value at position
				// j is replaced if the value to its left is bigger than it

				searchList.set(j, searchList.get(j - 2));
				searchList.set(j + 1, searchList.get(j - 1));

				// searchList.set(j, searchList.get(j-2));
				// searchList.set(j+1, searchList.get(j-1));

			}
			searchList.set(j, valI);
			searchList.set(j + 1, valJ);
		}
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
