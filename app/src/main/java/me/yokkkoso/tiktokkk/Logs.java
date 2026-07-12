package me.yokkkoso.tiktokkk;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

public final class Logs {
    private static final int CAP = 4000;
    private static final ArrayDeque<String> BUFFER = new ArrayDeque<>();

    static synchronized void add(String line) {
        if (BUFFER.size() >= CAP) BUFFER.pollFirst();
        BUFFER.addLast(line);
    }

    public static synchronized List<String> snapshot() {
        return new ArrayList<>(BUFFER);
    }

    public static synchronized void clear() {
        BUFFER.clear();
    }

    private Logs() {}
}
