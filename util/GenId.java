package util;

public class GenId {
    private static int counterTask;

    public static int setTaskId(){
        return ++counterTask;
    }
    
}
