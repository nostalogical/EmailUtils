# Email Parser

### Overview

This utility allows individual and lists of email addresses to be parsed for validity and display preference, and for 
more detailed analysis of lists of addresses. 

#### Main features:
* Identify valid email addresses
* Extract email domains
* Deduplicate email addresses
* Identify & remove sub-addresses (such as "example+subaddress@gmail.com")
* Apply custom validation and parsing rules

#### Background

This project originally began with a very specific purpose of reading in a list of email addresses from a file, 
aggregating their domains, and returning a list of the top X domains along with their email counts.

After fulfilling its original purpose this has since been revisited to allow more thorough and configurable analysis to
be done to single email addresses and lists of them. To keep the project lightweight this done with pure java.

### Using the Code

There are 3 helper utility classes which allow quick analysis of email addresses. These are mainly wrappers around 
functions from `EmailContext` and `EmailListContext`, so additional functionality is possible through direct instantiation
of these context classes.

#### EmailUtils
This class focuses on single email addresses, allowing validity and the existence of certain email parts to be checked.
This also contains functions for stripping certain parts from an email.

#### EmailListUtils
This class contains functions for processing a list of emails, primarily removing invalid email addresses and optionally 
removing duplicates, based on customisable configuration.

#### EmailListAnalytics
This class focuses on providing analytical data for lists of emails, such as listing domains with or without the number
of email addresses attached to them, and also exposing lists of the general break down of email addresses or their 
domains.

### Configuration
Configuration of email address lists functions can be done via the classes `EmailValidationConfig` and 
`EmailParserConfig`. A default form of these configuration classes is always applied when emails are parsed, but if this
configuration is unsuitable a custom config can be created through their build methods, and applied to any method that 
takes a config class as an argument. 

The **validation** configuration covers what parts of an email are considered valid, primarily covering the parts 
mentioned in [Terminology](#Terminology) below. This can also be used to specify which printable characters (those in the main body 
of the email address) and special characters (those only allowed in quotes, if enabled) are permitted.

The **parser** configuration controls how emails are displayed after being processed. For example, by default 
sub-addresses are enabled, but if these are excluded in the parser any email addresses processed by this utility will 
have any sub-addresses removed. The parser also controls which characters are considered sub-address delimiters.

Parsing rules are separate to validation rules, so for example all emails parts could be allowed in the validator but 
stripped out when parsing email addresses by disabling them in the parser.

#### Strict and Generic


According to formal standards for email addresses (see [Terminology](#Terminology)) there is quite a lot of flexibility 
in what's considered a valid email address, such as quotes containing spaces and special characters, comments in brackets, 
and IP addresses for domains.

This utility has aimed to properly support the RFC5322 specifications for valid email addresses, and does so through 
`EmailValidationConfig.strict()` which strictly follows the specification, allowing everything it allows.

In practice what's commonly considered as a valid email address is likely to differ a lot from this specification 
though, it's likely a many users wanting to parse email addresses won't want to accept an IP address for a domain or 
comments, so more restrictive common rules can be applied through `EmailValidationConfig.generic()`.

Generally where not specified, most function for parsing or analysing email lists will use the generic configuration.

### Terminology

Internally when parsing an email address, the address is being split into its component parts for analysis. These 
components are:
* Local part
    * E.g. "test" in the email address `test@example.com`
* Domain
    * E.g. "example.com" in the email address `test@example.com`
* Comments
  * Comments can be included in parentheses at the start or end of both the local part and domain of an email address. 
  Therefore, each email address can have up to four comments.
  * E.g. `(comment)test(other comment)@(third)example.com(fourth)`
* Sub-address
  * A string at the end of a local part separated by a character, such as `+`, which is usually used to create alternate 
  emails from a single mailbox address.
  * E.g. "+subaddress" in `test+subaddress@example.com`
* Quotes
  * Double quotes are technically supported in email addresses, and if so special characters not usually allowed in an email
  address can be used within them.
  * E.g. `"address in quotes @nd special characters"@example.com`
* Dot
  * A dot/full stop/period character, e.g. in `test.name@example.com`

These definitions are mainly taken from the email address standards outlined in 
[RFC5322](https://www.ietf.org/rfc/rfc5322.txt).
