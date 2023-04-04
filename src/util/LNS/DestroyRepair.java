package util.LNS;

import global.Parameters;
import model.*;
import util.algorithms.Calculations;

import java.util.*;

public class DestroyRepair {
    static Calculations c;
    private static ArrayList<Block> blocks;
    private static ArrayList<Integer> removedBlocks;
    private static ArrayList<Schedule> changedSchedules;

    public DestroyRepair(Calculations c){
        DestroyRepair.c = c;
        blocks = c.blocks;
        removedBlocks = new ArrayList<>();
        changedSchedules = new ArrayList<>();
    }

    public PossibleSolution destruct2(Solution solution, int destructions){
        Solution oldSolution = new Solution(solution);
        Solution newSolution = oldSolution.clone();

        oldSolution.calculateCost();
        int oldCost = oldSolution.getTotalCost();

        PossibleSolution result = new PossibleSolution();
        result.setOldSolution(oldSolution);
        result.setOldCost(oldCost);


        if(solution == null){
            return null;
        }

        removedBlocks.clear();
        int removed = 0;

        //REMOVE
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

        //FIRST CHECK
        changedSchedules.clear();
        for(Integer block : removedBlocks){
            InfoBestFit bestFit = greedyLNS.bestFitBlock(blocks.get(block-1),newSolution.getSchedules());
            if(bestFit == null){
                //System.out.println("NO GOOD FIT FOUND");
                return null;
            }else{
                bestFits.add(bestFit);
            }
        }

        while(!bestFits.isEmpty()){
            //ADD THE LOWEST COST
            bestFits.sort(new BestFitComparator());
            newSolution.insertBestFit(bestFits.get(0));
            c.calculateSchedule(newSolution.getScheduleByID(bestFits.get(0).getScheduleID()));
            changedSchedules.add(0,newSolution.getScheduleByID(bestFits.get(0).getScheduleID()));
            removedBlocks.remove(bestFits.get(0).getBlock());
            bestFits.remove(0);

            if(bestFits.isEmpty()){
                break;
            }
            //IF MULTIPLE BLOCKS ARE PLANNED TO BE ADDED TO THE SAME SCHEDULE, WE RECHECK THEIR BEST FIT
            for(InfoBestFit b : bestFits){
                if(changedSchedules.get(0) == newSolution.getScheduleByID(b.getScheduleID())){
                    InfoBestFit bestFit = greedyLNS.bestFitBlock(blocks.get(b.getBlock()-1),newSolution.getSchedules());
                    if(bestFit == null){
                        return null;
                    }else{
                        //RESET THE CURRENT BESTFIT
                        b.setCost(bestFit.getCost());
                        b.setBlock(bestFit.getBlock());
                        b.setIndex(bestFit.getIndex());
                        b.setScheduleID(bestFit.getScheduleID());
                    }
                }
            }

            for(Integer block : removedBlocks){
                InfoBestFit bestFit = greedyLNS.bestFitBlock(blocks.get(block-1),changedSchedules);
                if(bestFit != null){
                    for(InfoBestFit bestFitOld : bestFits){
                        if(Objects.equals(bestFitOld.getBlock(), bestFit.getBlock()) && bestFitOld.getCost() > bestFit.getCost()){
                            bestFits.remove(bestFitOld);
                            bestFits.add(bestFit);
                            break;
                        }
                    }
                }
            }
        }

        newSolution.calculateSolution();
        result.setNewSolution(newSolution);
        result.setNewCost(newSolution.getTotalCost());
        return result;
    }

    public static int getRandomNumberInRange(int min, int max) {
        if (min >= max) return 1;
        Random r = new Random();
        int number = r.nextInt((max - min) + 1) + min;
        return number;
    }
}

class BestFitComparator implements Comparator<InfoBestFit> {
    @Override
    public int compare(InfoBestFit o1, InfoBestFit o2) {
            return o1.getCost() - o2.getCost();
    }
}