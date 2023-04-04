package model;

import java.util.ArrayList;

public class Block {
    private final int id;
    private final int startLoc;
    private final int endLoc;
    private final int startWeekday;
    private final int endWeekday;
    private final int localDriver;
    private final int departureTime;
    private final int arrivalTime;
    private final String name;
    private final String rollingStock;
    private final ArrayList<Integer> trainNr;
    private int executedBy; //This will show the integer (id) of the driver that will execute this block

    public Block(String name, int departureTime, int arrivalTime, int startLoc, int endLoc, int startWeekday, int endWeekday, String rollingStock, int localDriver, ArrayList<Integer> trainNr, int id) {
        this.name = name;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.startLoc = startLoc;
        this.endLoc = endLoc;
        this.startWeekday = startWeekday;
        this.endWeekday = endWeekday;
        this.rollingStock = rollingStock;
        this.localDriver = localDriver;
        this.trainNr = new ArrayList<>(trainNr);
        this.id = id;
    }

    public Block(Block other){
        this.name = other.name;
        this.departureTime = other.departureTime;
        this.arrivalTime = other.arrivalTime;
        this.startLoc = other.startLoc;
        this.endLoc = other.endLoc;
        this.startWeekday = other.startWeekday;
        this.endWeekday = other.endWeekday;
        this.rollingStock = other.rollingStock;
        this.localDriver = other.localDriver;
        this.trainNr = new ArrayList<>(other.trainNr);
        this.id = other.id;
    }


    public int getId() {
        return id;
    }

    public int getStartLoc() {
        return startLoc;
    }

    public int getEndLoc() {
        return endLoc;
    }

    public int getStartWeekday() {
        return startWeekday;
    }

    public int getEndWeekday() {
        return endWeekday;
    }

    public int getLocalDriver() {
        return localDriver;
    }

    public String getName() {
        return name;
    }

    public String getRollingStock() {
        return rollingStock;
    }

    public ArrayList<Integer> getTrainNr() {
        return trainNr;
    }

    public int getDepartureTime() {
        return departureTime;
    }

    public int getArrivalTime() {
        return arrivalTime;
    }

    public int getExecutedBy() {
        return executedBy;
    }

    public void setExecutedBy(int executedBy) {
        this.executedBy = executedBy;
    }

    @Override
    public String toString() {
        return "Block{" +
                "id=" + id +
                ", startLoc=" + startLoc +
                ", endLoc=" + endLoc +
                ", startWeekday=" + startWeekday +
                ", endWeekday=" + endWeekday +
                ", localDriver=" + localDriver +
                ", departureTime=" + departureTime +
                ", arrivalTime=" + arrivalTime +
                ", name='" + name + '\'' +
                ", rollingStock='" + rollingStock + '\'' +
                ", trainNr='" + trainNr + '\'' +
                ", executedBy=" + executedBy +
                '}';
    }
}