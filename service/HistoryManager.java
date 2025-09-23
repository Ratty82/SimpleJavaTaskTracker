package service;

import exceptions.TaskNotFoundException;
import model.Task;

import java.util.List;

public interface HistoryManager {

    List<Task> getHistory();

    default void addTaskToHistory(Task task) throws TaskNotFoundException{

    }


}
