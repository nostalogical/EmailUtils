package services;

import java.util.List;
import java.util.Set;

public interface EmailDomainCounter {

    List<String> countEmailDomains(List<String> inputEmails);
}
