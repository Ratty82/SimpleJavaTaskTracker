import java.util.*;
import java.util.concurrent.StructuredTaskScope.Subtask;
import java.util.stream.*;

public class Epic extends Task {
    
    private LinkedHashSet<Integer> subTasks;

    public Epic(Integer taskId, String taskName, String taskDetails, TaskStatus taskStatus, TaskType taskType) {
        super(taskId,taskName, taskDetails, taskStatus,taskType);
        setEpicStatus();
        this.subTasks = new LinkedHashSet<>();
    }

     public Epic(Integer taskId, String taskName, String taskDetails, TaskStatus taskStatus, TaskType taskType, Map<Integer,SubTask> subTasks) {
        super(taskId,taskName, taskDetails, taskStatus,taskType);
        setEpicStatus();
        this.subTasks = subTasks;
    }

    //добавить ID задачи в подзадачи
    public void addSubtask(Integer subTaskId){
        if (subTaskId == null || subTaskId < 0) {throw new IllegalArgumentException("ID не должен быть Null или отрицательным");}
        else {
            subTasks.add(subTaskId);
        }
    }

    //Проверить есть ли ID в списке подзадач
    public boolean getSubTaskById(Integer subTaskId){
        return subTasks.values().stream()
                .anyMatch(n -> n.equals(subTaskId));
    }

    //Достать все подзадачи 
    public Set<Integer> getSubTaskIds() {
        return Collections.unmodifiableSet(subTasks); 
    }

    /*public Collection<SubTask> getAllSubtasks(){
        return subTasks.values().stream()
                .map(tasks::get)
                .toList();                
    }*/

    /*public SubTask getSubtaskById(Integer id){
        SubTask foundSubTask = subTasks.values().stream()
                .filter(SubTask -> SubTask.getTaskId().equals(id))
                .findFirst()
                .orElse(new SubTask(-1,setTaskName("FAIL ID"),setTaskDetails("FAIL ID"),FAIL));
        return foundSubTask;
    }*/

    /*public Collection getSubtasksByStatus(TaskStatus status){
        Collection foundSubTasks = subTasks.values().stream()
                   .filter(SubTask -> SubTask.getTaskStatus().equals(status))
                   .toList();
        return foundSubTasks;
    }*/

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
