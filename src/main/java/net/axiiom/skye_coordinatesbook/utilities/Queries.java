package net.axiiom.skye_coordinatesbook.utilities;

// The queries run on the database
public enum Queries
{
    //Takes no arguments, creates base table
    CREATE_BASE("CREATE TABLE IF NOT EXISTS base (X INTEGER, Y INTEGER, Z INTEGER, WorldName TEXT, Description TEXT);"),

    //Gets all tables from the main database
    GET_ALL_TABLES("SELECT name FROM sqlite_master WHERE name != \"base\";"),

    //Gets all rows from the selected table
    SELECT_ALL_FROM("SELECT * FROM \"%s\";"),

    //Creates new table if it does not exist
    CREATE_TABLE_IF_NOT_EXISTS("CREATE TABLE IF NOT EXISTS \"%s\" AS SELECT * FROM base WHERE 0;"),

    //Inserts data into selected table
    INSERT_COORDINATE_INTO_TABLE("INSERT INTO \"%s\" VALUES (%d,%d,%d,\"%s\",\"%s\")"),

    //Check if row exists in selected table
    CHECK_ROW_EXISTS("SELECT 1 FROM \"%s\" WHERE X=%d AND Y=%d AND Z=%d AND WorldName=\"%s\" AND Description=\"%s\";"),

    //Remove coordinate from table
    REMOVE_COORDINATE_FROM_TABLE("DELETE FROM \"%s\" WHERE X=%d AND Y=%d AND Z=%d AND WorldName=\"%s\" AND Description=\"%s\";\"");

    private String action;

    public String query(Object _arg) {
        return query(new Object[] {_arg});
    }

    public String query(Object[] _args) {
        return String.format(this.action, _args);
    }

    public String query() {
        return this.action;
    }

    Queries(String action) {
        this.action = action;
    }
}
