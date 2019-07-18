import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.awt.event.*;

/**
 * Creates the interface for a grid so the user can select the tile properties.
 *
 */
public class Interface {
	public static void main(String args[]) {
		Interface x = new Interface();
	}

	// Variables of the different parts of the interface.
	private JFrame frame;
	private int xFrameSize = 800;
	private int yFrameSize = 700;

	private int xGridNum;
	private int yGridNum;

	private JPanel mainPanel;
	private JPanel gridPanel;
	private JPanel optionPanel;

	private JButton calculatePathButton;
	private JButton resetButton;
	private JButton loadFileButton;
	private JButton saveFileButton;

	private JComboBox<String> algorithmSelection;
	private JComboBox<String> gridSizeSelection;

	private Tile[][] tile2DArray;

	public final Path[] paths = new Path[6];

	private boolean gridResetting = false;
	private String currentAlgorithm = "Dijkstra";

	private ReentrantLock setLock = new ReentrantLock();

	// Creates the interface with an initial grid size of __ by __.
	public Interface() {
		// Initializes the paths and sets the colors to them.
		paths[0] = new Path("#ff0000", "#ff6666");
		paths[1] = new Path("#0000ff", "#6666ff");
		paths[2] = new Path("#33cc33", "#85e085");
		paths[3] = new Path("#ff9900", "#ffc266");
		paths[4] = new Path("#cc00cc", "#ff33ff");
		paths[5] = new Path("#663300", "#CC6600");

		// Initializes variables
		xGridNum = 10;
		yGridNum = 10;

		// Initializes all the components of the interface.
		frame = new JFrame();

		mainPanel = new JPanel();
		optionPanel = new JPanel();

		calculatePathButton = new JButton("Calculate Path");
		resetButton = new JButton("Reset Grid");
		loadFileButton = new JButton("Load File");
		saveFileButton = new JButton("Save File");

		Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
		labelTable.put(new Integer(0), new JLabel("None"));
		labelTable.put(new Integer(50), new JLabel("Fast"));
		labelTable.put(new Integer(500), new JLabel("Slow"));

		// Algorithm types for user to select is created here.
		algorithmSelection = new JComboBox<String>(new String[] { "Dijkstra", "A*", "A* Diagonal", "Concurrent" });

		// Grid size selection for user to select is here.
		gridSizeSelection = new JComboBox<String>(new String[] { "10x10", "20x20", "30x30", "40x40", "50x50" });

		// Sets the properties of the components.

		mainPanel.setLayout(new BorderLayout());
		optionPanel.setLayout(new GridBagLayout());
		optionPanel.setSize(new Dimension(100, yFrameSize));
		frame.setMinimumSize(new Dimension(800, 700));

		// Creates the default grid of size 10x10.
		createGrid(xGridNum, yGridNum);

		// Adds listeners to components that need them.
		calculatePathButton.addActionListener(new CalculatePathListener(this));
		resetButton.addActionListener(new ResetListener(this));
		gridSizeSelection.addActionListener(new SizeComboBoxListener(this));
		algorithmSelection.addActionListener(new AlgorithmComboBoxListener(this));
		saveFileButton.addActionListener(new SaveButtonListener(this));
		loadFileButton.addActionListener(new LoadButtonListener(this));

		// Adds all the components on the option pane to it.
		// Object c is used to align everything together.
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.PAGE_START;

		c.gridy = 0;
		optionPanel.add(calculatePathButton, c);

		c.gridy = 1;
		optionPanel.add(resetButton, c);

		c.gridy = 2;
		optionPanel.add(loadFileButton, c);

		c.gridy = 3;
		optionPanel.add(saveFileButton, c);

		c.gridy = 4;
		optionPanel.add(new JPanel(), c);

		c.gridy = 5;
		optionPanel.add(new JPanel(), c);

		c.gridy = 6;
		optionPanel.add(algorithmSelection, c);

		c.gridy = 7;
		optionPanel.add(gridSizeSelection, c);

		// Adds a filler panel to cause the layout manager to push
		// everything else to the top so the alignment is even.
		c.gridy = 8;
		c.weighty = 1;
		optionPanel.add(new Panel(), c);

		mainPanel.add(optionPanel, BorderLayout.WEST);

		// Sets the frame properties.
		frame.add(mainPanel);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(xFrameSize, yFrameSize);
		frame.setVisible(true);

	}

	/**
	 * Creates the grid of dimensions x by y and adds it to the frame.
	 * 
	 * @param x
	 *            int
	 * @param y
	 *            int
	 */
	private void createGrid(int x, int y) {
		// Create new objects for the gird.
		JPanel newPanel = new JPanel();
		Tile[][] tile2DArray = new Tile[x][y];

		newPanel.setLayout(new GridLayout(x, y));

		for (int i = 0; i < x; i++) {
			for (int j = 0; j < y; j++) {
				Tile tile = new Tile(i, j, paths);
				tile2DArray[i][j] = tile;
				tile.addMouseListener(new TileMouseListener(tile, this));
				tile.setBackground(Color.WHITE);
				tile.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
				newPanel.add(tile);
			}
		}

		// Updates variables to new values.
		xGridNum = x;
		yGridNum = y;
		this.tile2DArray = tile2DArray;

		// This catches an exception is there is no component inside the
		// mainPanel.
		// This happens when it first creates the grid for the first time.
		try {
			// Sets the panel inviable so the removal is a lot faster.
			gridPanel.setVisible(false);
			mainPanel.remove(gridPanel);
		} catch (Exception e) {
		}

		// Adds the new grid to the frame.
		gridPanel = newPanel;
		mainPanel.add(gridPanel, BorderLayout.CENTER);
		gridPanel.revalidate();
		gridPanel.repaint();

	}

	/**
	 * Resets the grid values after the user has calculated the path.
	 */
	public void resetTileValues() {
		gridResetting = false;
		for (int i = 0; i < xGridNum; i++) {
			for (int j = 0; j < yGridNum; j++) {
				Tile temp = tile2DArray[i][j];
				if (temp.getProperty().getPropNum() > 4) {
					temp.setTileProperty(TileProperties.Property.EMPTY);
				}
			}
		}
	}

	/**
	 * Resets the grid to initial state keeping the same size.
	 */
	public void resetGrid() {
		resetGrid(xGridNum, yGridNum);
	}

	/**
	 * Resets the grid to initial using the new size.
	 * 
	 * @param x
	 * @param y
	 */
	public void resetGrid(int x, int y) {
		// Resets the start and end points in the array.
		for (int i = 0; i < paths.length; i++) {
			paths[i].removeStart();
			paths[i].removeEnd();
		}
		// Updates the gird size combo bow with the new display.
		if (x != xGridNum) {
			gridSizeSelection.setSelectedIndex((x / 10) - 1);
		}
		createGrid(x, y);
	}

	/**
	 * Sets the given tile to a given property.
	 */
	public void setToProperty(int x, int y, TileProperties.Property p) {
		setLock.lock();
		try {
			tile2DArray[x][y].setTileProperty(p);
		} finally {
			setLock.unlock();
		}
	}

	/**
	 * Gets the given tile's property.
	 */
	public TileProperties.Property getProperty(int x, int y) {
		return tile2DArray[x][y].getProperty();
	}

	/**
	 * Gets the given tile's property number.
	 */
	public int getPropertyInt(int x, int y) {

		switch (tile2DArray[x][y].getProperty()) {
		case EMPTY:
			return 1;

		case OBSTACLE:
			return 2;

		case START:
			return 3;

		case END:
			return 4;

		case PATH:
			return 5;

		case CHECKED:
			return 6;

		case SHORT:
			return 7;

		default:
			return 0;// this should trigger an error/ notification that there is
						// a problem with this nodes property
		}
	}

	/**
	 * Sets the given tile to a path.
	 */
	public void setToPath(int x, int y) {
		tile2DArray[x][y].setTileProperty(TileProperties.Property.PATH);
	}

	/**
	 * Returns the size of the grid.
	 * 
	 * @return xGridNum
	 */

	/**
	 * Displays an pop up message to the user with the given input.
	 */
	public void displayToUser(String message) {
		JOptionPane.showMessageDialog(frame, message);
	}

	public Tile getTile(int x, int y) {
		return tile2DArray[x][y];
	}

	/**
	 * Returns the size of the grid's x length.
	 * 
	 * @return xGridNum
	 */
	public int getGridSizeX() {
		return xGridNum;
	}

	/**
	 * Returns the size of the grid's y length.
	 * 
	 * @return yGridNum
	 */
	public int getGridSizeY() {
		return yGridNum;
	}

	/**
	 * Sets whether the grid needs resetting variable.
	 * 
	 * @param b
	 */
	public void setGridResetting(Boolean b) {
		gridResetting = b;
	}

	/**
	 * Gets whether the grid needs resetting variable.
	 * 
	 * @param b
	 */
	public boolean getGridResetting() {
		return gridResetting;
	}

	/**
	 * Sets the algorithm type to the input.
	 * 
	 * @param s
	 */
	public void setCurrentAlgorithm(String s) {
		currentAlgorithm = s;
	}

	/**
	 * Returns the algorithm type.
	 * 
	 * @return String
	 */
	public String getCurrentAlgorithm() {
		return currentAlgorithm;
	}
}

/**
 * The grid is split up into a 2d grid of "tiles" which the user can interact
 * with to change properties.
 */
class Tile extends JPanel {
	public final ReentrantLock l = new ReentrantLock();
	private Path[] paths;
	private TileProperties.Property currentProp;
	private Color tileColor;
	private Color borderColor;

	// X and Y coordinated where it is on the grid.
	private int xVal;
	private int yVal;

	// Coordinates object if this tile is start or end.
	private Path startPath;
	private Path endPath;

	// Constructor for Tile Class.
	public Tile(int x, int y, Path[] paths) {
		currentProp = TileProperties.Property.EMPTY;
		this.paths = paths;
		xVal = x;
		yVal = y;
	}

	/**
	 * Changes the property of this tile to the next in the rotation.
	 */
	public void toggleNextProperty() {
		// If the property is the last in line, go back to beginning.
		if (currentProp.getPropNum() >= 4) {
			setTileProperty(TileProperties.Property.EMPTY);
		}
		// Else set it to the next one.
		else {
			// Since the arrays in property start at 0 but the numbers
			// associated with each
			// value start at 1, getting the next property is equivalent to
			// passing in
			// value associated with the current enum value.
			setTileProperty(TileProperties.Property.intToProperty(currentProp.getPropNum()));
		}
		System.out.println(currentProp);
	}

	/**
	 * Returns the tiles color.
	 * 
	 * @return Color
	 */
	public TileProperties.Property getProperty() {
		return currentProp;
	}

	/**
	 * Sets the next property to the parameter.
	 * 
	 * @param p
	 */
	public void setTileProperty(TileProperties.Property p) {
		currentProp = p;
		setTileColour();
	}

	/**
	 * Sets the color of the tile to let the user distinguish which property the
	 * tile has.
	 */
	private void setTileColour() {
		// Sets the color of the tile depending on the tile property
		switch (currentProp) {
		case EMPTY:
			tileColor = Color.WHITE;
			borderColor = Color.LIGHT_GRAY;
			try {
				startPath.removeStart();
				startPath = null;
			} catch (Exception e) {
			}
			try {
				endPath.removeEnd();
				endPath = null;
			} catch (Exception e) {
			}
			break;

		case OBSTACLE:
			tileColor = Color.BLACK;
			borderColor = Color.BLACK;
			try {
				startPath.removeStart();
				startPath = null;
			} catch (Exception e) {
			}
			try {
				endPath.removeEnd();
				endPath = null;
			} catch (Exception e) {
			}
			break;

		case START:
			try {
				startPath.removeStart();
				startPath = null;
			} catch (Exception e) {
			}
			tileColor = getColor(TileProperties.Property.START);
			borderColor = Color.LIGHT_GRAY;
			try {
				endPath.removeEnd();
				endPath = null;
			} catch (Exception e) {
			}
			break;

		case END:
			try {
				endPath.removeEnd();
				endPath = null;
			} catch (Exception e) {
			}
			tileColor = getColor(TileProperties.Property.END);
			borderColor = Color.LIGHT_GRAY;
			try {
				startPath.removeStart();
				startPath = null;
			} catch (Exception e) {
			}
			break;

		case PATH:
			// 255,253,199 is a light yellow color.
			tileColor = new Color(255, 253, 199);
			borderColor = Color.LIGHT_GRAY;
			try {
				startPath.removeStart();
				startPath = null;
			} catch (Exception e) {
			}
			try {
				endPath.removeEnd();
				endPath = null;
			} catch (Exception e) {
			}
			break;

		// this property is used for the current node being checked
		case CHECKED:
			tileColor = Color.GREEN;
			borderColor = Color.LIGHT_GRAY;
			break;
		// this property labels the shortest route
		case SHORT:
			tileColor = Color.CYAN;
			borderColor = Color.LIGHT_GRAY;
			break;
		}

		// If equals null, this means no more paths are available so skip over
		// property.
		if (tileColor == null) {
			toggleNextProperty();
			return;
		}
		// Updates the tile with the new colors.
		this.setBackground(tileColor);
		this.setBorder(BorderFactory.createLineBorder(borderColor));
	}

	/**
	 * Gets the next start/end color in line.
	 * 
	 * @param p
	 * @return Color
	 */
	private Color getColor(TileProperties.Property p) {
		// Gets the next start color available.
		if (p == TileProperties.Property.START) {
			for (int i = 0; i < paths.length; i++) {
				// Iterates through the path array looking for an available
				// start.
				if (paths[i].getStart() == null) {
					paths[i].setStart(xVal, yVal);
					startPath = paths[i];
					return paths[i].startColor;
				}
			}
		}
		// Gets the next end color available.
		else {
			for (int i = 0; i < paths.length; i++) {
				// Iterates through the path array looking for an available end.
				if (paths[i].getEnd() == null) {
					paths[i].setEnd(xVal, yVal);
					endPath = paths[i];
					return paths[i].endColor;
				}
			}
		}

		// If all the paths are full, return null as a signal.
		return null;
	}
}

/**
 * Creates the mouse listener for the "tiles" so that when clicked, they will
 * change their property. This allows the user to create the different aspects
 * for the path algorithms to work on.
 */
class TileMouseListener implements MouseListener {
	// Variable shared with all the tiles that signals whether to change
	// the state of the tile.
	static boolean doChange = false;
	static TileProperties.Property lastProp;

	private Tile thisTile;
	private Interface inter;

	// Constructor for the mouse listener.
	public TileMouseListener(Tile t, Interface inter) {
		thisTile = t;
		this.inter = inter;
	}

	// Method that triggers the tile which was pressed to change its state.
	@Override
	public void mousePressed(MouseEvent e) {
		doChange = true;

		// Since this is only triggered when the user presses the mouse on a
		// tile,
		// check to see if the grid needs resetting. If it does, then reset
		// before
		// performing the action on the tile.
		if (inter.getGridResetting()) {
			inter.resetTileValues();
		}

		// If the right button is pressed, then set to empty.
		if (SwingUtilities.isRightMouseButton(e)) {
			thisTile.setTileProperty(TileProperties.Property.EMPTY);
		}
		// Else, toggle the next property.
		else {
			thisTile.toggleNextProperty();
			lastProp = thisTile.getProperty();
		}
	}

	// Method that triggers to stop updating the tile when
	// entering the tile, but the mouse is not pressed.
	@Override
	public void mouseReleased(MouseEvent e) {
		// Don't allow tiles to be changed anymore.
		doChange = false;
	}

	// Method that triggers when the mouse enters the tile.
	// Only happens if the mouse is pressed.
	@Override
	public void mouseEntered(MouseEvent e) {
		// Only change the tile if the mouse is pressed down.
		if (doChange) {
			// If the right button is pressed, then set to empty.
			if (SwingUtilities.isRightMouseButton(e)) {
				thisTile.setTileProperty(TileProperties.Property.EMPTY);
			}
			// Else, set the property to the one the last tile had.
			else {
				thisTile.setTileProperty(lastProp);
			}
		}
	}

	// Unused methods that are here to fulfill the interface.
	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

}

/**
 * ActionListener for the calculatePathButton which will take the input on the
 * grid and give it to the selected algorithm to run.
 */
class CalculatePathListener implements ActionListener {
	Interface inter;

	public CalculatePathListener(Interface inter) {
		this.inter = inter;
	}

	/**
	 * Method to create way to calculate the paths. Also checks that there are
	 * no missing points.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		System.out.println("Run algorithms.");
		// Checks to see if every end/start point has a start/end point.
		boolean correct = true;
		int correctPaths = 0;

		for (int i = 0; i < inter.paths.length; i++) {
			if (inter.paths[i].getStart() == null) {
				if (inter.paths[i].getEnd() != null) {
					correct = false;
				}
			} else if (inter.paths[i].getEnd() == null) {
				if (inter.paths[i].getStart() != null) {
					correct = false;
				}
			} else {
				// If the path has a start and end, then increase the counter.
				correctPaths++;
			}
		}

		// Tells the user that it cannot continue since points are missing.
		if (!correct) {
			inter.displayToUser("Missing end/start points! Cannot calculate.");
			return;
		}
		// Tells the user that they do not have any points placed.
		else if (correctPaths == 0) {
			inter.displayToUser("No start and end points placed! ");
			return;
		}
		// Sets gridResetting to true so the values will reset when the user
		// makes a
		// a modification after calculating the path.
		inter.resetTileValues();
		inter.setGridResetting(true);

		// Continues on to next step
		ArrayList<Manager> mArray = new ArrayList<Manager>();
		for (int i = 0; i < inter.paths.length; i++) {
			if (inter.paths[i].getStart() != null) {
				Manager m = new Manager(this.inter, inter.paths[i]);
				mArray.add(m);
			}

		}
		for (int i = 0; i < mArray.size(); i++) {
			mArray.get(i).runAlgorithm();
		}
	}
}

/**
 * ActionListener for the resetGridButton which will reset all the tiles back to
 * their empty state.
 */
class ResetListener implements ActionListener {
	Interface inter;

	// Constructor for ResetListener.
	public ResetListener(Interface inter) {
		this.inter = inter;
	}

	/**
	 * Calls the reset function of the interface whenever the reset button is
	 * pressed.
	 * 
	 * @param e
	 *            exception
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		inter.resetGrid();
		System.out.println("Reset the Grid.");
	}

}

/**
 * Listener for the algorithm selection combo box which will update the variable
 * for which algorithm to use.
 */
class AlgorithmComboBoxListener implements ActionListener {
	Interface inter;

	// Constructor for SizeComboBoxListener
	public AlgorithmComboBoxListener(Interface inter) {
		this.inter = inter;
	}

	/**
	 * Updates the variable which holds what algorithm is selected.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		String stringAlgorithm = (String) ((JComboBox<String>) e.getSource()).getSelectedItem();
		inter.setCurrentAlgorithm(stringAlgorithm);
		System.out.println(stringAlgorithm);
	}

}

/**
 * Listener for the size combo box which will call the interface to update the
 * grid when given a new input.
 *
 */
class SizeComboBoxListener implements ActionListener {
	Interface inter;

	// Constructor for SizeComboBoxListener
	public SizeComboBoxListener(Interface inter) {
		this.inter = inter;
	}

	/**
	 * Resizes the grid whenever an event is called here. The new size is based
	 * on the user selection in the combo box.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		String stringSize = (String) ((JComboBox<String>) e.getSource()).getSelectedItem();
		int newSize = Integer.parseInt(stringSize.substring(0, 2));

		// Do not update the gird if the size selected is the current size.
		if (inter.getGridSizeX() != newSize) {
			inter.resetGrid(newSize, newSize);
		}
		System.out.println(newSize);
	}

}

/**
 * Saves the information on the grid to a .txt file to the location where the
 * user tells it to.
 *
 */
class SaveButtonListener implements ActionListener {
	Interface inter;

	public SaveButtonListener(Interface inter) {
		this.inter = inter;
	}

	/**
	 * Method to save the file.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		String location;
		String fileName = null;

		JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

		// If the user approves the location, ask for file name.
		if (fc.showSaveDialog(new JFrame()) == JFileChooser.APPROVE_OPTION) {

			// Gets the path to the selected location.
			location = fc.getSelectedFile().getAbsolutePath();

			// If the file contains a ".txt" extension, then it is a text file
			// so there is no need
			// to ask the user for file name. If it doesn't contain that
			// extension then we assume
			// the location is a directory and ask the user for a file name.
			if (!location.contains(".txt")) {
				location = location.concat(".txt");
			}
		}
		// This means the user has canceled the save so exit method.
		else {
			return;
		}

		// Write to file
		FileWriter fw = null;
		try {
			fw = new FileWriter(location);

			// The first line holds the grid size
			fw.write(Integer.toString(inter.getGridSizeX()));
			fw.write(System.lineSeparator());

			// Write the starting points to file.
			for (int i = 0; i < inter.paths.length; i++) {
				try {
					fw.write(inter.paths[i].getStart().getX() + "," + inter.paths[i].getStart().getY() + " ");
				}
				// If the point doesn'y exist, write null
				catch (NullPointerException NPe) {
					fw.write("null ");
				}
			}
			fw.write(System.lineSeparator());

			// Write the ending points to file.
			for (int i = 0; i < inter.paths.length; i++) {
				try {
					fw.write(inter.paths[i].getEnd().getX() + "," + inter.paths[i].getEnd().getY() + " ");
				}
				// If the point doesn'y exist, write null
				catch (NullPointerException NPe) {
					fw.write("null ");
				}
			}
			fw.write(System.lineSeparator());

			// Write the grid to file
			for (int i = 0; i < inter.getGridSizeX(); i++) {
				for (int j = 0; j < inter.getGridSizeX(); j++) {
					// 0 means that square is empty, 1 means not empty.
					TileProperties.Property p = inter.getProperty(i, j);
					if (p == TileProperties.Property.EMPTY) {
						fw.write("0 ");
					} else {
						fw.write("1 ");
					}
				}
				// Go to next line after writing this one.
				fw.write(System.lineSeparator());
			}
		}
		// If an error occurs, then tell the user.
		catch (IOException IOe) {
			IOe.printStackTrace();
			inter.displayToUser("Error saving file!");
		}
		// After done writing, close the file.
		finally {
			try {
				if (fw != null) {
					fw.flush();
					fw.close();
				}
			} catch (IOException IOe) {
				IOe.printStackTrace();
			}
		}
	}
}

/**
 * Loads a text file with the given information about the grid.
 *
 */
class LoadButtonListener implements ActionListener {
	Interface inter;

	// Constructor for class.
	public LoadButtonListener(Interface inter) {
		this.inter = inter;
	}

	/**
	 * Method that loads the file.
	 * 
	 * @param e
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		String location;

		JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);

		// If the user approves the location, ask for file name.
		if (fc.showOpenDialog(new JFrame()) == JFileChooser.APPROVE_OPTION) {
			location = fc.getSelectedFile().getAbsolutePath();
		}
		// This means the user has canceled the save so exit method.
		else {
			return;
		}
		System.out.println(location);

		BufferedReader br = null;
		// Tries to open the file.
		try {
			br = new BufferedReader(new FileReader(location));
			String line = br.readLine();
			int lineNum = 0;

			String[] startPoints = null;
			String[] endPoints = null;
			while (line != null) {
				switch (lineNum) {
				// Line 0 holds the grid size.
				case 0:
					int size = Integer.parseInt(line);
					inter.resetGrid(size, size);
					break;

				// Line 1 holds the start points.
				case 1:
					startPoints = line.split(" ");
					break;

				// Line 2 holds the end points.
				case 2:
					endPoints = line.split(" ");
					break;

				// All other lines holds the grid values.
				default:
					String[] values = line.split(" ");
					for (int i = 0; i < values.length; i++) {
						if (Integer.parseInt(values[i]) == 1) {
							inter.setToProperty(lineNum - 3, i, TileProperties.Property.OBSTACLE);
						}
					}
				}
				lineNum++;
				line = br.readLine();
			}
			// Adds the start points to the grid.
			for (int i = 0; i < startPoints.length; i++) {
				if (!startPoints[i].equals("null")) {
					String[] temp = startPoints[i].split(",");
					int x = Integer.parseInt(temp[0]);
					int y = Integer.parseInt(temp[1]);
					inter.setToProperty(x, y, TileProperties.Property.START);
				}
			}
			// Adds the end points to the grid.
			for (int i = 0; i < endPoints.length; i++) {
				if (!endPoints[i].equals("null")) {
					String[] temp = endPoints[i].split(",");
					int x = Integer.parseInt(temp[0]);
					int y = Integer.parseInt(temp[1]);
					inter.setToProperty(x, y, TileProperties.Property.END);
				}
			}
		}
		// Tell the user if you cannot open the file.
		catch (Exception e1) {
			e1.printStackTrace();
			inter.displayToUser("Cannot open the file.");
		}
		// Close the reader before exiting.

		finally {
			try {
				if (br != null) {
					br.close();
				}
			} catch (IOException IOe) {
				IOe.printStackTrace();
			}
		}
	}
}

/**
 * Enum class. This class creates the different settings that each tile can
 * currently be.
 */
class TileProperties {
	public static enum Property {
		EMPTY(1), OBSTACLE(2), START(3), END(4), PATH(5), CHECKED(6), SHORT(7);

		private final int propNum;
		private static int[] valueArray = new int[7];
		private static Property[] propertyArray = new Property[7];

		// Static part of class to initialize the lookup table.
		static {
			for (int i = 0; i < 6; i++) {
				valueArray[i] = i + 1;
			}
			int i = 0;
			for (Property p : Property.values()) {
				propertyArray[i] = p;
				i++;
			}
		}

		// Sets the tileNum value for each option.
		private Property(int v) {
			propNum = v;
		}

		// Public method which converts a given number into its associated enum
		// variable.
		public static Property intToProperty(int value) {
			return propertyArray[value];
		}

		// Public method to return the currentValue of tileNum
		public int getPropNum() {
			return propNum;
		}
	};
}