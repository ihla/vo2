package co.jaw.calculate;

/**
 *
 */
public class WalkEstimator {

    private double maxGaitSpeed;

    public WalkEstimator(int age, int heightInCm, boolean isMale) {
        double heightInM = (double)heightInCm / 100;
        double normalisedMaxGaitSpeed = isMale ? getMaleHeightNormalisedMaxGaitSpeed(age)
                                               : getFemaleHeightNormalisedMaxGaitSpeed(age);
        maxGaitSpeed = heightInM * normalisedMaxGaitSpeed;
    }

    public boolean isWalking(final double speedMs) {
        return speedMs <= maxGaitSpeed;
    }

    public boolean isRunning(final double speedMs) {
        return !isWalking(speedMs);
    }

    // The following are table values gathered from
    // http://ageing.oxfordjournals.org/content/26/1/15.full.pdf

    private double getMaleHeightNormalisedMaxGaitSpeed(int age) {
        if (age < 10) return 1.100; // Bogus value
        if (age < 20) return 1.200; // Bogus value
        if (age < 30) return 1.431;
        if (age < 40) return 1.396;
        if (age < 50) return 1.395;
        if (age < 60) return 1.182;
        if (age < 70) return 1.104;
        return  1.192;
    }

    private double getFemaleHeightNormalisedMaxGaitSpeed(int age) {
        if (age < 10) return 1.100; // Bogus value
        if (age < 20) return 1.200; // Bogus value
        if (age < 30) return 1.502;
        if (age < 40) return 1.428;
        if (age < 50) return 1.304;
        if (age < 60) return 1.243;
        if (age < 70) return 1.107;
        return 1.110;
    }

}
