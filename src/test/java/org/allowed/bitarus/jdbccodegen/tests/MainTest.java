package org.allowed.bitarus.jdbccodegen.tests;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import org.allowed.bitarus.jdbccodegen.StringUtils;
/* Classes names for tests: Test*  *Test *TestCase */
/* To ignore a @Test use @Ignore */
public class MainTest {
    
    @Test
    public void testUnderscoreLetterToUpper() {
        StringUtils su = new StringUtils();
        assertEquals("testString1" , su.underscoreLetterToUpper("test_string1") );
        assertEquals("testString1Test" , su.underscoreLetterToUpper("test_string1_test") );
        assertEquals("testString1AB" , su.underscoreLetterToUpper("test_string1_a_b") );
    }
    
    @Test
    public void testUppercaseFirstLetter(){
        StringUtils su = new StringUtils();
        assertEquals("Test_string1" , su.uppercaseFirstLetter("test_string1") );
        assertEquals("Testasd" , su.uppercaseFirstLetter("Testasd") );
    }
    
}