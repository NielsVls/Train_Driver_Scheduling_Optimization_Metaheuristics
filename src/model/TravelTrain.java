package model;

public class TravelTrain {
    final int startLoc;
    final int endLoc;
    final int startTime;
    final int endTime;
    final int trainNr;
    final int startDay;
    final int endDay;

    public TravelTrain(int startLoc, int endLoc, int startTime, int endTime, int trainNr, int startDay, int endDay) {
        this.startLoc = startLoc;
        this.endLoc = endLoc;
        this.startTime = startTime;
        this.endTime = endTime;
        this.trainNr = trainNr;
        this.startDay = startDay;
        this.endDay = endDay;
    }

    public int getStartLoc() {
        return startLoc;
    }

    public int getEndLoc() {
        return endLoc;
    }

    public int getStartTime() {
        return startTime;
    }

    public int getEndTime() {
        return endTime;
    }

    public int getTrainNr() {
        return trainNr;
    }

    public int getStartDay() {
        return startDay;
    }

    public int getEndDay() {
        return endDay;
    }

    @Override
    public String toString() {
        return "TravelTrain{" +
                "startLoc=" + startLoc +
                ", endLoc=" + endLoc +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                '}';
    }
}
