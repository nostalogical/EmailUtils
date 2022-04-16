package emails.analysis;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import emails.constants.EmailListOrder;

/**
 * The results of analysis of a single domain. This is implicitly expected to contain the results from several email
 * addresses, so while there is a single domain there may be multiple total local parts and unique local parts.
 */
public class DomainAnalysis {

    private boolean valid;
    private String domain;
    private List<String> totalLocalParts = new ArrayList<>();
    private Set<String> uniqueLocalParts = new HashSet<>();
    private List<String> totalSubAddresses = new ArrayList<>();
    private Set<String> uniqueSubAddresses = new HashSet<>();

    public DomainAnalysis(ParsedEmail parsedEmail) {
        this.valid = parsedEmail != null && parsedEmail.isValid();
        if (valid) {
            domain = parsedEmail.getDomain();
            addParsedEmail(parsedEmail);
        } else
            domain = null;
    }

    public void addParsedEmail(ParsedEmail parsedEmail) {
        if (valid && parsedEmail.isValid() && domain.equals(parsedEmail.getDomain())) {
            totalLocalParts.add(parsedEmail.getParsedLocalPart());
            uniqueLocalParts.add(parsedEmail.getParsedLocalPart());
            if (parsedEmail.hasSubAddress()) {
                totalSubAddresses.add(parsedEmail.getSubAddress());
                uniqueSubAddresses.add(parsedEmail.getSubAddress());
            }
        }
    }

    public String getDomain() {
        return domain;
    }

    /**
     * Returns the total number of email addresses attached to this domain, including duplicates.
     */
    public int getTotalEmailAddressCount() {
        return totalLocalParts.size();
    }

    /**
     * Returns the number of unique email addresses attached to this domain based on parsing rules. Adjust parsing
     * config to control if this includes case sensitivity, comments, or sub-addresses.
     */
    public int getUniqueEmailAddressCount() {
        return uniqueLocalParts.size();
    }


    public int compareTo(DomainAnalysis o, EmailListOrder orderType) {
        if (EmailListOrder.OCCURRENCES.equals(orderType)) {
            int totalCountCompare = o.getTotalEmailAddressCount() - getTotalEmailAddressCount();
            if (totalCountCompare != 0)
                return totalCountCompare;
        }
        return getDomain().compareTo(o.getDomain());
    }
}
