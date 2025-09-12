import java.util.HashMap;
import java.util.Map;

public class TaskManager {
    private Map<Integer, Task> tasks = new HashMap<>();
    private Map<Integer, User> users = new HashMap<>();

    //создание задачи или эпика с проверкой по типу
    public Task createTask(String taskName, String taskDetails, TaskStatus taskStatus, TaskType taskType){
        if (taskType == TaskType.TASK) { 
            Task newTask = new Task(setTaskName(taskName), setTaskDetails(taskDetails), setTaskStatus(taskStatus), setTaskType(taskType));
        }
        if (taskType == TaskType.EPIC) {
            Epic newTask = new Epic(setTaskName(taskName), setTaskDetails(taskDetails), setEpicStatus(), setTaskType(taskType));
        }

        retun newTask;
    }

    //добавить задачу в каталог
    public void addTask(Task task) throws TaskAlreadyExistException,IllegalArgumentException{
        if (task == null) {throw new IllegalArgumentException("Добавляемая задача не может быть null");}
        if (tasks.containsKey(task.getTaskId())) 
        {
                throw new TaskAlreadyExistException(task.getTaskId());
        }                                        
        tasks.put(task.getTaskId(),task);
    }

    //найти задачу по ID
    public Task findTaskByID(Integer taskId) throws TaskNotFoundException,IllegalArgumentException {
        if (taskId == null || taskId < 0) {throw new IllegalArgumentException("ID не должен быть Null или отрицательным");}
        else { Task foundTask = tasks.get(taskId);}    
        return foundTask;    
    }
        
    //удалить задачи с аргументами
    public void removeTasks(OperationType operationType,Integer taskId,TaskType taskType){
        switch(operationType) {
            case ALL:
                tasks.clear();
                break;
            case ONE:
                tasks.remove(taskId);
                break;
            case TYPE:
                
                break;
            
            default:
                break;


        }

    }
    


    

}
