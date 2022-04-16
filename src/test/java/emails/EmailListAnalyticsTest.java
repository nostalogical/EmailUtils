package emails;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class EmailListAnalyticsTest {

    private static final List<String> TEST_LIST1 = Arrays.asList(
            "invalidemail",
            "valid@emample.com",
            "a1@emamplf.com",
            "a1@emample.com",
            "a1+subaddress@emample.com",
            "a1valid@emample.com",
            "(comment)rules@address.com",
            "two@rules@address.com",
            "z3@java.net",
            "\"   \"@java.net",
            "\"multi\".part(with comments)@(more comments)java.net",
            "ip.address@[172.103.10.254]",
            "duplicate@duplicate.com",
            "duplicate@duplicate.com",
            "DUPLIcate@duplicate.com",
            "duplicate+subaddress@duplicate.com",
            "duplicate+subaddress@duplicate.com"
    );

    @Test
    public void listDomainsTest() {
        List<String> result = EmailListAnalytics.listDomains(TEST_LIST1);
        Assert.assertEquals(4, result.size());
        Assert.assertEquals("duplicate.com", result.get(0));
        Assert.assertEquals("emample.com", result.get(1));
        Assert.assertEquals("emamplf.com", result.get(2));
        Assert.assertEquals("java.net", result.get(3));
    }

    @Test
    public void countDomainsTest() {
        Assert.assertEquals(4, EmailListAnalytics.countUniqueDomains(TEST_LIST1));
    }

    @Test
    public void resultCountAndOrderTest() {
        List<String> testInput = new ArrayList<>();
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
        List<String> result = EmailListAnalytics.listDomainsByCount(testInput, 10);
        Assert.assertEquals(5, result.size());
        Assert.assertEquals("aol.com 5", result.get(0));
        Assert.assertEquals("yahoo.com 4", result.get(1));
        Assert.assertEquals("gmail.com 3", result.get(2));
        Assert.assertEquals("domain.cc 2", result.get(3));
        Assert.assertEquals("test.net 1", result.get(4));
    }

    @Test
    public void domainCountCaseInsensitiveTest() {
        List<String> testInput = new ArrayList<>();
        testInput.add("testemail@aol.com");
        testInput.add("TESTEMAIL@AOL.com");
        testInput.add("TESTemail@aol.COM");
        testInput.add("testemail@yahoo.com");
        List<String> result = EmailListAnalytics.listDomainsByCount(testInput, 10);
        Assert.assertEquals("Domain counting should be case insensitive", "aol.com 3", result.get(0));
    }

    @Test
    public void emptyInputReturnsEmptyResponse() {
        List<String> result = EmailListAnalytics.listDomainsByCount(Collections.emptyList(), 10);
        Assert.assertEquals("Empty input should return an empty response", Collections.EMPTY_LIST, result);
    }

    @Test
    public void nullInputReturnsEmptyResponse() {
        List<String> result = EmailListAnalytics.listDomainsByCount(null, 10);
        Assert.assertEquals("Null input should return an empty response", Collections.EMPTY_LIST, result);
    }

    @Test
    public void properlyFormattedEmailsOnly() {
        List<String> testInput = new ArrayList<>();
        testInput.add("testemail@aol.com");
        testInput.add("testemail@aol.com");
        testInput.add("testemail@aolcom"); // Invalid
        testInput.add("testemail.aol.com"); // Invalid
        testInput.add("testemail@yahoo.com");
        testInput.add("testemail3"); // Invalid
        List<String> result = EmailListAnalytics.listDomainsByCount(testInput, 10);
        Assert.assertEquals("Only two of the input domains should be valid", 2, result.size());
        Assert.assertEquals( "There are two valid emails with the aol.com domain", "aol.com 2", result.get(0));
        Assert.assertEquals("There is one valid email with the yahoo.com domain", "yahoo.com 1", result.get(1));
    }

    @Test
    public void outputMax10ItemsTest() {
        List<String> testInput = new ArrayList<>();
        for (int i = 0; i <= 50; i++) {
            testInput.add("testaddress@testdomain" + i + ".com");
        }
        List<String> result = EmailListAnalytics.listDomainsByCount(testInput, 10);
        Assert.assertEquals("Output should be limit to 10 items", 10, result.size());

    }

    @Test
    public void multipleEqualDomainsTest() {
        List<String> testInput = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            testInput.add(String.format("testemail-1@aol%02d.com", i));
            testInput.add(String.format("testemail-2@aol%02d.com", i));
        }

        List<String> result = EmailListAnalytics.listDomainsByCount(testInput, 10);
        Assert.assertEquals("Output should be limit to 10 items", 10, result.size());
        Assert.assertEquals("When domains have the same counts they should be sorted by domain", "aol00.com 2", result.get(0));
        Assert.assertEquals("When domains have the same counts they should be sorted by domain", "aol09.com 2", result.get(9));
    }

}
