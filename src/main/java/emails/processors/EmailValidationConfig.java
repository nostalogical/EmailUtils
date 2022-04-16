package emails.processors;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Holds validation rules for email addresses.
 */
public class EmailValidationConfig {

    // Domain configuration
    boolean allowSingleNameDomains;
    boolean allowV4IPDomains;
    boolean allowV6IPDomains;

    // Local part configuration
    boolean allowQuotes;
    boolean allowComments;
    boolean allowDots;
    boolean allowSubAddresses;
    Set<Character> allowedPrintableCharacters = new HashSet<>();
    Set<Character> allowedSpecialCharacters = new HashSet<>();

    Pattern allowedPrintablePattern;
    Pattern allowedSpecialPattern;

    private static final Set<Character> allPrintableCharacters = new HashSet<>(Arrays.asList(
            '!', '#', '$', '%', '&', '\'', '*', '+', '-', '/', '=', '?', '^', '_', '`', '{', '}', '|', '~'));

    private static final Set<Character> allSpecialCharacters = new HashSet<>(Arrays.asList(
            ' ', '"',  '(', ')', ',', ':', ';', '<', '>', '@', '[', '\\', ']'));


    private void setPrintableRegex() {
        StringBuilder allowed = new StringBuilder();
        allowedPrintableCharacters.forEach(allowed::append);
        allowedPrintablePattern = Pattern.compile(
                "^[a-zA-Z\\d" + escapeCharactersForRegex(allowed.toString()) + "]+$");
    }

    private void setSpecialRegex() {
        StringBuilder allowed = new StringBuilder();
        boolean includeWhitespace = allowedSpecialCharacters.contains(' ');
        allPrintableCharacters.forEach(allowed::append);
        allowedSpecialCharacters.forEach(allowed::append);
        allowedSpecialPattern = Pattern.compile(
                "^[a-zA-Z\\d" + escapeCharactersForRegex(allowed.toString()) + (includeWhitespace ? "\\s" : "") + "]*$");
    }

    private String escapeCharactersForRegex(String unescaped) {
        unescaped = unescaped.replaceAll("([\\\\+*?\\[\\](){}|.^$])", "\\\\$1");
        return unescaped;
    }


    private EmailValidationConfig() {

    }

    /**
     * Strict configuration adheres to RFC5322 standards for emails. All technically valid emails will be allowed,
     * including IP addresses as domains and special characters in quotes.
     */
    public static EmailValidationConfig strict() {
        return custom().strict().build();
    }

    /**
     * Generic configuration disallows many things that are technically allowed in emails according to RFC5322 standards,
     * but allows emails that "look" right. Quotes, comments, and non-standard domains are disallowed, but dots, pluses
     * and hyphens are allowed.
     */
    public static EmailValidationConfig generic() {
        return custom().generic().build();
    }

    public static EmailValidationConfigBuilder custom() {
        return new EmailValidationConfigBuilder();
    }

    public static class EmailValidationConfigBuilder {

        private final EmailValidationConfig config = new EmailValidationConfig();

        public EmailValidationConfigBuilder generic() {
            config.allowSingleNameDomains = false;
            config.allowV4IPDomains = false;
            config.allowV6IPDomains = false;
            config.allowQuotes = false;
            config.allowDots = true;
            config.allowComments = false;
            config.allowSubAddresses = true;
            config.allowedPrintableCharacters.addAll(Arrays.asList('+', '-'));
            config.allowedSpecialCharacters.addAll(allSpecialCharacters);
            config.setPrintableRegex();
            config.setSpecialRegex();
            return this;
        }

        public EmailValidationConfigBuilder strict() {
            config.allowSingleNameDomains = true;
            config.allowV4IPDomains = true;
            config.allowV6IPDomains = true;
            config.allowQuotes = true;
            config.allowDots = true;
            config.allowComments = true;
            config.allowSubAddresses = true;
            config.allowedPrintableCharacters.addAll(allPrintableCharacters);
            config.allowedSpecialCharacters.addAll(allSpecialCharacters);
            config.setPrintableRegex();
            config.setSpecialRegex();
            return this;
        }

        public EmailValidationConfigBuilder allowSingleNameDomains(boolean allowSingleNameDomains) {
            config.allowSingleNameDomains = allowSingleNameDomains;
            return this;
        }

        public EmailValidationConfigBuilder allowV4IPDomains(boolean allowV4IPDomains) {
            config.allowV4IPDomains = allowV4IPDomains;
            return this;
        }

        public EmailValidationConfigBuilder allowV6IPDomains(boolean allowV6IPDomains) {
            config.allowV6IPDomains = allowV6IPDomains;
            return this;
        }

        public EmailValidationConfigBuilder allowQuotes(boolean allowQuotes) {
            config.allowQuotes = allowQuotes;
            return this;
        }

        public EmailValidationConfigBuilder allowDots(boolean allowDots) {
            config.allowDots = allowDots;
            return this;
        }

        public EmailValidationConfigBuilder allowComments(boolean allowComments) {
            config.allowComments = allowComments;
            return this;
        }

        public EmailValidationConfigBuilder allowHyphens(boolean allowHyphens) {
            if (allowHyphens) config.allowedPrintableCharacters.add('-');
            else config.allowedPrintableCharacters.remove('-');
            return this;
        }

        public EmailValidationConfigBuilder allowPluses(boolean allowPluses) {
            if (allowPluses) config.allowedPrintableCharacters.add('+');
            else config.allowedPrintableCharacters.remove('+');
            return this;
        }

        /**
         * Setting characters with this method will allow them in the main local part of an email address if they are
         * not currently allowed and are in the list below.
         *
         * Printable characters are those in the set !#$%&'*+-/=?^_`{|}~
         *
         * By default, all of these are allowed in the "strict" configuration, but only "+" and "-" are allowed in the
         * "generic" configuration.
         */
        public EmailValidationConfigBuilder allowPrintableCharacters(char... characters) {
            for (char character : characters) {
                if (allPrintableCharacters.contains(character))
                    config.allowedPrintableCharacters.add(character);
            }
            config.setPrintableRegex();
            return this;
        }

        /**
         * Setting characters with this method will disallow them in the main local part of an email address if they are
         * currently allowed.
         *
         * Printable characters are those in the set !#$%&'*+-/=?^_`{|}~
         *
         * By default, all of these are allowed in the "strict" configuration, but only "+" and "-" are allowed in the
         * "generic" configuration.
         */
        public EmailValidationConfigBuilder disallowPrintableCharacters(char... characters) {
            for (char character : characters) {
                config.allowedPrintableCharacters.remove(character);
            }
            config.setPrintableRegex();
            return this;
        }

        /**
         * Setting characters with this method will allow them within quotes in the local part of an email address if
         * they are not currently allowed and are in the list below.
         *
         * Special characters are a space and those in the set "(),:;<>@[\]
         *
         * By default, all special characters are allowed, however as they are only permitted within quotes they are
         * de facto not allowed when quotes are considered invalid, such as in the "generic" configuration.
         */
        public EmailValidationConfigBuilder allowSpecialCharacters(char... characters) {
            for (char character : characters) {
                if (allSpecialCharacters.contains(character))
                    config.allowedSpecialCharacters.add(character);
            }
            config.setPrintableRegex();
            config.setSpecialRegex();
            return this;
        }

        /**
         * Setting characters with this method will disallow them within quotes in the local part of an email address if
         * they are currently allowed.
         *
         * Special characters are a space and those in the set "(),:;<>@[\]
         *
         * By default, all special characters are allowed, however as they are only permitted within quotes they are
         * de facto not allowed when quotes are considered invalid, such as in the "generic" configuration.
         */
        public EmailValidationConfigBuilder disallowSpecialCharacters(char... characters) {
            for (char character : characters) {
                config.allowedSpecialCharacters.remove(character);
            }
            config.setPrintableRegex();
            config.setSpecialRegex();
            return this;
        }

        public EmailValidationConfig build() {
            return config;
        }

    }


}
