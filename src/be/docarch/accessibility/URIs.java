package be.docarch.accessibility;

/**
 *
 * @author Bert Frees
 */
public class URIs {

    public final static String CHECKER =  "http://www.docarch.be/accessibility";
    public final static String BERT =  "http://www.docarch.be/bert";

    private final static String RDF =      "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    private final static String DCT =      "http://purl.org/dc/elements/1.1/";
    private final static String FOAF =     "http://xmlns.com/foaf/0.1/";
    private final static String EARL =     "http://www.w3.org/ns/earl#";
    private final static String XSD =      "http://www.w3.org/2001/XMLSchema#";
    private final static String CHECKER_TYPES =      CHECKER + "/types#";

    public final static String CHECKER_CHECKS =      CHECKER + "/checks#";
    public final static String CHECKER_DOCUMENT =    CHECKER_TYPES + "document";
    public final static String CHECKER_PARAGRAPH =   CHECKER_TYPES + "paragraph";
    public final static String CHECKER_SPAN =        CHECKER_TYPES + "span";
    public final static String CHECKER_TABLE =       CHECKER_TYPES + "table";
    public final static String CHECKER_OBJECT =      CHECKER_TYPES + "object";
    public final static String CHECKER_START =       CHECKER + "/start";
    public final static String CHECKER_END =         CHECKER + "/end";
    public final static String CHECKER_NAME =        CHECKER + "/name";
    public final static String CHECKER_INDEX =       CHECKER + "/index";
    public final static String CHECKER_LASTCHECKED = CHECKER + "/lastChecked";
    public final static String CHECKER_IGNORE =      CHECKER + "/ignore";
    public final static String CHECKER_SAMPLE =      CHECKER + "/sample";
    public static final String RDF_TYPE =            RDF + "type";
    public static final String RDF_DATATYPE =        RDF + "datatype";
    public static final String XSD_INTEGER =         XSD + "integer";
    public static final String XSD_STRING =          XSD + "string";
    public static final String XSD_BOOLEAN =         XSD + "boolean";
    public static final String XSD_DATETIME =        XSD + "dateTime";
    public static final String DCT_TITLE =           DCT + "title";
    public static final String DCT_DESCRIPTION =     DCT + "description";
    public static final String DCT_HASVERSION =      DCT + "hasVersion";
    public static final String FOAF_GROUP =          FOAF + "Group";
    public static final String FOAF_MEMBER =         FOAF + "member";
    public static final String FOAF_PERSON =         FOAF + "Person";
    public static final String FOAF_NAME =           FOAF + "name";
    public static final String FOAF_HOMEPAGE =       FOAF + "homepage";
    public static final String EARL_ASSERTOR =       EARL + "Assertor";
    public static final String EARL_MAINASSERTOR =   EARL + "mainAssertor";
    public static final String EARL_SOFTWARE =       EARL + "Software";
    public static final String EARL_TESTSUBJECT =    EARL + "TestSubject";
    public static final String EARL_TESTRESULT =     EARL + "TestResult";
    public static final String EARL_TESTCASE =       EARL + "TestCase";
    public static final String EARL_ASSERTION =      EARL + "Assertion";
    public static final String EARL_OUTCOME =        EARL + "outcome";
    public static final String EARL_RESULT =         EARL + "result";
    public static final String EARL_TEST =           EARL + "test";
    public static final String EARL_SUBJECT =        EARL + "subject";
    public static final String EARL_ASSERTEDBY =     EARL + "assertedBy";
    public static final String EARL_FAILED =         EARL + "failed";
    public static final String EARL_PASSED =         EARL + "passed";

}
