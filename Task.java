public class Task {
    private int taskId;
    private String taskName;
    private String taskDetails;
    private TaskStatus taskStatus;
    
    public Task(String taskName, String taskDetails, TaskStatus taskStatus) {
        this.taskId = GenId.setTaskId();
        setTaskName(taskName);
        this.taskDetails = taskDetails;
        this.taskStatus = taskStatus;
    }
    
    public void setTaskName(String name) throws IllegalArgumentException{
        if (!name.trim().isEmpty()) {
            this.taskName = name;
        } else {throw new IllegalArgumentException("Название задачи задано некорректно");}
    }

    public void setTaskDetails(String details) throws IllegalArgumentException{
        if (!details.trim().isEmpty()) {
            this.taskDetails = details;
        } else {throw new IllegalArgumentException("Описание задачи задано некорректно");}

    }

    public Integer getTaskId() {
        return taskId;
    }

    public String getTaskName() {
        return taskName;
    }

    public String getTaskDetails() {
        return taskDetails;
    }

    public TaskStatus getTaskStatus() {
        return taskStatus;
    }

    @Override
    public String toString() {
        return "[ ID = " + getTaskId() + ", Название = " + getTaskName() + ", Описание = " + getTaskDetails() + ", Статус = " + getTaskStatus() + "]";
    }

    


    
}
