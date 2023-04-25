package util.LNS;

import global.Parameters;
import model.Block;
import model.Schedule;
import model.Station;
import util.algorithms.Calculations;

import java.util.ArrayList;


public class GreedyLNSAlgo {

    Calculations c;
    ArrayList<Block> blocks;
    ArrayList<Station> stations;
    ArrayList<Station> breakStations;
    ArrayList<Station> depots;
    Parameters parameters;
    int[][] travelmatrix;
    int[][] consmatrix;
    int[][] consbreakmatrix;

    public GreedyLNSAlgo(Calculations c) {
        this.c =c;
        this.blocks = c.blocks;
        this.stations = c.stations;
        this.breakStations = c.breakStations;
        this.depots = c.depots;
        this.parameters = c.parameters;
        this.travelmatrix = c.travelmatrix;
        this.consmatrix = c.consmatrix;
        this.consbreakmatrix = c.consbreakmatrix;
    }

    //True means that Block b is added to Schedule s
    public InfoBestFit bestFitBlock(Block block, ArrayList<Schedule> schedules){
        Integer b = block.getId();
        double cost = 9999999;
        int indexInSchedule = -1;
        int scheduleID = -1;
        for (Schedule s: schedules) {
            Schedule temp = new Schedule(s);
            for(int i = 0; i <= s.getBlocks().size(); i++) {
                if(checkAdd(temp,b,i)){
                    double sCost = c.calculateCost(s,temp);
                    if(sCost < cost){
                        cost = sCost;
                        indexInSchedule = i;
                        scheduleID = s.getId();
                    }
                }
                temp.getBlocks().remove(b);
                c.calculateSchedule(s);
            }
        }
        if(scheduleID != -1){
            return new InfoBestFit(b,scheduleID,indexInSchedule,cost);
        }else{
            return null;
        }
    }

    public InfoRegretFit bestRegretFitBlock(Block block, ArrayList<Schedule> schedules){
        Integer b = block.getId();
        double cost = 999999;
        double secondcost = 9999999;
        int indexInSchedule = -1;
        int scheduleID = -1;
        for (Schedule s: schedules) {
            Schedule temp = new Schedule(s);
            for(int i = 0; i <= s.getBlocks().size(); i++) {
                if(checkAdd(temp,b,i)){
                    double sCost = c.calculateCost(s,temp);
                    if(sCost < cost){
                        secondcost = cost;
                        cost = sCost;
                        indexInSchedule = i;
                        scheduleID = s.getId();
                    }else if(sCost < secondcost){
                        secondcost = sCost;
                    }
                }
                temp.getBlocks().remove(b);
                c.calculateSchedule(s);
            }
        }
        if(scheduleID != -1){
            return new InfoRegretFit(b,scheduleID,indexInSchedule,cost,secondcost);
        }else{
            return null;
        }
    }

    public boolean checkAdd(Schedule schedule, int block, int index) {
        if (schedule.getBlocks().isEmpty()) {
            schedule.getBlocks().add(block);
            c.calculateSchedule(schedule);
            return c.checkSchedule(schedule);
        }
        if (index == 0) {
            Block after = blocks.get(schedule.getBlocks().get(0) - 1);
            if (c.consmatrix[block][after.getId()] == 1) {
                schedule.getBlocks().add(0, block);
                c.calculateSchedule(schedule);
                return c.checkSchedule(schedule);
            }else{return false;}
        } else if (index == schedule.getBlocks().size()) {
            Block before = blocks.get(schedule.getBlocks().get(index - 1) - 1);
            if (c.consmatrix[before.getId()][block] == 1) {
                schedule.getBlocks().add(block);
                c.calculateSchedule(schedule);
                return c.checkSchedule(schedule);
            }else{return false;}
        } else {
            Block before = blocks.get(schedule.getBlocks().get(index - 1) - 1);
            Block after = blocks.get(schedule.getBlocks().get(index) - 1);
            if (c.consmatrix[before.getId()][block] == 1 && c.consmatrix[block][after.getId()] == 1) {
                schedule.getBlocks().add(index, block);
                c.calculateSchedule(schedule);
                return c.checkSchedule(schedule);
            }else{return false;}
        }
    }
}
