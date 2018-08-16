package services;

import java.util.List;

public interface EmailDomainCounter {

    List<String> countEmailDomains(List<String> inputEmails);
}
