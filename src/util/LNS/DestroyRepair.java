package util.LNS;

import global.Parameters;
import model.*;
import util.algorithms.Calculations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import static util.LNS.LargeNeighbourhoodSearch.getRandomNumberInRange;

public class DestroyRepair {
    static Calculations c;
    private static ArrayList<Block> blocks;
    private ArrayList<Station> stations;
    private ArrayList<Station> breakStations;
    private int[][] consmatrix;
    private int[][] consbreakmatrix;
    private int[][] travelmatrix;
    private Parameters parameters;
    private static ArrayList<Integer> removedBlocks;
    private static ArrayList<Integer> changedSchedules;

    public DestroyRepair(Calculations c){
        this.c = c;
        this.blocks = c.blocks;
        this.stations = c.stations;
        this.breakStations = c.breakStations;
        this.consmatrix = c.consmatrix;
        this.consbreakmatrix = c.consbreakmatrix;
        this.travelmatrix = c.travelmatrix;
        this.parameters = c.parameters;
        this.removedBlocks = new ArrayList<>();
        this.changedSchedules = new ArrayList<>();
    }

    public PossibleSolution destruct2(Solution solution, int destructions){
        Solution oldSolution = new Solution(solution);
        Solution newSolution = oldSolution.clone();

        oldSolution.calculateCost();
        int oldCost = oldSolution.getTotalTimeWasted();

        PossibleSolution result = new PossibleSolution();
        result.setOldSolution(oldSolution);
        result.setOldCost(oldCost);


        if(solution == null){
            return null;
        }

        removedBlocks.clear();
        changedSchedules.clear();
        int removed = 0;

        while(removed < destructions){
            int sId1 = 0;
            Schedule s1 = newSolution.getSchedules().get(sId1);
            while (s1 == null) {
                sId1 = getRandomNumberInRange(0, newSolution.getSchedules().size() - 1);
                s1 = newSolution.getSchedules().get(sId1);
                if (s1 == null){
                    return null;
                }
            }

            c.calculateSchedule(s1);
            Schedule tempS1 = new Schedule(s1);

            //keep track of the adjusted schedules
            changedSchedules.add(tempS1.getId());

            //Random Block from schedule removed
            int max1 = tempS1.getBlocks().size() - 1;
            int blockIndex1 = getRandomNumberInRange(0, max1);
            if (tempS1.getBlocks().size() == 1) {
                blockIndex1 = 0;
            }
            Integer block1 = tempS1.getBlocks().get(blockIndex1);
            tempS1.getBlocks().remove(blockIndex1);
            //Keep track of the removed blocks
            removedBlocks.add(block1);
            newSolution.getSchedules().remove(s1);
            if(!tempS1.getBlocks().isEmpty()){
                newSolution.getSchedules().add(tempS1);
                c.calculateSchedule(tempS1);
            }
            removed++;
        }
        ArrayList<InfoBestFit> bestFits = new ArrayList<>();

        GreedyLNS greedyLNS = new GreedyLNS(c);
        Collections.shuffle(removedBlocks);
        for(Integer block : removedBlocks){
            InfoBestFit bestFit = greedyLNS.bestFitBlock(blocks.get(block-1),newSolution);
            if(bestFit == null){
                System.out.println("NO GOOD FIT FOUND");
                return null;
            }else{
                bestFits.add(bestFit);
                boolean added = false;
                for (Schedule s : newSolution.getSchedules()){
                    if(s.getId() == bestFit.getScheduleID()){
                        s.getBlocks().add(bestFit.getIndex(),bestFit.getBlock());
                        c.calculateSchedule(s);
                        added = true;
                        break;
                    }
                }
                if (!added){
                    System.out.println("SOLUTION NOT FOUND, RETURNED NULL");
                    return null;
                }
            }
        }
        newSolution.calculateSolution();
        result.setNewSolution(newSolution);
        System.out.println("NEWCOST "+solution.getTotalTimeWasted());
        result.setNewCost(solution.getTotalTimeWasted());
        return result;
    }

    public static int getRandomNumberInRange(int min, int max) {
        if (min >= max) return 1;
        Random r = new Random();
        int number = r.nextInt((max - min) + 1) + min;
        return number;
    }
}
