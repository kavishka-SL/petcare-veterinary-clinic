package ead_cw;

/**
 * User-defined exception for appointment billing business rules.
 */
public class BillingValidationException extends Exception {

    public BillingValidationException(String message) {
        super(message);
    }
}
