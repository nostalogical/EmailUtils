package emails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import emails.analysis.DomainAnalysis;
import emails.analysis.EmailAddressAnalysis;
import emails.constants.EmailListOrder;
import emails.processors.EmailListContext;
import emails.processors.EmailParserConfig;
import emails.processors.EmailValidationConfig;

public class EmailListAnalytics {

    public static int countUniqueDomains(Collection<String> emailAddresses) {
        return analyseDomains(emailAddresses, EmailValidationConfig.generic(), EmailParserConfig.standard()).size();
    }

    public static List<String> listDomains(Collection<String> emailAddresses) {
        return analyseDomains(emailAddresses, EmailValidationConfig.generic(),
                EmailParserConfig.custom().setListOrder(EmailListOrder.DOMAIN_ALPHABETICAL).build())
                .stream().map(DomainAnalysis::getDomain).collect(Collectors.toList());
    }

    /**
     * List the domains included in the supplied email address list by the number of occurrences of the domains, with
     * the occurrence count appearing after the domain, separated by a space.
     */
    public static List<String> listDomainsByCount(Collection<String> emailAddresses) {
        return listDomainsByCount(emailAddresses, null);
    }

    /**
     * List the domains included in the supplied email address list by the number of occurrences of the domains, with
     * the occurrence count appearing after the domain, separated by a space.
     */
    public static List<String> listDomainsByCount(Collection<String> emailAddresses, Integer maxResults) {
        return analyseDomains(emailAddresses, EmailValidationConfig.generic(),
                EmailParserConfig.custom().setListOrder(EmailListOrder.OCCURRENCES).setMaxResults(maxResults).build())
                .stream().map(d -> String.format("%s %d", d.getDomain(), d.getTotalEmailAddressCount())).collect(Collectors.toList());
    }

    /**
     * Analysis domains across all email addresses in the list and returns the analysis results for each one, based on
     * the parsing and validation configurations set.
     */
    public static List<DomainAnalysis> analyseDomains(Collection<String> emailAddresses, EmailValidationConfig validator, EmailParserConfig parser) {
        return new EmailListContext(emailAddresses, validator, parser).analyseDomains();
    }

    /**
     * Analyses emails addresses and return the full analysis results for each email address, based on the parsing and
     * validation configurations set. Duplicates are NOT removed.
     */
    public static List<EmailAddressAnalysis> analyseEmailAddresses(Collection<String> emailAddresses, EmailValidationConfig validator, EmailParserConfig parser) {
        return new EmailListContext(emailAddresses, validator, parser).analyseEmailAddresses();
    }

}
