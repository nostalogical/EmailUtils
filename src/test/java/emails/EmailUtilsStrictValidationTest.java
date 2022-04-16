package emails;

import org.junit.Assert;
import org.junit.Test;

import emails.constants.InvalidReason;
import emails.processors.EmailValidationConfig;

public class EmailUtilsStrictValidationTest {

    private static final EmailValidationConfig config = EmailValidationConfig.strict();

    @Test
    public void simpleValid() {
        String email = "simple@example.com";
        assertValid(email, "A very standard email format should be valid");
    }

    @Test
    public void simpleWithDotsValid() {
        String email = "very.common@example.com";
        assertValid(email, "Dots in the local part are valid");
    }

    @Test
    public void subAddressedEmailValid() {
        String email = "disposable.style.email.with+symbol@example.com";
        assertValid(email, "An email address with a sub-address (using '+') is valid");
    }

    @Test
    public void withHyphenValid() {
        String email = "fully-qualified-domain@example.com";
        assertValid(email, "Hyphens in the local part are valid");
    }

    @Test
    public void withHyphensAndDotsValid() {
        String email = "other.email-with-hyphen@example.com";
        assertValid(email, "Hyphens and dots in the local part are valid");
    }

    @Test
    public void multiplePlusesValid() {
        String email = "user.name+tag+sorting@example.com";
        assertValid(email, "Several + symbols in the local part are valid");
    }

    @Test
    public void singleLetterLocalPartValid() {
        String email = "x@example.com";
        assertValid(email, "A single letter local part is valid");
    }

    @Test
    public void hyphensInLocalPartAndDomainValid() {
        String email = "example-indeed@strange-example.com";
        assertValid(email, "An address with hyphens in both the local part and domain is valid");
    }

    @Test
    public void slashesInLocalPartValid() {
        String email = "test/test@test.com";
        assertValid(email, "Slashes are printable characters and so valid in a local part");
    }

    @Test
    public void singlePartDomainValid() {
        String email = "admin@mailserver1";
        assertValid(email, "Although discouraged, a dotless domain is technically valid");
    }

    @Test
    public void singleLetterDomainPartValid() {
        String email = "example@s.example";
        assertValid(email, "A domain part can be a single letter");
    }

    @Test
    public void spaceOnlyLocalPartValid() {
        String email = "\" \"@example.org";
        assertValid(email, "The local part can be only special characters if it's in quotes");
    }

    @Test
    public void quotedDoubleDotsValid() {
        String email = "\"john..doe\"@example.org";
        assertValid(email, "Double dots are allowed in quotes");
    }

    @Test
    public void withExclamationValid() {
        String email = "mailhost!username@example.org";
        assertValid(email, "The '!' symbol in the local part is valid");
    }

    @Test
    public void veryStrangeInQuotesValid() {
        String email = "\"very.(),:;<>[]\\\".VERY.\\\"very@\\\\ \\\"very\\\".unusual\"@strange.example.com";
        assertValid(email, "Many special characters in the local part are valid if properly quoted and escaped");
    }

    @Test
    public void withDivideValid() {
        String email = "user%example.com@example.org";
        assertValid(email, "The '%' symbol in the local part is valid");
    }

    @Test
    public void nonAlphanumericEndingValid() {
        String email = "user-@example.org";
        assertValid(email, "Ending a local part with a non-alphanumeric character is valid");
    }

    @Test
    public void ipV4DomainValid() {
        String email = "postmaster@[123.123.123.123]";
        assertValid(email, "A domain with a V4 IP address in brackets is valid");
    }

    @Test
    public void ipV6DomainValid() {
        String email = "postmaster@[IPv6:2001:0db8:85a3:0000:0000:8a2e:0370:7334]";
        assertValid(email, "A domain using a V6 IP address is valid");
    }

    @Test
    public void addressWithCommentsIsValid() {
        String email = "(something)asdafs(asd)@(third)asfl.comms(asfsdfdgsg.asdad)";
        assertValid(email, "Comments (strings in parentheses) are valid");
    }

    @Test
    public void addressWithCommentsAndSpecialCharactersIsValid() {
        String email = "(some@thing)asdafs(two words)@(third)asfl.comms(asfsdf@dgsg.asdad)";
        assertValid(email, "Special characters, including @ symbols, are valid in comments");
    }

    @Test
    public void atMissingInvalid() {
        String email = "Abc.example.com";
        assertNOTValid(email, "An @ symbol is required", InvalidReason.NO_AT_SYMBOL);
    }

    @Test
    public void atMissingWithCommentsInvalid() {
        String email = "(some@thing)asdafs(two words)(third)asfl.comms(asfsdf@dgsg.asdad)";
        assertNOTValid(email, "An @ symbols must exist outside comments", InvalidReason.NO_AT_SYMBOL);
    }

    @Test
    public void multipleAtInvalid() {
        String email = "A@b@c@example.com";
        assertNOTValid(email, "Multiple @ symbols (without quotations) are invalid", InvalidReason.MULTIPLE_AT_SYMBOLS);
    }

    @Test
    public void unquotedSpecialCharactersInvalid() {
        String email = "a\"b(c)d,e:f;g<h>i[j\\k]l@example.com";
        assertNOTValid(email, "These special characters are invalid outside quotation marks",
                InvalidReason.UNCLOSED_QUOTE);
    }

    @Test
    public void unquotedSpacesInvalid() {
        String email = "this isnotallowed@example.com";
        assertNOTValid(email, "Space are not allowed outside quotes",
                InvalidReason.INVALID_CHARACTERS);
    }

    @Test
    public void additionalUnquotedSpecialCharactersInvalid() {
        String email = "this is\"not\\allowed@example.com";
        assertNOTValid(email, "Spaces, quotes, and backslashes must be escaped and within quotes",
                InvalidReason.UNCLOSED_QUOTE);
    }

    @Test
    public void escapedButUnquotedInvalid() {
        String email = "this\\ still\\\"not\\\\allowed@example.com";
        assertNOTValid(email, "Even with backslash escapes, spaces, quotes, and backslashes are invalid if not in quotes",
                InvalidReason.UNCLOSED_QUOTE);
    }

    @Test
    public void unclosedCommentIsInvalid() {
        String email = "(notallowed@example.com";
        assertNOTValid(email, "An unclosed parenthesis is not allowed",
                InvalidReason.UNCLOSED_PARENTHESIS);
    }

    @Test
    public void incorrectQuotesInvalid() {
        String email = "just\"not\"right@example.com";
        assertNOTValid(email, "Quotes must make up the entire local part, or be separated from the rest by dots",
                InvalidReason.INVALID_CHARACTERS);
    }

    @Test
    public void localPartOverCharacterLimitInvalid() {
        String email = "1234567890123456789012345678901234567890123456789012345678901234+x@example.com";
        assertNOTValid(email, "The local part of the email address cannot be longer than 64 characters",
                InvalidReason.LOCAL_PART_TOO_LONG);
    }

    @Test
    public void underscoresInDomainInvalid() {
        String email = "i_like_underscore@but_its_not_allowed_in_this_part.example.com";
        assertNOTValid(email, "Underscores in the domain are invalid", InvalidReason.INVALID_CHARACTERS);
    }

    @Test
    public void squareBracketsInvalid() {
        String email = "QA[icon]CHOCOLATE[icon]@test.com";
        assertNOTValid(email, "Square brackets are only allowed in a quoted string", InvalidReason.INVALID_CHARACTERS);
    }

    private void assertValid(String email, String testMessage) {
        Assert.assertTrue(testMessage, EmailUtils.isValid(email, config));
        Assert.assertNull(EmailUtils.invalidReason(email, config));
    }

    private void assertNOTValid(String email, String testMessage, InvalidReason reason) {
        Assert.assertTrue(testMessage, EmailUtils.isNotValid(email, config));
        Assert.assertEquals(reason, EmailUtils.invalidReason(email, config));
    }

}
