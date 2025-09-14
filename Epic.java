import java.util.*;
import java.util.stream.*;

public class Epic extends Task {
    
    private Map<Integer,SubTask> subTasks;

    public Epic(Integer taskId, String taskName, String taskDetails, TaskStatus taskStatus, TaskType taskType) {
        super(taskId,taskName, taskDetails, taskStatus,taskType);
        setEpicStatus();
        this.subTasks = new HashMap<>();
    }

     public Epic(Integer taskId, String taskName, String taskDetails, TaskStatus taskStatus, TaskType taskType, Map<Integer,SubTask> subTasks) {
        super(taskId,taskName, taskDetails, taskStatus,taskType);
        setEpicStatus();
        this.subTasks = subTasks;
    }

    public Collection getAllSubtasks(){
        return subTasks.values();                
    }

    public SubTask getSubtaskById(Integer id){
        SubTask foundSubTask = subTasks.values().stream()
                .filter(SubTask -> SubTask.getTaskId().equals(id))
                .findFirst()
                .orElse(new SubTask(-1,setTaskName("FAIL ID"),setTaskDetails("FAIL ID"),FAIL));
        return foundSubTask;
    }

    public Collection getSubtasksByStatus(TaskStatus status){
        Collection foundSubTasks = subTasks.values().stream()
                   .filter(SubTask -> SubTask.getTaskStatus().equals(status))
                   .toList();
        return foundSubTasks;
    }

    /*- b. Для эпиков:
    - если у эпика нет подзадач или все они имеют статус NEW, то статус должен быть NEW.
    - если все подзадачи имеют статус DONE, то и эпик считается завершённым — со статусом DONE.
    - во всех остальных случаях статус должен быть IN_PROGRESS.*/

    public void setEpicStatus(){
        if (getAllSubtasks().isEmpty() || getAllSubtasks().equals(getSubtasksByStatus(TaskStatus.NEW))) {
            this.TaskStatus = TaskStatus.NEW; 
        }
        if (getAllSubtasks().equals(getSubtasksByStatus(TaskStatus.DONE))) {
            this.TaskStatus = TaskStatus.DONE;
        }
        this.TaskStatus = TaskStatus.IN_PROGRESS;
    }

    
}
