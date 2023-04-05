package util.SA;

import global.Parameters;
import model.*;
import util.algorithms.Calculations;

import java.util.ArrayList;
import java.util.Random;

public class Permutations {

    Calculations c;
    private ArrayList<Block> blocks;
    private ArrayList<Station> stations;
    private ArrayList<Station> breakStations;
    private int[][] consmatrix;
    private int[][] consbreakmatrix;
    private int[][] travelmatrix;
    private Parameters parameters;

    public Permutations(Calculations c) {
        this.c = c;
        this.blocks = c.blocks;
        this.stations = c.stations;
        this.breakStations = c.breakStations;
        this.consmatrix = c.consmatrix;
        this.consbreakmatrix = c.consbreakmatrix;
        this.travelmatrix = c.travelmatrix;
        this.parameters = c.parameters;
    }

    public PossibleSolution switch2Blocks(Solution solution) {
        //Saving the old solution
        Solution oldSolution = solution;
        solution.calculateSolution();
        int oldCost = oldSolution.getTotalPaymentDrivers();

        //Making the new solution and list of schedules we can edit
        Solution newSolution = solution;
        ArrayList<Schedule> schedules = newSolution.getSchedules();

        //Picking random schedules out of all schedules
        int sId1 = 0;
        int sId2 = 0;
        Schedule s1 = schedules.get(sId1);
        Schedule s2 = schedules.get(sId1);
        while (sId1 == sId2 || s1 == null || s2 == null) {
            sId1 = getRandomNumberInRange(0, schedules.size() - 1);
            sId2 = getRandomNumberInRange(0, schedules.size() - 1);
            s1 = schedules.get(sId1);
            s2 = schedules.get(sId2);
            if (s1 == null || s2 == null) return null;
        }

        c.calculateSchedule(s1);
        c.calculateSchedule(s2);

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

            int diff1 = c.calculateCost(s1, tempS1);
            int diff2 = c.calculateCost(s2, tempS2);

            if ((diff1 + diff2) < 0) {
                newSolution.getSchedules().remove(s1);
                newSolution.getSchedules().remove(s2);
                newSolution.getSchedules().add(tempS1);
                newSolution.getSchedules().add(tempS2);
                newSolution.calculateCost();
                return new PossibleSolution(newSolution, oldCost, newSolution.getTotalPaymentDrivers());
            }
        }
        return null;
    }

    public PossibleSolution moveBlock(Solution solution) {
        //Saving the old solution
        Solution oldSolution = solution;
        solution.calculateSolution();
        int oldCost = oldSolution.getTotalPaymentDrivers();

        //Making the new solution and list of schedules we can edit
        Solution newSolution = solution;
        ArrayList<Schedule> schedules = newSolution.getSchedules();

        //Picking random schedules out of all schedules
        int sId1 = 0;
        int sId2 = 0;
        Schedule s1 = schedules.get(sId1);
        Schedule s2 = schedules.get(sId1);
        while (sId1 == sId2 || s1 == null || s2 == null) {
            sId1 = getRandomNumberInRange(0, schedules.size() - 1);
            sId2 = getRandomNumberInRange(0, schedules.size() - 1);
            s1 = schedules.get(sId1);
            s2 = schedules.get(sId2);
            if (s1 == null || s2 == null) return null;
        }

        c.calculateSchedule(s1);
        c.calculateSchedule(s2);

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
            c.calculateSchedule(tempS1);
        }

        int max2 = tempS2.getBlocks().size() - 1;
        int blockIndex2 = getRandomNumberInRange(0, max2);

        if (checkSwap(tempS2, block1, blockIndex2) && checkRemove(tempS1,blockIndex1)) {

            int diff1 = c.calculateCost(s1, tempS1);
            int diff2 = c.calculateCost(s2, tempS2);

            if ((diff1 + diff2) < 0) {
                newSolution.getSchedules().remove(s1);
                newSolution.getSchedules().remove(s2);
                if(!tempS1.getBlocks().isEmpty()){
                    newSolution.getSchedules().add(tempS1);
                }
                newSolution.getSchedules().add(tempS2);
                newSolution.calculateCost();
                return new PossibleSolution(newSolution, oldCost, newSolution.getTotalPaymentDrivers());
            }
        }
        return null;
    }

    private boolean checkRemove(Schedule s, int index) {
        if(s.getBlocks().isEmpty() || index == 0 || index == s.getBlocks().size()){
            return true;
        }
        Integer before = s.getBlocks().get(index-1);
        Integer after = s.getBlocks().get(index);
        return consmatrix[before][after] == 1;
    }

    private int calculateCost(Schedule oldS, Schedule newS) {
        if (newS.getDuration() == 0) {
            return oldS.getDuration();
        }
        double oldCostPerMinute = parameters.getSalary() / oldS.getDuration();
        double newCostPerMinute = parameters.getSalary() / newS.getDuration();

        double oldWastedTime = c.calculateTimeWaste(oldS);
        double newWastedTime = c.calculateTimeWaste(newS);

        if (!oldS.isLocal() && newS.isLocal()) {
            newCostPerMinute = (parameters.getCostFraction() * parameters.getSalary()) / newS.getDuration();
        }
        int diffvalid = 0;
        if(!oldS.isValid() && newS.isValid()){
            diffvalid = 1;
        } else if (oldS.isValid() && !newS.isValid()) {
            diffvalid = -1;
        }
        double diffWT = oldWastedTime - newWastedTime;
        double diffCPM = oldCostPerMinute - newCostPerMinute;

        int result = (int) (diffWT * 1 + diffCPM * 1 + diffvalid * 1000);

        //A positive result equals an improvement
        return result;
    }

    public boolean checkSwap(Schedule schedule, int block, int index) {
        if (schedule.getBlocks().isEmpty()) {
            schedule.getBlocks().add(block);
            c.calculateSchedule(schedule);
            return true;
        }
        if (index == 0) {
            Block after = blocks.get(schedule.getBlocks().get(0) - 1);
            if (consmatrix[block][after.getId()] == 1) {
                schedule.getBlocks().add(0, block);
                c.calculateSchedule(schedule);
                return c.checkDuration(schedule);
            }else{return false;}
        } else if (index == schedule.getBlocks().size()) {
            Block before = blocks.get(schedule.getBlocks().get(index - 1) - 1);
            if (consmatrix[before.getId()][block] == 1) {
                schedule.getBlocks().add(index, block);
                c.calculateSchedule(schedule);
                return c.checkDuration(schedule);
            }else{return false;}
        } else {
            Block before = blocks.get(schedule.getBlocks().get(index - 1) - 1);
            Block after = blocks.get(schedule.getBlocks().get(index) - 1);

            if (consmatrix[before.getId()][block] == 1 && consmatrix[block][after.getId()] == 1) {
                schedule.getBlocks().add(index, block);
                c.calculateSchedule(schedule);
                return c.checkDuration(schedule);
            }else{return false;}
        }
    }

    public int getRandomNumberInRange(int min, int max) {
        if (min >= max) return 1;
        Random r = new Random();
        int number = r.nextInt((max - min) + 1) + min;
        return number;
    }
}
