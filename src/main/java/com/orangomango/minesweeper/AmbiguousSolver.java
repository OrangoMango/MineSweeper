package com.orangomango.minesweeper;

import javafx.geometry.Point2D;

import java.util.ArrayList;

public class AmbiguousSolver{
	// 0-8 reveal
	// -1 not revealed
	// -2 flag
	// -3 to reveal
	private int[][] world;

	private static int EXCLUDED = 0;
	private static int EXCLUDED_2 = 0;

	public AmbiguousSolver(Map map){
		this.world = new int[map.getWidth()][map.getHeight()];

		for (int i = 0; i < map.getWidth(); i++){
			for (int j = 0; j < map.getHeight(); j++){
				Cell cell = map.getCellAt(i, j);
				if (cell.isFlag()){
					this.world[i][j] = -2;
				} else {
					this.world[i][j] = cell.getRevealed();
				}
			}
		}
	}

	public int apply(Map map){
		int count = 0;

		for (int i = 0; i < map.getWidth(); i++){
			for (int j = 0; j < map.getHeight(); j++){
				if (this.world[i][j] == -2 && !map.getCellAt(i, j).isFlag()){
					map.getCellAt(i, j).toggleFlag();
					count++;
				} else if (this.world[i][j] == -3){
					map.getCellAt(i, j).reveal(map);
				}
			}
		}

		return count;
	}

	public void solve(int minesAvailable){
		ArrayList<Point2D> availableCells = new ArrayList<>();
		ArrayList<Point2D> possibilities = new ArrayList<>();
		ArrayList<int[][]> maps = new ArrayList<>();

		int[][] flagMap = new int[this.world.length][this.world[0].length];

		for (int x = 0; x < this.world.length; x++){
			for (int y = 0; y < this.world[0].length; y++){
				int num = this.world[x][y];
				if (num > 0 && hasEmpty(x, y)){
					availableCells.add(new Point2D(x, y));
				}
			}
		}

		System.out.println("ac: "+availableCells+", "+availableCells.size());

		// Build current flags map
		for (Point2D p : availableCells){
			flagMap[(int)p.getX()][(int)p.getY()] = countFlags(this.world, (int)p.getX(), (int)p.getY());
		}

		for (int i = 0; i < availableCells.size(); i++){
			ArrayList<Point2D> neighbors = getNeighbors(this.world, (int)availableCells.get(i).getX(), (int)availableCells.get(i).getY());
			for (Point2D n : neighbors){
				if (!possibilities.contains(n)){
					possibilities.add(n);
				}
			}
		}

		System.out.println("poss: "+possibilities+", "+possibilities.size());

		walk(possibilities, availableCells, 0, maps, minesAvailable, flagMap, 0);

		System.out.format("Found %d maps, exluded %d maps for zeros and exluded %s in total\n", maps.size(), EXCLUDED, EXCLUDED_2);

		double[] count = calculateProb(possibilities, maps);
		System.out.println(java.util.Arrays.toString(count));

		for (int i = 0; i < count.length; i++){
			Point2D p = possibilities.get(i);
			if (count[i] == 1){
				this.world[(int)p.getX()][(int)p.getY()] = -2;
			} else if (count[i] == 0){
				this.world[(int)p.getX()][(int)p.getY()] = -3;
			}
		}
	}

	private static ArrayList<Point2D> getNeighbors(int[][] world, int x, int y){
		ArrayList<Point2D> output = new ArrayList<>();
		for (int i = Math.max(0, x-1); i < Math.min(world.length, x+2); i++){
			for (int j = Math.max(0, y-1); j < Math.min(world[0].length, y+2); j++){
				int num = world[i][j];
				if (num == -1){
					output.add(new Point2D(i, j));
				}
			}
		}

		return output;
	}

	private static double[] calculateProb(ArrayList<Point2D> possibilities, ArrayList<int[][]> maps){
		double[] count = new double[possibilities.size()];
		for (int i = 0; i < maps.size(); i++){
			int[][] map = maps.get(i);

			for (int j = 0; j < possibilities.size(); j++){
				Point2D p = possibilities.get(j);
				if (map[(int)p.getX()][(int)p.getY()] == -2){
					count[j]++;
				}
			}
		}

		for (int i = 0; i < count.length; i++){
			count[i] /= maps.size();
		}

		return count;
	}

	private void walk(ArrayList<Point2D> list, ArrayList<Point2D> availableCells, int index, ArrayList<int[][]> output, int minesAvailable, int[][] flagMap, int depth){
		if (index == list.size()){
			int[][] backup = backup();

			boolean valid = true;
			for (int i = 0; i < availableCells.size(); i++){
				Point2D p = availableCells.get(i);
				if (backup[(int)p.getX()][(int)p.getY()] != flagMap[(int)p.getX()][(int)p.getY()]){
					if (flagMap[(int)p.getX()][(int)p.getY()] == 0){
						MainApplication.DEBUG_INFO = "00\n";
						EXCLUDED++;
					} else {
						//System.out.format("Reason: %d,%d at %s\n", backup[(int)p.getX()][(int)p.getY()], flagMap[(int)p.getX()][(int)p.getY()], p);

						// DEBUG
						/*System.out.println("=====");
						for (int y = 0; y < backup[0].length; y++){
							for (int x = 0; x < backup.length; x++){
								int num = backup[x][y];
								if (num >= 0) System.out.print(" ");
								System.out.print(" "+num);
							}
							System.out.println();
						}
						System.out.println("=====");*/
					}
					valid = false;
					break;
				}
			}

			if (valid){
				output.add(backup);
				MainApplication.DEBUG_INFO += "Map found: "+output.size();
			} else {
				MainApplication.DEBUG_INFO += "Map invalid at depth "+depth;
				EXCLUDED_2++;
			}

			return;
		}

		Point2D pos = list.get(index);
		MainApplication.DEBUG_INFO = String.format("Depth: %d (%d)\nIndex: %d", depth, list.size(), index);

		// Try to use this flag
		if (minesAvailable > 0){
			if (isValid(this.world, flagMap, (int)pos.getX(), (int)pos.getY())){
				this.world[(int)pos.getX()][(int)pos.getY()] = -2;
				updateFlagMap(flagMap, (int)pos.getX(), (int)pos.getY(), 1);

				walk(list, availableCells, index+1, output, minesAvailable-1, flagMap, depth+1);

				this.world[(int)pos.getX()][(int)pos.getY()] = -1;
				updateFlagMap(flagMap, (int)pos.getX(), (int)pos.getY(), -1);
			}
		}

		// Try to not use this flag
		if (isEmptyValid(world, flagMap, (int)pos.getX(), (int)pos.getY(), list, index+1)){
			walk(list, availableCells, index+1, output, minesAvailable, flagMap, depth+1);
		}
	}

	private int[][] backup(){
		int[][] output = new int[this.world.length][this.world[0].length];

		for (int x = 0; x < this.world.length; x++){
			for (int y = 0; y < this.world[0].length; y++){
				output[x][y] = this.world[x][y];
			}
		}

		return output;
	}

	private static boolean isEmptyValid(int[][] world, int[][] flagMap, int x, int y, ArrayList<Point2D> list, int index){
		// Get all possible neighbors
		ArrayList<Point2D> neighbors = new ArrayList<>();
		for (int i = Math.max(0, x-1); i < Math.min(world.length, x+2); i++){
			for (int j = Math.max(0, y-1); j < Math.min(world[0].length, y+2); j++){
				int num = world[i][j];
				if (num > 0){
					neighbors.add(new Point2D(i, j));
				}
			}
		}

		// Filter the 'last-tile' ones
		ArrayList<Point2D> lastOnes = new ArrayList<>();
		for (Point2D p : neighbors){
			if (index == list.size()){
				lastOnes.add(p);
			} else {
				ArrayList<Point2D> subList = new ArrayList<>(list.subList(index, list.size()));
				boolean contained = false;
				final int px = (int)p.getX();
				final int py = (int)p.getY();
				x_loop:
				for (int i = Math.max(0, px-1); i < Math.min(world.length, px+2); i++){
					for (int j = Math.max(0, py-1); j < Math.min(world[0].length, py+2); j++){
						if (subList.contains(new Point2D(i, j))){
							contained = true;
							break x_loop;
						}
					}
				}

				if (!contained){
					lastOnes.add(p);
				}
			}
		}

		// Check if the 'last-tiles' are valid
		for (Point2D p : lastOnes){
			if (world[(int)p.getX()][(int)p.getY()] != flagMap[(int)p.getX()][(int)p.getY()]){
				return false;
			}
		}

		return true;
	}

	private static void updateFlagMap(int[][] flagMap, int x, int y, int inc){
		for (int i = Math.max(0, x-1); i < Math.min(flagMap.length, x+2); i++){
			for (int j = Math.max(0, y-1); j < Math.min(flagMap[0].length, y+2); j++){
				flagMap[i][j] += inc; // Ignore -1 tiles
			}
		}
	}

	private static boolean isValid(int[][] world, int[][] flagMap, int x, int y){
		for (int i = Math.max(0, x-1); i < Math.min(world.length, x+2); i++){
			for (int j = Math.max(0, y-1); j < Math.min(world[0].length, y+2); j++){
				int num = world[i][j];
				if (num > 0){
					if (flagMap[i][j]+1 > num){
						return false;
					}
				}
			}
		}

		return true;
	}

	private static int countFlags(int[][] world, int x, int y){
		int count = 0;
		for (int i = Math.max(0, x-1); i < Math.min(world.length, x+2); i++){
			for (int j = Math.max(0, y-1); j < Math.min(world[0].length, y+2); j++){
				int num = world[i][j];
				if (num == -2){
					count++;
				}
			}
		}

		return count;
	}

	private boolean hasEmpty(int x, int y){
		for (int i = Math.max(0, x-1); i < Math.min(this.world.length, x+2); i++){
			for (int j = Math.max(0, y-1); j < Math.min(this.world[0].length, y+2); j++){
				int num = this.world[i][j];
				if (num == -1){
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public String toString(){
		StringBuilder builder = new StringBuilder();
		for (int y = 0; y < this.world[0].length; y++){
			for (int x = 0; x < this.world.length; x++){
				int num = this.world[x][y];
				if (num >= 0) builder.append(" ");
				builder.append(" "+num);
			}
			builder.append("\n");
		}

		return builder.toString();
	}
}