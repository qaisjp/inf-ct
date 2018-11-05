package gen;

import java.util.HashSet;
import java.util.Set;

public class Labeller {
    private String prefix;
    private int count = 0;
    private static Set<String> labels = new HashSet<>();

    public Labeller(String prefix) {
        this.prefix = prefix;
    }

    public String label(String label) {
        label = prefix + "_" + label;

        if (labels.contains(label)) {
            throw new RuntimeException("Somehow generated duplicate label " + label);
        }

        labels.add(label);
        return label;
    }

    public String num() {
        String label = String.format("%03d", count);
        count++;

        return label(label);
    }

    public String num(String context) {
        String label = String.format("%s_%03d", context, count);
        count++;

        return label(label);
    }

    public static void verifyLabel(String label) {
        if (!labels.contains(label)) {
            throw new RuntimeException("Attempting to use label, but could not be found!");
        }
    }
}
