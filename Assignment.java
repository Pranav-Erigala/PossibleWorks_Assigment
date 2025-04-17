import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

class Point {
    BigInteger x;
    BigInteger y;

    public Point(BigInteger x, BigInteger y) {
        this.x = x;
        this.y = y;
    }
}

class JsonParser {
    public static Map<String, Object> parse(String jsonStr) throws Exception {
        Map<String, Object> result = new HashMap<>();

        jsonStr = jsonStr.replaceAll("\\s+", "");

        if (!jsonStr.startsWith("{") || !jsonStr.endsWith("}")) {
            throw new Exception("Invalid JSON format");
        }

        jsonStr = jsonStr.substring(1, jsonStr.length() - 1);

        int i = 0;
        while (i < jsonStr.length()) {
            if (jsonStr.charAt(i) != '"') {
                throw new Exception("Expected key at position " + i);
            }

            int keyStart = i + 1;
            i = jsonStr.indexOf('"', keyStart);
            if (i == -1) {
                throw new Exception("Unterminated key");
            }

            String key = jsonStr.substring(keyStart, i);
            i++;

            if (i >= jsonStr.length() || jsonStr.charAt(i) != ':') {
                throw new Exception("Expected ':' after key");
            }
            i++;

            Object value;
            if (jsonStr.charAt(i) == '{') {
                int braceCount = 1;
                int valueStart = i;
                i++;

                while (i < jsonStr.length() && braceCount > 0) {
                    if (jsonStr.charAt(i) == '{') braceCount++;
                    else if (jsonStr.charAt(i) == '}') braceCount--;
                    i++;
                }

                if (braceCount > 0) {
                    throw new Exception("Unterminated object");
                }

                String nestedJson = jsonStr.substring(valueStart, i);
                value = parse(nestedJson);
            } else if (jsonStr.charAt(i) == '"') {
                int valueStart = i + 1;
                i = jsonStr.indexOf('"', valueStart);
                if (i == -1) {
                    throw new Exception("Unterminated string value");
                }

                value = jsonStr.substring(valueStart, i);
                i++;
            } else {
                int valueStart = i;
                while (i < jsonStr.length() && jsonStr.charAt(i) != ',' && jsonStr.charAt(i) != '}') {
                    i++;
                }

                value = Integer.parseInt(jsonStr.substring(valueStart, i));
            }

            result.put(key, value);

            if (i < jsonStr.length() && jsonStr.charAt(i) == ',') {
                i++;
            }
        }

        return result;
    }
}

public class Main {

    public static BigInteger convertFromBase(String value, int base) {
        return new BigInteger(value, base);
    }

    public static BigInteger findSecret(List<Point> points, int k) {
        BigInteger result = BigInteger.ZERO;

        for (int i = 0; i < k; i++) {
            BigInteger term = points.get(i).y;
            BigInteger numerator = BigInteger.ONE;
            BigInteger denominator = BigInteger.ONE;

            for (int j = 0; j < k; j++) {
                if (i != j) {
                    numerator = numerator.multiply(points.get(j).x.negate());
                    denominator = denominator.multiply(points.get(i).x.subtract(points.get(j).x));
                }
            }

            term = term.multiply(numerator).divide(denominator);
            result = result.add(term);
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    public static BigInteger processTestCase(Map<String, Object> testCase) {
        Map<String, Object> keysMap = (Map<String, Object>) testCase.get("keys");
        int n = ((Number) keysMap.get("n")).intValue();
        int k = ((Number) keysMap.get("k")).intValue();

        List<Point> points = new ArrayList<>();

        for (int i = 1; i <= n; i++) {
            String key = String.valueOf(i);
            if (testCase.containsKey(key)) {
                Map<String, Object> pointData = (Map<String, Object>) testCase.get(key);

                String baseStr = (String) pointData.get("base");
                String valueStr = (String) pointData.get("value");

                int base = Integer.parseInt(baseStr);
                BigInteger x = BigInteger.valueOf(i);
                BigInteger y = convertFromBase(valueStr, base);

                points.add(new Point(x, y));
            }
        }

        if (points.size() < k) {
            throw new RuntimeException("Not enough points to reconstruct the polynomial");
        }

        List<Point> kPoints = points.subList(0, k);
        return findSecret(kPoints, k);
    }

    public static void main(String[] args) {
        try {
            String testCase1Json = "{\n" +
                "    \"keys\": {\n" +
                "        \"n\": 4,\n" +
                "        \"k\": 3\n" +
                "    },\n" +
                "    \"1\": {\n" +
                "        \"base\": \"10\",\n" +
                "        \"value\": \"4\"\n" +
                "    },\n" +
                "    \"2\": {\n" +
                "        \"base\": \"2\",\n" +
                "        \"value\": \"111\"\n" +
                "    },\n" +
                "    \"3\": {\n" +
                "        \"base\": \"10\",\n" +
                "        \"value\": \"12\"\n" +
                "    },\n" +
                "    \"6\": {\n" +
                "        \"base\": \"4\",\n" +
                "        \"value\": \"213\"\n" +
                "    }\n" +
                "}";

            String testCase2Json = "{\n" +
                "\"keys\": {\n" +
                "    \"n\": 10,\n" +
                "    \"k\": 7\n" +
                "  },\n" +
                "  \"1\": {\n" +
                "    \"base\": \"6\",\n" +
                "    \"value\": \"13444211440455345511\"\n" +
                "  },\n" +
                "  \"2\": {\n" +
                "    \"base\": \"15\",\n" +
                "    \"value\": \"aed7015a346d63\"\n" +
                "  },\n" +
                "  \"3\": {\n" +
                "    \"base\": \"15\",\n" +
                "    \"value\": \"6aeeb69631c227c\"\n" +
                "  },\n" +
                "  \"4\": {\n" +
                "    \"base\": \"16\",\n" +
                "    \"value\": \"e1b5e05623d881f\"\n" +
                "  },\n" +
                "  \"5\": {\n" +
                "    \"base\": \"8\",\n" +
                "    \"value\": \"316034514573652620673\"\n" +
                "  },\n" +
                "  \"6\": {\n" +
                "    \"base\": \"3\",\n" +
                "    \"value\": \"2122212201122002221120200210011020220200\"\n" +
                "  },\n" +
                "  \"7\": {\n" +
                "    \"base\": \"3\",\n" +
                "    \"value\": \"20120221122211000100210021102001201112121\"\n" +
                "  },\n" +
                "  \"8\": {\n" +
                "    \"base\": \"6\",\n" +
                "    \"value\": \"20220554335330240002224253\"\n" +
                "  },\n" +
                "  \"9\": {\n" +
                "    \"base\": \"12\",\n" +
                "    \"value\": \"45153788322a1255483\"\n" +
                "  },\n" +
                "  \"10\": {\n" +
                "    \"base\": \"7\",\n" +
                "    \"value\": \"1101613130313526312514143\"\n" +
                "  }\n" +
                "}";

            Map<String, Object> testCase1 = JsonParser.parse(testCase1Json);
            Map<String, Object> testCase2 = JsonParser.parse(testCase2Json);

            BigInteger secret1 = processTestCase(testCase1);
            BigInteger secret2 = processTestCase(testCase2);

            System.out.println("Secret for Test Case 1: " + secret1);
            System.out.println("Secret for Test Case 2: " + secret2);

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
