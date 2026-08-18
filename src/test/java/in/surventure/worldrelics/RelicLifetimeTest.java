package in.surventure.worldrelics;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RelicLifetimeTest {

    @Test
    public void testMinecraftDaysConversion() {
        int mcDays = 12;
        // 1 Minecraft day = 20 real minutes = 1,200,000 milliseconds
        long lifetimeMillis = mcDays * 20L * 60L * 1000L;
        long expectedMillis = 14_400_000L; // 4 hours in milliseconds

        Assertions.assertEquals(expectedMillis, lifetimeMillis);
    }
}
