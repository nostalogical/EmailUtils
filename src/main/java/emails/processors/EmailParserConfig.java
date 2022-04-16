package emails.processors;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import emails.constants.EmailListOrder;

/**
 * Holds rules for parsing emails, such as handling sub-addresses, stripping comments
 */
public class EmailParserConfig {

    boolean includeSubAddresses = true;
    boolean includeComments = false;
    boolean lowerCase = false;

    EmailListOrder order = EmailListOrder.ALPHABETICAL;
    Integer maxResults = null;
    Set<Character> subAddressCharacters = Collections.singleton('+');

    private EmailParserConfig() {
    }

    /**
     * Standard email parsing configuration with sub-addresses included, comments excluded, and local-parts set to lower
     * case.
     */
    public static EmailParserConfig standard() {
        return new EmailParserConfig();
    }

    /**
     * Returns a builder allowing customer parser configuration to be set through the builder's methods.
     */
    public static EmailParserConfigBuilder custom() {
        return new EmailParserConfigBuilder();
    }

    public static class EmailParserConfigBuilder {

        private final EmailParserConfig config = new EmailParserConfig();

        /**
         * If enabled the case of the local parts of emails will be preserved, if disabled it will be set to lower case.
         * By default, this is enabled. All domains are considered case-insensitive and are not affected by this.
         */
        public EmailParserConfigBuilder setCaseSensitive(boolean caseSensitive) {
            config.lowerCase = !caseSensitive;
            return this;
        }

        /**
         * If enabled, text in parentheses (which is technically valid) will be removed from emails. By default, this
         * is disabled.
         */
        public EmailParserConfigBuilder includeComments(boolean includeComments) {
            config.includeComments = includeComments;
            return this;
        }

        /**
         * If enabled, sub-addresses in emails (such as strings following a '+' character in the local part) will be
         * removed. By default, this is enabled.
         */
        public EmailParserConfigBuilder includeSubAddresses(boolean includeSubAddresses) {
            config.includeSubAddresses = includeSubAddresses;
            return this;
        }

        /**
         * Set characters to be classes as a sub-address of an email address. By default, this is only "+", for example
         * in the address "test+sub@example.com".
         *
         * Different email providers can user different sub-address rules, so providing an alternate set with this method
         * can override the default "+".
         */
        public EmailParserConfigBuilder setSubAddressCharacters(char... characters) {
            config.subAddressCharacters = new HashSet<>();
            for (char character : characters) {
                config.subAddressCharacters.add(character);
            }
            return this;
        }

        /**
         * Set the order method to be used when handling lists of email addresses. Default is alphabetical ordering on
         * the parsed email address.
         */
        public EmailParserConfigBuilder setListOrder(EmailListOrder order) {
            config.order = order;
            return this;
        }

        /**
         * Limit the maximum number of results to be returned. Only applies when returning a list of counts of the
         * occurrences of email addresses or domains. By default all results are returned.
         */
        public EmailParserConfigBuilder setMaxResults(int maxResults) {
            config.maxResults = maxResults;
            return this;
        }

        public EmailParserConfig build() {
            return config;
        }

    }

}
