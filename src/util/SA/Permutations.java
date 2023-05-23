package util.SA;

import model.*;
import util.algorithms.Calculations;

import java.util.ArrayList;

public class Permutations {

    Calculations c;
    private final ArrayList<Block> blocks;
    private final int[][] consmatrix;

    public Permutations(Calculations c) {
        this.c = c;
        this.blocks = c.blocks;
        this.consmatrix = c.consmatrix;
    }

    public PossibleSolution switch2Blocks(Solution solution) {
        Solution oldSolution = new Solution(solution);
        Solution newSolution = oldSolution.clone();

        //Saving the old solution
        oldSolution.calculateSolution();
        int oldCost = oldSolution.getTotalCost();

        PossibleSolution result = new PossibleSolution();
        result.setOldSolution(oldSolution);
        result.setOldCost(oldCost);

        //Picking random schedules out of all schedules
        int sId1 = -1;
        int sId2 = -1;
        Schedule s1 = null;
        Schedule s2 = null;
        while (sId1 == sId2 || s1.getBlocks().isEmpty() || s2.getBlocks().isEmpty()) {
            sId1 = getRandomNumberInRange(0, newSolution.getSchedules().size() - 1);
            sId2 = getRandomNumberInRange(0, newSolution.getSchedules().size() - 1);
            s1 = newSolution.getSchedules().get(sId1);
            s2 = newSolution.getSchedules().get(sId2);
            if (s1 == null || s2 == null) return null;
        }

        c.calculateScheduleFB(s1);
        c.calculateScheduleFB(s2);

        Schedule tempS1 = new Schedule(s1);
        Schedule tempS2 = new Schedule(s2);

        //Random Block from schedule 1
        int max1 = tempS1.getBlocks().size() - 1;
        int blockIndex1 = getRandomNumberInRange(0, max1);
        if (tempS1.getBlocks().size() == 1) {
            blockIndex1 = 0;
        }
        Integer block1 = tempS1.getBlocks().get(blockIndex1);
        tempS1.getBlocks().remove(blockIndex1);


        //Random Block from schedule 2
        int max2 = tempS2.getBlocks().size() - 1;
        int blockIndex2 = getRandomNumberInRange(0, max2);
        if (tempS2.getBlocks().size() == 1) {
            blockIndex2 = 0;
        }
        Integer block2 = tempS2.getBlocks().get(blockIndex2);
        tempS2.getBlocks().remove(blockIndex2);

        if (checkSwap(tempS1, block2, blockIndex1) &&
                checkSwap(tempS2, block1, blockIndex2)) {

            double diff1 = c.calculateCost(s1, tempS1);
            double diff2 = c.calculateCost(s2, tempS2);

            if ((diff1 + diff2) < 0) {
                newSolution.getSchedules().remove(s1);
                newSolution.getSchedules().remove(s2);
                newSolution.getSchedules().add(tempS1);
                newSolution.getSchedules().add(tempS2);
                newSolution.calculateSolution();
                result.setNewSolution(newSolution);
                result.setNewCost(newSolution.getTotalCost());
                return result;
            }
        }
        return null;
    }

    public PossibleSolution moveBlock(Solution solution) {
        Solution oldSolution = new Solution(solution);
        Solution newSolution = oldSolution.clone();

        oldSolution.calculateCost();
        int oldCost = oldSolution.getTotalCost();

        PossibleSolution result = new PossibleSolution();
        result.setOldSolution(oldSolution);
        result.setOldCost(oldCost);

//        //Saving the old solution
//        Solution oldSolution = solution;
//        oldSolution.calculateSolution();
//        int oldCost = oldSolution.getTotalCost();
//
//        //Making the new solution and list of schedules we can edit
//        Solution newSolution = solution;
//        ArrayList<Schedule> schedules = newSolution.getSchedules();

        //Picking random schedules out of all schedules
        int sId1 = -1;
        int sId2 = -1;
        Schedule s1 = null;
        Schedule s2 = null;
        while (sId1 == sId2 || s1.getBlocks().isEmpty()) {
            sId1 = getRandomNumberInRange(0, newSolution.getSchedules().size() - 1);
            sId2 = getRandomNumberInRange(0, newSolution.getSchedules().size() - 1);
            s1 = newSolution.getSchedules().get(sId1);
            s2 = newSolution.getSchedules().get(sId2);
            if (s1 == null || s2 == null) return null;
        }

        c.calculateScheduleFB(s1);
        c.calculateScheduleFB(s2);

        Schedule tempS1 = new Schedule(s1);
        Schedule tempS2 = new Schedule(s2);

        //Random Block from schedule 1
        int max1 = tempS1.getBlocks().size() - 1;
        int blockIndex1 = getRandomNumberInRange(0, max1);
        if (tempS1.getBlocks().size() == 1) {
            blockIndex1 = 0;
        }

        Integer block1 = tempS1.getBlocks().get(blockIndex1);
        tempS1.getBlocks().remove(blockIndex1);

        if(!tempS1.getBlocks().isEmpty()){
            c.calculateScheduleFB(tempS1);
        }

        int max2 = tempS2.getBlocks().size() - 1;
        int blockIndex2 = getRandomNumberInRange(0, max2);

        if (checkSwap(tempS2, block1, blockIndex2) && checkRemove(tempS1,blockIndex1)) {

            double diff1 = c.calculateCost(s1, tempS1);
            double diff2 = c.calculateCost(s2, tempS2);

            if ((diff1 + diff2) < 0) {
                newSolution.getSchedules().remove(s1);
                newSolution.getSchedules().remove(s2);
                if(!tempS1.getBlocks().isEmpty()){
                    newSolution.getSchedules().add(tempS1);
                }
                newSolution.getSchedules().add(tempS2);
                //newSolution.calculateCost();
                //return new PossibleSolution(newSolution, oldCost, newSolution.getTotalPaymentDrivers());
                newSolution.calculateSolution();
                result.setNewSolution(newSolution);
                result.setNewCost(newSolution.getTotalCost());
                return result;
            }
        }
        return null;
    }

//    public PossibleSolution removeSchedule(Solution solution){
//
//    }

    private boolean checkRemove(Schedule s, int index) {
        if(s.getBlocks().isEmpty()){
            return true;
        }
        if(index == 0 || index == s.getBlocks().size()){
            return c.checkSchedule(s);
        }
        Integer before = s.getBlocks().get(index-1);
        Integer after = s.getBlocks().get(index);
        if(consmatrix[before][after] == 1){
            return c.checkSchedule(s);
        }else{return false;}
    }

    public boolean checkSwap(Schedule schedule, int block, int index) {
        if (schedule.getBlocks().isEmpty()) {
            schedule.getBlocks().add(block);
            c.calculateScheduleFB(schedule);
            return true;
        }
        if (index == 0) {
            Block after = blocks.get(schedule.getBlocks().get(0) - 1);
            if (consmatrix[block][after.getId()] == 1) {
                schedule.getBlocks().add(0, block);
                c.calculateScheduleFB(schedule);
                return c.checkSchedule(schedule);
            }else{return false;}
        } else if (index == schedule.getBlocks().size()) {
            Block before = blocks.get(schedule.getBlocks().get(index - 1) - 1);
            if (consmatrix[before.getId()][block] == 1) {
                schedule.getBlocks().add(index, block);
                c.calculateScheduleFB(schedule);
                return c.checkSchedule(schedule);
            }else{return false;}
        } else {
            Block before = blocks.get(schedule.getBlocks().get(index - 1) - 1);
            Block after = blocks.get(schedule.getBlocks().get(index) - 1);

            if (consmatrix[before.getId()][block] == 1 && consmatrix[block][after.getId()] == 1) {
                schedule.getBlocks().add(index, block);
                c.calculateScheduleFB(schedule);
                return c.checkSchedule(schedule);
            }else{return false;}
        }
    }

    public int getRandomNumberInRange(int min, int max) {
        if (min >= max) return 1;
        return c.random.nextInt((max - min) + 1) + min;
    }
}
