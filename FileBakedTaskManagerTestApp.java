import exceptions.TaskAlreadyExistException;
import exceptions.TaskNotFoundException;
import model.Epic;
import model.SubTask;
import model.Task;
import service.FileBackedTasksManager;
import service.HistoryManager;
import service.Managers;
import service.TaskManager;
import util.GenId;
import util.TaskStatus;
import util.TaskType;
import java.nio.file.Path;


public class FileBakedTaskManagerTestApp {
    public static void main(String[] args) {
        HistoryManager hm = Managers.getDefaultHistory();
        FileBackedTasksManager ftm = Managers.getDefautFileBacked(hm,Path.of("D:\\JAVA_Projects\\java_course\\SimpleJavaTaskTracker\\data.csv"));
        System.out.println("Список загруженных задач: ");
        ftm.getAllTasks().forEach(System.out::println);
        //System.out.println("Список загруженной истории: ");
        //hm.getHistory().forEach(System.out::println);
        System.out.println("Максимальный ID задачи: ");
        System.out.println(GenId.getCounterTask());
        try {
            ftm.createTask(new Task(null, "Тестовый task 1", "Проверка метода createTask", TaskStatus.NEW, TaskType.TASK));
            //ftm.createTask(new Task(null, "Тестовый task 2", "Проверка метода createTask", TaskStatus.NEW, TaskType.TASK));
            //ftm.createTask(new Task(null, "Тестовый task 3", "Проверка метода createTask", TaskStatus.NEW, TaskType.TASK));
            //ftm.createTask(new Task(null, "Тестовый task 4", "Проверка метода createTask", TaskStatus.NEW, TaskType.TASK));
            ftm.createTask(new Epic(null, "Тестовый epic 1", "Проверка метода createTask", TaskStatus.NEW, TaskType.EPIC));
        }
        catch (IllegalArgumentException | TaskAlreadyExistException e) {System.out.println("Ошибка : " + e.getMessage());} finally{
            System.out.println("Список задач: ");
            ftm.getAllTasks().forEach(System.out::println);
        }

        try {
            ftm.findTaskByID(2,Task.class);
            //ftm.findTaskByID(5,Task.class);
            //ftm.findTaskByID(2,Task.class);
            ftm.includeTaskToEpic(ftm.findTaskByID(4,Task.class) ,ftm.findTaskByID(6, Epic.class));
        }
        catch (IllegalArgumentException e) {System.out.println("Ошибка : " + e.getMessage());}
        catch (TaskNotFoundException e) {System.out.println("Ошибка : " + e.getMessage());}
        catch (TaskAlreadyExistException e) {System.out.println("Ошибка : " + e.getMessage());}
        finally{
            System.out.println("Список задач: ");
            ftm.getAllTasks().forEach(System.out::println);
            System.out.println("История: ");
            hm.getHistory().forEach(System.out::println);
        }



    }


}
