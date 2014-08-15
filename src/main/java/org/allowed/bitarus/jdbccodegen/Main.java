/*
Build:
  mvn clean compile package
  
Run for mysql:  
  java -cp target/jdbcCodeGen-0.0.1-SNAPSHOT.jar:/tmp/mysql-connector-java-5.1.30-bin.jar org.allowed.bitarus.jdbccodegen.Main
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

    private static Connection getConnection() throws SQLException, ClassNotFoundException {
        // register MySQL Connector/J
        Class.forName("com.mysql.jdbc.Driver");
        Connection conn = DriverManager.getConnection(
                              "jdbc:mysql://localhost:3306/osticket?zeroDateTimeBehavior=convertToNull", "root", "12345678");
        return conn;
    }

    private static void info(String msg) {
        System.out.println(msg);
    }

    private static void getMetaData(Connection conn) throws SQLException {
        DatabaseMetaData metaData = conn.getMetaData();
        String[] types = { "TABLE" };
        ResultSet rs = metaData.getTables(null, null, null, types);

        while (rs.next())
        {
            String table = rs.getString("TABLE_NAME");
            String tableFormatted = formatTableName(table);
            StringBuilder dto = new StringBuilder();
            StringBuilder dao = new StringBuilder();

            dto.append("import java.util.Date;");
            dto.append("public class " + tableFormatted + " { \n");
            String getAllBody = showColumns(metaData, table, dto);
            dto.append("} ");
            // DAO ....
            String daoName = tableFormatted + "DAO";

            dao.append("import java.sql.ResultSet;\n");
            dao.append("import java.util.ArrayList;\n");
            dao.append("import java.sql.Connection;\n");
            dao.append("import java.util.Date;\n");
            dao.append("import java.sql.PreparedStatement;\n");
            dao.append(String.format("public class %s {\n", daoName));
            dao.append(" private Connection conn;");
            dao.append(String.format(
                           "    public %s(Connection conn){this.conn=conn;} \n",
                           daoName));
            dao.append(String
                       .format("    public void create(%s obj){ throw new UnsupportedOperationException(); } \n",
                               tableFormatted));
            dao.append(String
                       .format("    public %s read(%s obj){ throw new UnsupportedOperationException(); } \n",
                               tableFormatted, tableFormatted));
            dao.append(String
                       .format("    public void update(%s obj){ throw new UnsupportedOperationException();} \n",
                               tableFormatted));
            dao.append(String
                       .format("    public void delete(%s obj){ throw new UnsupportedOperationException(); } \n",
                               tableFormatted));
            dao.append(String.format("    public %s[] getAll(){%s} \n",
                                     tableFormatted, getAllBody));
            dao.append("} ");

            StringBuilder mainApp = new StringBuilder();
            mainApp.append("import java.sql.DriverManager; \n");
            mainApp.append("import java.sql.Connection; \n");
            mainApp.append("public class MainApp{ \n");
            mainApp.append("    public static void main(String[] args){ \n");
            mainApp.append("        try{ \n");
            mainApp.append("            Class.forName(\"com.mysql.jdbc.Driver\"); \n");
            mainApp.append("            Connection conn = DriverManager.getConnection(\"jdbc:mysql://localhost:3306/osticket?zeroDateTimeBehavior=convertToNull\", \"root\", \"12345678\"); \n");
            mainApp.append("        }catch(Exception ex){ \n");
            mainApp.append("            System.out.println(ex.getMessage() ); \n");
            mainApp.append("        } \n");
            mainApp.append("    } \n");
            mainApp.append("} \n");

            try
            {
                java.io.FileOutputStream fos = new java.io.FileOutputStream(
                                                   "/tmp/gen/" + tableFormatted + ".java", false);
                java.io.DataOutputStream dos = new java.io.DataOutputStream(fos);
                dos.writeBytes(dto.toString());
                dos.flush();
                dos.close();

                fos = new java.io.FileOutputStream("/tmp/gen/" + daoName
                                                   + ".java", false);
                dos = new java.io.DataOutputStream(fos);
                dos.writeBytes(dao.toString());
                dos.flush();
                dos.close();
                fos = new java.io.FileOutputStream("/tmp/gen/" + "MainApp.java", false);
                dos = new java.io.DataOutputStream(fos);
                dos.writeBytes(mainApp.toString());
                dos.flush();
                dos.close();

            }
            catch (FileNotFoundException e)
            {
                e.printStackTrace();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        rs.close();

    }

    private static String underscoreLetterToUpper(String value) {
        String temp = value;
        // replaces _a by A ...
        for (char letter = 'a'; letter <= 'z'; letter++)
        {
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

    private static String showColumns(DatabaseMetaData metaData, String table,
                                      StringBuilder dto) throws SQLException {
        ResultSet rs = metaData.getColumns(null, null, table, null);
        String formattedTable = Main.formatTableName(table);
        ArrayList<String> columns = new ArrayList<String>();
        ArrayList<String> formcolumns = new ArrayList<String>();
        ArrayList<Integer> types = new ArrayList<Integer>();

        while (rs.next())
        {
            String column = rs.getString("COLUMN_NAME");
            int dataType = rs.getInt("DATA_TYPE");
            int nullable = rs.getInt("NULLABLE");
            //rs.getTimestamp(null).getTime()
            String columnFormattedName = underscoreLetterToUpper(column);
            String formattedDataType = getDataType(dataType, nullable);

            String member = String.format(
                                "    private %s %s; // is nullable: %d datatype: %d\n",
                                formattedDataType, columnFormattedName, nullable, dataType);
            String getter = createGetter(columnFormattedName, formattedDataType);
            String setter = createSetter(columnFormattedName, formattedDataType);

            dto.append(member);
            dto.append(getter);
            dto.append(setter);
            columns.add(column);
            formcolumns.add(columnFormattedName);
            types.add(dataType);
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("ArrayList<%s> list = new ArrayList<%s>(); \n",
                                formattedTable, formattedTable));
        sb.append("try{ \n");
        // sb.append("    Connection conn = ds.getConnection(); \n");
        sb.append(String
                  .format("    PreparedStatement ps = conn.prepareStatement(\"SELECT * FROM %s;\"); \n",
                          table));
        sb.append("    ResultSet rs = ps.executeQuery(); \n");
        sb.append("    while(rs.next()){ \n");
        sb.append(String.format("         %s objx = new %s();  \n",
                                formattedTable, formattedTable));
        for (int i = 0; i < columns.size(); i++)
        {
            String col = columns.get(i);
            String form = formcolumns.get(i);
            Integer dtype = types.get(i);
            form = uppercaseFirstLetter(form);

            if (dtype == Types.TIMESTAMP)
            {
                sb.append(String
                          .format("         if(rs.getTimestamp(\"%s\")!=null ){ objx.set%s( new Date(rs.getTimestamp(\"%s\").getTime() ) ); } \n",
                                  col,form, col));

            }
            else if (dtype == Types.INTEGER || dtype == Types.TINYINT)
            {
                sb.append(String.format(
                              "         objx.set%s( rs.getInt(\"%s\")  );  \n", form,
                              col));
            }
            else if (dtype == Types.DATE)
            {
                sb.append(String.format(
                              "         objx.set%s( rs.getDate(\"%s\")  );  \n",
                              form, col));
            }
            else if (dtype == Types.REAL)
            {
                sb.append(String.format(
                              "         objx.set%s( rs.getDouble(\"%s\")  );  \n",
                              form, col));
            }
            else if (dtype == Types.BIT)
            {
                sb.append(String.format(
                              "         objx.set%s( rs.getBoolean(\"%s\")  );  \n",
                              form, col));
            }
            else if (dtype == Types.LONGVARBINARY)
            {
                sb.append(String.format(
                              "         objx.set%s( rs.getBytes(\"%s\")  );  \n",
                              form, col));
            }
            else if (dtype == Types.VARCHAR || dtype == Types.LONGVARCHAR || dtype == Types.CHAR)
            {
                sb.append(String.format(
                              "         objx.set%s( rs.getString(\"%s\")  );  \n",
                              form, col));
            }
            else
            {
                sb.append(String.format(
                              "         objx.set%s(rs.getObject(\"%s\") );  \n",
                              form, col));

            }
        }
        sb.append("         list.add(objx);  \n");
        sb.append("    }\n");
        sb.append("    rs.close(); \n");
        //sb.append("    conn.close(); \n");
        sb.append("}catch(Exception ex){   \n");
        sb.append("    System.out.println(ex.getMessage()); \n");
        sb.append("} \n");
        sb.append(String.format(
                      "return list.toArray(new %s[list.size()]) ; \n ",
                      formattedTable, formattedTable));
        return sb.toString();
    }

    private static String createGetter(String columnFormattedName,
                                       String formattedDataType) {
        String temp = uppercaseFirstLetter(columnFormattedName);
        String methodName = "get" + temp;
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("    public %s %s(){", formattedDataType,
                                methodName));
        sb.append(String.format("return this.%s;", columnFormattedName));
        sb.append("} \n");
        return sb.toString();
    }

    private static String createSetter(String columnFormattedName,
                                       String formattedDataType) {
        String temp = columnFormattedName.substring(0, 1).toUpperCase()
                      + columnFormattedName.substring(1);
        String methodName = "set" + temp;
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("    public void %s(%s %s){", methodName,
                                formattedDataType, columnFormattedName));
        sb.append(String.format("this.%s=%s;", columnFormattedName,
                                columnFormattedName));
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

    /**
     * @param args
     */
    public static void main(String[] args) {
        info("jdbcCodeGen on the way");

        try
        {
            Connection conn = getConnection();
            getMetaData(conn);
            conn.close();
        }
        catch (ClassNotFoundException e)
        {
            logger.log(Level.SEVERE, "Error ClassNotFoundException", e);
        }
        catch (SQLException e)
        {
            logger.log(Level.SEVERE, "Error SQLException", e);
        }

    }
}
