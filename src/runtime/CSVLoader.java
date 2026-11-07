package runtime;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class CSVLoader {
    public static List<OHLCV> load(String path) {
        List<OHLCV> data = new ArrayList<>();
        try {
            List<String> lines = Files.readAllLines(Paths.get(path));
            for (int i = 1; i < lines.size(); i++) {
                String[] parts = lines.get(i).split(",");
                if (lines.get(i).isBlank()) continue;
                if (parts.length != 6) throw new IllegalArgumentException("Expected six fields at line " + (i + 1));
                for (int j = 1; j < 6; j++) {
                    double v = Double.parseDouble(parts[j]);
                    if (!Double.isFinite(v) || v < 0 || (j < 5 && v == 0))
                        throw new IllegalArgumentException("Invalid market value at line " + (i + 1));
                }
                {
                    data.add(new OHLCV(parts[0], Double.parseDouble(parts[1]), Double.parseDouble(parts[2]),
                            Double.parseDouble(parts[3]), Double.parseDouble(parts[4]), Double.parseDouble(parts[5])));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load CSV: " + e.getMessage());
        }
        return data;
    }
}
