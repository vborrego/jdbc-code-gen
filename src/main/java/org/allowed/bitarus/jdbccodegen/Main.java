/*
Build:
mvn clean compile package

Run for mysql:  
java -cp target/jdbcCodeGen-0.0.1-SNAPSHOT.jar:/tmp/mysql-connector-java-5.1.30-bin.jar org.allowed.bitarus.jdbccodegen.Main

Test built Java files:
cd /tmp/gen
rm *.class
javac *.java
java -cp.:/tmp/mysql-connector-java-5.1.30-bin.jar MainApp #requires mysql-connector-java-5.1.30-bin.jar in /tmp 
*/
package org.allowed.bitarus.jdbccodegen;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    private static Logger logger = Logger.getAnonymousLogger();
    public static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    public static final String JDBC_URL="jdbc:mysql://localhost:3306/osticket?zeroDateTimeBehavior=convertToNull";
    public static final String OUTPUT_FOLDER = "/tmp/gen/";
    
    /**
    * @param args
    */
    public static void main(String[] args) {
        info("jdbcCodeGen on the way");        
        info(String.format("JDBC Driver: %s",JDBC_DRIVER)); 
        info(String.format("JDBC URL: %s",JDBC_URL));
        info(String.format("Output folder: %s", OUTPUT_FOLDER)); 
        
        try {
            Connection conn = getConnection();
            getMetaData(conn,OUTPUT_FOLDER);
            conn.close();
        }
        catch (ClassNotFoundException e) {
            logger.log(Level.SEVERE, "Error ClassNotFoundException", e);
        }
        catch (SQLException e) {
            logger.log(Level.SEVERE, "Error SQLException", e);
        }
        
        info(String.format("jdbcCodeGen finished. Check %s folder", OUTPUT_FOLDER) );
    }
    
    private static Connection getConnection() throws SQLException, ClassNotFoundException {
        // register MySQL Connector/J
        Class.forName(JDBC_DRIVER);
        Connection conn = DriverManager.getConnection(JDBC_URL, "root", "12345678");
        return conn;
    }
    
    private static void info(String msg) {
        System.out.println(msg);
    }
    
    private static void generateMainAppJavaFile(String outputFolder, ArrayList<String> daoNames){
        StringBuilder mainApp = new StringBuilder();
        mainApp.append("import java.sql.DriverManager; \n");
        mainApp.append("import java.sql.Connection; \n");
        mainApp.append("public class MainApp{ \n");
        mainApp.append("    public static void main(String[] args){ \n");
        mainApp.append("        try{ \n");
        mainApp.append("            Class.forName(\""+ JDBC_DRIVER +"\"); \n");
        mainApp.append("            Connection conn = DriverManager.getConnection(\"" + JDBC_URL + "\", \"root\", \"12345678\"); \n");
        
        for(String daoName:daoNames){
            mainApp.append(String.format("        System.out.println(\">>> getAll() for %s\");    \n", daoName) );
            mainApp.append(String.format("        for(Object obj : new %s(conn).getAll() ) { System.out.println(obj.toString());  }  \n", daoName) );                        
        }
        
        mainApp.append("        }catch(Exception ex){ \n");
        mainApp.append("            System.out.println(ex.getMessage() ); \n");
        mainApp.append("        } \n");
        mainApp.append("    } \n");
        mainApp.append("} \n");
        
        try {
            java.io.FileOutputStream fos = new java.io.FileOutputStream(String.format("%sMainApp.java",outputFolder), false);
            java.io.DataOutputStream dos = new java.io.DataOutputStream(fos);
            dos.writeBytes(mainApp.toString());
            dos.flush();
            dos.close();                
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        
    }
    
    private static String generateDTOJavaFile(DatabaseMetaData metaData, String table,String tableFormatted, String outputFolder)  throws SQLException {
        //DTO
        StringBuilder dto = new StringBuilder();                        
        dto.append("import java.util.Date;");
        dto.append("public class " + tableFormatted + " { \n");
        String getAllBody = createMembersAndGetAllSourceCode(metaData, table, dto);
        dto.append("} ");
        
        try {
            java.io.FileOutputStream fos = new java.io.FileOutputStream(String.format("%s%s.java",outputFolder,tableFormatted), false);
            java.io.DataOutputStream dos = new java.io.DataOutputStream(fos);
            dos.writeBytes(dto.toString());
            dos.flush();
            dos.close();            
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        
        return getAllBody;
    }
    
    private static String generateDAOJavaFile(String tableFormatted,String outputFolder,String getAllBody){
        // DAO ....
        String daoName = tableFormatted + "DAO";
        StringBuilder dao = new StringBuilder();
        dao.append("import java.sql.ResultSet;\n");
        dao.append("import java.util.ArrayList;\n");
        dao.append("import java.sql.Connection;\n");
        dao.append("import java.util.Date;\n");
        dao.append("import java.sql.PreparedStatement;\n");
        dao.append(String.format("public class %s {\n", daoName));
        dao.append(" private Connection conn;");
        dao.append(String.format("    public %s(Connection conn){this.conn=conn;} \n", daoName));
        dao.append(String.format("    public %s[] getAll(){%s} \n", tableFormatted, getAllBody));
        // TODO !!!!
        dao.append(String.format("    public void create(%s obj){ throw new UnsupportedOperationException(); } \n", tableFormatted));
        dao.append(String.format("    public %s read(%s obj){ throw new UnsupportedOperationException(); } \n", tableFormatted, tableFormatted));
        dao.append(String.format("    public void update(%s obj){ throw new UnsupportedOperationException();} \n", tableFormatted));
        dao.append(String.format("    public void delete(%s obj){ throw new UnsupportedOperationException(); } \n", tableFormatted));
        
        dao.append("} ");
        
        try {           
            java.io.FileOutputStream fos = new java.io.FileOutputStream( String.format("%s%s.java",outputFolder,daoName), false);
            java.io.DataOutputStream dos = new java.io.DataOutputStream(fos);
            dos.writeBytes(dao.toString());
            dos.flush();
            dos.close();                                
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        
        return daoName;        
    }
    
    private static void getMetaData(Connection conn,String outputFolder) throws SQLException {
        DatabaseMetaData metaData = conn.getMetaData();
        String[] types = { "TABLE" };
        ResultSet rs = metaData.getTables(null, null, null, types);
        
        ArrayList<String> daoNames = new ArrayList<String>(); 
        
        while (rs.next()) {
            String table = rs.getString("TABLE_NAME");
            String tableFormatted = formatTableName(table);
            String getAllBody = generateDTOJavaFile(metaData, table, tableFormatted,outputFolder);
            String daoName = generateDAOJavaFile(tableFormatted,outputFolder,getAllBody);
            daoNames.add(daoName);            
        }        
        rs.close();
        
        generateMainAppJavaFile(outputFolder,daoNames);
    }
    
    private static String underscoreLetterToUpper(String value) {
        String temp = value;
        // replaces _a by A ...
        for (char letter = 'a'; letter <= 'z'; letter++) {
            String base = new Character(letter).toString();
            temp = temp.replaceAll("_" + base, base.toUpperCase());
        }
        return temp;
    }
    
    private static String uppercaseFirstLetter(String value) {
        value = value.substring(0, 1).toUpperCase() + value.substring(1);
        return value;
    }
    
    private static String formatTableName(String table) {
        // first letter must be Upper
        String temp = uppercaseFirstLetter(table);
        temp = underscoreLetterToUpper(temp);
        return temp;
    }
    
    /**
    Creates DTO members and getAll body source code
    */
    private static String createMembersAndGetAllSourceCode(DatabaseMetaData metaData, String table, StringBuilder dto) throws SQLException {
        ResultSet rs = metaData.getColumns(null, null, table, null);
        String formattedTable = Main.formatTableName(table);
        ArrayList<String> columns = new ArrayList<String>();
        ArrayList<String> formcolumns = new ArrayList<String>();
        ArrayList<Integer> types = new ArrayList<Integer>();
        
        while (rs.next()) {
            String column = rs.getString("COLUMN_NAME");
            int dataType = rs.getInt("DATA_TYPE");
            int nullable = rs.getInt("NULLABLE");
            //rs.getTimestamp(null).getTime()
            String columnFormattedName = underscoreLetterToUpper(column);
            String formattedDataType = getDataType(dataType, nullable);
            
            String member = String.format("    private %s %s; // is nullable: %d datatype: %d\n", formattedDataType, columnFormattedName, nullable, dataType);
            String getter = createGetter(columnFormattedName, formattedDataType);
            String setter = createSetter(columnFormattedName, formattedDataType);
            
            dto.append(member);
            dto.append(getter);
            dto.append(setter);
            columns.add(column);
            formcolumns.add(columnFormattedName);
            types.add(dataType);
        }
        
        dto.append( createToStringMethod(formcolumns, types) );
        
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("ArrayList<%s> list = new ArrayList<%s>(); \n", formattedTable, formattedTable));
        sb.append("try{ \n");
        // sb.append("    Connection conn = ds.getConnection(); \n");
        sb.append(String.format("    PreparedStatement ps = conn.prepareStatement(\"SELECT * FROM %s;\"); \n", table));
        sb.append("    ResultSet rs = ps.executeQuery(); \n");
        sb.append("    while(rs.next()){ \n");
        sb.append(String.format("         %s objx = new %s();  \n", formattedTable, formattedTable));
        
        for (int i = 0; i < columns.size(); i++) {
            String col = columns.get(i);
            String form = formcolumns.get(i);
            Integer dtype = types.get(i);
            form = uppercaseFirstLetter(form);
            
            if (dtype == Types.TIMESTAMP) {
                sb.append(String.format("         if(rs.getTimestamp(\"%s\")!=null ){ objx.set%s( new Date(rs.getTimestamp(\"%s\").getTime() ) ); } \n", col,form, col));
            }
            else if (dtype == Types.INTEGER || dtype == Types.TINYINT) {
                sb.append(String.format("         objx.set%s( rs.getInt(\"%s\")  );  \n", form, col));
            }
            else if (dtype == Types.DATE) {
                sb.append(String.format("         objx.set%s( rs.getDate(\"%s\")  );  \n",form, col));
            }
            else if (dtype == Types.REAL) {
                sb.append(String.format("         objx.set%s( rs.getDouble(\"%s\")  );  \n", form, col));
            }
            else if (dtype == Types.BIT) {
                sb.append(String.format("         objx.set%s( rs.getBoolean(\"%s\")  );  \n", form, col));
            }
            else if (dtype == Types.LONGVARBINARY) {
                sb.append(String.format("         objx.set%s( rs.getBytes(\"%s\")  );  \n", form, col));
            }
            else if (dtype == Types.VARCHAR || dtype == Types.LONGVARCHAR || dtype == Types.CHAR) {
                sb.append(String.format("         objx.set%s( rs.getString(\"%s\")  );  \n", form, col));
            }
            else {
                sb.append(String.format("         objx.set%s(rs.getObject(\"%s\") );  \n",  form, col));
            }
        }
        sb.append("         list.add(objx);  \n");
        sb.append("    }\n");
        sb.append("    rs.close(); \n");
        //sb.append("    conn.close(); \n");
        sb.append("}catch(Exception ex){   \n");
        sb.append("    System.out.println(ex.getMessage()); \n");
        sb.append("} \n");
        sb.append(String.format("return list.toArray(new %s[list.size()]) ; \n ", formattedTable, formattedTable));
        return sb.toString();
    }

    private static String createToStringMethod(ArrayList<String> formcolumns , ArrayList<Integer> types) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("    public String toString(){" ));
        
        StringBuilder pattern=new StringBuilder();
        StringBuilder fields=new StringBuilder();
        
        for (int i = 0; i < formcolumns.size(); i++) {
            String form = formcolumns.get(i);
            Integer dtype = types.get(i);
                        
            if (dtype == Types.INTEGER || dtype == Types.TINYINT) {
                pattern.append(String.format(" %s: %%d " , form ));
                fields.append(String.format("this.%s," , form ));
            }
            else if (dtype == Types.VARCHAR || dtype == Types.LONGVARCHAR || dtype == Types.CHAR) {
                pattern.append(String.format(" %s: %%s " , form ));
                fields.append(String.format("this.%s," , form ));                
            }
        }
        
        // remove last ','
        String fieldsx = fields.toString();
        fieldsx = fieldsx.substring(0,fieldsx.length()-1);        
        
        sb.append(String.format(" return String.format(\"%s\", %s );", pattern.toString() , fieldsx ));
        sb.append("} \n");
        return sb.toString();
    }

    
    private static String createGetter(String columnFormattedName, String formattedDataType) {
        String temp = uppercaseFirstLetter(columnFormattedName);
        String methodName = "get" + temp;
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("    public %s %s(){", formattedDataType, methodName));
        sb.append(String.format("return this.%s;", columnFormattedName));
        sb.append("} \n");
        return sb.toString();
    }
    
    private static String createSetter(String columnFormattedName, String formattedDataType) {
        String temp = columnFormattedName.substring(0, 1).toUpperCase() + columnFormattedName.substring(1);
        String methodName = "set" + temp;
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("    public void %s(%s %s){", methodName, formattedDataType, columnFormattedName));
        sb.append(String.format("this.%s=%s;", columnFormattedName, columnFormattedName));
        sb.append("} \n");
        return sb.toString();
    }
    
    private static String getDataType(int dt, int nullable) {
        if (dt == Types.TIMESTAMP)
            return "Date";
        if (dt == Types.VARCHAR)
            return "String";
        if (dt == Types.BIT && nullable == 0)
            return "boolean";
        if (dt == Types.BIT && nullable == 1)
            return "Boolean";
        if (dt == Types.LONGVARCHAR)
            return "String";
        if (dt == Types.INTEGER && nullable == 0)
            return "int";
        if (dt == Types.INTEGER && nullable == 1)
            return "Integer";
        if (dt == Types.CHAR ) //&& nullable == 0)
            return "String";
        //if (dt == Types.CHAR && nullable == 1)
        // return "Integer";
        if (dt == Types.TINYINT && nullable == 0)
            return "int";
        if (dt == Types.TINYINT && nullable == 1)
            return "Integer";
        if (dt == Types.DATE)
            return "Date";
        if (dt == Types.REAL && nullable == 0)
            return "double";
        if (dt == Types.REAL && nullable == 1)
            return "Double";
        if (dt == Types.LONGVARBINARY)
            return "byte []";
        
        return "Object /*Type unrecognized. Check it ! */";
    }
    
}
