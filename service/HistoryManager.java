package service;

import exceptions.TaskNotFoundException;
import model.Task;

import java.util.List;

public interface HistoryManager {

    List<Integer> getHistory();

    default void addTaskToHistory(Integer taskId) throws TaskNotFoundException{

    }


}
