package billboard.controlpanel;

/**
 * Class for storing the login token and userId locally
 */
public class Token {
    private static String token = "";
    private static int userId;

    /**
     * Get the session token
     * @return session token
     */
    public static String getToken(){
        return token;
    }

    /**
     * Set the session token
     * @param setToken - Token to set
     */
    public static void setToken(String setToken){
        token = setToken;
    }

    /**
     * Gets the userId that is currently logged in to the control panel
     * @return User ID
     */
    public static int getUserId(){
        return userId;
    }

    /**
     * Sets the userId that is currently logged in
     * @param setUserId - User ID to set
     */
    public static void setUserId(int setUserId){
        userId = setUserId;
    }
}
