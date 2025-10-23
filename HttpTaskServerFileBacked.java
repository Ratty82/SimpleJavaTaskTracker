import com.google.gson.Gson;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import dto.TaskDto;
import exceptions.TaskNotFoundException;
import model.Epic;
import model.SubTask;
import model.Task;
import service.FileBackedTasksManager;
import service.HistoryManager;
import service.Managers;
import util.DtoVars;
import util.GenId;
import util.TaskType;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class HttpTaskServerFileBacked {
    //Создаем экземпляры менеджеров для работы с тасками и дополнительные утилиты
    static HistoryManager hm =Managers.getDefaultHistory();;
    static FileBackedTasksManager ftm = Managers.getDefautFileBacked(hm, Path.of("D:\\JAVA_Projects\\java_course\\SimpleJavaTaskTracker\\data.csv"));
    static Gson gson = new Gson();
    HttpServer server;

    public static void sendError(HttpExchange hex,Integer code,String body) throws IOException {
        byte[] bytesToSend = body.getBytes(StandardCharsets.UTF_8);
        hex.getResponseHeaders().set("Content-Type", "Application/JSON");
        hex.sendResponseHeaders(code,bytesToSend.length);
        OutputStream os = hex.getResponseBody();
        os.write(bytesToSend);
        os.close();
    }

    public static void sendJson(HttpExchange hex,int code, Object body) throws IOException {
        byte[] bytesToSend = gson.toJson(body).getBytes(StandardCharsets.UTF_8);
        hex.getResponseHeaders().set("Content-Type", "Application/JSON");
        hex.sendResponseHeaders(code,bytesToSend.length);
        OutputStream os = hex.getResponseBody();
        os.write(bytesToSend);
        os.close();
    }

    public static void getTaskHandler(HttpExchange he) throws IOException, TaskNotFoundException {
    //отсылаю запросы http://localhost:8080/tasks?id=1 - получить задачу по Id; http://localhost:8080/tasks - получить все задачи
        String query = he.getRequestURI().getQuery();
        if ( query == null ) {
            System.out.println("Request: " + he.getRequestURI().getPath());
            try {
                 sendJson(he, 200,ftm.getAllTasks());
             } catch (Exception e) {
                 sendError(he, 404, "Tasks not found");
             }
        } else {
            System.out.println("Requested task: " + query);
            try {
                 Integer taskId = Integer.parseInt(query.split("=")[1]);
                 TaskType taskType = ftm.getTaskType(taskId);
                 if (taskType == TaskType.TASK) {
                     sendJson(he, 200,ftm.findTaskByID(taskId, Task.class));
                 } else if (taskType == TaskType.EPIC) {
                     sendJson(he, 200,ftm.findTaskByID(taskId, Epic.class));
                 } else if (taskType == TaskType.SUBTASK) {
                     sendJson(he, 200, ftm.findTaskByID(taskId, SubTask.class));
                 }
             } catch (Exception e) {
                 sendError(he, 404, "Task not found" );
             }
        }
    }

    public static void postTaskHandler(HttpExchange he) throws IOException {
        // отсылаю body вида { "taskName": "Тестовый task 1", "taskDetails": "Проверка метода createTask", "taskType": "TASK" }
        String body = new String(he.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        TaskDto dto = gson.fromJson(body, TaskDto.class);
        Integer newId = GenId.setTaskId();
        switch (dto.getTaskType()) {
            case TASK -> {
                try {
                    ftm.createTask(new Task(newId, dto.getTaskName(), dto.getTaskDetails(), dto.getTaskStatus(), TaskType.TASK));
                    sendJson(he, 201, ftm.findTaskByID(newId, Task.class));
                } catch (Exception e) {
                    sendError(he, 404, "Problem creating task :" + e.getMessage());
                }
            }
            case EPIC -> {
                try {
                    ftm.createTask(new Epic(newId, dto.getTaskName(), dto.getTaskDetails(), dto.getTaskStatus(), TaskType.TASK));
                    sendJson(he, 201, ftm.findTaskByID(newId, Epic.class));
                } catch (Exception e) {
                    sendError(he, 404, "Problem creating epic :" + e.getMessage());
                }
            }
            case SUBTASK -> {
                sendError(he, 400, "Subtask is not allowed");
            }
        }
    }

    public static void putTaskHandler(HttpExchange he) throws IOException {
        // для обновления отсылаю body вида { "taskId": 1, "taskName" "Тестовый task 1", "taskDetails": "Проверка метода createTask" , "taskStatus": "NEW"}
        // для включения в эпик отсылаю query вида /tasks?id=1&id=4
        String body = new String(he.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        String query = he.getRequestURI().getQuery();
        System.out.println(query);
        if ( query == null ) {
            try {
                TaskDto dto = gson.fromJson(body, TaskDto.class);
                switch (DtoVars.getVariant(dto.getTaskType(),ftm.getTaskType(dto.getTaskId()))) {
                    case TASK -> {
                        Task taskToUpdate = ftm.findTaskByID(dto.getTaskId(), Task.class);
                        Task updated = ftm.updateTask(new Task(taskToUpdate.getTaskId(),
                                                               DtoVars.getVariant(dto.getTaskName(),taskToUpdate.getTaskName()),
                                                               DtoVars.getVariant(dto.getTaskDetails(),taskToUpdate.getTaskDetails()),
                                                               DtoVars.getVariant(dto.getTaskStatus(),taskToUpdate.getTaskStatus()),
                                                               TaskType.TASK),
                                                               Task.class);
                        sendJson(he, 202, updated);
                    }
                    case EPIC -> {
                        Epic epicToUpdate = ftm.findTaskByID(dto.getTaskId(), Epic.class);
                        Epic updated = ftm.updateTask(new Epic(epicToUpdate.getTaskId(),
                                                          DtoVars.getVariant(dto.getTaskName(),epicToUpdate.getTaskName()),
                                                          DtoVars.getVariant(dto.getTaskDetails(),epicToUpdate.getTaskDetails()),
                                                          epicToUpdate.getTaskStatus(),
                                                          TaskType.EPIC,
                                                          epicToUpdate.getAllSubtaskIds()),
                                                          Epic.class);
                        sendJson(he, 202, updated);
                    }
                    case SUBTASK -> {
                        SubTask subTaskToUpdate = ftm.findTaskByID(dto.getTaskId(), SubTask.class);
                        SubTask updated = ftm.updateTask(new SubTask(subTaskToUpdate.getTaskId(),
                                                             DtoVars.getVariant(dto.getTaskName(),subTaskToUpdate.getTaskName()),
                                                             DtoVars.getVariant(dto.getTaskDetails(),subTaskToUpdate.getTaskDetails()),
                                                             DtoVars.getVariant(dto.getTaskStatus(),subTaskToUpdate.getTaskStatus()),
                                                             TaskType.SUBTASK ,
                                                             subTaskToUpdate.getTaskParentId()),
                                                             SubTask.class);
                        sendJson(he, 202, updated);
                    }
                }
            } catch (Exception e) {
                sendError(he, 404, "Problem updating task :" + e.getMessage());
            }
        } else {
            try {
                List<Integer> idsEpicTask = Arrays.stream(query.split("&"))
                        .filter(s -> s.startsWith("id="))
                        .map(s -> s.substring(3))
                        .map(Integer::parseInt)
                        .toList();
                Task taskToInclude = ftm.findTaskByID(idsEpicTask.get(0), Task.class);
                Epic epicToIncludeIn = ftm.findTaskByID(idsEpicTask.get(1), Epic.class);
                ftm.includeTaskToEpic(taskToInclude,epicToIncludeIn);
                List<Task> result = List.of(ftm.findTaskByID(idsEpicTask.get(0), SubTask.class),ftm.findTaskByID(idsEpicTask.get(1), Epic.class));
                sendJson(he, 202, result);
            } catch (Exception e) {
                sendError(he, 404, "Problem including task :" + e.getMessage());
            }
         }
    }

    public static void deleteTaskHandler(HttpExchange he) throws IOException {
        // для удаления задач отсылаю query вида /tasks?id=1
        String query = he.getRequestURI().getQuery();
        if ( query != null ) {
            try {
                Integer taskId = Integer.parseInt(query.split("=")[1]);
                ftm.removeTaskById(taskId);
                sendJson(he, 204, null );
            } catch (Exception e) {
                sendError(he, 404, "Problem with deletion :" + e.getMessage());
            }
        } else {
            sendError(he, 400, "Request query is null - nothing to delete" );
        }
    }

    public static void getHistoryHandler(HttpExchange he) throws IOException {
        try {
            sendJson(he, 201, hm.getHistory());
        } catch (Exception e) {
            sendError(he, 404, "Problem with history :" + e.getMessage());
        }
    }


    public static void main(String[] args) throws IOException {
        HttpServer commonServer = HttpServer.create(new InetSocketAddress(8080), 0);

        commonServer.createContext("/tasks",exchange -> {
            try {
                switch (exchange.getRequestMethod()) {
                    case "GET" ->{getTaskHandler(exchange);}
                    case "POST" -> {postTaskHandler(exchange);}
                    case "PUT" -> {putTaskHandler(exchange);}
                    case "DELETE" -> {deleteTaskHandler(exchange);}
                    default -> sendError(exchange, 400, "Method not supported");
                }
            } catch (Exception e) {
                String body = "Task API Error:" + e.getMessage();
                sendError(exchange,500,body);
            }

        });

        commonServer.createContext("/history",exchange -> {
            try {
                switch (exchange.getRequestMethod()) {
                    case "GET" ->{getHistoryHandler(exchange);}
                    default -> sendError(exchange, 400, "Method not supported");
                }
            } catch (Exception e) {
                String body = "History API Error:" + e.getMessage();
                sendError(exchange,500,body);
            }
        });


        commonServer.start();

    }

}
