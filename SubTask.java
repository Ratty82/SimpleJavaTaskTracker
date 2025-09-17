public class SubTask extends Task {
    private Integer parentTaskId;

    public SubTask(Integer taskId, String taskName, String taskDetails, TaskStatus taskStatus, TaskType taskType, Integer parentTaskId) {
        super(taskId, taskName, taskDetails, taskStatus,taskType);
        this.parentTaskId =  validateInteger(parentTaskId);
    }

    private static Integer validateInteger(Integer intv, String message) {
        if (intv == null || intv < 0) throw new IllegalArgumentException(message);
        return string;
    }
    
    public Integer getTaskParentId(){              
        return parentTaskId;
    }
    
}
