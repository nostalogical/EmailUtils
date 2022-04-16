package emails.analysis;

import emails.constants.InvalidReason;

public interface ParsedEmail {

    /**
     * Returns true if the email address contains comments (string in parentheses).
     */
    boolean hasComments();

    /**
     * Returns true if the email address contains quoted parts.
     */
    boolean hasQuotes();

    /**
     * Returns true if the email address contains a sub-address.
     */
    boolean hasSubAddress();

    /**
     * Returns true if the local part of the email address contains dot "." characters.
     */
    boolean hasDots();

    /**
     * Returns true if this email address is valid according the applied validation configuration.
     */
    boolean isValid();

    /**
     * Returns the primary reason the email address is determined to be invalid, or null if the email address is valid.
     */
    InvalidReason invalidReason();

    /**
     * Returns an email address parsed according the parser configuration, or null if the email is invalid.
     */
    String getParsedEmailAddress();

    /**
     * Returns the unparsed email address.
     */
    String getRawEmailAddress();

    /**
     * Returns the full email address excluding any comments, or null if the email address is invalid.
     */
    String getFullEmailAddress();

    /**
     * Returns the local-part of the email address according to the parser configuration.
     */
    String getParsedLocalPart();

    /**
     * Returns the full local-part of the email address, including original case, any sub-address, and comments.
     * Returns null if the email address is invalid.
     */
    String getFullLocalPartWithComments();

    /**
     * Returns the full local-part of the address, including original case and any sub-address, but excluding comments.
     * Returns null if the email address is invalid.
     */
    String getFullLocalPart();

    /**
     * Returns the sub-address of the email address if it exists, including the leading delimiter.
     * If the email address is invalid null is returned, and if the email has no sub-address a blank string is returned.
     */
    String getSubAddress();

    /**
     * Returns the email address domain, or null if the email address is invalid.
     * If the domain contains comments these are discarded by default unless included by the configuration.
     */
    String getDomain();

}
