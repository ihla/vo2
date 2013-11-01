package co.jaw.calculate;

/**
 * Calculates distances from GPS positions
 */
public class DistanceCalculator {

    // Constants for sphericalCosine & haversine
    private static final double radianMultiplier = Math.PI / 180;
    private static final double earthRadius = 6378.1370; // km
    private static final double earthRadiusForMeters = earthRadius * 1000; // for results in meters

    // Constants for Vincenty
    private static final long a = 6378137;
    private static final double b = 6356752.314245;
    private static final double x = (a * a - b * b) / (b * b);
    private static final double f = 1 / 298.257223563; // WGS-84 ellipsoid params
    private static final double h = f/16; // WGS-84 ellipsoid params
    private static final double g = 1 - f;

    /***
     * Calculates geodetic distance between two points specified by latitude/longitude using Vincenty's inverse formula
     * for ellipsoids.
     * @param lat1 latitude of the first point in decimal degrees
     * @param lon1 longitude of the first point in decimal degrees
     * @param lat2 latitude of the second point in decimal degrees
     * @param lon2 longitude of the second point in decimal degrees
     * @return distance in metres between points
     */
    public static double vincentyDistance(double lat1, double lon1, double lat2, double lon2) {
        double L = toRad(lon2 - lon1);
        double U1 = Math.atan(g * Math.tan(toRad(lat1)));
        double U2 = Math.atan(g * Math.tan(toRad(lat2)));
        double sinU1 = Math.sin(U1),
               cosU1 = Math.cos(U1);
        double sinU2 = Math.sin(U2),
               cosU2 = Math.cos(U2);

        double lambda = L, lambdaP;
        int i = 100;
        double sigma, sinSigma, cosSqAlpha, cosSigma, cos2SigmaM;
        do {
            double sinLambda = Math.sin(lambda),
                   cosLambda = Math.cos(lambda);
            sinSigma = Math.sqrt((cosU2 * sinLambda) * (cosU2 * sinLambda) + (cosU1 * sinU2 - sinU1 * cosU2 * cosLambda) * (cosU1 * sinU2 - sinU1 * cosU2 * cosLambda));
            if (sinSigma == 0) return 0; // co-incident points
            cosSigma = sinU1 * sinU2 + cosU1 * cosU2 * cosLambda;
            sigma = Math.atan2(sinSigma, cosSigma);
            double sinAlpha = cosU1 * cosU2 * sinLambda / sinSigma;
            cosSqAlpha = 1 - sinAlpha * sinAlpha;
            cos2SigmaM = cosSigma - 2 * sinU1 * sinU2 / cosSqAlpha;
            if (Double.isNaN(cos2SigmaM)) cos2SigmaM = 0; // equatorial line: cosSqAlpha=0 (§6)
            double C = h * cosSqAlpha * (4 + f * (4 - 3 * cosSqAlpha));
            lambdaP = lambda;
            lambda = L + (1 - C) * f * sinAlpha * (sigma + C * sinSigma * (cos2SigmaM + C * cosSigma * (-1 + 2 * cos2SigmaM * cos2SigmaM)));
        } while (Math.abs(lambda - lambdaP) > 1e-12 && --i > 0);

        if (i == 0) return Double.NaN; // formula failed to converge
        double uSq = cosSqAlpha * x;
        double A = 1 + uSq / 16384 * (4096 + uSq * (-768 + uSq * (320 - 175 * uSq)));
        double B = uSq / 1024 * (256 + uSq * (-128 + uSq * (74 - 47 * uSq)));
        double deltaSigma = B * sinSigma * (cos2SigmaM + B / 4 * (cosSigma * (-1 + 2 * cos2SigmaM * cos2SigmaM) - B / 6 * cos2SigmaM * (-3 + 4 * sinSigma * sinSigma) * (-3 + 4 * cos2SigmaM * cos2SigmaM)));
        return b * A * (sigma - deltaSigma); // The distance in meters
    }

    /***
     * Distance in kilometers between two points using the Haversine algo.
     * Not used as the spherical cosines is faster and of same precision
     * @param lat1
     * @param lon1
     * @param lat2
     * @param lon2
     * @return
     */
    public static double haversineDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = toRad(lat2 - lat1);
        double dLong = toRad(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) * Math.sin(dLong / 2) * Math.sin(dLong / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadiusForMeters * c;
    }

    /***
     * Distance in kilometers between two points using The Spherical Law of Cosines algo.
     * Original source: "http://www.movable-type.co.uk/scripts/latlong.html"
     * @param lat1 latitude of the first point in decimal degrees
     * @param lon1 longitude of the first point in decimal degrees
     * @param lat2 latitude of the second point in decimal degrees
     * @param lon2 longitude of the second point in decimal degrees
     * @return distance in meters
     */
    public static double sphericalCosinesDistance(double lat1, double lon1, double lat2, double lon2) {
        double  dLon =  toRad(lon2-lon1);
        lat1 = toRad(lat1);
        lat2 = toRad(lat2);
        return Math.acos(Math.sin(lat1)* Math.sin(lat2) + Math.cos(lat1)*Math.cos(lat2) * Math.cos(dLon)) * earthRadiusForMeters;
    }

    private static double toRad(double degrees) {
        return degrees * radianMultiplier;
    }
}
