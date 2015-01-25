package com.kabouzeid.materialmusic.helper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by karim on 24.01.15.
 */
public class Shuffler {
    private static int MAX_HISTORY_SIZE = 250;
    private List<Integer> order = new ArrayList<>();
    private int position;
    private int interval;

    public Shuffler(final int interval) {
        order = getShuffledOrderList(interval);
        this.interval = interval;
    }

    public int nextInt(boolean infinite) {
        position = position + 1;
        if (position > order.size() - 1) {
            if (infinite) {
                order.addAll(getShuffledOrderList(interval));
                if (order.size() > Math.max(interval, MAX_HISTORY_SIZE)) {
                    order = order.subList(order.size() / 2 - 1, order.size() - 1);
                }
            } else {
                return order.get(order.size() - 1);
            }
        }
        return order.get(position);
    }

    public int previousInt() {
        position = position - 1;
        if (position < 0) {
            position = 0;
        }
        return order.get(position);
    }

    private List<Integer> getShuffledOrderList(int interval) {
        final List<Integer> newList = new ArrayList<>();
        for (int i = 0; i < interval; i++) {
            newList.add(i);
        }
        Collections.shuffle(newList);
        return newList;
    }
}
