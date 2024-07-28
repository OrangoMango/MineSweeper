package com.orangomango.minesweeper;

import javafx.scene.canvas.GraphicsContext;

public class Map{
	private int w, h;
	private Cell[][] map;

	public Map(int w, int h){
		this.w = w;
		this.h = h;
		this.map = new Cell[this.w][this.h];

		for (int i = 0; i < this.w; i++){
			for (int j = 0; j < this.h; j++){
				this.map[i][j] = new Cell(i, j, false);
			}
		}
	}

	public void buildMines(int x, int y){
		for (int i = 0; i < this.w; i++){
			for (int j = 0; j < this.h; j++){
				if (i != x || j != y){
					this.map[i][j].setMine(Math.random() < 0.2);
				}
			}
		}
	}

	public Cell getCellAt(int x, int y){
		if (x >= 0 && y >= 0 && x < this.w && y < this.h){
			return this.map[x][y];
		} else {
			return null;
		}
	}

	public void render(GraphicsContext gc){
		for (int i = 0; i < this.w; i++){
			for (int j = 0; j < this.h; j++){
				Cell cell = this.map[i][j];
				cell.render(gc);
			}
		}
	}

	public int getWidth(){
		return this.w;
	}

	public int getHeight(){
		return this.h;
	}
}