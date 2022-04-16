package emails.processors;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import emails.analysis.ParsedEmail;
import emails.constants.InvalidReason;

/**
 * Holds contextual data for a single email.
 */
public class EmailContext implements ParsedEmail {

    Pattern domainPartPattern = Pattern.compile("^[a-zA-Z\\d]+[a-zA-Z\\d\\-]*[a-zA-Z\\d]+$");
    Pattern anyLettersPattern = Pattern.compile("[a-zA-Z]+");
    Pattern ipV4DomainPattern = Pattern.compile("^\\[([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
            "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\]$");
    Pattern ipV6DomainPatern = Pattern.compile("\\[ipv6\\:([a-f\\d:]+:+)+[a-f\\d]+\\]");

    private final String rawEmailAddress;
    List<String> localParts = new ArrayList<>();
    List<String> domainParts = new ArrayList<>();
    List<Character> currentPart = new ArrayList<>();

    private String fullLocalPartWithComments;
    private String fullLocalPart;
    private String localSubAddress;
    private String parsedLocalPart;

    private String domain;
    private String domainWithComments;

    private boolean isValid = true;
    private InvalidReason invalidReason;

    private boolean hasDots;
    private boolean hasQuotes;
    private boolean hasComments;

    public EmailContext(String email) {
        this(email, null, null);
    }
    public EmailContext(String email, EmailValidationConfig validator, EmailParserConfig parser) {
        this.rawEmailAddress = email;
        if (validator == null) validator = EmailValidationConfig.generic();
        if (parser == null) parser = EmailParserConfig.standard();
        if (email == null || email.length() == 0)
            setInvalid(InvalidReason.BLANK);
        else parseBaseEmailParts(email, validator, parser);
    }

    private void addPart(String part, boolean domain) {
        if (domain) domainParts.add(part);
        else localParts.add(part);
    }

    private void addCurrentPart(boolean domain) {
        if (!currentPart.isEmpty()) {
            addPart(currentPart.stream().map(String::valueOf).collect(Collectors.joining()), domain);
            currentPart = new ArrayList<>();
        }
    }

    /**
     * Parse a non-null email address for comments, quotes, dotted parts, and domain.
     */
    private void parseBaseEmailParts(String email, EmailValidationConfig validator, EmailParserConfig parser) {
        boolean isDomain = false;
        boolean subAddressFound = false;
        Character closeCharacter = null;
        int atCount = 0;
        for (char ch : email.toCharArray()) {
            switch (ch) {
                case '@':
                    if (closeCharacter == null) {
                        addCurrentPart(isDomain);
                        isDomain = true;
                        atCount++;
                    } else currentPart.add(ch);
                    break;
                case '.':
                    if (closeCharacter == null) {
                        hasDots |= !isDomain;
                        addCurrentPart(isDomain);
                        addPart(String.valueOf(ch), isDomain);
                    } else currentPart.add(ch);
                    break;
                case ')':
                    currentPart.add(ch);
                    if (closeCharacter == null) {
                        setInvalid(InvalidReason.UNCLOSED_PARENTHESIS);
                        return;
                    }
                    if (closeCharacter == ch) {
                        addCurrentPart(isDomain);
                        closeCharacter = null;
                    }
                    break;
                case '"':
                    currentPart.add(ch);
                    if (closeCharacter != null && closeCharacter == ch) {
                        if (currentPart.size() > 1 && currentPart.get(currentPart.size() - 2) == '\\')
                            break;
                        addCurrentPart(isDomain);
                        closeCharacter = null;
                    } else {
                        closeCharacter = ch;
                        hasQuotes = true;
                    }
                    break;
                case '(':
                    if (closeCharacter == null) {
                        addCurrentPart(isDomain);
                        hasComments = true;
                        closeCharacter = ')';
                    }
                    currentPart.add(ch);
                    break;
                default:
                    if (closeCharacter == null && !isDomain && !subAddressFound && parser.subAddressCharacters.contains(ch)) {
                        subAddressFound = true;
                        addCurrentPart(false);
                        addPart(String.valueOf(ch), false);
                    } else
                        currentPart.add(ch);
            }
        }
        if (!currentPart.isEmpty())
            addCurrentPart(isDomain);
        if (closeCharacter != null) {
            setInvalid(closeCharacter == ')' ? InvalidReason.UNCLOSED_PARENTHESIS : InvalidReason.UNCLOSED_QUOTE);
            return;
        }

        if (atCount == 0) setInvalid(InvalidReason.NO_AT_SYMBOL);
        else if (atCount > 1) setInvalid(InvalidReason.MULTIPLE_AT_SYMBOLS);
        else if (hasQuotes && !validator.allowQuotes)
            setInvalid(InvalidReason.HAS_QUOTES);
        else if (hasComments && !validator.allowComments)
            setInvalid(InvalidReason.HAS_COMMENTS);

        if (isValid) parseLocalPart(validator, parser);
        if (isValid) parseDomain(validator, parser);
    }

    private void setInvalid(InvalidReason reason) {
        this.isValid = false;
        this.invalidReason = reason;
    }

    private void parseLocalPart(EmailValidationConfig validator, EmailParserConfig parser) {
        if (!validator.allowDots && localParts.contains(".")) {
            setInvalid(InvalidReason.HAS_DOTS);
            return;
        }
        StringBuilder localFullComments = hasComments ? new StringBuilder() : null;
        StringBuilder localFull = new StringBuilder();
        StringBuilder parsedLocal = new StringBuilder();
        StringBuilder subAddress = new StringBuilder();
        boolean isSubAddress = false;
        for (String part : localParts) {
            char startChar = part.charAt(0);
            if (localFullComments != null) localFullComments.append(part);
            boolean isComment = startChar == '(';
            boolean isQuote = startChar == '"';
            isSubAddress |= parser.subAddressCharacters.contains(startChar);

            if (isComment || isQuote) {
                Matcher matcher = validator.allowedSpecialPattern.matcher(part.substring(1, part.length() - 1));
                if (!matcher.find()) {
                    setInvalid(InvalidReason.INVALID_CHARACTERS);
                    return;
                }
            } else if (startChar != '.') {
                Matcher matcher = validator.allowedPrintablePattern.matcher(part);
                if (!matcher.find()) {
                    setInvalid(InvalidReason.INVALID_CHARACTERS);
                    return;
                }
            }

            if (!isComment) localFull.append(part);
            if (isSubAddress) subAddress.append(part);

            if ((!isComment || parser.includeComments) && (!isSubAddress || parser.includeSubAddresses))
                parsedLocal.append(part);

        }

        if (localFullComments != null)
            fullLocalPartWithComments = localFullComments.toString();
        fullLocalPart = localFull.toString();
        parsedLocalPart = parser.lowerCase ? parsedLocal.toString().toLowerCase() : parsedLocal.toString();
        localSubAddress = parser.lowerCase ? subAddress.toString().toLowerCase() : subAddress.toString();

        if (!validator.allowSubAddresses && localSubAddress.length() > 0)
            setInvalid(InvalidReason.HAS_SUB_ADDRESS);
        else if (fullLocalPartWithComments != null && fullLocalPartWithComments.length() > 64 || fullLocalPart.length() > 64)
            setInvalid(InvalidReason.LOCAL_PART_TOO_LONG);
    }

    private void parseDomain(EmailValidationConfig validator, EmailParserConfig parser) {
        StringBuilder domainCommentsBuilder = hasComments ? new StringBuilder() : null;
        StringBuilder domainBuilder = new StringBuilder();
        Matcher matcher;
        boolean hasInvalidCharacters = false;

        for (String part : domainParts) {
            char startChar = part.charAt(0);
            boolean isComment = startChar == '(';
            boolean isDot = startChar == '.';
            if (domainCommentsBuilder != null) domainCommentsBuilder.append(part);
            if (startChar == '.' && domainBuilder.length() > 0 && domainBuilder.charAt(domainBuilder.length() - 1) == startChar) {
                setInvalid(InvalidReason.CONSECUTIVE_DOTS);
                return;
            }
            if (!hasComments || parser.includeComments || !isComment) {
                if (startChar == '"') {
                    setInvalid(InvalidReason.DOMAIN_QUOTES);
                    return;
                }
                domainBuilder.append(part);
            }
            if (!isComment && !isDot) {
                if (part.charAt(0) == '-' || part.charAt(part.length() - 1) == '-') {
                    setInvalid(InvalidReason.DOMAIN_EDGE_HYPHEN);
                    return;
                }

                matcher = anyLettersPattern.matcher(part);
                hasInvalidCharacters |= !matcher.find();
                matcher = domainPartPattern.matcher(part);
                hasInvalidCharacters |= (!matcher.find() && part.length() > 1);
            }
        }
        if (domainBuilder.charAt(0) == '.' || domainBuilder.charAt(domainBuilder.length() - 1) == '.') {
            setInvalid(InvalidReason.EDGE_DOT);
            return;
        }
        if (domainBuilder.charAt(0) == '-' || domainBuilder.charAt(domainBuilder.length() - 1) == '-') {
            setInvalid(InvalidReason.DOMAIN_EDGE_HYPHEN);
            return;
        }

        if (domainCommentsBuilder != null) domainWithComments = domainCommentsBuilder.toString();
        domain = domainBuilder.toString().toLowerCase();

        boolean isIPDomain = false;
        matcher = ipV4DomainPattern.matcher(domain);
        if (matcher.find()) {
            isIPDomain = true;
            if (!validator.allowV4IPDomains) {
                setInvalid(InvalidReason.V4_IP_DOMAIN);
                return;
            }
        } else {
            matcher = ipV6DomainPatern.matcher(domain);
            if (matcher.find()) {
                isIPDomain = true;
                if (!validator.allowV6IPDomains) {
                    setInvalid(InvalidReason.V6_IP_DOMAIN);
                    return;
                }
            }
        }
        if (!isIPDomain && hasInvalidCharacters) {
            setInvalid(InvalidReason.INVALID_CHARACTERS);
            return;
        }

        if (!validator.allowSingleNameDomains && !domain.contains(".")) {
            setInvalid(InvalidReason.NO_TOP_LEVEL_DOMAIN);
        }
    }

    @Override
    public boolean hasComments() {
        return hasComments;
    }

    @Override
    public boolean isValid() {
        return isValid;
    }

    @Override
    public InvalidReason invalidReason() {
        return invalidReason;
    }

    @Override
    public boolean hasQuotes() {
        return hasQuotes;
    }

    @Override
    public boolean hasSubAddress() {
        return localSubAddress != null && localSubAddress.length() > 0;
    }

    @Override
    public boolean hasDots() {
        return hasDots;
    }

    @Override
    public String getParsedEmailAddress() {
        return !isValid ? null : String.format("%s@%s", parsedLocalPart, domain);
    }

    @Override
    public String getFullEmailAddress() {
        return !isValid ? null : String.format("%s@%s", parsedLocalPart, domain);
    }

    @Override
    public String getParsedLocalPart() {
        return parsedLocalPart;
    }

    @Override
    public String getFullLocalPart() {
        return fullLocalPart;
    }

    @Override
    public String getSubAddress() {
        return localSubAddress;
    }

    @Override
    public String getDomain() {
        return domain;
    }

    @Override
    public String getRawEmailAddress() {
        return rawEmailAddress;
    }

    @Override
    public String getFullLocalPartWithComments() {
        return fullLocalPartWithComments;
    }
}
