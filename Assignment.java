import java.io.FileReader;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class ShamirSecretSharing {
    public static void main(String[] args) {
        try {
            // Create an instance of SecretFinder
            SecretFinder secretFinder = new SecretFinder();
            
            // Process test case 1
            String testCase1Path = "testcase1.json";
            BigInteger secret1 = secretFinder.findSecret(testCase1Path);
            System.out.println("Secret for Test Case 1: " + secret1);
            
            // Process test case 2
            String testCase2Path = "testcase2.json";
            BigInteger secret2 = secretFinder.findSecret(testCase2Path);
            System.out.println("Secret for Test Case 2: " + secret2);
            
        } catch (Exception e) {
            System.err.println("Error in main: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

class SecretFinder {
    /**
     * Finds the secret 'c' from the given JSON file containing polynomial roots
     * @param filePath Path to the JSON file
     * @return The constant term 'c' of the polynomial
     */
    public BigInteger findSecret(String filePath) {
        try {
            // Parse the JSON file
            JSONObject jsonObject = parseJsonFile(filePath);
            
            // Extract key information
            JSONObject keys = (JSONObject) jsonObject.get("keys");
            long n = (long) keys.get("n");
            long k = (long) keys.get("k");
            
            // Collect and decode the points
            List<Point> points = decodePoints(jsonObject, n);
            
            // Use Lagrange interpolation to find the constant term (f(0))
            return lagrangeInterpolation(points, k);
            
        } catch (Exception e) {
            System.err.println("Error finding secret: " + e.getMessage());
            e.printStackTrace();
            return BigInteger.ZERO;
        }
    }
    
    /**
     * Parses a JSON file to extract the polynomial data
     * @param filePath Path to the JSON file
     * @return JSONObject containing the parsed data
     */
    private JSONObject parseJsonFile(String filePath) {
        try (FileReader reader = new FileReader(filePath)) {
            JSONParser parser = new JSONParser();
            return (JSONObject) parser.parse(reader);
        } catch (Exception e) {
            throw new RuntimeException("Error parsing JSON file: " + e.getMessage(), e);
        }
    }
    
    /**
     * Decodes points from the JSON object based on their base and value
     * @param jsonObject The JSON object containing the points
     * @param n The number of points
     * @return List of decoded points (x, y)
     */
    private List<Point> decodePoints(JSONObject jsonObject, long n) {
        List<Point> points = new ArrayList<>();
        
        for (int i = 1; i <= n; i++) {
            String key = String.valueOf(i);
            
            if (jsonObject.containsKey(key)) {
                try {
                    JSONObject pointData = (JSONObject) jsonObject.get(key);
                    String baseStr = (String) pointData.get("base");
                    String valueStr = (String) pointData.get("value");
                    
                    int base = Integer.parseInt(baseStr);
                    BigInteger y = new BigInteger(valueStr, base);
                    BigInteger x = BigInteger.valueOf(i);
                    
                    points.add(new Point(x, y));
                } catch (Exception e) {
                    System.err.println("Error decoding point " + key + ": " + e.getMessage());
                }
            }
        }
        
        return points;
    }
    
    /**
     * Uses Lagrange interpolation to find the constant term of the polynomial
     * @param points List of points (x, y)
     * @param k The minimum number of points needed (degree + 1)
     * @return The constant term (f(0))
     */
    private BigInteger lagrangeInterpolation(List<Point> points, long k) {
        // We only need k points for interpolation
        List<Point> selectedPoints = points.subList(0, (int) k);
        
        // Evaluate the polynomial at x = 0 using Lagrange interpolation
        BigInteger result = BigInteger.ZERO;
        
        for (int i = 0; i < selectedPoints.size(); i++) {
            BigInteger term = selectedPoints.get(i).y;
            BigInteger numerator = BigInteger.ONE;
            BigInteger denominator = BigInteger.ONE;
            
            for (int j = 0; j < selectedPoints.size(); j++) {
                if (i != j) {
                    // Calculate the numerator: (0 - x_j)
                    BigInteger xj = selectedPoints.get(j).x;
                    numerator = numerator.multiply(xj.negate());
                    
                    // Calculate the denominator: (x_i - x_j)
                    BigInteger xi = selectedPoints.get(i).x;
                    denominator = denominator.multiply(xi.subtract(xj));
                }
            }
            
            // Calculate the term: y_i * Î (0 - x_j) / Î (x_i - x_j)
            BigInteger coefficient = numerator.multiply(denominator.modInverse(new BigInteger("10000000000000000000000000")));
            term = term.multiply(coefficient);
            
            // Add this term to the result
            result = result.add(term);
        }
        
        return result;
    }
}

/**
 * Class representing a point (x, y) on the polynomial
 */
class Point {
    public BigInteger x;
    public BigInteger y;
    
    public Point(BigInteger x, BigInteger y) {
        this.x = x;
        this.y = y;
    }
    
    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}
