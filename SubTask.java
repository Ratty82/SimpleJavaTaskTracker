public class SubTask extends Task {
    private Integer parentTaskId;

    public SubTask(Integer taskId, String taskName, String taskDetails, TaskStatus taskStatus, TaskType taskType) {
        super(taskId, taskName, taskDetails, taskStatus,taskType);
        setSubTaskParentId(parentTaskId);
    }

    
    public void setSubTaskParentId(Integer id) throws IllegalArgumentException{
        if (id > 0 ) {
            this.parentTaskId = id;
        } else {throw new IllegalArgumentException("Epic ID не задан или меньше нуля");}

    }
    
    public Integer getTaskParentId(){              
        return parentTaskId;
    }
    
}
