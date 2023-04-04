package model;

public class PossibleSolution {
    private Solution newSolution;
    private Solution oldSolution;
    private int oldCost;
    private int newCost;

    public PossibleSolution(Solution newSolution, int oldCost, int newCost) {
        this.newSolution = newSolution;
        this.oldCost = oldCost;
        this.newCost = newCost;
    }

    public PossibleSolution(Solution newSolution, Solution oldSolution, int oldCost, int newCost) {
        this.newSolution = new Solution(newSolution);
        this.oldSolution = new Solution(oldSolution);
        this.oldCost = oldCost;
        this.newCost = newCost;
    }

    public PossibleSolution(){}

    public Solution getNewSolution() {
        return newSolution;
    }

    public Solution getOldSolution() {
        return oldSolution;
    }

    public void setNewSolution(Solution newSolution) {
        this.newSolution = new Solution(newSolution);
    }

    public void setOldSolution(Solution oldSolution) {
        this.oldSolution = new Solution(oldSolution);
    }

    public void setOldCost(int oldCost) {
        this.oldCost = oldCost;
    }

    public void setNewCost(int newCost) {
        this.newCost = newCost;
    }

    public int getOldCost() {
        return oldCost;
    }

    public int getNewCost() {
        return newCost;
    }
}
