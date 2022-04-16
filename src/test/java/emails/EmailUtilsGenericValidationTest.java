package emails;

import org.junit.Assert;
import org.junit.Test;

import emails.constants.InvalidReason;

public class EmailUtilsGenericValidationTest {

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
    public void slashesInLocalPartInvalid() {
        String email = "test/test@test.com";
        assertNOTValid(email, "Slashes are not valid characters under generic rules", InvalidReason.INVALID_CHARACTERS);
    }

    @Test
    public void singlePartDomainInvalid() {
        String email = "admin@mailserver1";
        assertNOTValid(email, "Under generic rules a single part domain is invalid",
                InvalidReason.NO_TOP_LEVEL_DOMAIN);
    }

    @Test
    public void singleLetterDomainPartValid() {
        String email = "example@s.example";
        assertValid(email, "A domain part can be a single letter");
    }

    @Test
    public void spaceOnlyLocalPartInvalid() {
        String email = "\" \"@example.org";
        assertNOTValid(email, "Spaces cannot be used with generic email rules since these require quotes",
                InvalidReason.HAS_QUOTES);
    }

    @Test
    public void quotedDoubleDotsInvalid() {
        String email = "\"john..doe\"@example.org";
        assertNOTValid(email, "Double dots require quotes which are not valid under generic rules",
                InvalidReason.HAS_QUOTES);
    }

    @Test
    public void withExclamationInvalid() {
        String email = "mailhost!username@example.org";
        assertNOTValid(email, "The '!' symbol in the local part is invalid under generic rules",
                InvalidReason.INVALID_CHARACTERS);
    }

    @Test
    public void veryStrangeInQuotesInvalid() {
        String email = "\"very.(),:;<>[]\\\".VERY.\\\"very@\\\\ \\\"very\\\".unusual\"@strange.example.com";
        assertNOTValid(email, "All of these special characters and the quotes are invalid under generic rules", InvalidReason.HAS_QUOTES);
    }

    @Test
    public void withDivideInvalid() {
        String email = "user%example.com@example.org";
        assertNOTValid(email, "The '%' symbol is not allowed under generic rules",
                InvalidReason.INVALID_CHARACTERS);
    }

    @Test
    public void nonAlphanumericEndingValid() {
        String email = "user-@example.org";
        assertValid(email, "Ending a local part with a non-alphanumeric character is valid");
    }

    @Test
    public void ipV4DomainInvalid() {
        String email = "postmaster@[123.123.123.123]";
        assertNOTValid(email, "A domain with a V4 IP address is invalid under generic rules",
                InvalidReason.V4_IP_DOMAIN);
    }

    @Test
    public void ipV6DomainInvalid() {
        String email = "postmaster@[IPv6:2001:0db8:85a3:0000:0000:8a2e:0370:7334]";
        assertNOTValid(email, "A domain using a V6 IP address is invalid under generic rules",
                InvalidReason.V6_IP_DOMAIN);
    }

    @Test
    public void addressWithCommentsInvalid() {
        String email = "(something)asdafs(asd)@(third)asfl.comms(asfsdfdgsg.asdad)";
        assertNOTValid(email, "Comments (strings in parentheses) are invalid under generic rules",
                InvalidReason.HAS_COMMENTS);
    }

    @Test
    public void addressWithCommentsAndSpecialCharactersIsInvalid() {
        String email = "(some@thing)asdafs(two words)@(third)asfl.comms(asfsdf@dgsg.asdad)";
        assertNOTValid(email, "Comments are invalid under generic rules", InvalidReason.HAS_COMMENTS);
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
    public void unclosedCommentInvalid() {
        String email = "(notallowed@example.com";
        assertNOTValid(email, "An unclosed parenthesis is not allowed",
                InvalidReason.UNCLOSED_PARENTHESIS);
    }

    @Test
    public void incorrectQuotesInvalid() {
        String email = "just\"not\"right@example.com";
        assertNOTValid(email, "Quotes are invalid under generic rules",
                InvalidReason.HAS_QUOTES);
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
        Assert.assertTrue(testMessage, EmailUtils.isValid(email));
        Assert.assertNull(EmailUtils.invalidReason(email));
    }

    private void assertNOTValid(String email, String testMessage, InvalidReason reason) {
        Assert.assertTrue(testMessage, EmailUtils.isNotValid(email));
        Assert.assertEquals(reason, EmailUtils.invalidReason(email));
    }

}
