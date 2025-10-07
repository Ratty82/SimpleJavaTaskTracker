package service;

import exceptions.ManagerSaveException;
import exceptions.TaskAlreadyExistException;
import exceptions.TaskNotFoundException;
import model.Epic;
import model.SubTask;
import model.Task;
import util.GenId;
import util.TaskStatus;
import util.TaskType;

import java.io.BufferedWriter;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class FileBackedTasksManager extends InMemoryTaskManager implements TaskManager {

    private final Path saveFile;


    public FileBackedTasksManager(HistoryManager history, Path saveFile) {
        super(history);
        this.saveFile = saveFile;
        try {
            super.createTaskStorage();
            loadFromFile();
        } catch (IOException | TaskNotFoundException | TaskAlreadyExistException e) {
            super.createTaskStorage();
        }
    }

    static TaskType parseTaskType(String s) {
        if (s == null) throw new IllegalArgumentException("Строка не может быть пустой");
        return switch (s.trim()) {
            case "Задача" -> TaskType.TASK;
            case "Эпик" -> TaskType.EPIC;
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


    public static Task fromString(String line) throws IllegalArgumentException {
        if (line == null || line.isBlank()) {
            throw new IllegalArgumentException("Строка не может быть пустой");
        } else {
            String[] parts = line.split(";", -1);
            Integer taskId = Integer.parseInt(parts[0].trim());
            String taskName = parts[1].trim();
            String taskDetails = parts[2].trim();
            TaskStatus taskStatus = parseTaskStatus(parts[3].trim());
            TaskType taskType = parseTaskType(parts[4].trim());
            if (taskType == TaskType.TASK) {
                return new Task(taskId, taskName, taskDetails, taskStatus, taskType);
            } else if (taskType == TaskType.EPIC) {
                HashSet<Integer> subTasks = parts[5].isEmpty()
                        ? new HashSet<>()
                        : Arrays.stream(parts[5].split(",", -1))
                        .map(n -> n.replaceAll("[\\[\\]]", "").trim())
                        .map(Integer::parseInt)
                        .collect(Collectors.toCollection(HashSet::new));
                return new Epic(taskId, taskName, taskDetails, taskStatus, taskType, subTasks);
            } else if (taskType == TaskType.SUBTASK) {
                Integer parentTaskId = Integer.parseInt(parts[5].trim());
                return new SubTask(taskId, taskName, taskDetails, taskStatus, taskType, parentTaskId);
            } else {
                return null;
            }
        }
    }

    public void loadFromFile() throws IOException, TaskNotFoundException, TaskAlreadyExistException {
        List<String> listHist = Files.lines(saveFile, StandardCharsets.UTF_8)
                .dropWhile(s -> !s.isBlank())
                .skip(1)
                .toList();
        List<String> listTasks = Files.lines(saveFile, StandardCharsets.UTF_8)
                .takeWhile(s -> !s.isBlank())
                .toList();
        for (String l : listTasks) {
            Task t = fromString(l);
            super.createTask(t);
        }
        int maxId = super.getAllTasks().stream()
                .mapToInt(Task::getTaskId)
                .max()
                .orElse(0);
        int maxIdInt = maxId;
        GenId.setCounterTask(maxIdInt);
        for (String l : listHist) {
            Task t = fromString(l);
            super.getHistory().addTaskToHistory(t);
        }
    }

    public void save() throws ManagerSaveException {
        BufferedWriter wr = null;
        try {
            wr = Files.newBufferedWriter(saveFile, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new ManagerSaveException("Не удалось сохранить данные в файл: " + saveFile, e);
        }
        BufferedWriter finalWr = wr;
        super.getAllTasks().stream()
                .forEach(task -> {
                    try {
                        finalWr.write(task.toCSV());
                        finalWr.newLine();
                    } catch (IOException e) {
                        throw new ManagerSaveException("Не удалось сохранить данные в файл: " + saveFile, e);
                    }
                });
        try {
            wr.newLine();
        } catch (IOException e) {
            throw new ManagerSaveException("Не удалось сохранить данные в файл: " + saveFile, e);
        }
        if (super.getHistory() != null) {
            HistoryManager historyToWrite = super.getHistory();
            BufferedWriter finalWr1 = wr;
            historyToWrite.getHistory().stream()
                    .forEach(task -> {
                        try {
                            finalWr1.write(task.toCSV());
                            finalWr1.newLine();
                        } catch (IOException e) {
                            throw new ManagerSaveException("Не удалось сохранить данные в файл: " + saveFile, e);
                        }
                    });
        }
        try {
            wr.close();
        } catch (IOException e) {
            throw new ManagerSaveException("Не удалось сохранить данные в файл: " + saveFile, e);
        }
    }

    //создание таска с сохранением
    @Override
    public void createTask(Task task) throws TaskAlreadyExistException {
        super.createTask(task);
        save();
    }

    //обновление с сохранением
    @Override
    public <T extends Task> T updateTask(T task, Class<T> type) throws IllegalArgumentException, TaskNotFoundException {
        T t = super.updateTask(task,type);
        save();
        return t;
    }

    //удаление с сохранением
    @Override
    public void removeTaskById(Integer taskId) throws IllegalArgumentException, TaskNotFoundException {
        super.removeTaskById(taskId);
        save();

    }

    //добавить задачу в эпик с сохранением
    @Override
    public void includeTaskToEpic(Task task, Epic epic) throws IllegalArgumentException, TaskAlreadyExistException {
        super.includeTaskToEpic(task, epic);
        save();

    }

}


