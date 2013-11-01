package co.jaw.calculate;

/**
 */
public class MotionMeter {
    private final WalkEstimator walkEstimator;

    private double prevLat;
    private double prevLon;
    private long prevTime;

    public MotionMeter(int age, int heightInCm, boolean isMale) {
        walkEstimator = new WalkEstimator(age,heightInCm,isMale);
    }

    public void setStartPosition(double latitude, double longitude, long currTimeMs) {
        prevTime = currTimeMs;
        prevLat = latitude;
        prevLon = longitude;
    }

    public double calculateVO2(double latitude, double longitude, long currTimeMs) {
        // 1. Find out how much time passed from the last calculation
        long absTime = currTimeMs;
        long interval = absTime - prevTime;
        prevTime = absTime;

        // 2. Find out the distance walked/run
        double distanceMeters = DistanceCalculator.vincentyDistance(prevLat,prevLon,latitude,longitude);
        prevLat = latitude;
        prevLon = longitude;

        // 3. Calculate the speed and from it the VO2 for that distance.
        double speed = distanceMeters/interval*1000;
        return VO2Calculator.calculate(speed,walkEstimator.isRunning(speed));
    }
}
