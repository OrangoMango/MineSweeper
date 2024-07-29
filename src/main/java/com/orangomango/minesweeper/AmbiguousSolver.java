package com.orangomango.minesweeper;

import javafx.geometry.Point2D;

import java.util.ArrayList;

public class AmbiguousSolver{
	// 0-8 reveal
	// -1 not revealed
	// -2 flag
	private int[][] world;

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

	public void solve(){
		ArrayList<Point2D> possibilities = new ArrayList<>();

		for (int x = 0; x < this.world.length; x++){
			for (int y = 0; y < this.world[0].length; y++){
				int num = this.world[x][y];
				if (num == -1){
					if (hasRevealed(x, y)){
						possibilities.add(new Point2D(x, y));
					}
				}
			}
		}

		System.out.println(possibilities);

		for (Point2D p : possibilities){
			if (isFlagAllowed((int)p.getX(), (int)p.getY())){
				this.world[(int)p.getX()][(int)p.getY()] = -2;
				System.out.println("Toggled: "+p);
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
				}
			}
		}

		return count;
	}

	private boolean isFlagAllowed(int x, int y){
		this.world[x][y] = -2; // Try to toggle flag

		for (int i = Math.max(0, x-1); i < Math.min(this.world.length, x+2); i++){
			for (int j = Math.max(0, y-1); j < Math.min(this.world[0].length, y+2); j++){
				int num = this.world[i][j];
				if (num > 0){
					int count = countFlags(i, j);
					if (count != num){
						this.world[x][y] = -1;
						return false;
					}
				}
			}
		}

		this.world[x][y] = -1;
		return true;
	}

	private int countFlags(int x, int y){
		int count = 0;
		for (int i = Math.max(0, x-1); i < Math.min(this.world.length, x+2); i++){
			for (int j = Math.max(0, y-1); j < Math.min(this.world[0].length, y+2); j++){
				int num = this.world[i][j];
				if (num == -2){
					count++;
				}
			}
		}

		return count;
	}

	private boolean hasRevealed(int x, int y){
		for (int i = Math.max(0, x-1); i < Math.min(this.world.length, x+2); i++){
			for (int j = Math.max(0, y-1); j < Math.min(this.world[0].length, y+2); j++){
				int num = this.world[i][j];
				if (num > 0){
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