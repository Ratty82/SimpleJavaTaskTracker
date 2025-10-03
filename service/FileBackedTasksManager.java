package service;

import exceptions.TaskAlreadyExistException;
import exceptions.TaskNotFoundException;
import model.Epic;
import model.SubTask;
import model.Task;
import util.TaskStatus;
import util.TaskType;

import java.io.BufferedWriter;
import java.io.UncheckedIOException;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class FileBackedTasksManager extends InMemoryTaskManager implements TaskManager{

    private Map<Integer, Task> tasks;
    private final Path saveFile;
    private HistoryManager history;

    public FileBackedTasksManager(HistoryManager history, Path saveFile) {
        super(history);
        this.saveFile = saveFile;
        try {
            loadFromFile();
        }
        catch (IOException | TaskNotFoundException e) {
            tasks = new HashMap<>();
        }
    }

    static TaskType parseTaskType(String s) {
        if (s == null) throw new IllegalArgumentException("Строка не может быть пустой");
        return switch (s.trim()) {
            case "Задача"    -> TaskType.TASK;
            case "Эпик"      -> TaskType.EPIC;
            case "Подзадача" -> TaskType.SUBTASK;
            default -> throw new IllegalArgumentException("Неверное значение: " + s);
        };
    }

    static TaskStatus parseTaskStatus(String s) {
        if (s == null) throw new IllegalArgumentException("Строка не может быть пустой");
        return switch (s.trim()) {
            case "Новая" -> TaskStatus.NEW;
            case "В работе" -> TaskStatus.IN_PROGRESS;
            case "Готова" -> TaskStatus.DONE;
            default -> throw new IllegalArgumentException("Неверное значение: " + s);
        };
    }


    public static Task fromString(String line) throws IllegalArgumentException{
        if (line == null || line.isBlank() ) {
            throw new IllegalArgumentException("Строка не может быть пустой");
        }
        else {
            String[] parts = line.split(";", -1);
            Integer taskId = Integer.parseInt(parts[0].trim());
            String taskName = parts[1].trim();
            String taskDetails = parts[2].trim();
            TaskStatus taskStatus = parseTaskStatus(parts[3].trim());
            TaskType taskType = parseTaskType(parts[4].trim());
            if (taskType == TaskType.TASK) {
                return new Task(taskId, taskName, taskDetails, taskStatus, taskType);
            }
            else if (taskType == TaskType.EPIC) {
                HashSet<Integer> subTasks = parts[5].isEmpty()
                        ? new HashSet<>()
                        : Arrays.stream(parts[5].split(",", -1))
                                .map(n -> n.replaceAll("[\\[\\]]", "").trim())
                                .map(Integer::parseInt)
                                .collect(Collectors.toCollection(HashSet::new));
                return new Epic(taskId, taskName, taskDetails, taskStatus, taskType,subTasks);
            }
            else if (taskType == TaskType.SUBTASK) {
                Integer parentTaskId = Integer.parseInt(parts[5].trim());
                return new SubTask(taskId, taskName, taskDetails, taskStatus, taskType,parentTaskId);
            }
            else {
                return null;
            }
        }
    }

    public void loadFromFile() throws IOException, TaskNotFoundException {
        List<String> listHist = Files.lines(saveFile,StandardCharsets.UTF_8)
                .dropWhile(s -> !s.isBlank())
                .skip(1)
                .toList();
        List<String> listTasks = Files.lines(saveFile,StandardCharsets.UTF_8)
                .takeWhile(s -> !s.isBlank())
                .toList();
        for (String l : listTasks) {
            Task t = fromString(l);
            tasks.put(t.getTaskId(),t);
        }
        for (String l : listHist) {
            Task t = fromString(l);
            history.addTaskToHistory(t);
            }
        }

    public void save() throws IOException {
        BufferedWriter wr = Files.newBufferedWriter(saveFile, StandardCharsets.UTF_8,StandardOpenOption.CREATE,StandardOpenOption.TRUNCATE_EXISTING);
        tasks.values().stream()
                .forEach(task -> {
                    try {
                        wr.write(task.toCSV());
                        wr.newLine();
                    }
                    catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }});
        wr.newLine();
        history.getHistory().stream()
                .forEach(task -> {
                    try {
                        wr.write(task.toCSV());
                        wr.newLine();
                    }
                    catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }});
        wr.close();
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
            try {
                save();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

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
            try {
                save();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return updated;
        }
        if (task instanceof SubTask subTask) {
            T updated = type.cast(subTask);
            tasks.put(task.getTaskId(), updated);
            Integer parentId = subTask.getTaskParentId();
            Epic epicToUpdate = findTaskByID(parentId, Epic.class);
            tasks.put(parentId, setEpicStatus(epicToUpdate));
            try {
                save();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return updated;
        }
        else {
            T updated = type.cast(task);
            tasks.put(task.getTaskId(), updated);
            try {
                save();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return updated;
        }
    }

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
            try {
                save();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        if (task instanceof Epic epic){
            HashSet<Integer> subTasks = epic.getAllSubtaskIds();
            tasks.remove(taskId);
            tasks.keySet().removeAll(subTasks);
            try {
                save();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        else {
            tasks.remove(taskId);
            try {
                save();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }


    }




