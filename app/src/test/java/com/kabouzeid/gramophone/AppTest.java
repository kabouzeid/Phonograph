package com.kabouzeid.gramophone;

import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class AppTest {

    @Test
    public void TestisProVersionEnabled() {
        App app=new App();
        boolean result=app.isProVersion();
        assertEquals(true,result);
    }
}