package org.allowed.bitarus.jdbccodegen;

/*
 * Contains methods to process string for the app
 */
public class StringUtils{
    
    public String underscoreLetterToUpper(String value) {
        String temp = value;
        // replaces _a by A ...
        for (char letter = 'a'; letter <= 'z'; letter++) {
            String base = new Character(letter).toString();
            temp = temp.replaceAll("_" + base, base.toUpperCase());
        }
        return temp;
    }
    
    public String uppercaseFirstLetter(String value) {
        value = value.substring(0, 1).toUpperCase() + value.substring(1);
        return value;
    }
    
    public String formatTableName(String table) {
        // first letter must be Upper
        String temp = uppercaseFirstLetter(table);
        temp = underscoreLetterToUpper(temp);
        return temp;
    }
    
}