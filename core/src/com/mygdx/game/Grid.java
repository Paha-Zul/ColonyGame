package com.mygdx.game;

import com.mygdx.game.entity.Entity;
import com.mygdx.game.interfaces.Functional;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Bbent_000 on 11/16/2014.
 */
public class Grid {
	public static GridInstance activeGrid;
	public static HashMap<String, GridInstance> gridMap = new HashMap<>();

	public static void NewGrid(String name, float sizeX, float sizeY, int squareSize){
		gridMap.put(name, new GridInstance(sizeX, sizeY, squareSize));
	}

	public static void NewGrid(String name, float sizeX, float sizeY, int squareSize, boolean active){
		GridInstance grid = new GridInstance(sizeX, sizeY, squareSize);
		gridMap.put(name, grid);
		if(active)
			Grid.activeGrid = grid;
	}

	public static GridInstance getGrid(String name){
		return gridMap.get(name);
	}

	public static class GridInstance{
		public int squareSize;

		Cell[][] grid;

		private GridInstance(float sizeX, float sizeY, int squareSize){
			int cols = ((int)sizeX/squareSize) + 1;
			int rows = ((int)sizeY/squareSize) + 1;
			this.squareSize = squareSize;

			grid = new Cell[cols][rows];

			for(int col = 0;col < cols; col++){
				for(int row = 0; row < rows; row++){
					grid[col][row] = new Cell(col, row);
				}
			}
		}

		/**
		 * Gets a Cell from this grid using a float X and Y value.
		 * @param x The X position which will be converted into an index.
		 * @param y The Y Position which will be converted into an index.
		 * @return A Cell if one could be retrieved from the grid, null otherwise (if the position was out of the grid bounds).
		 */
		public Cell getCell(float x, float y){
			return this.getCell((int)x/squareSize, (int)y/squareSize);
		}

		/**
		 * Gets a Cell from this grid using an integer X and Y index.
		 * @param x The X index to get the cell from.
		 * @param y THe Y index to get the cell from.
		 * @return A Cell if one could be retrieved from the grid, null otherwise (if the position was out of the grid bounds).
		 */
		public Cell getCell(int x, int y){
			if(x >= 0 && x < grid.length && y >= 0 && y < grid[0].length)
				return grid[x][y];
			return null;
		}

		/**
		 * Gets the number of columns in this grid.
		 * @return An integer which is the length of the first dimension.
		 */
		public int getNumCols(){
			return grid.length;
		}

		/**
		 * Gets the number of rows in this grid.
		 * @return An integer which is the length of the second dimension.
		 */
		public int getNumRows(){
			return grid[0].length;
		}

		public final Cell[][] getGrid(){
			return grid;
		}

		public <T> void  iterate(Functional.Perform<T> perform){
			for(int x=0;x<grid.length;x++){
				for(int y=0;y<grid[0].length;y++){
					perform.perform((T)grid[x][y]);
				}
			}
		}
	}

	public static class Cell{
		private HashMap<Double, Entity> map = new HashMap<>();
		private int col, row;

		public Cell(int col, int row){
			this.col = col;
			this.row = row;
		}

		public void addToCell(Entity Entity) {
			map.put(Entity.getID(), Entity);
	}

		public void removeFromCell(Entity Entity){
			map.remove(Entity.getID());
		}

		public Entity getFromCell(Entity Entity){
			return map.get(Entity.getID());
		}

		public ArrayList<Entity> getObjectList(){
			return new ArrayList<>(map.values());
		}

		public int getCol(){
			return this.col;
		}

		public int getRow(){
			return this.row;
		}
	}
}
