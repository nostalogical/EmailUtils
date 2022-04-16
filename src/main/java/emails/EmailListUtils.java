package emails;

import java.util.Collection;
import java.util.List;

import emails.processors.EmailListContext;
import emails.processors.EmailParserConfig;
import emails.processors.EmailValidationConfig;

/**
 * Provides functions for processing lists of emails. These are helper functions built on the processing of single
 * email addresses by the EmailContext class, and further by the bulk processing done by an EmailListContext.
 *
 * The nature of the lists produced by these functions are based on varying configuration through the EmailParserConfig
 * and EmailValidationConfig classes.
 */
public class EmailListUtils {

    /**
     * Returns a list of emails with any invalid or duplicate entries filtered out. This is case-insensitive and the
     * resultant list will be entirely lower case.
     */
    public static List<String> deduplicateEmails(Collection<String> emails) {
        return deduplicateEmails(emails, null, null);
    }

    /**
     * Returns a list of emails with any invalid or duplicate entries filtered out. If case sensitivity is enabled, any
     * otherwise identical emails email with different cases will be considered unique.
     */
    public static List<String> deduplicateEmails(Collection<String> emails, boolean ignoreCase) {
        return deduplicateEmails(emails, null, EmailParserConfig.custom()
                .setCaseSensitive(!ignoreCase).build());
    }

    /**
     * Returns a list of emails with any invalid or duplicate entries filtered out. If case sensitivity is enabled, any
     * otherwise identical emails email with different cases will be considered unique.
     */
    public static List<String> deduplicateEmails(Collection<String> emails, boolean ignoreCase, boolean removeSubAddresses) {
        return deduplicateEmails(emails, null, EmailParserConfig.custom()
                        .setCaseSensitive(!ignoreCase).includeSubAddresses(!removeSubAddresses).build());
    }

    /**
     * Returns a list of emails with any invalid or duplicate entries filtered out, applying the criteria in the
     * supplied configuration.
     */
    public static List<String> deduplicateEmails(Collection<String> emails, EmailValidationConfig validator, EmailParserConfig parser) {
        return new EmailListContext(emails, validator, parser).getValidDeduplicate();
    }

    /**
     * Returns a list of only the emails in the supplied list determined to be valid. Any valid duplicates will be
     * preserved.
     */
    public static List<String> validateEmails(Collection<String> emails) {
        return validateEmails(emails, null, null);
    }

    /**
     * Returns a list of emails with any determined to be valid filtered out after applying the criteria in the
     * supplied configuration.
     */
    public static List<String> validateEmails(Collection<String> emails, EmailValidationConfig validator, EmailParserConfig parser) {
        return new EmailListContext(emails, validator, parser).getValid();
    }

}
