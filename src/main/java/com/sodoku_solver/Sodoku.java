package com.sodoku_solver;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;


import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import java.nio.charset.StandardCharsets;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


import processing.core.PApplet;

public class Sodoku extends PApplet{
    

    int width = 1500; 
    int height = 1500; 

    float cellWidth;
    float cellHeight;

    int[][] grid;
    int [][] solvedGrid; 

    String puzzlesFile = "puzzles/hard.txt";
    int puzzleNum = 1;

    // float knownProbability = 0.0f;

    ConstraintHandler constraintHandler;


    public void settings(){
		size(width, height);
	}

    public void setup(){
        

        String puzzleString = loadPuzzle(puzzlesFile, puzzleNum);
        String[] puzzleParts = puzzleString.split(" ");
        String puzzle = puzzleParts[1];

        grid = getGridFromString(puzzle);

        cellWidth = width / grid.length;
        cellHeight = height / grid[0].length;

        textSize(100);
        textAlign(CENTER, CENTER);


        

        constraintHandler = new ConstraintHandler(grid);
        solvedGrid = constraintHandler.getSolvedGrid();

        if (solvedGrid == null) System.out.println("Solution Not Found ");
            
    
    }
    
    public void draw(){

        background(225);
        drawGrid();
    }



    private void drawGrid(){

        drawValues();
        drawHorizontalLines();
        drawVerticalLines();

    }

    void drawValues(){
        
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[0].length; j++) {
                
                // if this grid square was initially not given 
                if(grid[i][j] == 0 ){ 
                    fill(0, 0, 612);
                } 
                // this grid square was initially given 
                else{
                    fill(204, 102, 0);
                }


               
                String s = String.valueOf(solvedGrid[i][j]);
                text(s, cellWidth*j + cellWidth/2,  cellHeight*i + cellHeight/2);// 20+textSize, 20+textSize );

            }
        }
    }

    void drawHorizontalLines(){

        for (int i = 0; i <= grid.length; i++) {
            if(i % 3 == 0){
                stroke(0f, 0f, 0f);
                strokeWeight(5);
            }
            else{
                stroke(0.0f, 0f, 1.0f);
                strokeWeight(1);
            }


            line(0, i * cellHeight, width, i*cellHeight);
        }
    }

    void drawVerticalLines(){
        for (int j = 0; j <= grid[0].length; j++) {
            if(j % 3 == 0){
                stroke(0f, 0f, 0f);
                strokeWeight(5);
            }
            else{
                stroke(0.0f, 0.3f, 1.0f);
                strokeWeight(1);
            }
            line(j*cellWidth, 0,  j*cellWidth, height);            
        }
    }




    static String loadPuzzle(String filePath, int puzzleNum){


        try {
            

            byte[] bytes = Files.readAllBytes(Paths.get(filePath)); 
            String line = new String(Arrays.copyOfRange(bytes, puzzleNum * 100 + 12, puzzleNum*100+12+82), StandardCharsets.UTF_8);
            return line;

            
        } catch (Exception e) {
            

            System.out.println("ERROR LOADING PUZZLE: " + e);
        }

        return null;


    }

    static ArrayList<String> loadAllPuzzles(String filePath){
        try {
            


       
            ArrayList<String> puzzleStrings = new ArrayList<>();
            byte[] bytes = Files.readAllBytes(Paths.get(filePath)); 


            int puzzleNum = 0;
            
            while (puzzleNum * 100 + 12 + 81 +1 < bytes.length){

                String line = new String(Arrays.copyOfRange(bytes, puzzleNum * 100 + 12, puzzleNum*100+12+82), StandardCharsets.UTF_8);
                puzzleStrings.add(line);

            }

            
            

            return puzzleStrings;
            
        } catch (Exception e) {
            

            System.out.println("ERROR LOADING PUZZLE: " + e);
        }

        return null;

    }




    static int[][] getGridFromString(String gridString){


        int[][] grid = new int[9][9];



        int index = 0; 

        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[0].length; j++) {
                
                
                
                grid[i][j] = Character.getNumericValue(gridString.charAt(index));;

                index += 1;
            }
            
        }



        return grid;

    }

    static String getStringFromGrid(int[][] grid){

        StringBuilder gridString = new StringBuilder();

        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[0].length; j++) {
                gridString.append(grid[i][j]);
            }
        }

        return gridString.toString();


    }

    static void printGrid(int[][] grid){

        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[0].length; j++) {
                System.out.print(grid[i][j] + " | ");
            }
            System.out.println();
        }
    }



    static void solvePuzzleFile(String puzzleFile){

        int puzzleNum = 0;

        StringBuilder solutions = new StringBuilder();

        do{

            
            
            String puzzleString = loadPuzzle(puzzleFile, puzzleNum);
            if(puzzleString==null) break;

            String[] puzzleParts = puzzleString.split(" ");
            String puzzleHash = puzzleParts[0];
            String puzzle = puzzleParts[1]; 

            int[][] grid = getGridFromString(puzzle);
            ConstraintHandler constraintHandler = new ConstraintHandler(grid);
            

            int[][] solvedGrid = constraintHandler.getSolvedGrid();

            

            // convert grid to string 
            solutions.append(puzzleHash + " " + getStringFromGrid(solvedGrid)+"\n");


            puzzleNum += 1;


        }while(true);

        String fileName = Paths.get(puzzleFile).getFileName().toString();

        try (FileWriter writer = new FileWriter("solutions/"+fileName)) {
            writer.write(solutions.toString()); 
            System.out.println("Content successfully written to " + fileName + " using FileWriter.");

        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
            e.printStackTrace();
        }


    }


    /*
     * Reads a puzzle file and returns a list of String
     * containing the problem hash and the sodoku board string  
     *  
     */
    static List<String> readPuzzlesFile(String puzzlesFile) throws IOException{
        BufferedReader bufferedReader = new BufferedReader(new FileReader(puzzlesFile, StandardCharsets.UTF_8));
        ArrayList<String> puzzles = new ArrayList<>();

        String line; 
        while( (line = bufferedReader.readLine())!=null){


            String[] lineParts = line.split(" ");

            String puzzleHash = lineParts[0]; 
            String puzzleString = lineParts[1];

            
            puzzles.add(puzzleHash + " " + puzzleString);
        }

        bufferedReader.close();

        return puzzles;
    }

    static void writeSolutionsToFile(List<String> solutions, String saveLocation ) throws IOException{
        
        FileWriter fileWriter = new FileWriter(saveLocation);
        
        
        for (String solution : solutions) {

            fileWriter.write(solution+ "\n");
            
        }

        fileWriter.close();

    }

    static void solvePuzzleFileConcurrent(String puzzleFile, String solutionFile){


        try {

            // read puzzles from file 
            List<String> puzzles = readPuzzlesFile(puzzleFile); 

            ExecutorService executor = Executors.newFixedThreadPool(5); 
            List<solveSodokuThread> tasks = new ArrayList<>();

            for (String puzzleString : puzzles) {

                String[] puzzleParts = puzzleString.split(" ");

                String puzzleHash = puzzleParts[0]; 
                String puzzle = puzzleParts[1];
                
                tasks.add(new solveSodokuThread(puzzle, puzzleHash));

            }


            List<Future<String>> results = executor.invokeAll(tasks);
            executor.shutdown();

            
            
            FileWriter fileWriter = new FileWriter(solutionFile);
            
            for (Future<String> future : results) {
                fileWriter.write(future.get()+ "\n");
            }
            
            fileWriter.close();
           
            
            

       
        } catch (IOException e) {
            System.out.println("File Unable to be Opened");
        }
         catch (InterruptedException e)        
        {
            System.out.println("Interrupted");

        }
        catch(ExecutionException e){
            System.out.println("Execution Exception");

        }

    }




    public static void main(String[] args){

        
            
        
            long start = System.nanoTime();

            solvePuzzleFileConcurrent("puzzles/test.txt", "solutions/test.txt");

            long end = System.nanoTime();

            System.out.println((end - start)/1_000_000_000);
     

		// String[] processingArgs = {"Sodoku Solver"};
		// Sodoku mySketch = new Sodoku();
		// PApplet.runSketch(processingArgs, mySketch);
	
    }
}
