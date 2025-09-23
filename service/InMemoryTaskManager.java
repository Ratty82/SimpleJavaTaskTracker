package service;

import exceptions.TaskAlreadyExistException;
import exceptions.TaskNotFoundException;
import model.Epic;
import model.SubTask;
import model.Task;
import util.TaskStatus;
import util.TaskType;

import java.util.*;

public class InMemoryTaskManager implements TaskManager {
    private Map<Integer, Task> tasks = new HashMap<>();
    private HistoryManager history;

    public InMemoryTaskManager(HistoryManager history) {
        this.history = history;
    }


    //a. Получение списка всех задач.
    @Override
    public List<Task> getAllTasks()
    {
        return tasks.values().stream().toList();
    }

    //b. Удаление всех задач.
    @Override
    public void removeAllTasks() {
        tasks.clear();
    }

    //c. Получение по идентификатору.
    @Override
    public <T extends Task> T findTaskByID(Integer taskId, Class<T> type) throws TaskNotFoundException, IllegalArgumentException {
        if (taskId == null || taskId < 0) {
            throw new IllegalArgumentException("ID не должен быть null или отрицательным");
        }
        Task t = tasks.get(taskId);
        if (t == null) {
            throw new TaskNotFoundException(taskId);
        }
        else {
            history.addTaskToHistory(t);
            return type.cast(t);
        }
    }


    //d. Создание. Сам объект должен передаваться в качестве параметра.
    @Override
    public void createTask(Task task) throws IllegalArgumentException, TaskAlreadyExistException {
        if (task == null) {
            throw new IllegalArgumentException("Задача не должна быть Null");
        }
        if (tasks.containsKey(task.getTaskId())) {
            throw new TaskAlreadyExistException(task.getTaskId());
        }
        if (task instanceof Epic) {
            tasks.put(task.getTaskId(),setEpicStatus((Epic) task));
        }
        else {
            tasks.put(task.getTaskId(),task);
        }
    }

    //- e. Обновление. Новая версия объекта с верным идентификатором передаётся в виде параметра.
    @Override
    public <T extends Task> T updateTask(T task, Class<T> type) throws IllegalArgumentException, TaskNotFoundException {
        if (task == null) {
            throw new IllegalArgumentException("Задача не должна быть null");
        }
        Task old = tasks.get(task.getTaskId());
        if (old == null) {
            throw new TaskNotFoundException(task.getTaskId());
        }
        if (task instanceof Epic epic) {
            T updated = type.cast(setEpicStatus(epic));
            tasks.put(task.getTaskId(), updated);
            return updated;
        }
        if (task instanceof SubTask subTask) {
            T updated = type.cast(subTask);
            tasks.put(task.getTaskId(), updated);
            Integer parentId = subTask.getTaskParentId();
            Epic epicToUpdate = findTaskByID(parentId, Epic.class);
            tasks.put(parentId, setEpicStatus(epicToUpdate));
            return updated;
        }
        else {
            T updated = type.cast(task);
            tasks.put(task.getTaskId(), updated);
            return updated;
        }
    }


    //f. Удаление по идентификатору.
    @Override
    public void removeTaskById(Integer taskId) throws IllegalArgumentException,TaskNotFoundException {
        if (taskId == null || taskId < 0) {
            throw new IllegalArgumentException("ID не должен быть Null или отрицательным");
        }
        Task task = tasks.get(taskId);
        if (task == null) {
            throw new TaskNotFoundException(taskId);
        }
        if (task instanceof SubTask sub) {
            Integer parentId = sub.getTaskParentId();
            Epic epicToUpdate = findTaskByID(parentId, Epic.class);
            HashSet<Integer> subTasks = epicToUpdate.getAllSubtaskIds();
            tasks.remove(taskId);
            subTasks.remove(taskId);
            Epic updatedEpic = new Epic(epicToUpdate.getTaskId(),epicToUpdate.getTaskName(),epicToUpdate.getTaskDetails(),epicToUpdate.getTaskStatus(),epicToUpdate.getTaskType(),subTasks);
            setEpicStatus(updatedEpic);
        } 
        if (task instanceof Epic epic){
            HashSet<Integer> subTasks = epic.getAllSubtaskIds();
            tasks.remove(taskId);
            tasks.keySet().removeAll(subTasks);
        }
        else {
        tasks.remove(taskId);
        }
    }

    //- a. Получение списка всех подзадач определённого эпика.
    @Override
    public List<SubTask> getAllSubTasks(Epic epic) {
        return tasks.values().stream()
            .filter(t -> t instanceof SubTask subTask  && subTask.getTaskParentId().equals(epic.getTaskId()))
            .map(t -> (SubTask) t)
            .toList();
    }

    /*- b. Для эпиков:
    - если у эпика нет подзадач или все они имеют статус NEW, то статус должен быть NEW.
    - если все подзадачи имеют статус DONE, то и эпик считается завершённым — со статусом DONE.
    - во всех остальных случаях статус должен быть IN_PROGRESS.*/ 

    @Override
    public Epic setEpicStatus(Epic epic){
        if (epic.getAllSubtaskIds().isEmpty() || getAllSubTasks(epic).stream().allMatch(t -> t.getTaskStatus() == TaskStatus.NEW)) {
            return new Epic(epic.getTaskId(),epic.getTaskName(),epic.getTaskDetails(),TaskStatus.NEW, TaskType.EPIC,epic.getAllSubtaskIds());
        }
        if (getAllSubTasks(epic).stream().allMatch(t -> t.getTaskStatus() == TaskStatus.DONE)) {
            return new Epic(epic.getTaskId(),epic.getTaskName(),epic.getTaskDetails(),TaskStatus.DONE,TaskType.EPIC,epic.getAllSubtaskIds());
        }
        else {
            return new Epic(epic.getTaskId(),epic.getTaskName(),epic.getTaskDetails(),TaskStatus.IN_PROGRESS,TaskType.EPIC,epic.getAllSubtaskIds());
        }
    }
    
    
    //добавить задачу в эпик
    @Override
    public void includeTaskToEpic(Task task,Epic epic) throws IllegalArgumentException, TaskAlreadyExistException {
        if (task == null) {
            throw new IllegalArgumentException("Добавляемая задача не может быть null");
        }
        if (epic == null) {
            throw new IllegalArgumentException("Эпик не должен быть Null");
        }
        if (epic.checkSubTaskById(task.getTaskId())) {
            throw new TaskAlreadyExistException(task.getTaskId());
        }
        HashSet<Integer> newSubTasks = new HashSet<>();
        newSubTasks = epic.getAllSubtaskIds();
        newSubTasks.add(task.getTaskId());
        Epic epicToUpdate = new Epic(epic.getTaskId(),epic.getTaskName(),epic.getTaskDetails(),epic.getTaskStatus(),epic.getTaskType(),newSubTasks);
        tasks.put(epic.getTaskId(), setEpicStatus(epicToUpdate));
        SubTask newSub = new SubTask(task.getTaskId(), task.getTaskName(), task.getTaskDetails(), task.getTaskStatus(), TaskType.SUBTASK,epic.getTaskId());
        tasks.put(newSub.getTaskId(),newSub);
    }


}
