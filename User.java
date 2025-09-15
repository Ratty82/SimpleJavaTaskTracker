import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class User {
    private int userId;
    private String userName;
    private String userPosition;
    private LinkedHashSet<Integer> assignedTasks;

    public user(String userName,String userPosition){
        this.userId = GenId.setUserId();
        setUserName(userName);
        setUserPosition(userPosition);
        assignedTasks = new LinkedHashSet<>();
    }

    public void setUserName(String name) throws IllegalArgumentException{
        if (!name.trim().isEmpty()) {
            this.userName = name;
        } else {throw new IllegalArgumentException("Имя пользователя задано некорректно");}
    }

    public void setUserPosition(String position) throws IllegalArgumentException{
        if (!position.trim().isEmpty()) {
            this.userPosition = position;
        } else {throw new IllegalArgumentException("Роль пользователя задана некорректно");}
    }

    public int getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserPosition() {
        return userPosition;
    }

    public boolean getAssignedTaskById(Integer assignedTaskId){
        return assignedTasks.values().stream()
                .anyMatch(n -> n.equals(assignedTaskId));
    }


    public void addAssignedtask(Integer assignedTaskId){
        if (assignedTaskId == null || assignedTaskId < 0) {throw new IllegalArgumentException("ID не должен быть Null или отрицательным");}
        else {
            assignedTasks.add(assignedTaskId);
        }
    }

    public Set<Integer> getAsignedTaskIds() {
        return Collections.unmodifiableSet(assignedTasks); 
    }


    @Override
    public String toString() {
        return "User [ID = " + getUserId() + ", Имя = " + getUserName() + ", Роль = " + getUserPosition() + "]";
    }

    




    
}
