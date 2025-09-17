public class TaskManagerTestApp {

    public static void main(String[] args) {
        TaskManager tm = new TaskManager();

        //- Создайте 2 задачи, один эпик с 2 подзадачами, а другой эпик с 1 подзадачей.
        //- Распечатайте списки эпиков, задач и подзадач, через `System.out.println(..)`
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

        try {
            tm.includeTaskToEpic(tm.findTaskByID(1,Task.class) ,tm.findTaskByID(6, Epic.class));
            tm.includeTaskToEpic(tm.findTaskByID(2,Task.class) ,tm.findTaskByID(6, Epic.class));
            tm.includeTaskToEpic(tm.findTaskByID(3,Task.class) ,tm.findTaskByID(7, Epic.class));
        }
        catch (IllegalArgumentException e) {System.out.println("Ошибка : " + e.getMessage());}
        catch (TaskNotFoundException e) {System.out.println("Ошибка : " + e.getMessage());}
        catch (TaskAlreadyExistException e) {System.out.println("Ошибка : " + e.getMessage());}
        finally{
            System.out.println("Список созданных задач: " + tm.getAllTasks());}

        System.out.println("");
        //- Измените статусы созданных объектов, распечатайте. Проверьте, что статус задачи и подзадачи сохранился, а статус эпика рассчитался по статусам подзадач.
        System.out.println("3. Меняем статус задач");    

        try {
            SubTask taskToUpdate = tm.findTaskByID(1,SubTask.class);
            Task taskToUpdateNew = tm.findTaskByID(4,Task.class);
            tm.updateTask(new SubTask(taskToUpdate.getTaskId(), taskToUpdate.getTaskName(), taskToUpdate.getTaskDetails(), TaskStatus.IN_PROGRESS, TaskType.SUBTASK, taskToUpdate.getTaskParentId()), SubTask.class); 
            tm.updateTask(new Task(taskToUpdateNew.getTaskId(), taskToUpdateNew.getTaskName(), taskToUpdateNew.getTaskDetails(), TaskStatus.IN_PROGRESS, TaskType.TASK), Task.class); 
        }
        catch (IllegalArgumentException e) {System.out.println("Ошибка : " + e.getMessage());}
        catch (TaskNotFoundException e) {System.out.println("Ошибка : " + e.getMessage());}
        finally{
            System.out.println("Список созданных задач: " + tm.getAllTasks());}

        System.out.println("");
        System.out.println("4. Меняем статус задачи");   
        


    }
    
}
