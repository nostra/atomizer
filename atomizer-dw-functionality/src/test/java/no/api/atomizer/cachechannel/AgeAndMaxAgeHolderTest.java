package no.api.atomizer.cachechannel;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class AgeAndMaxAgeHolderTest {

    @Test
    public void fairlyNormalUse() {
        AgeAndMaxAgeHolder aama = new AgeAndMaxAgeHolder();
        aama = aama.adjustAge(10);
        aama = aama.adjustMaxage(15).calculateMinimal();
        assertEquals(1, aama.size());
        assertEquals(10, aama.getAge());
        assertEquals(15, aama.getMaxAge());
    }

    @Test
    public void scenario_caac_2() {
        AgeAndMaxAgeHolder aama = new AgeAndMaxAgeHolder();
        aama = aama.adjustMaxage(90);
        aama = aama.adjustAge(10);
        aama = aama.adjustAge(30);
        aama = aama.adjustMaxage(100);
        assertEquals(2, aama.size());
        aama = aama.calculateMinimal();
        assertEquals(30, aama.getAge());
        assertEquals(100, aama.getMaxAge());
    }

    @Test
    public void scenario_aacc() {
        AgeAndMaxAgeHolder aama = new AgeAndMaxAgeHolder();
        aama = aama.adjustAge(10); // Discarded
        aama = aama.adjustAge(30);
        aama = aama.adjustMaxage(90); // 90-30 : 60: Keep
        aama = aama.adjustMaxage(70); // 70 - 0 : 70
        assertEquals(2, aama.size());
        aama = aama.calculateMinimal();
        assertEquals(30, aama.getAge());
        assertEquals(90, aama.getMaxAge());
    }

    @Test
    public void scenario_caca() {
        AgeAndMaxAgeHolder aama = new AgeAndMaxAgeHolder();
        aama = aama.adjustMaxage(250); // 250 - 50: 200
        aama = aama.adjustAge(50);
        aama = aama.adjustMaxage(300); // 300 - 160 : 140
        aama = aama.adjustAge(160);
        aama = aama.calculateMinimal();
        assertEquals(300, aama.getMaxAge());
        assertEquals(160, aama.getAge());
    }
}
