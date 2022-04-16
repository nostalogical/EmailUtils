package emails.constants;

public enum InvalidReason {

    // Always invalid
    BLANK,
    NO_AT_SYMBOL,
    MULTIPLE_AT_SYMBOLS,
    LOCAL_PART_TOO_LONG,
    UNCLOSED_PARENTHESIS,
    UNCLOSED_QUOTE,
    INVALID_CHARACTERS,
    DOMAIN_QUOTES,
    UNDERSCORES,
    CONSECUTIVE_DOTS,
    EDGE_DOT,
    DOMAIN_EDGE_HYPHEN,

    // Optionally invalid
    NO_TOP_LEVEL_DOMAIN,
    V4_IP_DOMAIN,
    V6_IP_DOMAIN,
    HAS_QUOTES,
    HAS_COMMENTS,
    HAS_SUB_ADDRESS,
    HAS_DOTS,

}
