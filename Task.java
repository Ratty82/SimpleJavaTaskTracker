public class Task {
    private int taskId;
    private String taskName;
    private String taskDetails;
    private TaskStatus taskStatus;
    private TaskType taskType;
    
    public Task(Integer taskId,String taskName, String taskDetails, TaskStatus taskStatus, TaskType taskType) {
        this.taskId = (taskId == null) ? GenId.setTaskId() : taskId;
        setTaskName(taskName);
        setTaskDetails(taskDetails);
        setTaskStatus(taskStatus);
        setTaskType(taskType);
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

    public void setTaskStatus(TaskStatus taskStatus) {
    if (taskStatus == null) {
        throw new IllegalArgumentException("Статус не может быть null");
    }
    this.taskStatus = taskStatus;
}


    public void setTaskType(TaskType taskType) {
    if (taskType == null) {
        throw new IllegalArgumentException("Тип не может быть null");
    }
    this.taskType = taskType;
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

     public TaskType getTaskType() {
        return taskType;
    }

    @Override
    public String toString() {
        return "[ ID = " + getTaskId() + ", Название = " + getTaskName() + ", Описание = " + getTaskDetails() + ", Статус = " + getTaskStatus() + "]";
    }

   
    


    
}
