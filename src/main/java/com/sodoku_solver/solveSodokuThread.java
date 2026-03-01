package com.sodoku_solver;

import java.util.concurrent.Callable;

public class solveSodokuThread implements Callable<String>{
    String puzzle;
    String puzzleHash;

    public solveSodokuThread(String puzzle, String puzzleHash ){
        this.puzzle = puzzle;
        this.puzzleHash = puzzleHash;
    }

    public String call(){
        // convert to grid 
        int[][] grid = Sodoku.getGridFromString(puzzle);

        // solve grid
        ConstraintHandler constraintHandler = new ConstraintHandler(grid);
        int[][] solvedGrid = constraintHandler.getSolvedGrid();

        // convert solved grid to string 
        String solvedGridString = Sodoku.getStringFromGrid(solvedGrid);

        return puzzleHash + " " + solvedGridString;
    }
}
