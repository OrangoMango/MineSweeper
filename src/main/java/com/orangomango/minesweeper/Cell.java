package com.orangomango.minesweeper;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.text.TextAlignment;

public class Cell{
	private int x, y;
	private boolean mine;
	private int revealed = -1;
	private boolean flag = false;

	public static final double SIZE = 25;
	private static final Image IMAGE = new Image(Cell.class.getResourceAsStream("/cell.png"));

	public Cell(int x, int y, boolean mine){
		this.x = x;
		this.y = y;
		this.mine = mine;
	}

	public void toggleFlag(){
		this.flag = !this.flag;
	}

	public int getRevealed(){
		return this.revealed;
	}

	public boolean isFlag(){
		return this.flag;
	}

	public boolean isMine(){
		return this.mine;
	}

	public void setMine(boolean value){
		this.mine = value;
	}

	public boolean reveal(Map map){
		if (this.revealed != -1) return false;
		if (this.mine){
			return true;
		} else {
			int count = 0;
			for (int i = Math.max(this.x-1, 0); i < Math.min(this.x+2, map.getWidth()); i++){
				for (int j = Math.max(this.y-1, 0); j < Math.min(this.y+2, map.getHeight()); j++){
					Cell cell = map.getCellAt(i, j);
					if (cell.mine){
						count++;
					}
				}
			}

			this.revealed = count;
			if (count == 0){
				for (int i = Math.max(this.x-1, 0); i < Math.min(this.x+2, map.getWidth()); i++){
					for (int j = Math.max(this.y-1, 0); j < Math.min(this.y+2, map.getHeight()); j++){
						Cell cell = map.getCellAt(i, j);
						if (cell != this){
							cell.reveal(map);
						}
					}
				}
			}
		}

		return false;
	}

	public void render(GraphicsContext gc){
		int frameIndex = 0;

		if (this.revealed > 0){
			frameIndex = this.revealed;
		} else if (this.flag){
			frameIndex = 10;
		} else if (this.mine){
			//frameIndex = 9;
		}

		gc.drawImage(IMAGE, 1+frameIndex*22, 1, 20, 20, this.x*SIZE, this.y*SIZE, SIZE, SIZE);
	}
}