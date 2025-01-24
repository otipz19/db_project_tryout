package app.exceptions;

public class EntityNotFoundException extends Exception {
    public EntityNotFoundException(int id) {
        super("Entity with id = " + id + " was not found");
    }
}
