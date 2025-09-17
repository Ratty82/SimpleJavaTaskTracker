public class TaskManagerTestApp {

    public static void main(String[] args) {
        TaskManager tm = new TaskManager();

        Task createdTask = tm.createTask(new Task("Тестовая задача", "Проверка метода createTask", setTaskStatus(TaskStatus.NEW),setTaskType(TaskType.TASK)));

        System.out.println("Создана задача: " + createdTask.toString());


    }
    
}
