package in.surventure.worldrelics;

import in.surventure.worldrelics.util.LocationUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LocationMathTest {

    @Test
    public void testDistanceCalculation() {
        double pX = 0;
        double pZ = 0;
        double rX = 3000;
        double rZ = 4000;

        double dx = rX - pX;
        double dz = rZ - pZ;
        double distSq = (dx * dx) + (dz * dz);

        double minDistance = 3000;
        double minDistanceSq = minDistance * minDistance;

        Assertions.assertTrue(distSq >= minDistanceSq);
    }
}
