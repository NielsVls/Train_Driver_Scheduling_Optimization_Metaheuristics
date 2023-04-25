package util.algorithms;

import model.Block;

import java.util.Comparator;

public class BlockComparator implements Comparator<Block> {
    @Override
    public int compare(Block o1, Block o2) {
        if (o1.getStartWeekday() == o2.getStartWeekday()) {
            return o1.getDepartureTime() - o2.getDepartureTime();
        } else {
            return o1.getStartWeekday() - o2.getStartWeekday();
        }
    }
}
