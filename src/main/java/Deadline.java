public class Deadline extends Task {
    public Deadline(String description, String deadline) {
        super(description);
        this.deadline = deadline;
    }

    private String deadline;

    @Override
    public String toString() {
        return super.toString() + " (by: " + deadline + ")";
    }
}