public class TaskManagerNew {

    /*//создание задачи или эпика с проверкой по типу
    public Task createTask(String taskName, String taskDetails, TaskStatus taskStatus, TaskType taskType){
        if (taskType == TaskType.TASK) { 
            Task newTask = new Task(null, taskName, taskDetails, taskStatus, taskType);
            return newTask;
        }
        if (taskType == TaskType.EPIC) {
            Epic newTask = new Epic(null,taskName, taskDetails, taskStatus, taskType);
            setEpicStatus(newTask);
            return newTask;
        }        
    }

    //добавить или обновить задачу в каталоге
    public void addUpdateTask(Task task) throws IllegalArgumentException{
        if (task == null) {throw new IllegalArgumentException("Добавляемая или обновляемая задача не может быть null");}                                  
        tasks.put(task.getTaskId(),task);
    }

    //найти задачу по ID
    public <T extends Task> T findTaskByID(Integer taskId) throws TaskNotFoundException,IllegalArgumentException {
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
                if (taskId == null || taskId < 0) {throw new IllegalArgumentException("ID задачи не может быть null или отрицательным");}
                tasks.remove(taskId);
                break;
            case TYPE:
                tasks.values().removeIf(t -> t.getTaskType() == taskType);
                break;
        }

    }

    //обновить данные задачи: принять аргументы, создать новую задачу с измененными данными
    public void updateTaskData(Integer taskId, String taskName, String taskDetail, TaskStatus taskStatus, TaskType taskType) throws IllegalArgumentException {
        if (taskId == null || taskId < 0) {
            throw new IllegalArgumentException("ID задачи не может быть null или отрицательным");
        }
        Task taskToEdit = findTaskByID(taskId);
        String newTaskName = (taskName == null) ? taskToEdit.getTaskName() : taskName;
        String newTaskDetail = (taskDetail == null) ? taskToEdit.getTaskDetails() : taskDetail;
        TaskStatus newTaskStatus = (taskStatus == null) ? taskToEdit.getTaskStatus() : taskStatus;
        TaskType newTaskType = (taskType == null) ? taskToEdit.getTaskType() : taskType; 
        if (taskToEdit.getTaskType() == TaskType.TASK) {
            tasks.put(taskToEdit.getTaskId(),new Task(taskId,newTaskName, newTaskDetail, newTaskStatus, newTaskType));
        }
        if (taskToEdit.getTaskType() == TaskType.EPIC) {
            HashSet<Integer> newSubTasks = new HashSet<>(getAllEpicSubtasks(findTaskByID(taskId)).keySet());
            tasks.put(taskToEdit.getTaskId(),new Epic(taskId,newTaskName, newTaskDetail, newTaskStatus, newTaskType,newSubTasks));
        }
        if (taskToEdit.getTaskType() == TaskType.SUBTASK) {
            Integer newParentTask = taskToEdit.getTaskParentId();
            tasks.put(taskToEdit.getTaskId(),new SubTask(taskId,newTaskName, newTaskDetail, newTaskStatus, newTaskType, newParentTask));
        } 
         
    }
    
    //Определить все подзадачи эпика
    public Map<Integer, SubTask> getAllEpicSubtasks(Epic epic) {
        Map<Integer, SubTask> allSubTasks = new HashMap<>();
        allSubTasks = tasks.entrySet().stream()
            .filter(n -> n.getValue().getTaskParentId() == epic.getTaskId())
            .collect(Collectors.toMap(Map.entry::getKey, e -> e.getValue()));
        return allSubTasks;
    }


    //Определить статус эпика
    /*- b. Для эпиков:
    - если у эпика нет подзадач или все они имеют статус NEW, то статус должен быть NEW.
    - если все подзадачи имеют статус DONE, то и эпик считается завершённым — со статусом DONE.
    - во всех остальных случаях статус должен быть IN_PROGRESS.*/ 
    /* 
    public void setEpicStatus(Epic epic){
        if (epic.getAllSubtaskIds().isEmpty() || getAllEpicSubtasks(epic).values().stream().allMatch(t -> t.getTaskStatus() == TaskStatus.NEW)) {
            epic.TaskStatus = TaskStatus.NEW; 
        }
        if (getAllEpicSubtasks(epic).values().stream().allMatch(t -> t.getTaskStatus() == TaskStatus.DONE)) {
            epic.TaskStatus = TaskStatus.DONE;
        }
        epic.TaskStatus = TaskStatus.IN_PROGRESS;
    }


    //добавить задачу в эпик
    public void includeTaskToEpic(Task task,Epic epic) throws IllegalArgumentException, TaskAlreadyExistException {
        if (task == null) {throw new IllegalArgumentException("Добавляемая задача не может быть null");} 
        if (epic == null) {throw new IllegalArgumentException("Эпик не должен быть Null");}
        if (epic.getSubTaskById(task.getTaskId())) {throw new TaskAlreadyExistException(task.getTaskId);}
        epic.addSubtask(task.getTaskId());
        SubTask subTask = new SubTask(task.getTaskId(),task.getTaskName(),task.getTaskDetails(),task.getTaskStatus(),TaskType.SUBTASK,epic.addSubtask(task.getTaskId()));
        tasks.put(task.getTaskId(),subTask);
    }

    /*  
    //вывести все задачи эпика
    public List<SubTask> getSubTasksOfEpic(Epic epic) throws IllegalArgumentException {
        if (epic == null) {throw new IllegalArgumentException("Эпик не должен быть Null");}
        return epic.getSubTaskIds().stream()
                   .map(tasks::get)        
                   .filter(Objects::nonNull) 
                   .toList();
    }

    //вывести подзадачу эпика по id
    public Optional<SubTask> getSubtaskFromEpic(Epic epic, int taskId, Map<Integer, Task> tasks) {
        if (!epic.getSubTaskIds().contains(taskId)) return Optional.empty();
        Task task = tasks.get(taskId);
    return (task instanceof SubTask subTask) ? Optional.of(subTask) : Optional.empty();
    }

    //создание пользователя
    public User createUser(String userName, String userPosition) {
        User user = new User(setUserName(userName),setUserPosition(userPosition));
        
        return user;
    }

    //добавление или обновление пользователя в каталог
    public void addUser(User user) throws IllegalArgumentException {
        if (user == null) {throw new IllegalArgumentException("Пользователь не может быть null");}
        users.put(user.getUserId(),user);
    }

    //назначить задачу пользователю
    public void assignTaskToUser(Task task,User user) throws IllegalArgumentException, TaskAlreadyExistException {
        if (task == null) {throw new IllegalArgumentException("Добавляемая задача не может быть null");} 
        if (user == null) {throw new IllegalArgumentException("Пользователь не должен быть Null");}
        if (user.getSubTaskById(task.getTaskId())) {throw new TaskAlreadyExistException(task.getTaskId);}
        user.addAssignedtask(task.getTaskId());
    }

    //вывести все задачи пользователя
    public List<Task> getAssignedTasksOfUser(User user) throws IllegalArgumentException {
        if (user == null) {throw new IllegalArgumentException("Пользователь не должен быть Null");}
        return user.getAsignedTaskIds().stream()
                   .map(tasks::get)        
                   .filter(Objects::nonNull) 
                   .toList();
    }

    //вывести назначенную пользователю задачу по id
    public Optional<Task> getAssignedTaskFromUser(User user, int taskId, Map<Integer, Task> tasks) {
        if (!user.getSubTaskIds().contains(taskId)) return Optional.empty();
        Task task = tasks.get(taskId);
    return (task) ? Optional.of(task) : Optional.empty();
    }*/
    
    
}
