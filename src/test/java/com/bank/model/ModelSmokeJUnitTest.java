package com.bank.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Lets Maven run the starter's ModelSmokeTest checks as part of "mvn test". */
class ModelSmokeJUnitTest {
    @Test
    void allModelRulesPass() {
        ModelSmokeTest.passed = 0;
        ModelSmokeTest.failed = 0;
        ModelSmokeTest.main(new String[0]);
        assertTrue(ModelSmokeTest.passed > 0);
        assertEquals(0, ModelSmokeTest.failed, "some model checks failed - see the console output");
    }
}
