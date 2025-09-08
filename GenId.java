public class GenId {
    private static int counterTask;
    private static int counterUser;

    public static int setTaskId(){
        return ++counterTask;
    }

    public static int setUserId(){
        return ++counterUser;
    }
    
}
