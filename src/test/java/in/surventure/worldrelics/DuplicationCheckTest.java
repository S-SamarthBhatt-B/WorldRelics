package in.surventure.worldrelics;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.UUID;

public class DuplicationCheckTest {

    @Test
    public void testUuidMatching() {
        UUID validRelicUuid = UUID.randomUUID();
        UUID itemUuid = UUID.fromString(validRelicUuid.toString());
        UUID fakeUuid = UUID.randomUUID();

        Assertions.assertEquals(validRelicUuid, itemUuid);
        Assertions.assertNotEquals(validRelicUuid, fakeUuid);
    }
}
