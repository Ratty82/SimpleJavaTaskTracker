import java.util.HashMap;
import java.util.Map;

public class TaskManager {
    private Map<Integer, Task> tasks = new HashMap<>();
    private Map<Integer, User> users = new HashMap<>();

    //создание задачи или эпика с проверкой по типу
    public Task createTask(String taskName, String taskDetails, TaskStatus taskStatus, TaskType taskType){
        if (taskType == TaskType.TASK) { 
            Task newTask = new Task(null, setTaskName(taskName), setTaskDetails(taskDetails), setTaskStatus(taskStatus), setTaskType(taskType));
        }
        if (taskType == TaskType.EPIC) {
            Epic newTask = new Epic(null,setTaskName(taskName), setTaskDetails(taskDetails), setEpicStatus(), setTaskType(taskType));
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
        else { 
            if (tasks.containsKey(taskId)) {
                    return tasks.get(taskId);
            } else {throw new TaskNotFoundException(taskId);} }      
    }
        
    //удалить задачи с аргументами
    public void removeTasks(OperationType operationType,Integer taskId,TaskType taskType) throws IllegalArgumentException{
        switch(operationType) {
            case ALL:
                tasks.clear();
                break;
            case ONE:
                if (taskId == null || taskId < 0) {throw new IllegalArgumentException("ID задачи не может быть null или отрицательным")}
                tasks.remove(taskId);
                break;
            case TYPE:
                tasks.values().removeIf(t -> t.getTaskType() == taskType);
                break;
        }

    }

    //обновить задачу: принять аргументы, создать новую задачу с измененными данными, удалить старую задачу, внести в каталог новую
    public void updateTask(Integer taskId, String taskName, String taskDetail, TaskStatus taskStatus, TaskType taskType) throws IllegalArgumentException {
        if (taskId == null || taskId < 0) {
            throw new IllegalArgumentException("ID задачи не может быть null или отрицательным");
        }
        Task taskToEdit = findTaskByID(taskId);
        String newTaskName = (taskName == null) ? taskToEdit.getTaskName() : taskName;
        String newTaskDetail = (taskDetail == null) ? taskToEdit.getTaskDetails() : taskDetail;
        TaskStatus newTaskStatus = (taskStatus == null) ? taskToEdit.getTaskStatus() : taskStatus;
        TaskType newTaskType = (taskType == null) ? taskToEdit.getTaskType() : taskType; 
        if (taskToEdit.getTaskType() == TaskType.TASK) {
            tasks.remove(taskId);
            tasks.put(taskId,new Task(taskId,newTaskName, newTaskDetail, newTaskStatus, newTaskType));
        }
        if (taskToEdit.getTaskType() == TaskType.EPIC) {
            Map<Integer,SubTask> newSubTasks = taskToEdit.getAllSubtasks();
            tasks.remove(taskId);
            tasks.put(taskId,new Epic(taskId,newTaskName, newTaskDetail, newTaskStatus, newTaskType,newSubTasks));
        }
        if (taskToEdit.getTaskType() == TaskType.SUBTASK) {
            Integer newParentTask = taskToEdit.getTaskParentId();
            tasks.remove(taskId);
            tasks.put(taskId,new SubTask(taskId,newTaskName, newTaskDetail, newTaskStatus, newTaskType, newParentTask));
        } 
         
    }
    


    

}
