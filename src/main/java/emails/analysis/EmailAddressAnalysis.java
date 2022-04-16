package emails.analysis;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import emails.constants.EmailListOrder;

/**
 * Holds the analysis of an email address signature. The signature will vary based on parsing rules, but any email
 * matching this signature will be added to this analysis object.
 */
public class EmailAddressAnalysis {

    private final boolean valid;

    private String domain;
    private String parsedLocalPart;
    private String parsedEmailAddress;
    private List<String> rawEmails = new ArrayList<>();
    private Set<String> distinctRawEmails = new HashSet<>();
    private Set<String> distinctSubAddresses = new HashSet<>();

    public EmailAddressAnalysis(ParsedEmail parsedEmail) {
        this.valid = parsedEmail != null && parsedEmail.isValid();
        if (valid) {
            domain = parsedEmail.getDomain();
            parsedLocalPart = parsedEmail.getParsedLocalPart();
            parsedEmailAddress = parsedEmail.getParsedEmailAddress();
            addParsedEmail(parsedEmail);
        } else {
            parsedLocalPart = null;
            parsedEmailAddress = null;
        }
    }

    public void addParsedEmail(ParsedEmail parsedEmail) {
        if (valid && parsedEmail.isValid() && parsedLocalPart.equals(parsedEmail.getParsedLocalPart())) {
            rawEmails.add(parsedEmail.getRawEmailAddress());
            distinctRawEmails.add(parsedEmail.getRawEmailAddress());
            distinctSubAddresses.add(parsedEmail.getSubAddress());
            if (parsedEmail.hasSubAddress())
                distinctSubAddresses.add(parsedEmail.getSubAddress());
        }
    }

    public String getParsedEmailAddress() {
        return parsedEmailAddress;
    }

    public String getLocalPart() {
        return parsedLocalPart;
    }

    public String getDomain() {
        return domain;
    }

    public int getTotalCount() {
        return rawEmails.size();
    }

    public int getUniqueVariationCount() {
        return distinctRawEmails.size();
    }

    public int getUniqueSubAddressCount() {
        return distinctSubAddresses.size();
    }

    public int compareTo(EmailAddressAnalysis o, EmailListOrder orderType) {
        if (EmailListOrder.OCCURRENCES.equals(orderType)) {
            int totalCountCompare = getTotalCount() - o.getTotalCount();
            if (totalCountCompare != 0)
                return totalCountCompare;
        }
        if (EmailListOrder.DOMAIN_ALPHABETICAL.equals(orderType)) {
            int domainCompare = getDomain().compareTo(o.getDomain());
            if (domainCompare != 0)
                return domainCompare;
        }
        return getParsedEmailAddress().compareTo(o.getParsedEmailAddress());
    }
}
