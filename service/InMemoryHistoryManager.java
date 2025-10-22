package service;

import exceptions.TaskNotFoundException;
import model.Task;
import util.TaskType;

import java.util.ArrayList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager{

    private final List<Integer> history = new ArrayList<>();

    @Override
    public List<Integer> getHistory() {
        return history;
    }

    @Override
    //добавить задачу в историю
    public void addTaskToHistory(Integer taskId) throws TaskNotFoundException {
        if (taskId == null) {
            throw new TaskNotFoundException(-1);
        }
        else {
            if (history.size() < 10) {
                history.add(taskId);
            }
            else {
                history.removeFirst();
                history.add(taskId);
            }
        }
    }

}
