package Hospital.system.exception;

import org.springframework.validation.BindingResult;

public class ValidationException extends RuntimeException {
    private final BindingResult bindingResult;

    public ValidationException(String message, BindingResult bindingResult) {
        super(message);
        this.bindingResult = bindingResult;
    }

    public ValidationException(String message) {
        super(message);
        this.bindingResult = null;
    }

    public BindingResult getBindingResult() {
        return bindingResult;
    }
}
