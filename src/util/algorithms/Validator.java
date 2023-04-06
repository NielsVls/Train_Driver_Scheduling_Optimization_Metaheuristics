package util.algorithms;

import global.Parameters;
import model.Block;
import model.Schedule;
import model.Solution;

public class Validator {
    Calculations c;
    Parameters p;
    int conBreak;
    int cons;
    int durBefBre;
    int dur;
    int durAftBre;
    int befBreWNoBre;

    public Validator(Calculations c) {
        this.c = c;
        p = new Parameters();
    }

    public void validate(Solution solution){
        conBreak = 0;
        cons = 0;
        durBefBre = 0;
        dur = 0;
        durAftBre = 0;
        befBreWNoBre = 0;

        boolean feasible = true;
        int invalidSchedules = 0;

        for(Schedule s : solution.getSchedules()){
            if(!validateSchedule(s)){
               invalidSchedules ++;
               feasible = false;
            }
        }

        System.out.println("----------------------------------------------------");
        if (feasible){
            System.out.println("The following solution is FEASIBLE");
        }else{
            System.out.println("The following solution is INFEASIBLE.");
            System.out.println("There are " + invalidSchedules + " Schedules that are invalid.");
            System.out.println("Violations in: ");
            System.out.println("Total duration : " + dur);
            System.out.println("Consecutive blocks : " + cons);
            System.out.println("Consecutive blocks with breaks : " + conBreak);
            System.out.println("Duration before break (No break) : " + befBreWNoBre);
            System.out.println("Duration before break : " + durBefBre);
            System.out.println("Duration after break : " + durAftBre);

        }
        System.out.println("----------------------------------------------------");
    }

    public boolean validateSchedule(Schedule s){
        // CONSTRAINT 1 : MAXIMUM DURATION
        int duration;
        int startTime = s.getStartTime();
        Block lastBlock = c.blocks.get(s.getBlocks().get(s.getBlocks().size()-1)-1);
        Integer depot = s.getClosestDepot();

        if(s.getStartDay() != lastBlock.getEndWeekday()){
            duration = (1440 - startTime) + lastBlock.getArrivalTime() + c.travelmatrix[lastBlock.getEndLoc()][depot] + p.getCheckOutTime();
        }else{
            duration = (lastBlock.getArrivalTime() + c.travelmatrix[lastBlock.getEndLoc()][depot] + p.getCheckOutTime()) - startTime;
        }

        if(duration > p.getMaximumShiftLengthWeekend()){
            System.out.println("THE SCHEDULE IS LONGER THEN ALLOWED!");
            dur++;
            return false;
        }
        int endtime = startTime + duration;
        if(endtime >=1440){ endtime -= 1440; }

        //CONSTRAINT 2 : CONSECUTIVE BLOCKS
        for(int i = 0; i < s.getBlocks().size()-1 ; i++){
            Block a = c.blocks.get(s.getBlocks().get(i)-1);
            Block b = c.blocks.get(s.getBlocks().get(i+1)-1);

            int time;
            if(a.getEndLoc() == b.getStartLoc() && b.getTrainNr().contains(a.getTrainNr().get(a.getTrainNr().size()-1))){
                time = a.getArrivalTime();
            } else {
                time = a.getArrivalTime() + c.travelmatrix[a.getEndLoc()][b.getStartLoc()];
                if(time > 1440){
                    time -= 1440;
                }
            }
            if(a.getEndWeekday()+1 == b.getStartWeekday()){
                time = a.getArrivalTime() + c.travelmatrix[a.getEndLoc()][b.getStartLoc()];
                if(time > 1440){
                    time -= 1440;
                }else{
                    time = 0;
                }
            }

            if(time > b.getDepartureTime()){
                System.out.println("THE BLOCKS IN THE SCHEDULE CAN NOT BE CONSECUTIVE!");
                cons++;
                return false;
            }
        }

        //CONSTRAINT 3 : BREAKS
        return validateBreak(s);
    }

    public boolean validateBreak(Schedule s){
        //NO BREAK
        if(s.getBreakAfterBlock() == -1){
            if(s.getDuration() > p.getMaximumDurationBeforeBreak()){
                System.out.println("THE SCHEDULE HAS NO BREAK AND IS LONGER (WITHOUT BREAK) THEN ALLOWED!");
                befBreWNoBre++;
                return false;
            }
            return true;
        }

        //BREAK
        Block beforeBreak = c.blocks.get(s.getBreakAfterBlock()-1);
        int index = s.getBlocks().indexOf(s.getBreakAfterBlock());
        Integer afterBreak;
        Block after;
        boolean isLastBlock = false;
        if(index == s.getBlocks().size()-1){
            afterBreak = s.getClosestDepot();
            isLastBlock = true;
        }else{
            after = c.blocks.get(s.getBlocks().get(index+1)-1);
            afterBreak = after.getStartLoc();
        }
        int breakTime = p.getMaximumDurationBreak() + c.calculateTravelTimeFromBreak(beforeBreak.getEndLoc(),afterBreak ) + c.calculateTravelTimeToBreak(beforeBreak.getEndLoc(),afterBreak );

        //TODO FIX THE CHECK FOR THE SCHEDULES WHO NEED A BREAK WHERE THERE IS ONLY ONE BLOCK

        //TIME WORKING WITHOUT A BREAK (BEFORE BREAK)
        int durationBeforeBreak;
        if(s.getStartDay() != beforeBreak.getStartWeekday()){
            durationBeforeBreak = (1440 - s.getStartTime()) + beforeBreak.getArrivalTime() + c.calculateTravelTimeToBreak(beforeBreak.getEndLoc(),afterBreak );
            if(s.getBlocks().size() == 1){
                durationBeforeBreak = Math.min((1440 - s.getStartTime()) + beforeBreak.getArrivalTime() + c.calculateTravelTimeToBreak(beforeBreak.getEndLoc(),afterBreak ),(1440 - s.getStartTime()) + beforeBreak.getArrivalTime() + c.calculateTravelTimeToBreak(afterBreak,beforeBreak.getEndLoc() ));
            }
        }else{
            durationBeforeBreak = beforeBreak.getArrivalTime()  + c.calculateTravelTimeToBreak(beforeBreak.getEndLoc(), afterBreak) - s.getStartTime();
            if(s.getBlocks().size() == 1){
                durationBeforeBreak = Math.min(beforeBreak.getArrivalTime()  + c.calculateTravelTimeToBreak(beforeBreak.getEndLoc(), afterBreak) - s.getStartTime(),beforeBreak.getArrivalTime()  + c.calculateTravelTimeToBreak(afterBreak,beforeBreak.getEndLoc()) - s.getStartTime());
            }
            if(durationBeforeBreak >= 1440){
                durationBeforeBreak -= 1440;
            }
        }
        if(durationBeforeBreak > p.getMaximumDurationBeforeBreak() || durationBeforeBreak < 0){
            System.out.println("THE SCHEDULE IS LONGER THEN ALLOWED BEFORE THE DRIVER HAS A BREAK!");
            durBefBre++;
            System.out.println(durationBeforeBreak);
            System.out.println(s.getStartTime());
            System.out.println(beforeBreak.getStartWeekday());
            System.out.println(beforeBreak.getDepartureTime());
            System.out.println(beforeBreak.getArrivalTime());
            System.out.println(s);
            return false;
        }

        //TIME WORKING WITHOUT A BREAK (AFTER BREAK)
        int durationAfterBreak;
        durationAfterBreak = s.getDuration() - durationBeforeBreak - p.getMaximumDurationBreak();
        if(durationAfterBreak > p.getMaximumDurationBeforeBreak()){
            System.out.println(s.getDuration());
            System.out.println(durationBeforeBreak);
            System.out.println(durationAfterBreak);
            System.out.println(s);

            System.out.println("THE DRIVER IS WORKING LONGER THEN ALLOWED AFTER HIS BREAK!");
            durAftBre++;
            return false;
        }

        //IS THERE TIME FOR A BREAK BETWEEN THE BLOCKS
        int time;
        time = beforeBreak.getArrivalTime() + breakTime;
        if(time >= 1440){
            time -= 1440;
        }

        if(!isLastBlock){
            after = c.blocks.get(afterBreak-1);
            if(time > after.getDepartureTime()){
                System.out.println("THE BLOCKS CANNOT BE CONSECUTIVE WITH A BREAK IN BETWEEN!");
                conBreak++;
                return false;
            }
        }
        return true;
    }
}
