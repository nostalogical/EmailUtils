import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import services.EmailDomainCounter;
import services.EmailDomainCounterImpl;

public class EmailDomainsTest {

    private EmailDomainCounter emailDomainCounter = new EmailDomainCounterImpl();
    private List<String> testInput = new ArrayList<>();

    @Test
    public void resultCountAndOrderTest() {
        testInput.add("testemail@aol.com");
        testInput.add("testemail@aol.com");
        testInput.add("testemail@aol.com");
        testInput.add("testemail@aol.com");
        testInput.add("testemail@aol.com");

        testInput.add("third@gmail.com");
        testInput.add("third@gmail.com");
        testInput.add("third@gmail.com");

        testInput.add("another@yahoo.com");
        testInput.add("another@yahoo.com");
        testInput.add("another@yahoo.com");
        testInput.add("another@yahoo.com");

        testInput.add("fourth@domain.cc");
        testInput.add("fourth@domain.cc");

        testInput.add("final@test.net");
        List<String> result = emailDomainCounter.countEmailDomains(testInput);
        Assert.assertEquals(5, result.size());
        Assert.assertEquals("aol.com 5", result.get(0));
        Assert.assertEquals("yahoo.com 4", result.get(1));
        Assert.assertEquals("gmail.com 3", result.get(2));
        Assert.assertEquals("domain.cc 2", result.get(3));
        Assert.assertEquals("test.net 1", result.get(4));
    }

    @Test
    public void domainCountCaseInsensitiveTest() {
        testInput.add("testemail@aol.com");
        testInput.add("TESTEMAIL@AOL.com");
        testInput.add("TESTemail@aol.COM");
        testInput.add("testemail@yahoo.com");
        List<String> result = emailDomainCounter.countEmailDomains(testInput);
        Assert.assertEquals("Domain counting should be case insensitive", "aol.com 3", result.get(0));
    }

    @Test
    public void emptyInputReturnsEmptyResponse() {
        List<String> result = emailDomainCounter.countEmailDomains(testInput);
        Assert.assertEquals("Empty input should return an empty response", Collections.EMPTY_LIST, result);
    }

    @Test
    public void nullInputReturnsEmptyResponse() {
        List<String> result = emailDomainCounter.countEmailDomains(null);
        Assert.assertEquals("Null input should return an empty response", Collections.EMPTY_LIST, result);
    }

    @Test
    public void properlyFormattedEmailsOnly() {
        testInput.add("testemail@aol.com");
        testInput.add("testemail@aol.com");
        testInput.add("testemail@aolcom"); // Invalid
        testInput.add("testemail.aol.com"); // Invalid
        testInput.add("testemail@yahoo.com");
        testInput.add("testemail3"); // Invalid
        List<String> result = emailDomainCounter.countEmailDomains(testInput);
        Assert.assertEquals("Only two of the input domains should be valid", 2, result.size());
        Assert.assertEquals( "There are two valid emails with the aol.com domain", "aol.com 2", result.get(0));
        Assert.assertEquals("There is one valid email with the yahoo.com domain", "yahoo.com 1", result.get(1));
    }

    @Test
    public void outputMax10ItemsTest() {
        for (int i = 0; i <= 50; i++) {
            testInput.add("testaddress@testdomain" + i + ".com");
        }
        List<String> result = emailDomainCounter.countEmailDomains(testInput);
        Assert.assertEquals("Output should be limit to 10 items", 10, result.size());

    }

    @Test
    public void multipleEqualDomainsTest() {
        for (int i = 0; i < 30; i++) {
            testInput.add(String.format("testemail_1@aol%02d.com", i));
            testInput.add(String.format("testemail_2@aol%02d.com", i));
        }

        List<String> result = emailDomainCounter.countEmailDomains(testInput);
        Assert.assertEquals("Output should be limit to 10 items", 10, result.size());
        Assert.assertEquals("When domains have the same counts they should be sorted by domain", "aol00.com 2", result.get(0));
        Assert.assertEquals("When domains have the same counts they should be sorted by domain", "aol09.com 2", result.get(9));
    }
}
