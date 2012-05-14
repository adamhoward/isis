/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.isis.runtimes.dflt.objectstores.sql.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;

import org.apache.isis.applib.value.Color;
import org.apache.isis.applib.value.Date;
import org.apache.isis.applib.value.DateTime;
import org.apache.isis.applib.value.Money;
import org.apache.isis.applib.value.Password;
import org.apache.isis.applib.value.Percentage;
import org.apache.isis.applib.value.Time;
import org.apache.isis.applib.value.TimeStamp;
import org.apache.isis.runtimes.dflt.objectstores.sql.common.SqlIntegrationTestFixtures.State;
import org.apache.isis.tck.dom.sqlos.SqlDomainObjectRepository;
import org.apache.isis.tck.dom.sqlos.data.NumericTestClass;
import org.apache.isis.tck.dom.sqlos.data.SimpleClass;
import org.apache.isis.tck.dom.sqlos.data.SimpleClassTwo;
import org.apache.isis.tck.dom.sqlos.data.SqlDataClass;

/**
 * @author Kevin kevin@kmz.co.za
 * 
 *         The Singleton class {@link SqlIntegrationTestFixtures} is used to
 *         preserve values between tests.
 *         
 */
public abstract class SqlIntegrationTestData extends SqlIntegrationTestCommonBase {
    
    private static final Logger LOG = Logger.getLogger(SqlIntegrationTestData.class);

    private static List<SimpleClass> simpleClassList1 = new ArrayList<SimpleClass>();
    private static List<SimpleClass> simpleClassList2 = new ArrayList<SimpleClass>();

    private static SimpleClassTwo simpleClassTwoA;
    private static SimpleClassTwo simpleClassTwoB;

    private static NumericTestClass numericTestClassMax;
    private static NumericTestClass numericTestClassMin;

    @Test
    public void testAll() throws Exception {
        testSetup();
        setUpFactory();

        testCreate();
        testLoad();
        
        setUpFactory();

        testString();
        setStringToDifferentValue();
        testSimpleClassCollection1Lazy();
        testApplibDate();
        testSqlDate();
        //testDateTimezoneIssue();
        testMoney();
        testDateTime();
        testTimeStamp();
        testTime();
        testColor();
        testPassword();
        testPercentage();
        testStandardValueTypesMaxima();
        testStandardValueTypesMinima();
        
        // broken it...
        //testSingleReferenceLazy();
        //testSimpleClassTwoReferenceLazy();
        
        testSimpleClassCollection1();
        testSimpleClassCollection2();
        
        testSingleReferenceResolve();
        testSimpleClassTwoReferenceResolve();
        testSimpleClassTwo();
        testUpdate1();
        testUpdate2();
        testUpdateCollectionIsDirty();
        testFindByMatchString();
        testFindByMatchEntity();
        reinitializeFixtures();
        
    }
    

    private void testSetup() {
        resetPersistenceStoreDirectlyIfRequired();
        getSqlIntegrationTestFixtures().setState(State.INITIALIZE);
    }

    private void testCreate() throws Exception {
        for (final String tableName : Data.getTableNames()) {
            getSqlIntegrationTestFixtures().dropTable(tableName);
        }
        
        sqlDataClass = factory.newDataClass();
        
        sqlDataClass.setString("Test String");
        sqlDataClass.setDate(Data.applibDate);
        sqlDataClass.setSqlDate(Data.sqlDate);
        sqlDataClass.setMoney(Data.money);
        sqlDataClass.setDateTime(Data.dateTime);
        sqlDataClass.setTimeStamp(Data.timeStamp);
        sqlDataClass.setTime(Data.time);
        sqlDataClass.setColor(Data.color);
        sqlDataClass.setImage(Data.image);
        sqlDataClass.setPassword(Data.password);
        sqlDataClass.setPercentage(Data.percentage);

        // Setup SimpleClassTwo
        simpleClassTwoA = factory.newSimpleClassTwo();
        simpleClassTwoA.setText("A");
        simpleClassTwoA.setIntValue(999);
        simpleClassTwoA.setBooleanValue(true);

        simpleClassTwoB = factory.newSimpleClassTwo();
        simpleClassTwoB.setText("B");

        sqlDataClass.setSimpleClassTwo(simpleClassTwoA);

        // NumericClasses
        // standard min types
        numericTestClassMin = factory.newNumericTestClass();
        LOG.log(Level.INFO, "Bits to represent Double: " + Double.SIZE);
        numericTestClassMin.setIntValue(Data.intMinValue);
        numericTestClassMin.setShortValue(Data.shortMinValue);
        numericTestClassMin.setLongValue(Data.longMinValue);
        numericTestClassMin.setDoubleValue(Data.doubleMinValue);
        numericTestClassMin.setFloatValue(Data.floatMinValue);

        sqlDataClass.setNumericTestClassMin(numericTestClassMin);

        // standard max types
        numericTestClassMax = factory.newNumericTestClass();
        numericTestClassMax.setIntValue(Data.intMaxValue);
        numericTestClassMax.setShortValue(Data.shortMaxValue);
        numericTestClassMax.setLongValue(Data.longMaxValue);
        numericTestClassMax.setDoubleValue(Data.doubleMaxValue);
        numericTestClassMax.setFloatValue(Data.floatMaxValue);

        sqlDataClass.setNumericTestClassMax(numericTestClassMax);

        // Initialise collection1
        boolean bMustAdd = false;
        if (simpleClassList1.size() == 0) {
            bMustAdd = true;
        }
        for (final String string : Data.stringList1) {
            final SimpleClass simpleClass = factory.newSimpleClass();
            simpleClass.setString(string);
            simpleClass.setSimpleClassTwoA(simpleClassTwoA);
            sqlDataClass.addToSimpleClasses1(simpleClass);
            if (bMustAdd) {
                simpleClassList1.add(simpleClass);
            }
        }

        // Initialise collection2
        for (final String string : Data.stringList2) {
            final SimpleClass simpleClass = factory.newSimpleClass();
            simpleClass.setString(string);
            simpleClass.setSimpleClassTwoA(simpleClassTwoB);
            sqlDataClass.addToSimpleClasses2(simpleClass);
            if (bMustAdd) {
                simpleClassList2.add(simpleClass);
            }
        }
        factory.save(sqlDataClass);

        setFixtureInitializationState(State.DONT_INITIALIZE, "in-memory");
    }

    private void testLoad() throws Exception {
        final List<SqlDataClass> dataClasses = factory.allDataClasses();
        assertEquals(1, dataClasses.size());
        final SqlDataClass sqlDataClass = dataClasses.get(0);
        getSqlIntegrationTestFixtures().setSqlDataClass(sqlDataClass);
        
        setFixtureInitializationState(State.DONT_INITIALIZE);
    }

    private void testString() {
        assertEquals("Test String", sqlDataClass.getString());
    }

    private void setStringToDifferentValue() {
        sqlDataClass.setString("String 2");
    }

    private void testSimpleClassCollection1Lazy() {
        final List<SimpleClass> collection = sqlDataClass.simpleClasses1;

        assertEquals("collection size is not equal!", collection.size(), simpleClassList1.size());
    }

    /**
     * Test {@link SqlDataClass} {@link Date} field.
     * 
     * @throws Exception
     */
    private void testApplibDate() {

        LOG.log(Level.INFO, "Test: testDate() '2010-3-5' = 1267747200000");

        // 2010-3-5 = 1267747200000
        LOG.log(Level.INFO, "applibDate.dateValue() as String: " + Data.applibDate);
        LOG.log(Level.INFO, "applibDate.dateValue() as Long: " + Data.applibDate.getMillisSinceEpoch());

        // 2010-3-5 = 1267747200000
        LOG.log(Level.INFO, "sqlDataClass.getDate() as String: " + sqlDataClass.getDate());
        LOG.log(Level.INFO, "sqlDataClass.getDate().getTime() as Long: " + sqlDataClass.getDate().getMillisSinceEpoch());

        if (!Data.applibDate.isEqualTo(sqlDataClass.getDate())) {
            fail("Applib date: Test '2010-3-5', expected " + Data.applibDate.toString() + ", but got " + sqlDataClass.getDate().toString() + ". Check log for more info.");
            // LOG.log(Level.INFO, "Applib date: Test '2011-3-5', expected " +
            // applibDate.toString() + ", but got "
            // +
            // sqlDataClass.getDate().toString()+". Check log for more info.");
        } else {
            // LOG.log(Level.INFO,
            // "SQL applib.value.date: test passed! Woohoo!");
        }

    }

    /**
     * Test {@link SqlDataClass} {@link java.sql.Date} field.
     * 
     * @throws Exception
     */
    private void testSqlDate() {

        LOG.log(Level.INFO, "Test: testSqlDate() '2011-4-8' == 1302220800000");

        // 2011-4-8 = 1302220800000
        LOG.log(Level.INFO, "sqlDate.toString() as String:" + Data.sqlDate); // shows
                                                                        // as
                                                                        // 2011-04-07
        LOG.log(Level.INFO, "sqlDate.getTime() as Long:" + Data.sqlDate.getTime());

        // 2011-4-8 = 1302220800000
        LOG.log(Level.INFO, "sqlDataClass.getSqlDate() as String:" + sqlDataClass.getSqlDate()); // shows
                                                                                                 // as
        // 2011-04-07
        LOG.log(Level.INFO, "sqlDataClass.getSqlDate().getTime() as Long:" + sqlDataClass.getSqlDate().getTime());

        if (Data.sqlDate.compareTo(sqlDataClass.getSqlDate()) != 0) {
            fail("SQL date: Test '2011-4-8', expected " + Data.sqlDate.toString() + ", but got " + sqlDataClass.getSqlDate().toString() + ". Check log for more info.");
            // LOG.log(Level.INFO, "SQL date: Test '2011-4-8', expected " +
            // sqlDate.toString() + ", and got "
            // + sqlDataClass.getSqlDate().toString()
            // +". Check log for more info.");
        } else {
            // LOG.log(Level.INFO, "SQL date: test passed! Woohoo!");
        }

    }/**/

    @Ignore
    private void testDateTimezoneIssue() {
        /*
         * At the moment, applib Date and java.sql.Date are restored from
         * ValueSemanticsProviderAbstractTemporal with an explicit hourly offset
         * that comes from the timezone. I.e. in South Africa, with TZ +2h00,
         * they have an implicit time of 02h00 (2AM). This can potentially
         * seriously screw up GMT-X dates, which, I suspect, will actually be
         * set to the dat BEFORE.
         * 
         * This test is a simple test to confirm that date/time before and after
         * checks work as expected.
         */
        /*
         * *
         * SqlDataClass sqlDataClass = SqlIntegrationTestSingleton.getPerson();
         * 
         * DateTime dateTime = sqlDataClass.getDateTime(); // new DateTime(2010,
         * 3, 5, 1, 23); Date date = sqlDataClass.getDate(); // new Date(2010,
         * 3, 5);
         * 
         * //java.sql.Date sqlDate = sqlDataClass.getSqlDate(); // "2010-03-05"
         * //assertTrue("dateTime's value ("+dateTime.dateValue()+ //
         * ") should be after java.sql.date's ("+ sqlDate +")",
         * dateTime.dateValue().after(sqlDate));
         * 
         * assertTrue("dateTime's value ("+dateTime.dateValue()+
         * ") should be after date's ("+ date +")",
         * dateTime.dateValue().after(date.dateValue()));
         */
    }

    /**
     * Test {@link Money} type.
     */
    private void testMoney() {
        assertEquals(Data.money, sqlDataClass.getMoney());
        // assertTrue("Money " + money.toString() + " is not equal to " +
        // sqlDataClass.getMoney().toString(),
        // money.equals(sqlDataClass.getMoney()));
    }

    /**
     * Test {@link DateTime} type.
     */
    private void testDateTime() {

        LOG.log(Level.INFO, "Test: testDateTime()");
        LOG.log(Level.INFO, "sqlDataClass.getDateTime() as String:" + sqlDataClass.getDateTime());
        LOG.log(Level.INFO, "dateTime.toString() as String:" + Data.dateTime);

        LOG.log(Level.INFO, "sqlDataClass.getDateTime().getTime() as Long:" + sqlDataClass.getDateTime().millisSinceEpoch());
        LOG.log(Level.INFO, "dateTime.getTime() as Long:" + Data.dateTime.millisSinceEpoch());

        if (!Data.dateTime.equals(sqlDataClass.getDateTime())) {
            fail("DateTime " + Data.dateTime.toString() + " is not equal to " + sqlDataClass.getDateTime().toString());
        }
    }

    /**
     * Test {@link TimeStamp} type.
     */
    private void testTimeStamp() {
        assertTrue("TimeStamp " + Data.timeStamp.toString() + " is not equal to " + sqlDataClass.getTimeStamp().toString(), Data.timeStamp.isEqualTo(sqlDataClass.getTimeStamp()));
    }

    /**
     * Test {@link Time} type.
     */
    /**/
    private void testTime() {
        assertNotNull("sqlDataClass is null", sqlDataClass);
        assertNotNull("getTime() is null", sqlDataClass.getTime());
        assertTrue("Time 14h56: expected " + Data.time.toString() + ", but got " + sqlDataClass.getTime().toString(), Data.time.isEqualTo(sqlDataClass.getTime()));
    }

    /**
     * Test {@link Color} type.
     */
    private void testColor() {
        assertEquals(Data.color, sqlDataClass.getColor());
        // assertTrue("Color Black, expected " + color.toString() + " but got "
        // + sqlDataClass.getColor().toString(),
        // color.isEqualTo(sqlDataClass.getColor()));
    }

    /**
     * Test {@link Image} type.
     */
    // TODO: Images are not equal...
    /*
     * public void testImage(){ SqlDataClass sqlDataClass =
     * SqlIntegrationTestSingleton.getPerson(); Image image2 =
     * sqlDataClass.getImage(); assertEqual(image, image2); }
     * 
     * private void assertEqual(Image image2, Image image3) {
     * assertEquals(image2.getHeight(), image3.getHeight());
     * assertEquals(image2.getWidth(), image3.getWidth()); boolean same = true;
     * int i=0,j=0; int p1=0, p2=0; String error = ""; int [][] i1 =
     * image2.getImage(), i2 = image3.getImage(); for(i = 0; same &&
     * i<image2.getHeight();i++){ int [] r1 = i1[i], r2 = i2[i]; for (j = 0;
     * same && j < image2.getWidth(); j++){ if (r1[j] != r2[j]){ same = false;
     * p1 = r1[j]; p2 = r2[j]; error =
     * "Images differ at i = "+i+", j = "+j+", "+p1+ " is not "+p2+"!"; break; }
     * } } assertTrue(error, same); }
     */

    /**
     * Test {@link Password} type.
     */
    private void testPassword() {
        assertEquals(Data.password, sqlDataClass.getPassword());
    }

    /**
     * Test {@link Percentage} type.
     */
    private void testPercentage() {
        assertEquals(Data.percentage, sqlDataClass.getPercentage());
    }

    private void testStandardValueTypesMaxima() {
        final NumericTestClass numericTestMaxClass = sqlDataClass.getNumericTestClassMax();

        assertEquals(Data.shortMaxValue, numericTestMaxClass.getShortValue());
        assertEquals(Data.intMaxValue, numericTestMaxClass.getIntValue());
        assertEquals(Data.longMaxValue, numericTestMaxClass.getLongValue());
        assertEquals(Data.doubleMaxValue, numericTestMaxClass.getDoubleValue(), 0.00001f); // fails
                                                                            // in
        assertEquals(Data.floatMaxValue, numericTestMaxClass.getFloatValue(), 0.00001f);
    }

    private void testStandardValueTypesMinima() {
        final NumericTestClass numericTestMinClass = sqlDataClass.getNumericTestClassMin();

        assertEquals(Data.shortMinValue, numericTestMinClass.getShortValue());
        assertEquals(Data.intMinValue, numericTestMinClass.getIntValue());
        assertEquals(Data.longMinValue, numericTestMinClass.getLongValue());
        assertEquals(Data.doubleMinValue, numericTestMinClass.getDoubleValue(), 0.00001f); // fails
                                                                            // in
                                                                            // MySQL
                                                                            // =
                                                                            // infinity
        assertEquals(Data.floatMinValue, numericTestMinClass.getFloatValue(), 0.00001f);
    }

    /**
     * Test {@link StringCollection} type.
     */
    /*
     * public void testStringCollection(){ SqlDataClass sqlDataClass =
     * SqlIntegrationTestSingleton.getPerson(); List<String> collection =
     * sqlDataClass.getStringCollection(); int i = 0; for (String string :
     * collection) { assertEquals(SqlIntegrationTestCommon.stringList.get(i++),
     * string); } }
     */

    private void testSingleReferenceLazy() {
        final SimpleClassTwo a = sqlDataClass.getSimpleClassTwo();
        if (!persistenceMechanismIs("in-memory")) {
            assertEquals(null, a.text); // must check direct value, as
            // framework can auto-resolve, if you use getText()
        }
    }

    /**
     * Test a collection of {@link SimpleClass} type.
     */
    private void testSimpleClassCollection1() {
        final List<SimpleClass> collection = sqlDataClass.getSimpleClasses1();

        assertEquals("collection size is not equal!", simpleClassList1.size(), collection.size());

        int i = 0;
        for (final SimpleClass simpleClass : simpleClassList1) {
            assertEquals(simpleClass.getString(), collection.get(i++).getString());
        }
    }

    /**
     * Test another collection of {@link SimpleClass} type.
     */
    private void testSimpleClassCollection2() {
        final List<SimpleClass> collection = sqlDataClass.getSimpleClasses2();

        assertEquals("collection size is not equal!", simpleClassList2.size(), collection.size());

        int i = 0;
        for (final SimpleClass simpleClass : simpleClassList2) {
            assertEquals(simpleClass.getString(), collection.get(i++).getString());
        }
    }

    private void testSimpleClassTwoReferenceLazy() {
        final List<SimpleClass> collection = sqlDataClass.getSimpleClasses1();
        if (getProperties().getProperty("isis.persistor") != "in-memory") {
            for (final SimpleClass simpleClass : collection) {
                final SimpleClassTwo a = simpleClass.getSimpleClassTwoA();
                assertEquals(null, a.text); // must check direct value, as
                                            // framework can auto-resolve, if
                                            // you use getText()
            }
        }
    }

    private void testSingleReferenceResolve() {
        final SimpleClassTwo a = sqlDataClass.getSimpleClassTwo();
        factory.resolve(a);
        assertEquals(simpleClassTwoA.getText(), a.getText());
    }

    private void testSimpleClassTwoReferenceResolve() {
        final List<SimpleClass> collection = sqlDataClass.getSimpleClasses1();
        for (final SimpleClass simpleClass : collection) {
            final SimpleClassTwo a = simpleClass.getSimpleClassTwoA();
            factory.resolve(a);
            assertEquals(simpleClassTwoA.getText(), a.getText());
            assertEquals(simpleClassTwoA.getIntValue(), a.getIntValue());
            assertEquals(simpleClassTwoA.getBooleanValue(), a.getBooleanValue());
        }
    }

    private void testSimpleClassTwo() {
        final SqlDomainObjectRepository factory = getSqlIntegrationTestFixtures().getSqlDataClassFactory();
        final List<SimpleClassTwo> classes = factory.allSimpleClassTwos();
        assertEquals(2, classes.size());
        for (final SimpleClassTwo simpleClass : classes) {
            // assertEquals(simpleClassTwoA.getText(), simpleClass.getText());
            assertTrue("AB".contains(simpleClass.getText()));
        }
    }

    private void testUpdate1() {
        final List<SimpleClassTwo> classes = factory.allSimpleClassTwos();
        assertEquals(2, classes.size());

        final SimpleClassTwo simpleClass = classes.get(0);
        simpleClass.setText("XXX");
        simpleClass.setBooleanValue(false);
        simpleClassTwoA.setBooleanValue(false);
        
        setFixtureInitializationStateIfNot(State.INITIALIZE, "in-memory");
    }

    private void testUpdate2() {
        final List<SimpleClassTwo> classes = factory.allSimpleClassTwos();
        assertEquals(2, classes.size());

        final SimpleClassTwo simpleClass = classes.get(0);
        assertEquals("XXX", simpleClass.getText());
        assertEquals(simpleClassTwoA.getBooleanValue(), simpleClass.getBooleanValue());

        setFixtureInitializationState(State.DONT_INITIALIZE);
    }

    private void testUpdateCollectionIsDirty() {

        final List<SqlDataClass> sqlDataClasses = factory.allDataClasses();
        final SqlDataClass sqlDataClass = sqlDataClasses.get(0);

        final List<SimpleClass> collection = sqlDataClass.getSimpleClasses1();
        final SimpleClass simpleClass1 = collection.get(0);
        // simpleClass1.setString(stringList1.get(3));

        collection.remove(simpleClass1);
        
        // REVIEW: I'm very doubtful about this...
        // what exactly is meant by updating an internal collection?
        if (!persistenceMechanismIs("xml") ) {
            factory.update(collection);
        }
        
        factory.update(sqlDataClass);
    }

    private void testFindByMatchString() {
        final SimpleClass simpleClassMatch = new SimpleClass();
        simpleClassMatch.setString(Data.stringList1.get(1));

        final List<SimpleClass> classes = factory.allSimpleClassesThatMatch(simpleClassMatch);
        assertEquals(1, classes.size());

    }

    private void testFindByMatchEntity() {
        final List<SimpleClassTwo> classTwos = factory.allSimpleClassTwos();

        final SimpleClass simpleClassMatch = new SimpleClass();
        simpleClassMatch.setSimpleClassTwoA(classTwos.get(0));

        final List<SimpleClass> classes = factory.allSimpleClassesThatMatch(simpleClassMatch);
        
        // TODO: Why is this hack required?
        if (!getProperties().getProperty("isis.persistor").equals("in-memory")) {
            assertEquals(Data.stringList1.size(), classes.size());
        } else {
            assertEquals(Data.stringList1.size() + 2, classes.size());
        }
    }

    private void reinitializeFixtures() {
        setFixtureInitializationState(State.INITIALIZE);
        SqlIntegrationTestFixtures.recreate();
    }

}