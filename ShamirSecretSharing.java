import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ShamirSecretSharing {

    public record Point(BigInteger x, BigInteger y) {}

    public static void main(String[] args) {
        try {
            BigInteger secret1 = solveForConstant("testcase1.json");
            BigInteger secret2 = solveForConstant("testcase2.json");

            System.out.println("Secret for Test Case 1: " + secret1);
            System.out.println("Secret for Test Case 2: " + secret2);

        } catch (IOException e) {
            System.err.println("Error reading test case file: " + e.getMessage());
        }
    }

    private static BigInteger solveForConstant(String filePath) throws IOException {
        String jsonContent = readFile(filePath);

        int k = parseK(jsonContent);
        List<Point> allPoints = parsePoints(jsonContent);

        List<Point> pointsForInterpolation = allPoints.stream().limit(k).collect(Collectors.toList());

        return lagrangeInterpolateAtZero(pointsForInterpolation);
    }

    private static BigInteger lagrangeInterpolateAtZero(List<Point> points) {
        BigInteger finalNumerator = BigInteger.ZERO;
        BigInteger finalDenominator = BigInteger.ONE;

        for (int j = 0; j < points.size(); j++) {
            Point currentPoint = points.get(j);
            BigInteger termNumerator = currentPoint.y();
            BigInteger termDenominator = BigInteger.ONE;

            for (int m = 0; m < points.size(); m++) {
                if (m == j) continue;
                Point otherPoint = points.get(m);
                
                termNumerator = termNumerator.multiply(otherPoint.x().negate());
                termDenominator = termDenominator.multiply(currentPoint.x().subtract(otherPoint.x()));
            }

            finalNumerator = finalNumerator.multiply(termDenominator).add(termNumerator.multiply(finalDenominator));
            finalDenominator = finalDenominator.multiply(termDenominator);
        }

        return finalNumerator.divide(finalDenominator);
    }

    private static int parseK(String json) {
        String key = "\"k\":";
        int startIndex = json.indexOf(key) + key.length();

        // Find the start of the number, skipping whitespace
        while (startIndex < json.length() && !Character.isDigit(json.charAt(startIndex))) {
            startIndex++;
        }

        // Find the end of the number
        int endIndex = startIndex;
        while (endIndex < json.length() && Character.isDigit(json.charAt(endIndex))) {
            endIndex++;
        }

        return Integer.parseInt(json.substring(startIndex, endIndex));
    }

    private static List<Point> parsePoints(String json) {
        List<Point> points = new ArrayList<>();
        int i = 1;
        while (true) {
            String pointKey = "\"" + i + "\":";
            int pointStartIndex = json.indexOf(pointKey);
            if (pointStartIndex == -1) break;

            int objectStartIndex = json.indexOf('{', pointStartIndex);
            int objectEndIndex = json.indexOf('}', objectStartIndex);
            String pointObjectJson = json.substring(objectStartIndex, objectEndIndex);
            
            String baseKey = "\"base\":";
            int baseValueStartIndex = pointObjectJson.indexOf(baseKey) + baseKey.length();
            baseValueStartIndex = pointObjectJson.indexOf('"', baseValueStartIndex) + 1;
            int baseValueEndIndex = pointObjectJson.indexOf('"', baseValueStartIndex);
            int base = Integer.parseInt(pointObjectJson.substring(baseValueStartIndex, baseValueEndIndex));

            String valueKey = "\"value\":";
            int valueValueStartIndex = pointObjectJson.indexOf(valueKey) + valueKey.length();
            valueValueStartIndex = pointObjectJson.indexOf('"', valueValueStartIndex) + 1;
            int valueValueEndIndex = pointObjectJson.indexOf('"', valueValueStartIndex);
            String encodedValue = pointObjectJson.substring(valueValueStartIndex, valueValueEndIndex);
            
            BigInteger x = BigInteger.valueOf(i);
            BigInteger y = new BigInteger(encodedValue, base);

            points.add(new Point(x, y));
            i++;
        }
        return points;
    }

    private static String readFile(String filePath) throws IOException {
        return new String(Files.readAllBytes(Paths.get(filePath)));
    }
}