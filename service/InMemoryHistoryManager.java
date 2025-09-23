package service;

import exceptions.TaskNotFoundException;
import model.Task;

import java.util.ArrayList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager{

    private final List<Task> history = new ArrayList<>();

    @Override
    public List<Task> getHistory() {
        return history;
    }

    @Override
    //добавить задачу в историю
    public void addTaskToHistory(Task task) throws TaskNotFoundException {
        if (task == null) {
            throw new TaskNotFoundException(-1);
        }
        else {
            if (history.size() < 10) {
                history.add(task);
            }
            else {
                history.removeFirst();
                history.add(task);
            }
        }
    }
}
