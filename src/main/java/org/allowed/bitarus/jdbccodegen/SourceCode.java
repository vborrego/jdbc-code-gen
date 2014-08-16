package org.allowed.bitarus.jdbccodegen;

public class SourceCode{

    private String getAll;
    private String create;
    private String update;
    private String read;
    private String delete;

    /**
    Basic constructor for SourceCode{
    */
    public SourceCode() {
        this.getAll = "throw new UnsupportedOperationException();";
        this.create = "throw new UnsupportedOperationException();";
        this.update = "throw new UnsupportedOperationException();";
        this.read = "throw new UnsupportedOperationException();";
        this.delete = "throw new UnsupportedOperationException();";
    }

    /**
    * Returns the value of getAll.
    */
    public String getGetAll() {
        return getAll;
    }


    /**
    * Sets the value of getAll.
    * @param getAll The value to assign getAll.
    */
    public void setGetAll(String getAll) {
        this.getAll = getAll;
    }


    /**
    * Returns the value of create.
    */
    public String getCreate() {
        return create;
    }


    /**
    * Sets the value of create.
    * @param create The value to assign create.
    */
    public void setCreate(String create) {
        this.create = create;
    }


    /**
    * Returns the value of update.
    */
    public String getUpdate() {
        return update;
    }


    /**
    * Sets the value of update.
    * @param update The value to assign update.
    */
    public void setUpdate(String update) {
        this.update = update;
    }

    /**
    * Returns the value of read.
    */
    public String getRead() {
        return read;
    }


    /**
    * Sets the value of read.
    * @param read The value to assign read.
    */
    public void setRead(String read) {
        this.read = read;
    }

    /**
    * Returns the value of delete.
    */
    public String getDelete() {
        return delete;
    }


    /**
    * Sets the value of delete.
    * @param delete The value to assign delete.
    */
    public void setDelete(String delete) {
        this.delete = delete;
    }

}
