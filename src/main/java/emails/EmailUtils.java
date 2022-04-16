package emails;

import emails.constants.InvalidReason;
import emails.processors.EmailContext;
import emails.processors.EmailParserConfig;
import emails.processors.EmailValidationConfig;

public class EmailUtils {

    public static boolean isValid(String emailAddress, EmailValidationConfig config) {
        return new EmailContext(emailAddress, config, EmailParserConfig.standard()).isValid();
    }

    public static boolean isValid(String emailAddress) {
        return isValid(emailAddress, EmailValidationConfig.generic());
    }

    public static boolean isNotValid(String emailAddress, EmailValidationConfig config) {
        return !isValid(emailAddress, config);
    }

    public static boolean isNotValid(String emailAddress) {
        return !isValid(emailAddress, EmailValidationConfig.generic());
    }

    public static InvalidReason invalidReason(String emailAddress) {
        return invalidReason(emailAddress, EmailValidationConfig.generic());
    }

    public static InvalidReason invalidReason(String emailAddress, EmailValidationConfig config) {
        return new EmailContext(emailAddress, config, EmailParserConfig.standard()).invalidReason();
    }

    public static boolean hasSubAddress(String emailAddress) {
        return new EmailContext(emailAddress, EmailValidationConfig.strict(), EmailParserConfig.standard()).hasSubAddress();
    }

    public static boolean hasQuotes(String emailAddress) {
        return new EmailContext(emailAddress, EmailValidationConfig.strict(), EmailParserConfig.standard()).hasQuotes();
    }

    public static boolean hasComments(String emailAddress) {
        return new EmailContext(emailAddress, EmailValidationConfig.strict(), EmailParserConfig.standard()).hasComments();
    }

    public static boolean hasDots(String emailAddress) {
        return new EmailContext(emailAddress, EmailValidationConfig.strict(), EmailParserConfig.standard()).hasDots();
    }

    /**
     * Removes comments and sub-addresses from the supplied email address and converts the local-part to lower case.
     */
    public static String strip(String emailAddress) {
        String parsed = new EmailContext(emailAddress, EmailValidationConfig.strict(),
                EmailParserConfig.custom().includeSubAddresses(false).setCaseSensitive(false).build())
                .getParsedEmailAddress();
        return parsed == null ? emailAddress : parsed;
    }

    /**
     * Removes any sub-address and comments from the supplied email address, but preserves the local-part case.
     */
    public static String removeSubAddress(String emailAddress) {
        String parsed = new EmailContext(emailAddress, EmailValidationConfig.strict(),
                EmailParserConfig.custom().includeSubAddresses(false).build())
                .getParsedEmailAddress();
        return parsed == null ? emailAddress : parsed;
    }

    /**
     * Removes any comments from the supplied email address, but preserves any sub-address and the local-part case.
     */
    public static String removeComments(String emailAddress) {
        return new EmailContext(emailAddress, EmailValidationConfig.strict(), null).getParsedEmailAddress();
    }

}
