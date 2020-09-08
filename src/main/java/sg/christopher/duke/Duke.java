package sg.christopher.duke;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.image.Image;

import sg.christopher.duke.entities.Deadline;
import sg.christopher.duke.entities.Event;
import sg.christopher.duke.entities.Task;
import sg.christopher.duke.entities.Todo;
import sg.christopher.duke.io.DataManager;
import sg.christopher.duke.ui.DialogBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Duke extends Application {
    private ScrollPane scrollPane;
    private VBox dialogContainer;
    private TextField userInput;
    private Button sendButton;
    private Scene scene;
    private Image userImage = new Image(this.getClass().getResourceAsStream("/images/DaUser.png"));
    private Image dukeImage = new Image(this.getClass().getResourceAsStream("/images/DaDuke.png"));

    private static List<Task> savedItems = loadSavedItems();

    /**
     * Hydrates the task data store with saved data from disk.
     *
     * @return list of tasks that are saved on disk
     */
    public static List<Task> loadSavedItems() {
        List<Task> saved = DataManager.readList();
        return saved != null ? saved : new ArrayList<>();
    }

    /**
     * Saves a task to memory and writes it to disk.
     *
     * @param task the task to be saved
     */
    public static void saveItem(Task task) {
        savedItems.add(task);
        DataManager.writeList(savedItems);
    }

    /**
     * Loads saved tasks from disk, and get a specific task using its index.
     *
     * @param index index of the task to be retrieved
     * @return the task at that index
     */
    public static Task loadItem(int index) {
        savedItems = DataManager.readList();
        return savedItems.get(index);
    }

    private static void doneHandler(String userInput) {
        int taskNo;
        try {
            taskNo = Integer.parseInt(userInput.split(" ")[1]);
        } catch (ArrayIndexOutOfBoundsException aioobe) {
            System.out.println("ERROR: No task no. found. Did you input the task no. of the task you'd like to mark as done?");
            return;
        } catch (NumberFormatException nfe) {
            System.out.println("ERROR: Unrecognized task. Please input the task no. of the task you'd like to mark as done.");
            return;
        }
        Task task;
        try {
            task = loadItem(taskNo - 1);
        } catch (IndexOutOfBoundsException ioobe) {
            System.out.println("ERROR: Task no. not found. Does that task exist?");
            return;
        }
        task.markAsDone();

        System.out.println("Nice! I've marked this task as done:");
        System.out.println(taskNo + ". " + task);
    }

    private static void todoHandler(String userInput) {
        // Check for description
        if (userInput.split(" ").length < 2) {
            System.out.println("ERROR: Description of todo cannot be empty.");
            return;
        }
        String description = userInput.replaceFirst("todo ", "");
        Todo todo = new Todo(description);
        saveItem(todo);
        System.out.println("Got it. I've added this task:");
        System.out.println(todo);
        printRemainingCount();
    }

    private static void deadlineHandler(String userInput) {
        // Check for description
        if (userInput.split(" ").length < 2) {
            System.out.println("ERROR: Description of deadline cannot be empty.");
            return;
        }
        String[] input = userInput.replaceFirst("deadline ", "").split(" /by ");

        // Check for deadline in description
        if (input.length < 2) {
            System.out.println("ERROR: Deadline not found. Did you input a deadline with `/by`?");
            return;
        } else if (input.length > 2) {
            System.out.println("ERROR: Multiple deadlines found. Please only input one deadline.");
            return;
        }
        Deadline deadline = new Deadline(input[0], input[1]);
        saveItem(deadline);
        System.out.println("Got it. I've added this task:");
        System.out.println(deadline);
        printRemainingCount();
    }

    private static void eventHandler(String userInput) {
        // Check for description
        if (userInput.split(" ").length < 2) {
            System.out.println("ERROR: Description of event cannot be empty.");
            return;
        }
        String[] input = userInput.replaceFirst("event ", "").split(" /at ");

        // Check for dateTime in description
        if (input.length < 2) {
            System.out.println("ERROR: Date/time not found. Did you input a date/time with `/at`?");
            return;
        } else if (input.length > 2) {
            System.out.println("ERROR: Multiple date/times found. Please only input one date/time.");
            return;
        }

        Event event = new Event(input[0], input[1]);
        saveItem(event);
        System.out.println("Got it. I've added this task:");
        System.out.println(event);
        printRemainingCount();
    }

    private static void findHandler(String userInput) {
        // Check for search term
        if (userInput.split(" ").length < 2) {
            System.out.println("ERROR: Search term not found. Did you type a search term?");
            return;
        }
        String searchTerm = userInput.replaceFirst("find ", "").toLowerCase();

        savedItems = loadSavedItems();

        List<Task> foundTasks = savedItems.stream().filter(task -> task.getDescription().toLowerCase().contains(searchTerm)).collect(Collectors.toList());

        if (foundTasks.size() == 0) {
            System.out.println("No task matching your search term was found. Perhaps try another search term?");
            return;
        }
        System.out.println("Here are the matching tasks in your list:");
        for (int i = 0; i < foundTasks.size(); ++i) {
            Task task = foundTasks.get(i);
            System.out.println(i + 1 + ". " + task);
        }
    }

    private static void lsHandler() {
        if (savedItems.size() == 0) {
            System.out.println("No tasks found. Start adding your first few tasks!");
            return;
        }
        assert savedItems.size() >= 1;
        System.out.println("Here are the tasks in your list:");
        for (int i = 0; i < savedItems.size(); ++i) {
            Task task = loadItem(i);
            System.out.println(i + 1 + ". " + task);
        }
        printRemainingCount();
    }

    private static void deleteHandler(String userInput) {
        int taskNo;
        try {
            taskNo = Integer.parseInt(userInput.split(" ")[1]);
        } catch (ArrayIndexOutOfBoundsException aioobe) {
            System.out.println("ERROR: No task no. found. Did you input the task no. of the task you'd like to delete?");
            return;
        } catch (NumberFormatException nfe) {
            System.out.println("ERROR: Unrecognized task. Please input the task no. of the task you'd like to delete.");
            return;
        }

        Task task;
        try {
            task = savedItems.remove(taskNo - 1);
        } catch (IndexOutOfBoundsException ioobe) {
            System.out.println("ERROR: Task no. not found. Does that task exist?");
            return;
        }

        System.out.println("Noted. I've removed this task:");
        System.out.println(taskNo + ". " + task);
        printRemainingCount();
    }

    private static void printRemainingCount() {
        System.out.println("You now have " + savedItems.size() + " tasks in the list.");
    }

    private static CommandType getCommandType(String command) {
        switch (command) {
        case "bye":
            // Fallthrough
        case "exit":
            return CommandType.EXIT;
        case "todo":
            return CommandType.TODO;
        case "deadline":
            return CommandType.DEADLINE;
        case "event":
            return CommandType.EVENT;
        case "find":
            return CommandType.FIND;
        case "rm":
            // Fallthrough
        case "delete":
            return CommandType.DELETE;
        case "done":
            return CommandType.DONE;
        case "ls":
            return CommandType.LIST;
        default:
            return CommandType.UNRECOGNISED;
        }
    }

    public static void main(String[] args) {
        System.out.println("Hello I'm Duke!");
        System.out.println("What can I do for you?");

        String userInput;
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("$ ");
            userInput = scanner.nextLine();
            String command = userInput.split(" ")[0];

            CommandType commandType = getCommandType(command);

            switch (commandType) {
            case EXIT:
                System.out.println("Bye. Hope to see you again soon!");
                return;
            case TODO:
                todoHandler(userInput);
                break;
            case DEADLINE:
                deadlineHandler(userInput);
                break;
            case EVENT:
                eventHandler(userInput);
                break;
            case DELETE:
                deleteHandler(userInput);
                break;
            case FIND:
                findHandler(userInput);
                break;
            case DONE:
                doneHandler(userInput);
                break;
            case LIST:
                lsHandler();
                break;
            case UNRECOGNISED:
                System.out.println("ERROR: Unrecognised command. Did you make a typo?");
                break;
            }
        }
    }

    @Override
    public void start(Stage stage) {
        //Step 1. Setting up required components

        //The container for the content of the chat to scroll.
        scrollPane = new ScrollPane();
        dialogContainer = new VBox();
        scrollPane.setContent(dialogContainer);

        userInput = new TextField();
        sendButton = new Button("Send");

        AnchorPane mainLayout = new AnchorPane();
        mainLayout.getChildren().addAll(scrollPane, userInput, sendButton);

        scene = new Scene(mainLayout);

        stage.setScene(scene);
        stage.show();

        //Step 2. Formatting the window to look as expected
        stage.setTitle("Duke");
        stage.setResizable(false);
        stage.setMinHeight(600.0);
        stage.setMinWidth(400.0);

        mainLayout.setPrefSize(400.0, 600.0);

        scrollPane.setPrefSize(385, 535);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);

        scrollPane.setVvalue(1.0);
        scrollPane.setFitToWidth(true);

        // You will need to import `javafx.scene.layout.Region` for this.
        dialogContainer.setPrefHeight(Region.USE_COMPUTED_SIZE);

        userInput.setPrefWidth(325.0);

        sendButton.setPrefWidth(55.0);

        AnchorPane.setTopAnchor(scrollPane, 1.0);

        AnchorPane.setBottomAnchor(sendButton, 1.0);
        AnchorPane.setRightAnchor(sendButton, 1.0);

        AnchorPane.setLeftAnchor(userInput , 1.0);
        AnchorPane.setBottomAnchor(userInput, 1.0);

        //Step 3. Add functionality to handle user input.
        sendButton.setOnMouseClicked((event) -> {
            handleUserInput();
        });

        userInput.setOnAction((event) -> {
            handleUserInput();
        });

        //Scroll down to the end every time dialogContainer's height changes.
        dialogContainer.heightProperty().addListener((observable) -> scrollPane.setVvalue(1.0));
    }

    /**
     * Iteration 2:
     * Creates two dialog boxes, one echoing user input and the other containing Duke's reply and then appends them to
     * the dialog container. Clears the user input after processing.
     */
    private void handleUserInput() {
        dialogContainer.getChildren().addAll(
                DialogBox.getUserDialog(userInput.getText(), userImage),
                DialogBox.getDukeDialog(getResponse(userInput.getText()), dukeImage)
        );
        userInput.clear();
    }

    /**
     * You should have your own function to generate a response to user input.
     * Replace this stub with your completed method.
     */
    public String getResponse(String input) {
        return "Duke heard: " + input;
    }
}
