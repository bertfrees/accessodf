package be.docarch.accessibility.ooo;

import com.sun.star.uno.XComponentContext;
import com.sun.star.rdf.URI;
import com.sun.star.rdf.XURI;

import com.sun.star.lang.IllegalArgumentException;

import be.docarch.accessibility.Constants;

/**
 *
 * @author Bert Frees
 */
public abstract class URIs {

    public static XURI CHECKER;

    public static XURI RDF_TYPE;

    public static XURI EARL_TESTSUBJECT;
    public static XURI EARL_TESTRESULT;
    public static XURI EARL_TESTCASE;
    public static XURI EARL_ASSERTION;
    public static XURI EARL_OUTCOME;
    public static XURI EARL_FAILED;
    public static XURI EARL_PASSED;
    public static XURI EARL_RESULT;
    public static XURI EARL_TEST;
    public static XURI EARL_SUBJECT;
    public static XURI EARL_ASSERTEDBY;    
    public static XURI EARL_MAINASSERTOR;
    public static XURI EARL_SOFTWARE;

    public static XURI FOAF_GROUP;
    public static XURI FOAF_MEMBER;
    public static XURI FOAF_PERSON;
    public static XURI FOAF_NAME;

    public static XURI CHECKER_START;
    public static XURI CHECKER_END;
    public static XURI CHECKER_NAME;
    public static XURI CHECKER_SAMPLE;
    public static XURI CHECKER_CHECKS;
    public static XURI CHECKER_DOCUMENT;
    public static XURI CHECKER_PARAGRAPH;
    public static XURI CHECKER_SPAN;
    public static XURI CHECKER_TABLE;
    public static XURI CHECKER_OBJECT;
    public static XURI CHECKER_LASTCHECKED;
    public static XURI CHECKER_IGNORE;
    public static XURI CHECKER_INDEX;

    public static void init(XComponentContext context) {

        try {

            CHECKER = URI.create(context, Constants.CHECKER);

            RDF_TYPE = URI.create(context, Constants.RDF_TYPE);

            EARL_TESTSUBJECT = URI.create(context, Constants.EARL_TESTSUBJECT);
            EARL_TESTRESULT = URI.create(context, Constants.EARL_TESTRESULT);
            EARL_TESTCASE = URI.create(context, Constants.EARL_TESTCASE);
            EARL_ASSERTION = URI.create(context, Constants.EARL_ASSERTION);
            EARL_OUTCOME = URI.create(context, Constants.EARL_OUTCOME);
            EARL_RESULT = URI.create(context, Constants.EARL_RESULT);
            EARL_TEST = URI.create(context, Constants.EARL_TEST);
            EARL_SUBJECT = URI.create(context, Constants.EARL_SUBJECT);
            EARL_ASSERTEDBY = URI.create(context, Constants.EARL_ASSERTEDBY);
            EARL_FAILED = URI.create(context, Constants.EARL_FAILED);
            EARL_PASSED = URI.create(context, Constants.EARL_PASSED);
            EARL_MAINASSERTOR = URI.create(context, Constants.EARL_MAINASSERTOR);
            EARL_SOFTWARE = URI.create(context, Constants.EARL_SOFTWARE);

            FOAF_GROUP = URI.create(context, Constants.FOAF_GROUP);
            FOAF_MEMBER = URI.create(context, Constants.FOAF_MEMBER);
            FOAF_PERSON = URI.create(context, Constants.FOAF_PERSON);
            FOAF_NAME = URI.create(context, Constants.FOAF_NAME);

            CHECKER_START = URI.create(context, Constants.CHECKER_START);
            CHECKER_END = URI.create(context, Constants.CHECKER_END);
            CHECKER_NAME = URI.create(context, Constants.CHECKER_NAME);
            CHECKER_SAMPLE = URI.create(context, Constants.CHECKER_SAMPLE);
            CHECKER_LASTCHECKED = URI.create(context, Constants.CHECKER_LASTCHECKED);
            CHECKER_IGNORE = URI.create(context, Constants.CHECKER_IGNORE);
            CHECKER_CHECKS = URI.create(context, Constants.CHECKER_CHECKS);
            CHECKER_DOCUMENT = URI.create(context, Constants.CHECKER_DOCUMENT);
            CHECKER_PARAGRAPH = URI.create(context, Constants.CHECKER_PARAGRAPH);
            CHECKER_SPAN = URI.create(context, Constants.CHECKER_SPAN);
            CHECKER_TABLE = URI.create(context, Constants.CHECKER_TABLE);
            CHECKER_OBJECT = URI.create(context, Constants.CHECKER_OBJECT);
            CHECKER_INDEX = URI.create(context, Constants.CHECKER_INDEX);

        } catch (IllegalArgumentException e) {
        }
    }
}
