public class User {
    private int userId;
    private String userName;
    private String userPosition;

    public user(String userName,String userPosition){
        this.userId = GenId.setUserId();
        setUserName(userName);
        setUserPosition(userPosition);
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

    @Override
    public String toString() {
        return "User [ID = " + getUserId() + ", Имя = " + getUserName() + ", Роль = " + getUserPosition() + "]";
    }

    




    
}
