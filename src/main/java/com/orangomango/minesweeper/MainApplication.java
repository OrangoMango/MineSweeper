package com.orangomango.minesweeper;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.canvas.*;
import javafx.scene.paint.Color;
import javafx.animation.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.KeyCode;

import java.util.HashMap;

public class MainApplication extends Application{
	private static final int WIDTH = 600;
	private static final int HEIGHT = 750;
	private static final int OFFSET_X = 50;
	private static final int OFFSET_Y = 140;

	private Map map;
	private boolean firstClick;
	private boolean showMines;
	private HashMap<KeyCode, Boolean> keys = new HashMap<>();

	@Override
	public void start(Stage stage){
		StackPane pane = new StackPane();
		Canvas canvas = new Canvas(WIDTH, HEIGHT);
		GraphicsContext gc = canvas.getGraphicsContext2D();
		pane.getChildren().add(canvas);

		this.map = new Map(20, 20);

		canvas.setOnMousePressed(e -> {
			final int cellX = (int)((e.getX()-OFFSET_X)/Cell.SIZE);
			final int cellY = (int)((e.getY()-OFFSET_Y)/Cell.SIZE);
			Cell cell = this.map.getCellAt(cellX, cellY);

			if (cell != null){
				if (!this.firstClick){
					this.map.buildMines(cellX, cellY);
				}
				this.firstClick = true;
				if (e.getButton() == MouseButton.PRIMARY){
					if (cell.getRevealed() > 0){
						int flagCells = 0;
						for (int i = Math.max(cellX-1, 0); i < Math.min(cellX+2, this.map.getWidth()); i++){
							for (int j = Math.max(cellY-1, 0); j < Math.min(cellY+2, this.map.getHeight()); j++){
								Cell c = this.map.getCellAt(i, j);
								if (c.isFlag()){
									flagCells++;
								}
							}
						}

						if (flagCells == cell.getRevealed()){
							for (int i = Math.max(cellX-1, 0); i < Math.min(cellX+2, this.map.getWidth()); i++){
								for (int j = Math.max(cellY-1, 0); j < Math.min(cellY+2, this.map.getHeight()); j++){
									Cell c = this.map.getCellAt(i, j);
									if (!c.isFlag()){
										if (c.isMine()){
											gameOver();
										} else {
											c.reveal(this.map);
										}
									}
								}
							}
						}
					} else {
						if (cell.isFlag()) cell.toggleFlag();
						boolean gameOver = cell.reveal(this.map);
						if (gameOver) gameOver();
					}				
				} else if (e.getButton() == MouseButton.SECONDARY){
					cell.toggleFlag();
				}

				// Check if the game is finished
				if (this.map.isFinished()){
					System.out.println("YOU WIN");
				}
			}
		});

		canvas.setFocusTraversable(true);
		canvas.setOnKeyPressed(e -> this.keys.put(e.getCode(), true));
		canvas.setOnKeyReleased(e -> this.keys.put(e.getCode(), false));

		AnimationTimer loop = new AnimationTimer(){
			@Override
			public void handle(long time){
				update(gc);
			}
		};
		loop.start();

		Scene scene = new Scene(pane, WIDTH, HEIGHT);
		stage.setScene(scene);
		stage.setResizable(false);
		stage.setTitle("Minesweeper");
		stage.show();
	}

	private void gameOver(){
		System.out.println("GAME OVER");
		this.showMines = true;
	}

	private void update(GraphicsContext gc){
		gc.clearRect(0, 0, WIDTH, HEIGHT);
		gc.setFill(Color.BLACK);
		gc.fillRect(0, 0, WIDTH, HEIGHT);

		if (this.keys.getOrDefault(KeyCode.F1, false)){
			this.showMines = !this.showMines;
			this.keys.put(KeyCode.F1, false);
		} else if (this.keys.getOrDefault(KeyCode.R, false)){
			this.map = new Map(20, 20);
			this.firstClick = false;
			this.keys.put(KeyCode.R, false);
		}

		gc.translate(OFFSET_X, OFFSET_Y);
		this.map.render(gc, this.showMines);
		gc.translate(-OFFSET_X, -OFFSET_Y);
	}

	public static void main(String[] args){
		launch(args);
	}
}