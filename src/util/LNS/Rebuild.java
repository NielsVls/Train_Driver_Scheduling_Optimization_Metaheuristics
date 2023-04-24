package util.LNS;

import model.Block;
import model.PossibleSolution;
import model.Schedule;
import model.Solution;
import util.algorithms.Calculations;

import java.util.*;

public class Rebuild {
    static Calculations c;
    private static ArrayList<Block> blocks;
    private static ArrayList<Integer> removedBlocks;
    private static ArrayList<Schedule> changedSchedules;

    public Rebuild(Calculations c){
        Rebuild.c = c;
        blocks = c.blocks;
        removedBlocks = new ArrayList<>();
        changedSchedules = new ArrayList<>();
    }

    public PossibleSolution destructAndRepair(Solution solution, int destructions){
        Solution oldSolution = new Solution(solution);
        Solution newSolution = oldSolution.clone();

        oldSolution.calculateCost();
        int oldCost = oldSolution.getTotalCost();

        PossibleSolution result = new PossibleSolution();
        result.setOldSolution(oldSolution);
        result.setOldCost(oldCost);

        removedBlocks.clear();
        int removed = 0;

        //REMOVE
        while(removed < destructions){
            int sId1;
            Schedule s1 = null;
            while (s1 == null || s1.getBlocks().isEmpty()) {
                sId1 = getRandomNumberInRange(0, newSolution.getSchedules().size() - 1);
                s1 = newSolution.getSchedules().get(sId1);
                if (s1 == null){
                    System.out.println("Schedule was null");
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
            tempS1.getBlocks().remove(block1);

            c.calculateSchedule(tempS1);

            if(checkRemove2(tempS1,blockIndex1)){
                //Keep track of the removed blocks
                removedBlocks.add(block1);
                newSolution.getSchedules().remove(s1);

                if(!tempS1.getBlocks().isEmpty()){
                    newSolution.getSchedules().add(tempS1);
                    c.calculateSchedule(tempS1);
                }
                removed++;
            }
        }


        //REPAIR
        ArrayList<InfoBestFit> bestFits = new ArrayList<>();

        GreedyLNSAlgo greedyLNS = new GreedyLNSAlgo(c);
        Collections.shuffle(removedBlocks);

        //FIND BEST FIT FOR EVERY REMOVED BLOCK
        changedSchedules.clear();
        for(Integer block : removedBlocks){
            InfoBestFit bestFit = greedyLNS.bestFitBlock(blocks.get(block-1),newSolution.getSchedules());
            if(bestFit == null){
                return null;
            }else{
                bestFits.add(bestFit);
            }
        }

        //RE-ADD THE REMOVED BLOCKS STARTING FROM THE LOWEST COST
        while(!bestFits.isEmpty()){
            bestFits.sort(new BestFitComparator());
            newSolution.insertBestFit(bestFits.get(0));
            c.calculateSchedule(newSolution.getScheduleByID(bestFits.get(0).getScheduleID()));
            changedSchedules.add(newSolution.getScheduleByID(bestFits.get(0).getScheduleID()));
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
            changedSchedules.remove(0);
        }

        newSolution.calculateSolution();
        result.setNewSolution(newSolution);
        result.setNewCost(newSolution.getTotalCost());
        return result;
    }

    private boolean checkRemove(Schedule s, Integer block){
        Schedule temp = new Schedule(s);
        temp.getBlocks().remove(block);

        if(temp.getBlocks().isEmpty()){
            return true;
        }
        c.calculateSchedule(temp);
        return c.checkSchedule(temp);
    }

    private boolean checkRemove2(Schedule s, int index) {
        if(s.getBlocks().isEmpty()){
            return true;
        }
        if(index == 0 || index == s.getBlocks().size()){
            return c.checkSchedule(s);
        }
        Integer before = s.getBlocks().get(index-1);
        Integer after = s.getBlocks().get(index);
        if(c.consmatrix[before][after] == 1){
            return c.checkSchedule(s);
        }else{return false;}
    }

    public static int getRandomNumberInRange(int min, int max) {
        if (min >= max) return 1;
        return c.random.nextInt((max - min) + 1) + min;
    }
}

class BestFitComparator implements Comparator<InfoBestFit> {
    @Override
    public int compare(InfoBestFit o1, InfoBestFit o2) {
        Integer cost1 = o1.getCost();
        Integer cost2 = o2.getCost();
        return cost1.compareTo(cost2);
    }
}