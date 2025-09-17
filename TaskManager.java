import java.util.*;

public class TaskManager {
    private Map<Integer, Task> tasks = new HashMap<>();

    //a. Получение списка всех задач.
    public List<Task> getAllTasks() {
        return tasks.values().stream().toList();
    }

    //b. Удаление всех задач.
    public void removeAllTasks() {
        tasks.clear();
    }

    //c. Получение по идентификатору.
    public Task findTaskByID(Integer taskId) throws TaskNotFoundException,IllegalArgumentException {
        if (taskId == null || taskId < 0) {throw new IllegalArgumentException("ID не должен быть Null или отрицательным");}
        else { 
            if (tasks.containsKey(taskId)) {
                    return tasks.get(taskId);
            } else {throw new TaskNotFoundException(taskId);} }      
    }

    //d. Создание. Сам объект должен передаваться в качестве параметра.
    public void createTask(Task task) throws IllegalArgumentException,TaskAlreadyExistException {
        if (task == null) {throw new IllegalArgumentException("Задача не должна быть Null");}
        if (tasks.containsKey(task.getTaskId())) {throw new TaskAlreadyExistException(task.getTaskId());}
        if (task instanceof Epic) {tasks.put(task.getTaskId(),setEpicStatus((Epic) task));}
        else {tasks.put(task.getTaskId(),task);}
    }

    //- e. Обновление. Новая версия объекта с верным идентификатором передаётся в виде параметра.
    public void updateTask(Task task) throws IllegalArgumentException,TaskNotFoundException {
        if (task == null) {throw new IllegalArgumentException("Задача не должна быть Null");}
        if (!tasks.containsKey(task.getTaskId())) {{throw new TaskNotFoundException(task.getTaskId());}}
        if (task instanceof Epic) {tasks.put(task.getTaskId(),setEpicStatus((Epic) task));}
        else {tasks.put(task.getTaskId(),task);}
    }

    //f. Удаление по идентификатору.
    public void removeTaskById(Integer taskId) throws IllegalArgumentException,TaskNotFoundException {
        if (taskId == null || taskId < 0) {throw new IllegalArgumentException("ID не должен быть Null или отрицательным");}
        if (!tasks.containsKey(taskId)) {{throw new TaskNotFoundException(taskId);}}
        tasks.remove(taskId);
    }

    //- a. Получение списка всех подзадач определённого эпика.
    public List<SubTask> getAllSubTasks(Epic epic) {
        return tasks.values().stream()
            .filter(t -> t instanceof SubTask subTask  && subTask.getTaskParentId() == epic.getTaskId()) 
            .map(t -> (SubTask) t)
            .toList();

    }

    /*- b. Для эпиков:
    - если у эпика нет подзадач или все они имеют статус NEW, то статус должен быть NEW.
    - если все подзадачи имеют статус DONE, то и эпик считается завершённым — со статусом DONE.
    - во всех остальных случаях статус должен быть IN_PROGRESS.*/ 
     
    public Epic setEpicStatus(Epic epic){
        if (epic.getAllSubtaskIds().isEmpty() || getAllSubTasks(epic).stream().allMatch(t -> t.getTaskStatus() == TaskStatus.NEW)) {
            Epic newEpic = new Epic(epic.getTaskId(),epic.getTaskName(),epic.getTaskDetails(),TaskStatus.NEW,TaskType.EPIC,epic.getAllSubtaskIds());
            return newEpic;
        }
        if (getAllSubTasks(epic).stream().allMatch(t -> t.getTaskStatus() == TaskStatus.DONE)) {
            Epic newEpic = new Epic(epic.getTaskId(),epic.getTaskName(),epic.getTaskDetails(),TaskStatus.DONE,TaskType.EPIC,epic.getAllSubtaskIds());
            return newEpic;
        }
        Epic newEpic = new Epic(epic.getTaskId(),epic.getTaskName(),epic.getTaskDetails(),TaskStatus.IN_PROGRESS,TaskType.EPIC,epic.getAllSubtaskIds());
        return newEpic;
    }
    
    
    //добавить задачу в эпик
    public void includeTaskToEpic(Task task,Epic epic) throws IllegalArgumentException, TaskAlreadyExistException {
        if (task == null) {throw new IllegalArgumentException("Добавляемая задача не может быть null");} 
        if (epic == null) {throw new IllegalArgumentException("Эпик не должен быть Null");}
        if (epic.checkSubTaskById(task.getTaskId())) {throw new TaskAlreadyExistException(task.getTaskId());}
        Epic epicToUpdate = new Epic(epic.getTaskId(),epic.getTaskName(),epic.getTaskDetails(),epic.getTaskStatus(),epic.getTaskType(),epic.getAllSubtaskIds());
        tasks.put(epic.getTaskId(), setEpicStatus(epicToUpdate));
        SubTask newSub = new SubTask(task.getTaskId(), task.getTaskName(), task.getTaskDetails(), task.getTaskStatus(), TaskType.SUBTASK,epic.getTaskId());
        tasks.put(newSub.getTaskId(),newSub);
    }
    
}
