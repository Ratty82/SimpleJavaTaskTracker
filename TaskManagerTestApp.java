public class TaskManagerTestApp {

    public static void main(String[] args) {
        TaskManager tm = new TaskManager();

        //- Создайте 2 задачи, один эпик с 2 подзадачами, а другой эпик с 1 подзадачей.
        System.out.println("1. Создаем задачи и эпики");

        try {
        tm.createTask(new Task(null,"Тестовая задача 1", "Проверка метода createTask", TaskStatus.NEW,TaskType.TASK));
        tm.createTask(new Task(null,"Тестовая задача 2", "Проверка метода createTask", TaskStatus.NEW,TaskType.TASK));
        tm.createTask(new Task(null,"Тестовая задача 3", "Проверка метода createTask", TaskStatus.NEW,TaskType.TASK));
        tm.createTask(new Task(null,"Тестовая задача 4", "Проверка метода createTask", TaskStatus.NEW,TaskType.TASK));
        tm.createTask(new Task(null,"Тестовая задача 5", "Проверка метода createTask", TaskStatus.NEW,TaskType.TASK));      
        tm.createTask(new Epic(null,"Тестовый эпик 1","Проверка создания эпика",TaskStatus.DONE,TaskType.EPIC));
        tm.createTask(new Epic(null,"Тестовый эпик 2","Проверка создания эпика",TaskStatus.DONE,TaskType.EPIC));}

        catch (IllegalArgumentException e) {System.out.println("Ошибка : " + e.getMessage());}
        catch (TaskAlreadyExistException e) {System.out.println("Ошибка : " + e.getMessage());}
        finally{
            System.out.println("Список созданных задач: " + tm.getAllTasks());}

        System.out.println("");
        System.out.println("2. Ищем задачи и включаем их в эпики");

        try {}


        

    }
    
}
