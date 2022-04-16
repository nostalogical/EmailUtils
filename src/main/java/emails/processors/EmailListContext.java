package emails.processors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import emails.analysis.DomainAnalysis;
import emails.analysis.EmailAddressAnalysis;
import emails.analysis.ParsedEmail;
import emails.constants.EmailListOrder;

/**
 * Holds contextual information for the processing a list of emails. based on configuration settings.
 */
public class EmailListContext {

    private final EmailValidationConfig validationConfig;
    private final EmailParserConfig parserConfig;

    private final Collection<String> emailAddresses;

    public EmailListContext(Collection<String> emailAddresses) {
        this(emailAddresses, null, null);
    }

    public EmailListContext(Collection<String> emailAddresses, EmailValidationConfig validator, EmailParserConfig parser) {
        this.validationConfig = validator == null ? EmailValidationConfig.generic() : validator;
        this.parserConfig = parser == null ? EmailParserConfig.standard() : parser;
        this.emailAddresses = emailAddresses == null ? Collections.emptyList() : emailAddresses;
    }

    private List<String> validateEmailAddresses() {
        List<EmailAddressAnalysis> validEmailAddresses = new ArrayList<>();
        for (String emailAddress : emailAddresses) {
            ParsedEmail email = new EmailContext(emailAddress, validationConfig, parserConfig);
            if (email.isValid())
                validEmailAddresses.add(new EmailAddressAnalysis(email));
        }
        return validEmailAddresses.stream().sorted((o1, o2) -> o1.compareTo(o2, parserConfig.order))
                .map(EmailAddressAnalysis::getParsedEmailAddress)
                .collect(Collectors.toList());
    }

    private Map<String, EmailAddressAnalysis> deduplicateAndAnalyse() {
        Map<String, EmailAddressAnalysis> analysedEmails = new HashMap<>();
        for (String emailAddress : emailAddresses) {
            ParsedEmail email = new EmailContext(emailAddress, validationConfig, parserConfig);
            if (email.isValid()) {
                if (!analysedEmails.containsKey(email.getParsedEmailAddress()))
                    analysedEmails.put(email.getParsedEmailAddress(), new EmailAddressAnalysis(email));
                else
                    analysedEmails.get(email.getParsedEmailAddress()).addParsedEmail(email);
            }
        }
        return analysedEmails;
    }

    private List<String> deduplicateEmailAddresses() {
        Map<String, EmailAddressAnalysis> analysedEmails = deduplicateAndAnalyse();
        return analysedEmails.values().stream()
                .sorted((o1, o2) -> o1.compareTo(o2, parserConfig.order))
                .map(EmailAddressAnalysis::getParsedEmailAddress)
                .collect(Collectors.toList());
    }

    public List<String> getValid() {
        return validateEmailAddresses();
    }

    public List<String> getValidDeduplicate() {
        return deduplicateEmailAddresses();
    }

    /**
     * Analysis domains across all email addresses in the list and returns the analysis results for each one, based on
     * the parsing and validation configurations set.
     */
    public List<DomainAnalysis> analyseDomains() {
        Map<String, DomainAnalysis> domainAnalysis = new HashMap<>();
        for (String emailAddress : emailAddresses) {
            ParsedEmail email = new EmailContext(emailAddress, validationConfig, parserConfig);
            if (email.isValid()) {
                if (!domainAnalysis.containsKey(email.getDomain()))
                    domainAnalysis.put(email.getDomain(), new DomainAnalysis(email));
                else
                    domainAnalysis.get(email.getDomain()).addParsedEmail(email);
            }
        }

        List<DomainAnalysis> analysedDomains = domainAnalysis.values().stream().sorted((o1, o2) -> o1.compareTo(o2, parserConfig.order)).collect(Collectors.toList());
        if (parserConfig.order == EmailListOrder.OCCURRENCES && parserConfig.maxResults != null && analysedDomains.size() > parserConfig.maxResults)
            analysedDomains = analysedDomains.subList(0, parserConfig.maxResults);
        return analysedDomains;
    }

    /**
     * Analyses emails addresses and return the full analysis results for each email address, based on the parsing and
     * validation configurations set. Duplicates are NOT removed.
     */
    public List<EmailAddressAnalysis> analyseEmailAddresses() {
        List<EmailAddressAnalysis> validEmailAddresses = new ArrayList<>();
        for (String emailAddress : emailAddresses) {
            ParsedEmail email = new EmailContext(emailAddress, validationConfig, parserConfig);
            if (email.isValid())
                validEmailAddresses.add(new EmailAddressAnalysis(email));
        }
        validEmailAddresses = validEmailAddresses.stream().sorted((o1, o2) -> o1.compareTo(o2, parserConfig.order)).collect(Collectors.toList());
        if (parserConfig.order == EmailListOrder.OCCURRENCES && parserConfig.maxResults != null && validEmailAddresses.size() > parserConfig.maxResults)
            validEmailAddresses = validEmailAddresses.subList(0, parserConfig.maxResults);
        return validEmailAddresses;
    }

    /**
     * Analyses emails addresses and return the full analysis results for each email address, based on the parsing and
     * validation configurations set. Duplicates are removed.
     */
    private List<EmailAddressAnalysis> deduplicateAndAnalyseEmailAddresses() {
        Map<String, EmailAddressAnalysis> analysedEmails = deduplicateAndAnalyse();
        return analysedEmails.values().stream()
                .sorted((o1, o2) -> o1.compareTo(o2, parserConfig.order)).collect(Collectors.toList());
    }

}
