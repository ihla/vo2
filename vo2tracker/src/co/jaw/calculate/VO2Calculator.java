package co.jaw.calculate;

/**
 *
 */
public class VO2Calculator {

    public static double calculate(final double speedMs, final boolean isRunning) {
        return isRunning ? horizontalRunFormula(speedMs)
                         : horizontalWalkFormula(speedMs);
    }

    public static double calculate(final double speedMs, final double steepnessPct, final boolean isRunning) {
        return isRunning ? generalRunFormula(speedMs,steepnessPct)
                         : generalWalkFormula(speedMs, steepnessPct);
    }

    /***
     * A simplified walk formula for walking on a flat terrain
     * @param speedMs walk speed in meters per second
     * @return VO2
     */
    private static double horizontalWalkFormula(final double speedMs) {
        return 6 * speedMs + 3.5;
    }

    /***
     * A simplified run formula for running on a flat terrain
     * @param speedMs run speed in meters per second
     * @return VO2
     */
    private static double horizontalRunFormula(final double speedMs) {
        return 12 * speedMs + 3.5;
    }

    /***
     * Walk formula derived from: http://blue.utb.edu/mbailey/handouts/pdf/MetCalnew.pdf
     * VO2 = 0.1(speed) + 1.8 (speed) (fractional grade) + 3.5
     * where speed is in meters per minute
     * @param speedMs walk speed
     * @param verticalAscentPct The steepness of the slope,one is walking on, in percentage
     * @return the VO2
     */
    private static double generalWalkFormula(final double speedMs, final double verticalAscentPct) {
        double fractionalGrade = verticalAscentPct/100;
        //     Horizontal part +  Vertical + part            +   Resting part
        return 6 * speedMs     +  108*speedMs*fractionalGrade +  3.5;
    }

    /***
     * Run formula derived from: http://blue.utb.edu/mbailey/handouts/pdf/MetCalnew.pdf
     * VO2 = 0.2 (speed) + 0.9 (speed)(fractional grade) + 3.5
     * where speed is in meters per minute
     * @param speedMs run speed
     * @param verticalAscentPct The steepness of the slope,one is walking on, in percentage
     * @return the VO2
     */
    private static double generalRunFormula(final double speedMs, final double verticalAscentPct) {
        double fractionalGrade = verticalAscentPct/100;
        //     Horizontal part +  Vertical + part            +   Resting part
        return 12 * speedMs     +  54*speedMs*fractionalGrade +  3.5;
    }
}
