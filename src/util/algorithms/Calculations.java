package util.algorithms;

import global.Parameters;
import model.Block;
import model.Schedule;
import model.Station;

import java.util.ArrayList;
import java.util.Random;

public class Calculations {
    public final ArrayList<Block> blocks;
    public final ArrayList<Station> stations;
    public final ArrayList<Station> breakStations;
    public final ArrayList<Station> depots;
    public final Parameters parameters;
    public final int[][] travelmatrix;
    public final int[][] consmatrix;
    public final int[][] consbreakmatrix;
    public final Random random;

    public Calculations(ArrayList<Block> blocks, ArrayList<Station> stations, ArrayList<Station> breakStations, ArrayList<Station> depots, Parameters parameters, int[][] travelmatrix, int[][] consmatrix, int[][] consbreakmatrix, Random random) {
        this.blocks = blocks;
        this.stations = stations;
        this.breakStations = breakStations;
        this.depots = depots;
        this.parameters = parameters;
        this.travelmatrix = travelmatrix;
        this.consmatrix = consmatrix;
        this.consbreakmatrix = consbreakmatrix;
        this.random = random;
    }

    public void calculateSchedule(Schedule s){
        if(s.getBlocks().isEmpty()){
            return;
        }
        //Calculate how late the schedule starts (with the travel from the depot included)
        calculateStartTime(s);
        //Calculate when a break can be taken
        calculateBreak(s);
        //Calculates the duration of a schedule
        calculateDuration(s);
        //Calculates the time a driver is working without a break
        calculateTimeWithoutBreak(s);
        //Calculates the time that is wasted in the schedule, so the driver isn't doing anything
        calculateTimeWaste(s);
        //Calculates the time that is travelled during a schedule, also the average per block
        calculateTravelTime(s);
    }

    void calculateStartTime(Schedule s){

        ArrayList<Integer> sblocks = s.getBlocks();

        Block first = blocks.get(sblocks.get(0) - 1);
        int depot = findClosestDepot(first);
        s.setClosestDepot(depot);
        int travel = travelmatrix[s.getClosestDepot()][s.getStartStation()];

        int startTime = first.getDepartureTime() - travel - parameters.getCheckInTime();
        if (startTime < 0) {
            startTime = 1440 + (first.getDepartureTime() - travel - parameters.getCheckInTime());
            if(first.getStartWeekday() == 1){
                s.setStartDay(7);
            }else{
                s.setStartDay(first.getStartWeekday()-1);
            }
        }else{
            s.setStartDay(first.getStartWeekday());
        }
        s.setStartTime(startTime);
    }

    void calculateDuration(Schedule s){

        ArrayList<Integer> sblocks = s.getBlocks();
        Block last = blocks.get(sblocks.get(sblocks.size() - 1) - 1);

        int travel = travelmatrix[last.getEndLoc()][s.getClosestDepot()];

        int duration = 0;
        if (s.getStartDay() != last.getEndWeekday()) {
            duration += (1440 - s.getStartTime()) + last.getArrivalTime() + travel + parameters.getCheckOutTime();
            s.setDuration(duration);
        } else {
            duration += (last.getArrivalTime() - s.getStartTime()) + travel + parameters.getCheckOutTime();
            s.setDuration(duration);
        }
    }

    void calculateTimeWithoutBreak(Schedule s){
        int timeSinceLastBreak;
        if(s.getBreakAfterBlock() != -1){
            timeSinceLastBreak = s.getDuration() - s.getTimeWorkingBeforeBreak() - 30;
            s.setTimeWorkingWithoutBreak(timeSinceLastBreak);
        }else{
            timeSinceLastBreak = s.getDuration();
            s.setTimeWorkingWithoutBreak(timeSinceLastBreak);
        }
    }

    void calculateBreak(Schedule s) {
        breaksPossible(s);
        Block last = blocks.get(s.getBlocks().get(s.getBlocks().size() - 1) - 1);
        if (!s.getBreakPossibleAfterBlocks().isEmpty()) {
            int longest = 0;
            for (Integer i : s.getBreakPossibleAfterBlocks()) {
                Block beforeBreak = blocks.get(i - 1);
                int index = s.getBlocks().indexOf(i);
                if(index != s.getBlocks().size()-1){
                    int i2 = s.getBlocks().get(index + 1);
                    Block afterBreak = blocks.get(i2 - 1);
                    int dur = beforeBreak.getArrivalTime() - s.getStartTime() + calculateTravelTimeToBreak(stations.get(beforeBreak.getEndLoc() - 1), stations.get(afterBreak.getStartLoc() - 1));
                    if (dur > longest && dur < parameters.getMaximumDurationBeforeBreak() && s.getBlocks().size() > 1) {
                        longest = dur;
                        s.setBreakAfterBlock(i);
                        if (last.getEndWeekday() != afterBreak.getStartWeekday()) {
                            s.setTimeWorkingWithoutBreak(1440 - afterBreak.getDepartureTime() + last.getArrivalTime() + calculateTravelTimeFromBreak(stations.get(beforeBreak.getEndLoc() - 1), stations.get(afterBreak.getStartLoc() - 1)));
                        } else {
                            s.setTimeWorkingWithoutBreak(calculateTravelTimeFromBreak(stations.get(beforeBreak.getEndLoc() - 1), stations.get(afterBreak.getStartLoc() - 1)) + (last.getArrivalTime() - afterBreak.getDepartureTime()));
                        }
                        s.setTimeWorkingBeforeBreak(dur);
                    }
                }
            }
        }
    }

    void breaksPossible(Schedule s){
        s.getBreakPossibleAfterBlocks().clear();
        s.setBreakAfterBlock(-1);
        if (s.getBlocks().size() == 1){
            s.setBreakAfterBlock(s.getBlocks().get(0));
        }else{
            for (int i =0; i < s.getBlocks().size()-1  ;i++){
                Integer b1 = s.getBlocks().get(i);
                Integer b2 = s.getBlocks().get(i+1);
                if(consbreakmatrix[b1][b2] == 1){
                    s.getBreakPossibleAfterBlocks().add(b1);
                }
            }
        }
    }

    public int calculateTimeWaste(Schedule s) {
        int waste = 0;
        if (s.getBlocks().size() > 1) {
            for (int i = 0; i < s.getBlocks().size() - 2; i++) {
                Block a = blocks.get(s.getBlocks().get(i) - 1);
                Block b = blocks.get(s.getBlocks().get(i + 1) - 1);

                if (a.getId() == s.getBreakAfterBlock()) {
                    int travelToAndFromBreak = calculateTravelTimeFromBreak(stations.get(a.getEndLoc() - 1), stations.get(b.getStartLoc() - 1)) + calculateTravelTimeToBreak(stations.get(a.getEndLoc() - 1), stations.get(b.getStartLoc() - 1));
                    if(travelToAndFromBreak > 120){
                        waste += (travelToAndFromBreak-120);
                    }
                    int connectionWaste;
                    if(a.getStartWeekday() != b.getEndWeekday()){
                        connectionWaste = (1440 - a.getArrivalTime() + b.getDepartureTime() - 30 - travelToAndFromBreak);
                    }else{
                        connectionWaste = (b.getDepartureTime() - a.getArrivalTime() - 30 - travelToAndFromBreak);
                    }
                    if (connectionWaste < 0) {
                        connectionWaste = 0;
                    }
                    waste += connectionWaste;
                } else {
                    int connectionWaste;
                    if(travelmatrix[a.getEndLoc()][b.getStartLoc()] > 120){
                        waste += (travelmatrix[a.getEndLoc()][b.getStartLoc()] - 120);
                    }
                    if(a.getStartWeekday() != b.getEndWeekday()){
                        connectionWaste = (1440 - a.getArrivalTime() + b.getDepartureTime() - travelmatrix[a.getEndLoc()][b.getStartLoc()]);
                    }else{
                        connectionWaste = (b.getDepartureTime() - a.getArrivalTime() - travelmatrix[a.getEndLoc()][b.getStartLoc()]);
                    }
                    if (connectionWaste < 0) {
                        connectionWaste = 0;
                    }
                    waste += connectionWaste;
                }
            }
        }
        s.setTimeWasted(waste);
        return waste;
    }

    public int calculateTravelTime(Schedule s){
        int travel = 0;
        int fromDepot = travelmatrix[s.getClosestDepot()][s.getStartStation()];
        if (s.getBlocks().size() > 1) {
            for (int i = 0; i < s.getBlocks().size() - 2; i++) {
                Block a = blocks.get(s.getBlocks().get(i) - 1);
                Block b = blocks.get(s.getBlocks().get(i + 1) - 1);

                if (a.getId() == s.getBreakAfterBlock()) {
                    int travelToAndFromBreak = calculateTravelTimeFromBreak(stations.get(a.getEndLoc() - 1), stations.get(b.getStartLoc() - 1)) + calculateTravelTimeToBreak(stations.get(a.getEndLoc() - 1), stations.get(b.getStartLoc() - 1));
                    travel += travelToAndFromBreak;
                } else {
                    travel += travelmatrix[a.getEndLoc()][b.getStartLoc()];
                }
            }
        }
        Block last = blocks.get(s.getBlocks().get(s.getBlocks().size()-1)-1);
        int toDepot = travelmatrix[last.getEndLoc()][s.getClosestDepot()];
        travel += fromDepot + toDepot;
        s.setTravelTime(travel);
        double travelPerBlock = travel/s.getBlocks().size();
        s.setTravelTimePerBlock(travelPerBlock);
        return travel;
    }

    public int calculateTravelTimeToBreak(Station a, Station b) {
        int closest = 99999;
        int closestStation = 0;
        int distance;
        for (Station s : breakStations) {
            distance = travelmatrix[a.getID()][s.getID()] + travelmatrix[s.getID()][b.getID()];
            if (distance < closest) {
                closest = distance;
                closestStation = s.getID();
            }
        }
        return travelmatrix[a.getID()][closestStation];
    }

    public int calculateTravelTimeFromBreak(Station a, Station b) {
        int closest = 99999;
        int closestStation = 0;
        int distance;
        for (Station s : breakStations) {
            distance = travelmatrix[a.getID()][s.getID()] + travelmatrix[s.getID()][b.getID()];
            if (distance < closest) {
                closest = distance;
                closestStation = s.getID();
            }
        }
        return travelmatrix[closestStation][b.getID()];
    }

    public int calculateTravelTimeToBreak(Integer a, Integer b) {
        int closest = 99999;
        int closestStation = 0;
        int distance;
        for (Station s : breakStations) {
            distance = travelmatrix[a][s.getID()] + travelmatrix[s.getID()][b];
            if (distance < closest) {
                closest = distance;
                closestStation = s.getID();
            }
        }
        return travelmatrix[a][closestStation];
    }

    public int calculateTravelTimeFromBreak(Integer a, Integer b) {
        int closest = 99999;
        int closestStation = 0;
        int distance;
        for (Station s : breakStations) {
            distance = travelmatrix[a][s.getID()] + travelmatrix[s.getID()][b];
            if (distance < closest) {
                closest = distance;
                closestStation = s.getID();
            }
        }
        return travelmatrix[closestStation][b];
    }

    public boolean checkDuration(Schedule s) {
        Block last = blocks.get(s.getBlocks().get(s.getBlocks().size() - 1) - 1);
        int travel = travelmatrix[last.getEndLoc()][s.getClosestDepot()];

        if(!breakCheck(s)){
            s.setValid(false);
            return false;
        }

        int duration = travel + s.getDuration();
        boolean needBreak = duration >= parameters.getMaximumDurationBeforeBreak();

        if (needBreak) {
            if(duration <= parameters.getMaximumShiftLengthWeekend() && s.getBreakAfterBlock() != -1 && s.getTimeWorkingWithoutBreak() < parameters.getMaximumDurationBeforeBreak()){
                s.setValid(true);
                return true;
            }
        } else {
            if(duration <= parameters.getMaximumShiftLengthWeekend() && s.getTimeWorkingWithoutBreak() < parameters.getMaximumDurationBeforeBreak()){
                s.setValid(true);
                return true;
            }
        }
        s.setValid(false);
        return false;
    }

    boolean breakCheck(Schedule s){
        Integer blockBeforeBreak = s.getBreakAfterBlock();
        if(blockBeforeBreak == -1){return true;}
        if(s.getBlocks().size() == 1){
            return blockBeforeBreak.equals(s.getBlocks().get(0));
        }
        int index = s.getBlocks().indexOf(blockBeforeBreak);
        if(index == s.getBlocks().size()-1){
            return true;
        }
        Integer blockAfterBreak = s.getBlocks().get(index+1);
        return consbreakmatrix[blockBeforeBreak][blockAfterBreak] == 1;
    }

    public boolean checkSchedule(Schedule s){
        return (checkDuration(s) && breakCheck(s) && s.getTimeWorkingWithoutBreak() <= parameters.getMaximumDurationBeforeBreak() && s.getTimeWorkingWithoutBreak() > 0);
    }
    public int calculateCost(Schedule oldS, Schedule newS){
        int cost;
        //double feasibility =0; //Feasability //TODO CHECK IF NECESSARY

        double diffLD; //Local driver
        double diffWT; //Wasted Time
        double diffDur; //Duration
        double diffTT; //Travel Time
        double diffTC; //Total Cost

        if(oldS.getBlocks().isEmpty()){
            //TOTAL COST + LOCAL DRIVER
            if(newS.isLocal()){
                diffTC = parameters.getSalary() * parameters.getCostFraction();
                diffLD = -1;
            }else{
                diffTC = parameters.getSalary();
                diffLD = 0;
            }

            //WASTED TIME
            diffWT = newS.getTimeWasted();

            //TRAVEL TIME
            diffTT = newS.getTravelTimePerBlock();

            //TOTAL DURATION
            diffDur = newS.getDuration();

            cost = (int) (diffWT * 2 + diffDur * 1 + diffTC * 10000 + diffTT * 5 + diffLD * 10000);
            return cost;
        }

        // 1 SCHEDULE REMOVED
        if (newS.getDuration() == 0) {
            return -1000000;
        }

        //ERROR DETECTION
        if(oldS.getDuration() == 0){
            System.out.println(oldS);
            System.out.println("\n-------------------\n");
            System.out.println(newS);
        }

        //TOTAL COST + LOCAL DRIVER
        if(newS.isLocal() && !oldS.isLocal()){
            diffTC = parameters.getSalary()*parameters.getCostFraction() - parameters.getSalary();
            diffLD = -1;
        } else if (!newS.isLocal() && oldS.isLocal()) {
            diffTC = parameters.getSalary() - parameters.getSalary()*parameters.getCostFraction();
            diffLD = 1;
        } else {
            diffTC = 0;
            diffLD = 0;
        }

        //WASTED TIME
        diffWT = newS.getTimeWasted() - oldS.getTimeWasted();

        //TRAVEL TIME
        diffTT = newS.getTravelTimePerBlock() - oldS.getTravelTimePerBlock();

        //TOTAL DURATION
        diffDur = newS.getDuration() - oldS.getDuration();

        //NEGATIVE IS GOOD
        cost = (int) (diffWT * 2 + diffDur * 1 + diffTC * 10 + diffTT * 5 + diffLD * 10000);
        return cost;
    }

    public int findClosestDepot(Block b) {
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
