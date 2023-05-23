package util.algorithms;

import global.Parameters;
import model.*;

import java.util.ArrayList;
import java.util.Collections;


public class GreedyBaseAlgo {

    Calculations c;
    ArrayList<Block> blocks;
    ArrayList<Station> stations;
    ArrayList<Station> breakStations;
    ArrayList<Station> depots;
    Parameters parameters;
    int[][] travelmatrix;
    int[][] consmatrix;
    int[][] consbreakmatrix;

    public GreedyBaseAlgo(Calculations c) {
        this.c = c;
        this.blocks = c.blocks;
        this.stations = c.stations;
        this.breakStations = c.breakStations;
        this.depots = c.depots;
        this.parameters = c.parameters;
        this.travelmatrix = c.travelmatrix;
        this.consmatrix = c.consmatrix;
        this.consbreakmatrix = c.consbreakmatrix;
    }

    public Solution runInitialSolution() {
        Solution solution = new Solution();
        solution.setSchedules(makeSchedulesByID());
        solution.calculateSolution();
        return solution;
    }

    public Solution runTimeBasedInitialSolution() {
        Solution solution = new Solution();
        solution.setSchedules(makeSchedulesByTime());
        solution.calculateSolution();
        return solution;
    }

    public Solution runTimeBasedInitialSolutionWE() {
        Solution solution = new Solution();
        solution.setSchedules(makeSchedulesByTimeWE());
        solution.calculateSolution();
        return solution;
    }

    public Solution run1BlockPerScheduleInitialSolution(){
        Solution solution = new Solution();
        solution.setSchedules(make1BlockPerSchedule());
        solution.calculateSolution();
        return solution;
    }

    public Solution runRandomInitialSolution(){
        Solution solution = new Solution();
        solution.setSchedules(makeRandomSolution());
        solution.calculateSolution();
        return solution;
    }

    public Solution runStationDriverSolution(){
        Solution solution = new Solution();
        solution.setSchedules(makeStationDriverSchedules());
        solution.calculateSolution();
        return solution;
    }

    public ArrayList<Schedule> makeSchedulesByID() {

        ArrayList<Schedule> schedules = makeSchedulesForList(blocks);
        int counter = 1;
        for (Schedule s : schedules) {
            s.setId(counter);
            c.calculateScheduleFB(s);
            counter++;
        }

        //OPTIONAL: ADDING EMPTY SCHEDULES
//        int max = 10;
//        for(int i = 0 ; i < max ; i++){
//            Schedule schedule = new Schedule();
//            schedule.setId(counter);
//            schedules.add(schedule);
//            counter++;
//        }

        return schedules;
    }

    public ArrayList<Schedule> makeSchedulesByTime() {
        ArrayList<Block> tempBlocks = new ArrayList<>(blocks);
        Collections.sort(tempBlocks, new BlockComparator());
        ArrayList<Schedule> schedules = makeSchedulesForList(tempBlocks);
        int counter = 1;
        for (Schedule s : schedules) {
            s.setId(counter);
            c.calculateScheduleFB(s);
            counter++;
        }
        return schedules;
    }

    public ArrayList<Schedule> makeSchedulesByTimeWE() {
        ArrayList<Block> tempBlocks = new ArrayList<>(blocks);
        Collections.sort(tempBlocks, new BlockComparator());
        ArrayList<Schedule> schedules = makeSchedulesForList(tempBlocks);
        int counter = 1;
        for (Schedule s : schedules) {
            s.setId(counter);
            c.calculateScheduleFB(s);
            counter++;
        }

        int max = 10;
        for(int i = 0 ; i < max ; i++){
            Schedule schedule = new Schedule();
            schedule.setId(counter);
            schedules.add(schedule);
            counter++;
        }

        return schedules;
    }

    public ArrayList<Schedule> make1BlockPerSchedule(){
        ArrayList<Schedule> schedules = new ArrayList<>();
        for (Block b : blocks) {
            //Add to new schedule
            Schedule newschedule = new Schedule();
            newschedule.getBlocks().add(b.getId());
            newschedule.setClosestDepot(findClosestDepot(b));
            newschedule.setStartStation(b.getStartLoc());
            newschedule.setStartDay(b.getStartWeekday());
            c.calculateScheduleFB(newschedule);
            schedules.add(newschedule);
        }
        int counter = 0;
        for (Schedule s : schedules) {
            s.setId(counter);
            c.calculateScheduleFB(s);
            counter++;
        }
        return schedules;
    }

    public ArrayList<Schedule> makeRandomSolution(){

        ArrayList<Block> tempBlocks = new ArrayList<>(blocks);
        Collections.shuffle(tempBlocks);
        ArrayList<Schedule> schedules = makeSchedulesForList(tempBlocks);
        int counter = 1;
        for (Schedule s : schedules) {
            s.setId(counter);
            c.calculateScheduleFB(s);
            counter++;
        }
        return schedules;
    }

    public ArrayList<Schedule> makeStationDriverSchedules(){
        ArrayList<Block> regularBlocks = new ArrayList<>();
        ArrayList<Block> stationBlocks1 = new ArrayList<>();
        ArrayList<Block> stationBlocks2 = new ArrayList<>();
        ArrayList<Block> stationBlocks3 = new ArrayList<>();
        ArrayList<Block> stationBlocks4 = new ArrayList<>();
        ArrayList<Block> stationBlocks5 = new ArrayList<>();
        ArrayList<Block> stationBlocks6 = new ArrayList<>();

        for(Block b : blocks){
            switch (b.getLocalDriver()) {
                case 0 -> regularBlocks.add(b);
                case 1 -> stationBlocks1.add(b);
                case 2 -> stationBlocks2.add(b);
                case 3 -> stationBlocks3.add(b);
                case 4 -> stationBlocks4.add(b);
                case 5 -> stationBlocks5.add(b);
                case 6 -> stationBlocks6.add(b);
            }
        }

        ArrayList<Schedule> schedules = new ArrayList<>();
        schedules.addAll(makeSchedulesForList(regularBlocks));
        schedules.addAll(makeSchedulesForList(stationBlocks1));
        schedules.addAll(makeSchedulesForList(stationBlocks2));
        schedules.addAll(makeSchedulesForList(stationBlocks3));
        schedules.addAll(makeSchedulesForList(stationBlocks4));
        schedules.addAll(makeSchedulesForList(stationBlocks5));
        schedules.addAll(makeSchedulesForList(stationBlocks6));

        int counter = 1;
        for (Schedule s : schedules) {
            s.setId(counter);
            c.calculateScheduleFB(s);
            counter++;
        }

        return schedules;
    }

    public ArrayList<Schedule> makeSchedulesForList(ArrayList<Block> list){
        ArrayList<Schedule> schedules = new ArrayList<>();
        Schedule schedule1 = new Schedule();
        schedules.add(schedule1);
        for (Block b : list) {
            int size = schedules.size();
            int counter = 0;
            for (Schedule s : schedules) {
                if (bestFitBlock(b, s)) {
                    break;
                }
                counter++;
            }

            //Add to new schedule
            if (counter == size) {
                Schedule newschedule = new Schedule();
                newschedule.getBlocks().add(b.getId());
                newschedule.setClosestDepot(findClosestDepot(b));
                newschedule.setStartStation(b.getStartLoc());
                newschedule.setStartDay(b.getStartWeekday());
                c.calculateScheduleFB(newschedule);
                schedules.add(newschedule);
            }
        }

        return schedules;
    }

    //True means that Block b is added to Schedule s
    public boolean bestFitBlock(Block b, Schedule s) {
        if(s.getBlocks().isEmpty()){
            s.getBlocks().add(b.getId());
            s.setClosestDepot(findClosestDepot(b));
            s.setStartStation(b.getStartLoc());
            s.setStartDay(b.getStartWeekday());
            c.calculateScheduleFB(s);
            return true;
        }
        if (checkBreakFit(b, s)) {
            if (checkNormalFit(b, s)) {
                // BREAK : YES ||| BLOCK : YES
                //If this block is added, there can be a break between temporary last and the new block
                s.getBlocks().add(b.getId());
                c.calculateScheduleFB(s);
                //TotalDuration checked here
                if (c.checkSchedule(s)) {
                    //BLOCK ADDED
                    return true;
                } else {
                    s.getBlocks().remove(s.getBlocks().size() - 1);
                    c.calculateScheduleFB(s);
                    //BLOCK NOT ADDED
                    return false;
                }
            }
        } else if (checkNormalFit(b, s)) {
            // BREAK : NO ||| BLOCK : YES
            //The block can be added but there can be no break
            s.getBlocks().add(b.getId());
            c.calculateScheduleFB(s);
            if (c.checkSchedule(s)) {
                //BLOCK ADDED
                return true;
            } else {
                s.getBlocks().remove(s.getBlocks().size() - 1);
                c.calculateScheduleFB(s);
                //BLOCK NOT ADDED
                return false;
            }
        }
        return false;
    }

    public boolean checkNormalFit(Block b, Schedule s) {
        Block lastblock = blocks.get(s.getBlocks().get(s.getBlocks().size() - 1) - 1);
        return consmatrix[lastblock.getId()][b.getId()] == 1;
    }

    public boolean checkBreakFit(Block b, Schedule s) {
        Block lastblock = blocks.get(s.getBlocks().get(s.getBlocks().size() - 1) - 1);
        return consbreakmatrix[lastblock.getId()][b.getId()] == 1;
    }

    int findClosestDepot(Block b) {
        int closest = 0;
        int distance = 9999;
        for (Station s : depots) {
            int travel = travelmatrix[s.getID()][b.getStartLoc()];
            if (travel < distance) {
                closest = s.getID();
                distance = travel;
            }
        }
        return closest;
    }
}

