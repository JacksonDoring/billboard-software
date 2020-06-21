package billboard.server;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Class for handling the connection to the database
 */
public class DbConnection {
    private static Connection connection = null;

    private static final String USER_TABLE = "CREATE TABLE IF NOT EXISTS Users(" +
            "userId int NOT NULL AUTO_INCREMENT," +

            "username varchar(255) NOT NULL UNIQUE," +
            "password varchar(255) NOT NULL," +
            "salt varchar(255) NOT NULL," +

            "editBillboards boolean DEFAULT False," +
            "createBillboards boolean DEFAULT False," +
            "scheduleBillboards boolean DEFAULT False," +
            "editUsers boolean DEFAULT False," +

            "PRIMARY KEY(userId)" +
            ");";

    private static final String BILLBOARDS_TABLE = "CREATE TABLE IF NOT EXISTS Billboards(" +
            "billboardId int NOT NULL AUTO_INCREMENT," +

            "name varchar(100) NOT NULL UNIQUE," +
            "data LONGTEXT NOT NULL," +

            "userId int NOT NULL," +

            "PRIMARY KEY(billboardId)," +
            "FOREIGN KEY(userId) REFERENCES Users(userId)" +
            ");";

    private static final String SCHEDULE_BILLBOARDS_TABLE = "CREATE TABLE IF NOT EXISTS Schedule(" +
            "scheduleId int NOT NULL AUTO_INCREMENT," +
            "billboardId int NOT NULL," +

            "creationTime DATETIME NOT NULL," +

            "userId int NOT NULL," +

            "day int NOT NULL," +
            "minutesStart int NOT NULL," +
            "minutesDuration int NOT NULL," +
            "repeating int NOT NULL," +
            "minutesRepeatGap int NOT NULL," +

            "PRIMARY KEY(scheduleId, billboardId)," +
            "FOREIGN KEY(billboardId) REFERENCES Billboards(billboardId)," +
            "FOREIGN KEY(userId) REFERENCES Users(userId)" +
            ");";

    private static final String SCHEDULE_BILLBOARDS_TIME_TABLE = "CREATE TABLE IF NOT EXISTS ScheduleTimes(" +
            "scheduleTimeId int NOT NULL AUTO_INCREMENT," +
            "scheduleId int NOT NULL," +

            "day int NOT NULL," +

            "startMinutes int NOT NULL," +
            "endMinutes int NOT NULL," +

            "PRIMARY KEY(scheduleTimeId)," +
            "FOREIGN KEY(scheduleId) REFERENCES Schedule(scheduleId)" +
            ");";

    private static final String SESSION_TABLE = "CREATE TABLE IF NOT EXISTS Sessions(" +
            "sessionId int NOT NULL AUTO_INCREMENT," +
            "userId int NOT NULL," +

            "sessionKey varchar(255) NOT NULL," +
            "expiry DATETIME NOT NULL," +

            "PRIMARY KEY(sessionId, userId)," +
            "FOREIGN KEY(userId) REFERENCES Users(userId)" +
            ");";

    /**
     * Creates the database tables
     * @throws SQLException if an SQL exception occurs
     */
    private static void createTables() throws SQLException {
        connection = DbConnection.getInstance();

        Statement statement = connection.createStatement();

        statement.execute(USER_TABLE);
        statement.execute(BILLBOARDS_TABLE);
        statement.execute(SCHEDULE_BILLBOARDS_TABLE);
        statement.execute(SCHEDULE_BILLBOARDS_TIME_TABLE);
        statement.execute(SESSION_TABLE);
    }

    /**
     * Resets all of the DB tables
     * @throws SQLException if an SQL exception occurs
     */
    public static void resetTables() throws SQLException {
        connection = DbConnection.getInstance();

        Statement statement = connection.createStatement();

        statement.execute("DROP table Sessions");
        statement.execute("DROP table ScheduleTimes");
        statement.execute("DROP table Schedule");
        statement.execute("DROP table Billboards");
        statement.execute("DROP table Users");

        createTables();
    }

    /**
     * Connects to the database
     */
    private static void connect() {
        Properties props = new Properties();
        FileInputStream in = null;

        try {
            in = new FileInputStream("resources/db.props");
            props.load(in);
            in.close();

            // specify the data source, username and password
            String url = props.getProperty("jdbc.url");
            String username = props.getProperty("jdbc.username");
            String password = props.getProperty("jdbc.password");
            String schema = props.getProperty("jdbc.schema");

            // get a connection
            connection = DriverManager.getConnection(url + "/" + schema, username, password);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Initialises the connection to the database
     * @throws SQLException if an SQL exception occurs
     */
    private static void dbConnect() throws SQLException {
        connect();

        createTables();
    }

    /**
     * Closes the connection to the database
     * @throws SQLException if an SQL exception occurs
     */
    public static void dbClose() throws SQLException {
        connection.close();
    }

    /**
     * Gets the database connection, or connects if it isn't already
     * @return Database connection
     * @throws SQLException if an SQL exception occurs
     */
    public static Connection getInstance() throws SQLException {
        if (connection == null) {
            dbConnect();
        }

        return connection;
    }
}
