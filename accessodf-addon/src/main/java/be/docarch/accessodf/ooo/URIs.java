/**
 *  AccessODF - Accessibility checker for OpenOffice.org and LibreOffice Writer.
 *
 *  Copyright (c) 2011 by DocArch <http://www.docarch.be>.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package be.docarch.accessodf.ooo;

import com.sun.star.uno.XComponentContext;
import com.sun.star.rdf.URI;
import com.sun.star.rdf.XURI;

import com.sun.star.lang.IllegalArgumentException;

import be.docarch.accessodf.Constants;

/**
 *
 * @author Bert Frees
 */
public abstract class URIs {

    public static XURI RDF_TYPE;
    public static XURI RDFS_SUBCLASSOF;

    public static XURI EARL_TESTSUBJECT;
    public static XURI EARL_TESTRESULT;
    public static XURI EARL_TESTCASE;
    public static XURI EARL_ASSERTION;
    public static XURI EARL_ASSERTOR;
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

    public static XURI DCT_DATE;
    public static XURI DCT_TITLE;
    public static XURI DCT_DESCRIPTION;

    public static XURI A11Y_CHECKER;
    public static XURI A11Y_START;
    public static XURI A11Y_END;
    public static XURI A11Y_DOCUMENT;
    public static XURI A11Y_PARAGRAPH;
    public static XURI A11Y_SPAN;
    public static XURI A11Y_TABLE;
    public static XURI A11Y_OBJECT;
    public static XURI A11Y_IGNORE;
    public static XURI A11Y_COUNT;

    public static void init(XComponentContext context) {

        try {
            
            RDF_TYPE = URI.createKnown(context, com.sun.star.rdf.URIs.RDF_TYPE);
            RDFS_SUBCLASSOF = URI.createKnown(context, com.sun.star.rdf.URIs.RDFS_SUBCLASSOF);

            EARL_TESTSUBJECT = URI.create(context, Constants.EARL_TESTSUBJECT);
            EARL_TESTRESULT = URI.create(context, Constants.EARL_TESTRESULT);
            EARL_TESTCASE = URI.create(context, Constants.EARL_TESTCASE);
            EARL_ASSERTION = URI.create(context, Constants.EARL_ASSERTION);
            EARL_ASSERTOR = URI.create(context, Constants.EARL_ASSERTOR);
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

            DCT_DATE = URI.create(context, Constants.DCT_DATE);
            DCT_TITLE = URI.create(context, Constants.DCT_TITLE);
            DCT_DESCRIPTION = URI.create(context, Constants.DCT_DESCRIPTION);

            A11Y_CHECKER = URI.create(context, Constants.A11Y_CHECKER);
            A11Y_START = URI.create(context, Constants.A11Y_START);
            A11Y_END = URI.create(context, Constants.A11Y_END);
            A11Y_IGNORE = URI.create(context, Constants.A11Y_IGNORE);
            A11Y_COUNT = URI.create(context, Constants.A11Y_COUNT);
            A11Y_DOCUMENT = URI.create(context, Constants.A11Y_DOCUMENT);
            A11Y_PARAGRAPH = URI.create(context, Constants.A11Y_PARAGRAPH);
            A11Y_SPAN = URI.create(context, Constants.A11Y_SPAN);
            A11Y_TABLE = URI.create(context, Constants.A11Y_TABLE);
            A11Y_OBJECT = URI.create(context, Constants.A11Y_OBJECT);

        } catch (IllegalArgumentException e) {
        }
    }
}
