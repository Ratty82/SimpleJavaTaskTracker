## Задание: добавление файлового менеджера задач с автосохранением

### 1\. Проблема

Сейчас `InMemoryTasksManager` хранит всё в оперативной памяти, поэтому после перезапуска приложения данные теряются. Нужно реализовать менеджер, который **автоматически сохраняет** состояние в файл.

---

### 2\. Новая реализация: `FileBackedTasksManager`

| Требование | Детали реализации |
| --- | --- |
| **Наследование** | `FileBackedTasksManager` наследуется от `InMemoryTasksManager`, чтобы повторно использовать готовую логику и избежать дублирования кода. |
| **Интерфейс** | Класс по-прежнему реализует интерфейс `TasksManager`. Отличия – только во внутренней «кухне» (сохранение/загрузка). |
| **Конструктор** | Получает объект `File` (или `Path`) и хранит его в приватном поле: `private final Path saveFile;` |

---

### 3\. Автосохранение

```java
@Override
public void addSubtask(Subtask subtask) {
    super.addSubtask(subtask); // логика родителя
    save();                    // + автосохранение
}
```

*Переопределите таким образом **все** методы, которые изменяют состояние менеджера  
(добавление, обновление, удаление задач, очистку истории и пр.).*

---

### 4\. Метод `save()`

1.  **Чем он занимается**

    -   Сериализует:

        -   все `Task`, `Epic`, `Subtask`;

        -   историю просмотров.

2.  **Формат** – CSV:

    ```csv
    id,type,name,status,description,epic
    1,TASK,Task1,NEW,Описание task1,
    2,EPIC,Epic2,DONE,Описание epic2,
    3,SUBTASK,Sub Task3,DONE,Описание subtask3,2
    
    2,3
    ```

    *Первая строка — заголовок;  
    далее*\* все задачи;  
    пустая строка;  
    последняя строка — id из истории (через запятую).\*

3.  **Обработка исключений**

    -   Любой `IOException` внутри `save()` перехватывается  
        и оборачивается в unchecked-класс `ManagerSaveException`  
        (чтобы не менять сигнатуру интерфейса).


---

### 5\. Вспомогательные методы

| Метод | Что делает |
| --- | --- |
| `enum TaskType { TASK, EPIC, SUBTASK }` | Хранит типы задач. |
| `String toString(Task task)` | Превращает объект задачи в CSV-строку. |
| `static Task fromString(String line)` | Создаёт объект из CSV-строки. |
| `static String historyToString(HistoryManager hm)` | Сериализует историю в строку (`"4,1,5"`). |
| `static List<Integer> historyFromString(String value)` | Десериализует историю. |

---

### 6\. Чтение файла

```java
String content = Files.readString(saveFile);
```

*Далее разбирайте строки и восстанавливайте состояние менеджера.*

---

### 7\. Загрузка: `loadFromFile`

```java
public static FileBackedTasksManager loadFromFile(Path file) {
    FileBackedTasksManager manager = new FileBackedTasksManager(file);
    // чтение, парсинг, восстановление history
    return manager;
}
```

---

### 8\. Самопроверка (метод `main`)

1.  Создайте несколько `Task`, `Epic`, `Subtask`.

2.  Запросите часть объектов, чтобы сформировать history.

3.  Завершите работу программы (сработает `save()`).

4.  Снова запустите приложение, вызвав `loadFromFile`.

5.  Убедитесь, что:

    -   все задачи восстановлены;

    -   история просмотров совпадает.


```java
public static void main(String[] args) {
    Path path = Paths.get("tasks.csv");
    FileBackedTasksManager manager = new FileBackedTasksManager(path);

    // 1. Создаём данные
    manager.addTask(new Task("Task1", "..."));
    Epic epic = new Epic("Epic1", "...");
    manager.addEpic(epic);
    manager.addSubtask(new Subtask("Sub1", "...", epic.getId()));

    // 2. Запрашиваем, чтобы появилась history
    manager.getTaskById(1);
    manager.getEpicById(epic.getId());

    // 3. Загружаем из файла
    FileBackedTasksManager restored = FileBackedTasksManager.loadFromFile(path);

    System.out.println("History ok? " + 
        restored.getHistory().equals(manager.getHistory()));
}
```

---

### 9\. Итог

*Вы получили*:

-   `FileBackedTasksManager` — менеджер с автосохранением.

-   `TaskType`, helpers `toString / fromString`, методы для истории.

-   `ManagerSaveException` для обработки I/O-ошибок.

-   Пример в `main`, демонстрирующий корректность сериализации и восстановления.


> Теперь данные трекера живут между перезапусками приложения, а код остался сухим и аккуратным благодаря наследованию и единообразному формату CSV.