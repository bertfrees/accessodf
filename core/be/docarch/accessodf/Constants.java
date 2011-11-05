package be.docarch.accessodf;

import java.io.File;


/**
 *
 * @author Bert Frees
 */
public abstract class Constants {

    public static final String LOGGER_NAME = "be.docarch.accessodf";

    public static final String TMP_PREFIX = "accessodf.";
    private static final File TMP_DIRECTORY = new File(System.getProperty("java.io.tmpdir") + File.separator + "accessodf");

    public final static String BERT =  "http://www.docarch.be/bert";

    private final static String DCT =  "http://purl.org/dc/elements/1.1/";
    private final static String FOAF = "http://xmlns.com/foaf/0.1/";
    private final static String EARL = "http://www.w3.org/ns/earl#";
    private final static String XSD =  "http://www.w3.org/2001/XMLSchema#";
    private final static String A11Y = "http://www.docarch.be/accessodf/";

    public final static String A11Y_CHECKER =        A11Y + "Checker";
    public final static String A11Y_DOCUMENT =       A11Y + "types#Document";
    public final static String A11Y_PARAGRAPH =      A11Y + "types#Paragraph";
    public final static String A11Y_SPAN =           A11Y + "types#Span";
    public final static String A11Y_TABLE =          A11Y + "types#Table";
    public final static String A11Y_OBJECT =         A11Y + "types#Object";
    public final static String A11Y_CHECKS =         A11Y + "checks#";
    public final static String A11Y_START =          A11Y + "start";
    public final static String A11Y_END =            A11Y + "end";
    public final static String A11Y_IGNORE =         A11Y + "ignore";
    public final static String A11Y_COUNT =          A11Y + "count";
    public static final String XSD_INTEGER =         XSD + "integer";
    public static final String XSD_STRING =          XSD + "string";
    public static final String XSD_BOOLEAN =         XSD + "boolean";
    public static final String XSD_DATETIME =        XSD + "dateTime";
    public static final String DCT_DATE =            DCT + "date";
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

    public static File getTmpDirectory() {
        if (!TMP_DIRECTORY.isDirectory()) {
            TMP_DIRECTORY.mkdir();
        }
        return TMP_DIRECTORY;
    }
}
