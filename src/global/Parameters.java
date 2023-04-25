package global;

public class Parameters {

    final double costFraction;
    final int salary;
    final double costPerMinute;
    final int minimumShiftLength;
    final int maximumShiftLengthWeekday;
    final int maximumShiftLengthWeekend;
    final int maximumShiftLengthNight;
    final int maximumDurationBreak;
    final int maximumDurationBeforeBreak;
    final int maximumTimeBetweenBlocks; //Time between blocks where the driver is doing nothing, so not riding, not taking a break or not travelling
    final int checkInTime;
    final int checkOutTime;

    public Parameters() {
        this.costFraction = 0.86;
        this.salary = 2000;
        this.minimumShiftLength = 1;
        this.maximumShiftLengthWeekday = 570;
        this.maximumShiftLengthWeekend = 600;
        this.maximumShiftLengthNight = 480;
        this.maximumDurationBreak = 30;
        this.maximumDurationBeforeBreak = 300;
        this.maximumTimeBetweenBlocks = 600;
        this.checkInTime = 20;
        this.checkOutTime = 5;
        this.costPerMinute = 0.21;
    }

    public double getCostFraction() {
        return costFraction;
    }

    public int getSalary() {
        return salary;
    }

    public int getMinimumShiftLength() {
        return minimumShiftLength;
    }

    public int getMaximumShiftLengthWeekday() {
        return maximumShiftLengthWeekday;
    }

    public int getMaximumShiftLengthWeekend() {
        return maximumShiftLengthWeekend;
    }

    public int getMaximumShiftLengthNight() {
        return maximumShiftLengthNight;
    }

    public int getMaximumDurationBreak() {
        return maximumDurationBreak;
    }

    public int getMaximumDurationBeforeBreak() {
        return maximumDurationBeforeBreak;
    }

    public int getMaximumTimeBetweenBlocks() {
        return maximumTimeBetweenBlocks;
    }

    public int getCheckInTime() {
        return checkInTime;
    }

    public int getCheckOutTime() {
        return checkOutTime;
    }

    public double getCostPerMinute() {
        return costPerMinute;
    }
}
