package util.algorithms;

import global.Parameters;
import model.Block;
import model.Schedule;
import model.Station;
import model.TravelTrain;

import java.util.ArrayList;
import java.util.Random;

public class Calculations {
    public final ArrayList<Block> blocks;
    public final ArrayList<Station> stations;
    public final ArrayList<Station> breakStations;
    public final ArrayList<Station> depots;
    public final ArrayList<TravelTrain> travelTrains;
    public final Parameters parameters;
    public final int[][] travelmatrix;
    public final int[][] consmatrix;
    public final int[][] consbreakmatrix;
    public final Random random;

    public Calculations(ArrayList<Block> blocks, ArrayList<Station> stations, ArrayList<Station> breakStations, ArrayList<Station> depots, Parameters parameters, int[][] travelmatrix, int[][] consmatrix, int[][] consbreakmatrix, Random random, ArrayList<TravelTrain> travelTrains) {
        this.blocks = blocks;
        this.stations = stations;
        this.breakStations = breakStations;
        this.depots = depots;
        this.parameters = parameters;
        this.travelmatrix = travelmatrix;
        this.consmatrix = consmatrix;
        this.consbreakmatrix = consbreakmatrix;
        this.random = random;
        this.travelTrains = travelTrains;
    }

    public void calculateSchedule(Schedule s) {
        //CALCULATES EVERYTHING NEEDED WHEN THE NEW BLOCK IS THE FIRST ONE
        if (s.getBlocks().isEmpty()) {
            return;
        }
        //Calculate how late the schedule starts (with the travel from the depot included)
        calculateStartTime(s);
        //Calculate when a break can be taken
        calculateBreak2(s);
        //Calculates the duration of a schedule
        calculateDuration(s);
        //Calculates the time a driver is working without a break
        calculateTimeWithoutBreak(s);
        //Calculates the time that is wasted in the schedule, so the driver isn't doing anything
        calculateTimeWaste(s);
        //Check if the schedule is local or not
        possibleForStationDriver(s);
    }

    void calculateStartTime(Schedule s) {

        ArrayList<Integer> sblocks = s.getBlocks();

        Block first = blocks.get(sblocks.get(0) - 1);
        int depot = findClosestDepot(first);
        s.setClosestDepot(depot);
        int travel = travelmatrix[s.getClosestDepot()][first.getStartLoc()];

        int startTime = first.getDepartureTime() - travel - parameters.getCheckInTime();
        if (startTime < 0) {
            startTime = 1440 + (first.getDepartureTime() - travel - parameters.getCheckInTime());
            if (first.getStartWeekday() == 1) {
                s.setStartDay(7);
            } else {
                s.setStartDay(first.getStartWeekday() - 1);
            }
        } else {
            s.setStartDay(first.getStartWeekday());
        }
        s.setStartTime(startTime);
    }

    void calculateDuration(Schedule s) {

        ArrayList<Integer> sblocks = s.getBlocks();
        Block last = blocks.get(sblocks.get(sblocks.size() - 1) - 1);

        int travel = travelmatrix[last.getEndLoc()][s.getClosestDepot()];

        int duration;
        if (last.getId() == s.getBreakAfterBlock()) {
            int breakTime = calculateTravelTimeToBreak(last.getEndLoc(),s.getClosestDepot()) + calculateTravelTimeFromBreak(last.getEndLoc(),s.getClosestDepot()) + 30;
            if (s.getStartDay() != last.getEndWeekday()) {
                duration = (1440 - s.getStartTime()) + last.getArrivalTime() + breakTime + parameters.getCheckOutTime();
                s.setDuration(duration);
            } else {
                duration = (last.getArrivalTime() - s.getStartTime()) + breakTime + parameters.getCheckOutTime();
                s.setDuration(duration);
            }
        } else {
            if (s.getStartDay() != last.getEndWeekday()) {
                duration = (1440 - s.getStartTime()) + last.getArrivalTime() + travel + parameters.getCheckOutTime();
                s.setDuration(duration);
            } else {
                duration = (last.getArrivalTime() - s.getStartTime()) + travel + parameters.getCheckOutTime();
                s.setDuration(duration);
            }
        }
    }

    void calculateTimeWithoutBreak(Schedule s) {
        int timeSinceLastBreak;
        if (s.getBreakAfterBlock() != -1) {
            timeSinceLastBreak = s.getDuration() - s.getTimeWorkingBeforeBreak() - 30;
            s.setTimeWorkingWithoutBreak(timeSinceLastBreak);
        } else {
            timeSinceLastBreak = s.getDuration();
            s.setTimeWorkingWithoutBreak(timeSinceLastBreak);
        }
    }

    void calculateBreak(Schedule s) {
        breaksPossible(s);
        if (!s.getBreakPossibleAfterBlocks().isEmpty()) {
            int longest = 0;
            for (Integer i : s.getBreakPossibleAfterBlocks()) {
                Block beforeBreak = blocks.get(i - 1);
                int index = s.getBlocks().indexOf(i);
                if (index != s.getBlocks().size() - 1) {
                    int i2 = s.getBlocks().get(index + 1);
                    Block afterBreak = blocks.get(i2 - 1);
                    int temp = beforeBreak.getArrivalTime() - s.getStartTime(); //199
                    if (temp < 0) {
                        temp += 1440;
                    }
                    int dur = temp + calculateTravelTimeToBreak(stations.get(beforeBreak.getEndLoc() - 1), stations.get(afterBreak.getStartLoc() - 1));
                    if (dur > longest && dur < parameters.getMaximumDurationBeforeBreak() && s.getBlocks().size() > 1) {
                        longest = dur;
                        s.setBreakAfterBlock(i);
//                        if (last.getEndWeekday() != afterBreak.getStartWeekday()) {
//                            s.setTimeWorkingWithoutBreak(1440 - afterBreak.getDepartureTime() + last.getArrivalTime() + calculateTravelTimeFromBreak(stations.get(beforeBreak.getEndLoc() - 1), stations.get(afterBreak.getStartLoc() - 1)));
//                        } else {
//                            s.setTimeWorkingWithoutBreak(calculateTravelTimeFromBreak(stations.get(beforeBreak.getEndLoc() - 1), stations.get(afterBreak.getStartLoc() - 1)) + (last.getArrivalTime() - afterBreak.getDepartureTime()));
//                        }
                        s.setTimeWorkingBeforeBreak(dur);
                    }
                }
            }
        }
    }

    void calculateBreak2(Schedule s) {
        breaksPossible(s);
        if (!s.getBreakPossibleAfterBlocks().isEmpty()) {
            int longest = 0;
            for (Integer i : s.getBreakPossibleAfterBlocks()) {
                Block beforeBreak = blocks.get(i - 1);
                int index = s.getBlocks().indexOf(i);
                if (index != s.getBlocks().size() - 1) {
                    int i2 = s.getBlocks().get(index + 1);
                    Block afterBreak = blocks.get(i2 - 1);
                    int temp = afterBreak.getDepartureTime() - s.getStartTime(); //254
                    if (temp < 0) {
                        temp += 1440;
                    }
                    int dur = temp - calculateTravelTimeFromBreak(stations.get(beforeBreak.getEndLoc() - 1), stations.get(afterBreak.getStartLoc() - 1)) - 30;
                    if (dur > longest /*&& dur < parameters.getMaximumDurationBeforeBreak()*/ && s.getBlocks().size() > 1) {
                        if(dur > parameters.getMaximumDurationBeforeBreak()){
                            dur = 300;
                        }
                        longest = dur;
                        s.setBreakAfterBlock(i);
//                        if (last.getEndWeekday() != afterBreak.getStartWeekday()) {
//                            s.setTimeWorkingWithoutBreak(1440 - afterBreak.getDepartureTime() + last.getArrivalTime() + calculateTravelTimeFromBreak(stations.get(beforeBreak.getEndLoc() - 1), stations.get(afterBreak.getStartLoc() - 1)));
//                        } else {
//                            s.setTimeWorkingWithoutBreak(calculateTravelTimeFromBreak(stations.get(beforeBreak.getEndLoc() - 1), stations.get(afterBreak.getStartLoc() - 1)) + (last.getArrivalTime() - afterBreak.getDepartureTime()));
//                        }
                        s.setTimeWorkingBeforeBreak(dur);
                    }
                }
            }
        }
    }


    void breaksPossible(Schedule s) {
        s.getBreakPossibleAfterBlocks().clear();
        s.setBreakAfterBlock(-1);
        if (s.getBlocks().size() == 1) {
            s.setBreakAfterBlock(s.getBlocks().get(0));
        } else {
            for (int i = 0; i < s.getBlocks().size() - 1; i++) {
                Integer b1 = s.getBlocks().get(i);
                Integer b2 = s.getBlocks().get(i + 1);
                if (consbreakmatrix[b1][b2] == 1) {
                    s.getBreakPossibleAfterBlocks().add(b1);
                }
            }
        }
    }

    void possibleForStationDriver(Schedule s) {
        int driver = blocks.get(s.getBlocks().get(0) - 1).getLocalDriver();
        for (Integer i : s.getBlocks()) {
            Block b = blocks.get(i - 1);
            if (b.getLocalDriver() == 0) {
                s.setDriverType(0);
                return;
            }
            if (b.getLocalDriver() != driver) {
                s.setDriverType(0);
                return;
            }
        }
        s.setDriverType(driver);
    }

    public int calculateTimeWaste(Schedule s) {
        int waste = 0;
        if (s.getBlocks().size() > 1) {
            for (int i = 0; i < s.getBlocks().size() - 2; i++) {
                Block a = blocks.get(s.getBlocks().get(i) - 1);
                Block b = blocks.get(s.getBlocks().get(i + 1) - 1);

                if (a.getId() == s.getBreakAfterBlock()) {
                    int travelToAndFromBreak = calculateTravelTimeFromBreak(stations.get(a.getEndLoc() - 1), stations.get(b.getStartLoc() - 1)) + calculateTravelTimeToBreak(stations.get(a.getEndLoc() - 1), stations.get(b.getStartLoc() - 1));
//                    if (travelToAndFromBreak > 60) {
//                        waste += (travelToAndFromBreak - 60);
//                    }
                    int connectionWaste;
                    if (a.getEndWeekday() != b.getStartWeekday()) {
                        connectionWaste = (1440 - a.getArrivalTime() + b.getDepartureTime() - 30 - travelToAndFromBreak);
                    } else {
                        connectionWaste = (b.getDepartureTime() - a.getArrivalTime() - 30 - travelToAndFromBreak);
                    }
                    if (connectionWaste < 0) {
                        connectionWaste = 0;
                    }
                    waste += connectionWaste;
                } else {
                    int connectionWaste;
//                    if (travelmatrix[a.getEndLoc()][b.getStartLoc()] > 120) {
//                        waste += (travelmatrix[a.getEndLoc()][b.getStartLoc()] - 120);
//                    }
                    if (a.getEndWeekday() != b.getStartWeekday()) {
                        connectionWaste = (1440 - a.getArrivalTime() + b.getDepartureTime() - travelmatrix[a.getEndLoc()][b.getStartLoc()]);
                    } else {
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

    public boolean isNightShift(Schedule s) {
        int startNight = 89;
        int endNight = 271;

        int startShift = s.getStartTime();
        int endShift = s.getStartTime() + s.getDuration();

        //Shift ends on different day
        if (endShift > 1440) {
            endShift -= 1440;
            return startNight <= endShift;
        }

        return (startShift <= endNight && endShift >= startNight);
    }

    public boolean checkDuration(Schedule s) {

        // CHECK IF THE DURATION IS VALID
        int maxDur;
        if (isNightShift(s)) {
            maxDur = parameters.getMaximumShiftLengthNight();
        } else if (s.getStartDay() == 6 || s.getStartDay() == 7) {
            maxDur = parameters.getMaximumShiftLengthWeekend();
        } else {
            maxDur = parameters.getMaximumShiftLengthWeekday();
        }

        if (s.getDuration() > maxDur) {
            return false;
        }

        if(s.getStartTime() < 180){
            return false;
        }
        // CHECK TIME WORKING BEFORE OR/AND  AFTER BREAK
        if (s.getBreakAfterBlock() != -1) {
            return s.getTimeWorkingBeforeBreak() <= parameters.getMaximumDurationBeforeBreak() && s.getTimeWorkingWithoutBreak() <= parameters.getMaximumDurationBeforeBreak();
        } else {
            return s.getDuration() <= parameters.getMaximumDurationBeforeBreak();
        }
    }

    boolean breakCheck(Schedule s) {
        Integer blockBeforeBreak = s.getBreakAfterBlock();
        if (blockBeforeBreak == -1) {
            return false;
            //TODO CHECK IF THIS FIXED IT
        }
        if (s.getBlocks().size() == 1) {
            return blockBeforeBreak.equals(s.getBlocks().get(0));
        }
        int index = s.getBlocks().indexOf(blockBeforeBreak);
        if (index == s.getBlocks().size() - 1) {
            return true;
        }
        Integer blockAfterBreak = s.getBlocks().get(index + 1);
        return consbreakmatrix[blockBeforeBreak][blockAfterBreak] == 1;
    }

    public boolean checkSchedule(Schedule s) {
        return (checkDuration(s) && breakCheck(s) && s.getTimeWorkingWithoutBreak() <= parameters.getMaximumDurationBeforeBreak() && s.getTimeWorkingWithoutBreak() > 0 && checkRegulations(s));
    }

    public double calculateCost(Schedule oldS, Schedule newS) {
        double oldcost;
        if (oldS.getDuration() > 0 && oldS.getDuration() < 360) {
            oldcost = 360 * parameters.getCostPerMinute();
        } else {
            oldcost = oldS.getDuration() * parameters.getCostPerMinute();
        }

        double newcost;
        if (newS.getDuration() > 0 && newS.getDuration() < 360) {
            newcost = 360 * parameters.getCostPerMinute();
        } else {
            newcost = newS.getDuration() * parameters.getCostPerMinute();
        }

        if (oldS.getDriverType() != 0) {
            oldcost = oldcost * parameters.getCostFraction();
        }
        if (newS.getDriverType() != 0) {
            newcost = newcost * parameters.getCostFraction();
        }

        double cost = newcost - oldcost;
        return cost;
    }

    public double calculateCost2(Schedule oldS, Schedule newS) {
        double oldcost;
        if (oldS.getDuration() > 0 && oldS.getDuration() < 360) {
            oldcost = 360 * parameters.getCostPerMinute();
        } else {
            oldcost = oldS.getDuration() * parameters.getCostPerMinute();
        }

        double newcost;
        if (newS.getDuration() > 0 && newS.getDuration() < 360) {
            newcost = 360 * parameters.getCostPerMinute();
        } else {
            newcost = newS.getDuration() * parameters.getCostPerMinute();
        }

        if (oldS.getDriverType() != 0) {
            oldcost = oldcost * parameters.getCostFraction();
        }
        if (newS.getDriverType() != 0) {
            newcost = newcost * parameters.getCostFraction();
        }

        int wasteO = oldS.getTimeWasted();
        int wasteN = newS.getTimeWasted();

        double cost = newcost - oldcost;
        int waste = wasteN - wasteO;
        return (cost*2 + waste);
    }


    public boolean checkRegulations(Schedule schedule) {
        Station depot = stations.get(schedule.getClosestDepot() - 1);
        for (Integer i : schedule.getBlocks()) {
            Block b = blocks.get(i - 1);
            if (depot.getRegulations().get(b.getStartLoc()) != 1 || depot.getRegulations().get(b.getEndLoc()) != 1) {
                return false;
            }
        }
        return true;
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
