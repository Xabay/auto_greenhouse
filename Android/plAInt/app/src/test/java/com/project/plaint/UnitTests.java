package com.project.plaint;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class UnitTests {

    @Test
    public void constructor_tests() {
        Model m;

        //wrong ip or port
        try {
            m = new Model("0.0.0.0", 11111);
            fail();
        } catch (Exception ignored) {}
        try {
            m = new Model("0.0.0.0", 12345);
            fail();
        } catch (Exception ignored) {}
        try {
            m = new Model("192.168.100.40", 11111);
            fail();
        } catch (Exception ignored) {}

        //correct ip, port
        try {
            m = new Model("192.168.100.40", 12345);
            m.closeConection();
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void instance_test() {
        Model m = null;
        //check instance getter, setter
        assertNull(Model.getInstance());
        try {
            m = new Model("192.168.100.40", 12345);
            m.closeConection();
        } catch (Exception e) {
            fail();
        }
        Model.setInstance(m);
        assertNotNull(Model.getInstance());
    }

    @Test
    public void mode_test() {
        Model m = null;

        //check switching mode
        try {
            m = new Model("192.168.100.40", 12345);
            m.switchMode(true);
            assertTrue(m.queryMan("c"));
            m.switchMode(false);
            assertFalse(m.queryMan("c"));
            m.closeConection();
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void module_test() {
        Model m = null;

        //check query success
        try {
            m = new Model("192.168.100.40", 12345);
            m.queryMan("m");
            m.queryMan("h");
            m.queryMan("t");
            m.queryMan("l");
            m.queryMan("f");
            m.closeConection();
        } catch (Exception e) {
            fail();
        }

        //check toggle success
        try {
            m = new Model("192.168.100.40", 12345);
            m.toggleMan("m", true);
            m.toggleMan("h", true);
            m.toggleMan("t", true);
            m.toggleMan("l", true);
            m.toggleMan("f", true);
            m.toggleMan("m", false);
            m.toggleMan("h", false);
            m.toggleMan("t", false);
            m.toggleMan("l", false);
            m.toggleMan("f", false);
            m.closeConection();
        } catch (Exception e) {
            fail();
        }

        //check correct funcioning
        try {
            m = new Model("192.168.100.40", 12345);
            m.switchMode(true);

            //turn components on
            m.toggleMan("m", true);
            assertTrue(m.queryMan("m"));

            m.toggleMan("h", true);
            assertTrue(m.queryMan("h"));

            m.toggleMan("t", true);
            assertTrue(m.queryMan("t"));

            m.toggleMan("l", true);
            assertTrue(m.queryMan("l"));

            m.toggleMan("f", true);
            assertTrue(m.queryMan("f"));

            //turn components off
            m.toggleMan("m", false);
            assertFalse(m.queryMan("m"));

            m.toggleMan("h", false);
            assertFalse(m.queryMan("h"));

            m.toggleMan("t", false);
            assertFalse(m.queryMan("t"));

            m.toggleMan("l", false);
            assertFalse(m.queryMan("l"));

            m.toggleMan("f", false);
            assertFalse(m.queryMan("f"));

            m.closeConection();
        } catch (Exception e) {
            fail();
        }



    }

    @Test
    public void graf_test() {
        Model m = null;

        //check query success
        try {
            m = new Model("192.168.100.40", 12345);

            m.getPlotData("m");
            m.getPlotData("h");
            m.getPlotData("t");

            m.closeConection();
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void goal_test() {
        Model m = null;

        //check get and set success
        try {
            m = new Model("192.168.100.40", 12345);

            m.getAttr("m");
            m.getAttr("h");
            m.getAttr("t");
            m.getAttr("ls");
            m.getAttr("le");
            m.getAttr("fs");
            m.getAttr("fe");

            m.setAttr("m", 0);
            m.setAttr("h", 20);
            m.setAttr("t", 0);
            m.setAttr("ls", 0);
            m.setAttr("le", 0);
            m.setAttr("fs", 0);
            m.setAttr("fe", 0);

            m.closeConection();
        } catch (Exception e) {
            fail();
        }

        //check get and set success
        try {
            m = new Model("192.168.100.40", 12345);

            m.getAttr("t");
            m.getAttr("ls");
            m.getAttr("le");
            m.getAttr("fs");
            m.getAttr("fe");

            m.setAttr("m", 20);
            assertEquals(m.getAttr("m"), 20);
            m.setAttr("m", 0);

            m.setAttr("h", 30);
            assertEquals(m.getAttr("h"), 30);
            m.setAttr("h", 20);

            m.setAttr("t", 40);
            assertEquals(m.getAttr("t"), 40);
            m.setAttr("t", 0);

            m.setAttr("ls", 3660);
            assertEquals(m.getAttr("ls"), 3660);
            m.setAttr("ls", 0);

            m.setAttr("le", 7260);
            assertEquals(m.getAttr("le"), 7260);
            m.setAttr("le", 0);

            m.setAttr("fs", 10860);
            assertEquals(m.getAttr("fs"), 10860);
            m.setAttr("fs", 0);

            m.setAttr("fe", 14460);
            assertEquals(m.getAttr("fe"), 14460);
            m.setAttr("fe", 0);

            m.closeConection();
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void current_test() {
        Model m = null;

        //check getting values' success
        try {
            m = new Model("192.168.100.40", 12345);

            m.getVal("m");
            m.getVal("h");
            m.getVal("t");
            m.getVal("w");


            m.closeConection();
        } catch (Exception e) {
            fail();
        }

        //check if values are in the correct interval
        try {
            m = new Model("192.168.100.40", 12345);

            assertTrue(-50 < m.getVal("m"));
            assertTrue(150 > m.getVal("m"));
            assertTrue(0 < m.getVal("h"));
            assertTrue(100 > m.getVal("h"));
            assertTrue(0 < m.getVal("t"));
            assertTrue(100 > m.getVal("t"));
            assertTrue(-50 < m.getVal("w"));
            assertTrue(150 > m.getVal("w"));

            m.closeConection();
        } catch (Exception e) {
            fail();
        }
    }
}