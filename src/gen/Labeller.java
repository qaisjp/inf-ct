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

    public String getLabel() {
        String label = String.format("%s_%03d", prefix, count);

        if (labels.contains(label)) {
            throw new RuntimeException("Somehow generated duplicate label " + label);
        }

        labels.add(label);
        count++;
        return label;
    }
}
