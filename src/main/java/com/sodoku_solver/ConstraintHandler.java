package com.sodoku_solver;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.cdcl.Formula;
import com.cdcl.Solver;

public class ConstraintHandler {
    int[][] currentGrid;
    Formula formula; 
    int problemSize= 3;
    
    public ConstraintHandler(int[][] grid){

        
        currentGrid = grid.clone();

        ArrayList<HashSet<Integer>> constraints = new ArrayList<>();

        constraints.addAll(createRowConstraints());
        constraints.addAll(createColumnConstraints());
        constraints.addAll(createBoxConstraints());
        constraints.addAll(createSingleNumberInCellConstraints());
        constraints.addAll(createCurrentGridConstraints());


        formula = new Formula(constraints, getNumPropositionalVariables() );

        
    }

    public int[][] getSolvedGrid(){


        Solver solver = new Solver(formula);
        Solver.setConfig(2, null, null, null, 1, 1000, "random", null, null);
        List<Integer> solution  = solver.Solve();

        if (solver.Solve() == null) {
            System.out.println("SOLUTION NOT FOUND");
            return null;
        }



        int[][] solvedGrid = new int[problemSize*problemSize][problemSize*problemSize];

     
        // convert literals to grid squares 
        for (Integer literal : solution) {
            
            if(literal<=0) continue;

            int[] coord = propositionalIDToCoord(literal);
            solvedGrid[coord[0]][coord[1]] = coord[2];
            
        }

       

        return solvedGrid;
    }

    int getNumPropositionalVariables(){

        return (int)Math.pow(problemSize, 6);
    }

    int cellID(int row, int col){
        return row * currentGrid[0].length + col;
    }

    int propositionalID(int cellID, int number){
        return (cellID*problemSize*problemSize) + number;
    }

    int[] propositionalIDToCoord(int id){


        int number = id % (problemSize * problemSize);//(int)Math.ceil( id/(Math.pow(problemSize, 4)));
        if (number == 0) number = problemSize*problemSize;

        int cellID = (id - number) / (problemSize * problemSize); 

        int row = cellID / (problemSize*problemSize);
        
        int col = cellID % (problemSize * problemSize);

        return new int[]{row, col, number};

    }

    int propositionalID(int row, int col, int number){
        
        return propositionalID(cellID(row, col), number);
    }

    List<HashSet<Integer>> createCurrentGridConstraints(){

        List<HashSet<Integer>> constraints = new ArrayList<HashSet<Integer>>();

        for (int i = 0; i < currentGrid.length; i++) {
            for (int j = 0; j < currentGrid[0].length; j++) {
                
                if(currentGrid[i][j] == 0) continue;

                
                HashSet<Integer> constraint = new HashSet<>();
                constraint.add( propositionalID(i, j, currentGrid[i][j] ));
                constraints.add(constraint);


                



            }
        }


        return constraints;



    }

    List<HashSet<Integer>> createSingleNumberInCellConstraints(){


        List<HashSet<Integer>> constraints = new ArrayList<>();


        // there has to be at least one number in each cell 
        for (int i = 0; i < currentGrid.length; i++) {
            for (int j = 0; j < currentGrid[0].length; j++) {
                
                HashSet<Integer> atLeastOneConstraint = new HashSet<>();
                for (int n = 1; n <= problemSize*problemSize; n++) {
                    
                    atLeastOneConstraint.add( propositionalID(i, j, n) );

                }
                constraints.add(atLeastOneConstraint);



            }
        }


        for (int i = 0; i < currentGrid.length; i++) {
            for (int j = 0; j < currentGrid[0].length; j++) {
                
                
                for (int a = 1; a <= problemSize*problemSize; a++) {
                    for (int b = 1; b <= problemSize*problemSize; b++) {
                        
                        if(a==b) continue;

                        HashSet<Integer> constraint = new HashSet<>();
                        constraint.add(-propositionalID(i, j, a));
                        constraint.add(-propositionalID(i, j, b));


                        constraints.add(constraint);
                    }

                }



            }
        }




        return constraints;
    }
    
    List<HashSet<Integer>> createRowConstraints(){
        
        ArrayList<HashSet<Integer>> constraints = new ArrayList<>();

        for (int n = 1; n <= problemSize*problemSize; n++) {
            
        
            // make sure each number occurs at least once in each row 
            for (int i = 0; i < currentGrid.length; i++) {
                

                // each number has to occur at least once in each row 
                HashSet<Integer> atLeastOne = new HashSet<>();
                for (int j = 0; j < currentGrid.length; j++) {
                    atLeastOne.add(propositionalID(i, j, n));
                }

                constraints.add(atLeastOne);

            
        


                // make sure this number occurs no more than once in this row 

                // consider every pair of cells in each row 
                for (int a = 0; a < currentGrid[0].length; a++) {
                    for (int b = 0; b < currentGrid[0].length; b++) {
                        
                        if(a==b) continue;
                        
                        HashSet<Integer> constraint = new HashSet<>();
                        constraint.add(-(propositionalID(i, a, n)));
                        constraint.add(-(propositionalID(i, b, n)));

                        constraints.add(constraint);

                    }
                }


            }
            

        }

        return constraints;
    }

    List<HashSet<Integer>> createColumnConstraints(){


        ArrayList<HashSet<Integer>> constraints = new ArrayList<>();

        for (int n = 1; n <= problemSize*problemSize; n++) {
            

            for (int j = 0; j < currentGrid[0].length; j++) {
                
                
                // make sure this number occurs at least once in this column 

                HashSet<Integer> atLeastOnceConstraint = new HashSet<>();
                for (int i = 0; i < currentGrid.length; i++) {
                    atLeastOnceConstraint.add(propositionalID(i, j, n));
                } 

                constraints.add(atLeastOnceConstraint);







                for (int a = 0; a < currentGrid.length; a++) {
                    for (int b = 0; b < currentGrid.length; b++) {
                        
                        if(a==b) continue;

                        HashSet<Integer> constraint = new HashSet<>();
                        constraint.add(-propositionalID(a, j, n));
                        constraint.add(-propositionalID(b, j, n));

                        constraints.add(constraint);
                        
                    }
                }



            }

        }






        return constraints;
    }

    List<HashSet<Integer>> createBoxConstraint(int box_i, int box_j, int number){


        List<HashSet<Integer>> constraints = new ArrayList<>();

        HashSet<Integer> atLeastOnceConstraint = new HashSet<Integer>();

        // iterate through box 
        for (int i = box_i*problemSize; i < box_i * problemSize + problemSize; i++) {
            for (int j = box_j * problemSize; j < box_j * problemSize + problemSize; j++) {
                
                atLeastOnceConstraint.add(propositionalID( +i,  + j, number));

            }
        }
        constraints.add(atLeastOnceConstraint);




        // iterate through box 
        for (int i = box_i*problemSize; i < box_i*problemSize + problemSize; i++) {
            for (int j = box_j * problemSize; j < box_j*problemSize + problemSize; j++) {
                
                // iterate through box again to create pairs 
                for (int a = box_i*problemSize; a < box_i*problemSize + problemSize; a++) {
                    for (int b = box_j * problemSize; b < box_j*problemSize + problemSize; b++) {
                        
                        

                        if(cellID(i, j) == cellID(a, b)) continue;

                        HashSet<Integer> constraint = new HashSet<>();
                        constraint.add(-propositionalID(i, j, number));
                        constraint.add(-propositionalID(a,b, number));
                        
        
                    }
                }

            }
        }







        return constraints;

    }

    List<HashSet<Integer>> createBoxConstraints(){

        List<HashSet<Integer>> constraints = new ArrayList<>();


        for (int n = 1; n <= problemSize*problemSize; n++) {
            
            
            // iterate through boxes 
            for (int i = 0; i < problemSize; i++) {
                for (int j = 0; j < problemSize; j++) {
                    
                    constraints.addAll(createBoxConstraint(i, j, n));

                }
            }
        }

        return constraints;
    }
}