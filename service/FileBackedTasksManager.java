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
            loadFromFile();
        } catch (IOException | TaskNotFoundException | TaskAlreadyExistException e) {
            System.out.println("Проблема с чтением из файла или файл не существует :" + e.getMessage());
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


    public Task fromString(String line) throws IllegalArgumentException, TaskNotFoundException {
        if (line == null || line.isBlank()) {
            throw new IllegalArgumentException("Строка не может быть пустой");
        } else {
            String[] parts = line.split(",", -1);
            Integer taskId = Integer.parseInt(parts[0].trim());
            String taskName = parts[1].trim();
            String taskDetails = parts[2].trim();
            TaskStatus taskStatus = parseTaskStatus(parts[3].trim());
            TaskType taskType = parseTaskType(parts[4].trim());
            if (taskType == TaskType.TASK ) {
                return new Task(taskId, taskName, taskDetails, taskStatus, taskType);
            } else if (taskType == TaskType.EPIC) {
                return new Epic(taskId, taskName, taskDetails, taskStatus, taskType);
            } else if (taskType == TaskType.SUBTASK) {
                Integer parentTaskId = Integer.parseInt(parts[5].trim());
                Epic epicToUpdate = findTaskByID(parentTaskId, Epic.class);
                epicToUpdate.addSubtask(taskId);
                updateTask(epicToUpdate,Epic.class);
                return new SubTask(taskId, taskName, taskDetails, taskStatus, taskType, parentTaskId);
            } else {
                return null;
            }
        }
    }

    public void loadFromFile() throws IOException, TaskNotFoundException, TaskAlreadyExistException {
       List<String> listTasks = Files.lines(saveFile, StandardCharsets.UTF_8)
                .skip(1)
                .takeWhile(s -> !s.isBlank())
                .toList();
       for (String l : listTasks) {
            Task t = fromString(l);
            createTaskNoSave(t);
        }
       int maxId = getAllTasks().stream()
                .mapToInt(Task::getTaskId)
                .max()
                .orElse(0);
       int maxIdInt = maxId;
       GenId.setCounterTask(maxIdInt);
       Optional<String> listHist = Files.lines(saveFile, StandardCharsets.UTF_8)
                .dropWhile(s -> !s.isBlank())
                .skip(1)
                .findFirst();
       if (listHist.isPresent() || !listHist.equals(Optional.empty())) {
            for (String l : listHist.get().split(",", -1)) {
                historyFromString(l);
            }
           System.out.println("Прочитанная история " + super.history.getHistory());
       }
       else {
           System.out.println("Прочитанная история пуста");
       }
    }

    public void save() throws ManagerSaveException {
        List<Task> sortedTasks = getAllTasks().stream()
                .sorted(
                        Comparator
                            .comparingInt((Task t) -> t.getClass() == SubTask.class ? 1 : 0)
                            .thenComparing(Task::getTaskId)
                )
                .toList();
        BufferedWriter wr = null;
        try {
            wr = Files.newBufferedWriter(saveFile, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new ManagerSaveException("Не удалось сохранить данные в файл: " + saveFile, e);
        }
        BufferedWriter finalWr = wr;
        try {
            finalWr.write("id,name,description,taskstatus,tasktype,epic");
            finalWr.newLine();
        } catch (IOException e) {
            throw new ManagerSaveException("Не удалось сохранить данные в файл: " + saveFile, e);
        }
        sortedTasks.stream()
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
        if (super.history.getHistory() != null) {
            String histId = super.history.getHistory().stream()
                    .map(Object::toString)
                    .collect(Collectors.joining(","));
            BufferedWriter finalWr1 = wr;
            try {
                finalWr1.write(histId);
                finalWr1.newLine();
            } catch (IOException e) {
                throw new ManagerSaveException("Не удалось сохранить данные в файл: " + saveFile, e);
            }
        } else {
            System.out.println("История пустая ");
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

    @Override
    public List<Task> getAllTasks()  {
        List<Task> tasks = super.getAllTasks();
        return tasks;
    }

    //поиск с сохранением истории
    @Override
    public <T extends Task> T findTaskByID(Integer taskId, Class<T> type) throws TaskNotFoundException, IllegalArgumentException {
        T t = super.findTaskByID(taskId, type);
        save();
        return t;
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


    //создать таск без сохранения
    public void createTaskNoSave(Task task) throws TaskAlreadyExistException {
        super.createTask(task);
    }

    //вернуть тип таска по ID


    public void historyFromString(String l) throws TaskAlreadyExistException, TaskNotFoundException {
        try {
            super.history.addTaskToHistory(Integer.parseInt(l.trim()));
        }
        catch (NumberFormatException e) {
            System.out.println("Невозможно конвертировать пустую строку");
        }
    }

    public TaskType getTaskType(Integer taskId) throws TaskNotFoundException {
        return super.tasks.get(taskId).getTaskType();
    }




}


