package emails;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import emails.processors.EmailValidationConfig;

public class EmailListUtilsTest {

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
    public void basicValidListTest() {
        List<String> result = EmailListUtils.validateEmails(TEST_LIST1);
        Assert.assertEquals(11, result.size());
        Assert.assertEquals("DUPLIcate@duplicate.com", result.get(0));
        Assert.assertEquals("a1+subaddress@emample.com", result.get(1));
        Assert.assertEquals("a1@emample.com", result.get(2));
        Assert.assertEquals("z3@java.net", result.get(10));
    }

    @Test
    public void validWithStrictListTest() {
        List<String> result = EmailListUtils.validateEmails(TEST_LIST1, EmailValidationConfig.strict(), null);
        Assert.assertEquals(15, result.size());
        Assert.assertEquals("\"   \"@java.net", result.get(0));
        Assert.assertEquals("\"multi\".part@java.net", result.get(1));
        Assert.assertEquals("DUPLIcate@duplicate.com", result.get(2));
        Assert.assertEquals("z3@java.net", result.get(14));
    }

    @Test
    public void basicDeduplicateListTest() {
        List<String> result = EmailListUtils.deduplicateEmails(TEST_LIST1);
        Assert.assertEquals(9, result.size());
        Assert.assertEquals("DUPLIcate@duplicate.com", result.get(0));
        Assert.assertEquals("a1+subaddress@emample.com", result.get(1));
        Assert.assertEquals("a1@emample.com", result.get(2));
        Assert.assertEquals("z3@java.net", result.get(8));
    }

    @Test
    public void deduplicateLowerCaseListTest() {
        List<String> result = EmailListUtils.deduplicateEmails(TEST_LIST1, true);
        Assert.assertEquals(8, result.size());
        Assert.assertEquals("a1+subaddress@emample.com", result.get(0));
        Assert.assertEquals("a1@emample.com", result.get(1));
        Assert.assertEquals("z3@java.net", result.get(7));
    }

    @Test
    public void deduplicateLowerCaseNoSubAddressesListTest() {
        List<String> result = EmailListUtils.deduplicateEmails(TEST_LIST1, true, true);
        Assert.assertEquals(6, result.size());
        Assert.assertEquals("a1@emample.com", result.get(0));
        Assert.assertEquals("z3@java.net", result.get(5));
    }

}
