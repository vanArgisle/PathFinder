import java.util.ArrayList;
import java.util.List;
//import java.lang.Thread;
import java.util.concurrent.locks.ReentrantLock;

public class ConcurrentAlgorithm extends Thread {

	private Coordinates coord;
	private static List<Coordinates> endList;
	private static List<Coordinates> searchList;
	private static ReentrantLock listLock = new ReentrantLock();
	private List<Coordinates> startList;
	private List<Thread> threads;
	Manager inter;

	public ConcurrentAlgorithm(Manager m) {
		this.inter = m;
	}

	public ConcurrentAlgorithm(Manager m, Coordinates c) {
		this.inter = m;
		coord = c;
	}

	public void setStarts(Coordinates start) {

		System.out.println("setStarts started");

		startList = new<Coordinates> ArrayList();
		searchList = new<Coordinates> ArrayList();
		endList = new<Coordinates> ArrayList();

		int startctr = 1;
		startList.add(start);

		// loading runDij with the start tile fills the search list
		for (int i = 0; i < startList.size(); i++) {
			runDij(startList.get(i));
		}

		if (endList.size() < startctr) {
			do {
				createThreads();

			} while (!checkThreads() && searchList.size() != 0 && endList.size() < startctr);
		}

		if (endList.size() == 0) {
			System.out.println("no path from start to end");
		}
		for (int i = 0; i < endList.size(); i++) {
			printPath(endList.get(i), 0);
		}
		// go to end tiles and now traverse back through their parents to get
		// shorts path, and print it out

	}

	public void createThreads() {
		threads = new<Thread> ArrayList();

		while (searchList.size() != 0) {
			try {
				ConcurrentAlgorithm cell = new ConcurrentAlgorithm(inter, searchList.get(0));
				searchList.remove(0);
				Thread t = new Thread(cell, "Cell");
				threads.add(t);
			} catch (Exception e) {
				// System.out.println("OutofboundSize");
				// searchList.remove(0);
				// break;
			}
		}

		for (int i = 0; i < threads.size(); i++)
			threads.get(i).start();
	}

	private boolean checkThreads()// Barrier for thread expansion
	{
		boolean t = true;
		do {
			for (int i = 0; i < threads.size(); i++) {
				if (threads.get(i).isAlive())
					t = true;
				else
					t = false;
			}
		} while (t);

		return false;
	}

	public void run() {
		runDij(coord);
	}

	private void runDij(Coordinates c) {

		// search tiles above,right,below,and left of given node
		// once this is done it sets the node to explored unless it is a start
		// or end tile
		// can't search obstacles
		// clength represents the current path length from the start node
		int X = c.getX();
		int Y = c.getY();
		int cLength;
		inter.dArray[X][Y].l.lock();
		try {
			cLength = inter.dArray[X][Y].tileLength;
		} finally {
			inter.dArray[X][Y].l.unlock();
		}

		if (inter.dArray[X][Y].currentProp == 3) {
			// for the starting tile we keep its parent as itself but change the
			// path length to 0 to indicate that it is the start of the path
			inter.dArray[X][Y].tileLength = 0;
		}
		if (X != 0) {
			checkSide(X - 1, Y, "Right", cLength + 10);
		}

		if (Y != inter.iface.getGridSizeY() - 1) {
			checkSide(X, Y + 1, "Up", cLength + 10);
		}

		if (X != inter.iface.getGridSizeX() - 1) {
			checkSide(X + 1, Y, "Left", cLength + 10);
		}

		if (Y != 0) {
			checkSide(X, Y - 1, "Down", cLength + 10);
		}

		if (X != 0 && Y != 0) {
			checkSide(X - 1, Y - 1, "D-RD", cLength + 15);
		}

		if (Y != inter.iface.getGridSizeY() - 1 && X != inter.iface.getGridSizeX() - 1) {
			checkSide(X + 1, Y + 1, "D-LU", cLength + 15);
		}

		if (X != inter.iface.getGridSizeX() - 1 && Y != 0) {
			checkSide(X + 1, Y - 1, "D-LD", cLength + 15);
		}

		if (X != 0 && Y != inter.iface.getGridSizeY() - 1) {
			checkSide(X - 1, Y + 1, "D-RU", cLength + 15);
		}
	}

	private void checkSide(int X, int Y, String direction, int cLength) {

		inter.dArray[X][Y].l.lock();
		try {
			if (inter.dArray[X][Y].currentProp == 1 || inter.dArray[X][Y].currentProp == 4) {
				// Tile has not yet been read.
				if (inter.dArray[X][Y].tileLength == 9999) {
					if (inter.dArray[X][Y].currentProp == 4) {
						foundEnd(X, Y);
					} else {
						listLock.lock();
						try {
							searchList.add(new Coordinates(X, Y));
						} finally {
							listLock.unlock();
						}
					}
					inter.dArray[X][Y].tileLength = cLength;
					inter.dArray[X][Y].parent = direction;
				}
				// If current tiles length is smaller than other tile length
				// then change it
				else if (inter.dArray[X][Y].tileLength > cLength) {
					inter.dArray[X][Y].tileLength = cLength;
					inter.dArray[X][Y].parent = direction;
				}

				if (inter.dArray[X][Y].currentProp == 1) {
					inter.dArray[X][Y].currentProp = 5;
					if (inter.iface.getPropertyInt(X, Y) != 7)
						inter.iface.setToProperty(X, Y, TileProperties.Property.PATH);
				}
			}
			// Tile has been read by another thread previous but current tile is
			// closer to it.
			else if (inter.dArray[X][Y].currentProp == 5 && inter.dArray[X][Y].tileLength > cLength) {
				inter.dArray[X][Y].tileLength = cLength;
				inter.dArray[X][Y].parent = direction;
			}
		} finally {
			inter.dArray[X][Y].l.unlock();
		}
	}

	// this method stores the end tiles in an array list as they are found by
	// the algorithm
	private void foundEnd(int X, int Y) {

		listLock.lock();
		try {
			endList.add(new Coordinates(X, Y));
		} finally {
			listLock.unlock();
		}
	}

	private void printPath(int X, int Y, int length) {

		if (inter.dArray[X][Y].currentProp == 5) {
			inter.iface.setToProperty(X, Y, TileProperties.Property.SHORT);
		}

		switch (inter.dArray[X][Y].parent) {
		case "Down":
			System.out.println("--> Up");
			printPath(X, Y + 1, length + 1);
			break;
		case "Right":
			System.out.println("--> Left");
			printPath(X + 1, Y, length + 1);
			break;
		case "Up":
			System.out.println("--> Down");
			printPath(X, Y - 1, length + 1);
			break;
		case "Left":
			System.out.println("--> Right");
			printPath(X - 1, Y, length + 1);
			break;
		case "D-LU":
			System.out.println("--> D-RD");
			printPath(X - 1, Y - 1, length + 1);
			break;
		case "D-RU":
			System.out.println("--> D-LD");
			printPath(X + 1, Y - 1, length + 1);
			break;
		case "D-RD":
			System.out.println("--> D-LU");
			printPath(X + 1, Y + 1, length + 1);
			break;
		case "D-LD":
			System.out.println("--> D-RU");
			printPath(X - 1, Y + 1, length + 1);
			break;
		case "Stop":
			System.out.println("Start");
			System.out.println("The length of the path is: " + length);
			break;
		default:
			System.out.println("Their is an unknown parent");
		}
	}

	private void printPath(Coordinates c, int length) {
		printPath(c.getX(), c.getY(), length);
	}
}