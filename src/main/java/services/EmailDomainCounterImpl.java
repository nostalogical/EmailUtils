package services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EmailDomainCounterImpl implements EmailDomainCounter {

    private static Pattern pattern = Pattern.compile("(?:.*)@(.*\\..*)");

    @Override
    public List<String> countEmailDomains(List<String> inputEmails) {
        if (inputEmails == null || inputEmails.isEmpty()) {
            return Collections.emptyList();
        }

        // Domain strings and their associated counts, unsorted - this function also discards invalid address formats
        Map<String, Long> countedEmails = countDomainsToMap(inputEmails);

        // Sort entries first by count value and then by domain alphabetically
        List<Map.Entry<String, Long>> sortedEntries = sortCountedDomains(countedEmails);

        // Output counted and sorted domains in an easily printable ordered list format
        return createPrintableSortedList(sortedEntries);
    }

    private Map<String, Long> countDomainsToMap(List<String> inputEmails) {
        return inputEmails
                .stream()
                .map(this::extractDomain)
                .flatMap(optEmail -> optEmail.map(Stream::of).orElseGet(Stream::empty))
                .collect(Collectors.groupingBy(validEmail -> validEmail, Collectors.counting()));
    }

    // Any invalid domain is discarded as an empty optional here
    private Optional<String> extractDomain(String email) {
        Matcher matcher = pattern.matcher(email);
        return matcher.find() ? Optional.of(matcher.group(1).toLowerCase()) : Optional.empty();
    }

    private List<Map.Entry<String, Long>> sortCountedDomains(Map<String, Long> countedEmails) {
        // Map of domain/count entries as a list of entries
        List<Map.Entry<String, Long>> sortedEntries = new ArrayList<>(countedEmails.entrySet());

        // Sort the domains by their counts in descending order
        sortedEntries.sort((this::compareCountedDomains));

        return sortedEntries;
    }

    private int compareCountedDomains(Map.Entry<String, Long> entry1, Map.Entry<String, Long> entry2) {
        int valueSort = entry2.getValue().compareTo(entry1.getValue());
        return valueSort == 0 ? entry1.getKey().compareTo(entry2.getKey()) : valueSort;
    }

    private List<String> createPrintableSortedList(List<Map.Entry<String, Long>> sortedEntries) {
        List<String> result = new ArrayList<>();
        // For the top 10 sorted domains, add the domain and its count to this output list
        for (Map.Entry<String, Long> entry : sortedEntries) {
            result.add(entry.getKey() + " " + entry.getValue());
            if (result.size() == 10) {
                break;
            }
        }
        return result;
    }

}
