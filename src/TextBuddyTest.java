import static org.junit.Assert.*;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TextBuddyTest {
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    String[] testData1 = { "First line", "Second line", "Third line" };
    String testFileName = "test.txt";

    @Before
    public void setUpStreams() {
        System.setOut(new PrintStream(outContent));
    }

    @After
    public void cleanUpStreams() {
        System.setOut(null);
    }

    public String initializeDummyFile(String[] dataLines) {
        Random rng = new Random();
        String fileName = "test" + rng.nextInt() + ".txt";
        File file;
        try {
            file = new File(fileName);
            while (file.exists()) {
                fileName = "test" + rng.nextInt() + ".txt";
                file = new File(fileName);
            }
            file.createNewFile();

            FileWriter fw = new FileWriter(fileName);
            BufferedWriter bw = new BufferedWriter(fw);

            for (String line : dataLines) {
                bw.write(line);
                bw.newLine();
            }
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        return fileName;
    }

    public void deleteDummyFile(String fileName) {
        File file;
        // cleaning the test file
        try {
            file = new File(fileName);
            file.delete();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    @Test
    public void fileReadTest() {
        // create a dummy file to read
        String fileName = initializeDummyFile(testData1);

        TextBuddy textBuddy = new TextBuddy(fileName);
        textBuddy.loadData();

        assertArrayEquals(testData1, textBuddy.getDataFromFile().toArray());

        deleteDummyFile(fileName);
    }

    @Test
    public void commandObjectNoParameterTest() {
        String message = "Clear";

        TextBuddy.CommandObject cmdObj = new TextBuddy.CommandObject(message);

        // check case insensitivity
        assertEquals(message.toLowerCase(), cmdObj.getCommand());

        assertFalse(cmdObj.hasParameters());
        assertEquals(null, cmdObj.getParameters());
    }

    @Test
    public void commandObjectWithStringParameter() {
        String message = "Add ten pies";

        TextBuddy.CommandObject cmdObj = new TextBuddy.CommandObject(message);

        assertEquals("add", cmdObj.getCommand());
        assertTrue(cmdObj.hasParameters());
        assertEquals("ten pies", cmdObj.getParameters());
        assertFalse(cmdObj.processParameterAsInteger());
    }

    @Test
    public void commandObjectWithIntegerParameter() {
        String message = "Delete 1000";

        TextBuddy.CommandObject cmdObj = new TextBuddy.CommandObject(message);

        assertEquals("delete", cmdObj.getCommand());
        assertTrue(cmdObj.hasParameters());
        assertEquals("1000", cmdObj.getParameters());
        assertTrue(cmdObj.processParameterAsInteger());
        assertEquals(1000, cmdObj.getParameterAsInteger());
    }

    @Test
    public void printMessageTest() {
        String message = "hello";

        TextBuddy.printMessage(message);

        assertEquals(message + System.lineSeparator(), outContent.toString());
    }

    @Test
    public void arguementNotOneTest() {
        String[] args = { "0", "1" };

        assertFalse(TextBuddy.isOfOneArgument(args));
    }

    @Test
    public void addEntryTest() {
        String addedLine = "fourth line";
        String addOutput = String.format("added to %1$s: \"%2$s\"", testFileName,
                addedLine);

        String[] dataAfterAdd = { testData1[0], testData1[1], testData1[2], addedLine };

        TextBuddy textBuddy = new TextBuddy(testFileName);
        textBuddy.setDataLines(testData1);

        //check add output and internal data
        assertEquals(addOutput, textBuddy.addEntry(addedLine));
        assertArrayEquals(dataAfterAdd, textBuddy.getDataLines().toArray());
    }

    @Test
    public void processAddCommandTest() {
        TextBuddy textBuddy = new TextBuddy(testFileName);
        textBuddy.setDataLines(testData1);

        //invalid add command
        String invalidCommandOutput = "Invalid command parameter";
        TextBuddy.CommandObject invalidAddCommand = new TextBuddy.CommandObject("Add");
        assertEquals(invalidCommandOutput,
                textBuddy.processDeleteCommand(invalidAddCommand));
        
        //valid add command
        String addedLine = "fourth line";
        String addOutput = String.format("added to %1$s: \"%2$s\"", testFileName,
                addedLine);
        String[] dataAfterAdd = { testData1[0], testData1[1], testData1[2], addedLine };
        TextBuddy.CommandObject validAddCommand = new TextBuddy.CommandObject("Add "
                + addedLine);
        assertEquals(addOutput, textBuddy.processAddCommand(validAddCommand));
        assertArrayEquals(dataAfterAdd, textBuddy.getDataLines().toArray());

    }

    @Test
    public void deleteEntryTest() {
        TextBuddy textBuddy = new TextBuddy(testFileName);
        textBuddy.setDataLines(testData1);

        //invalid index
        String invalidIndexOutput = "Invalid index";
        assertEquals(invalidIndexOutput, textBuddy.deleteEntry(10));
        
        //valid index
        String deleteOutput = String.format("deleted from %1$s: \"%2$s\"", testFileName,
                "Second line");
        String[] dataAfterDelete = { "First line", "Third line" };        
        assertEquals(deleteOutput, textBuddy.deleteEntry(1));
        assertArrayEquals(dataAfterDelete, textBuddy.getDataLines().toArray());

    }

    @Test
    public void processDeleteCommandTest() {
        TextBuddy textBuddy = new TextBuddy(testFileName);
        textBuddy.setDataLines(testData1);
        
        //invalid delete command
        String invalidCommandOutput = "Invalid command parameter";
        TextBuddy.CommandObject invalidDeleteCommand = new TextBuddy.CommandObject(
                "Delete");
        assertEquals(invalidCommandOutput,
                textBuddy.processDeleteCommand(invalidDeleteCommand));
                
        //valid delete command
        TextBuddy.CommandObject validDeleteCommand = new TextBuddy.CommandObject(
                "Delete 2");
        String deleteOutput = String.format("deleted from %1$s: \"%2$s\"", testFileName,
                "Second line");
        String[] dataAfterDelete = { "First line", "Third line" };        
        assertEquals(deleteOutput, textBuddy.processDeleteCommand(validDeleteCommand));
        assertArrayEquals(dataAfterDelete, textBuddy.getDataLines().toArray());

    }

    @Test
    public void clearEntriesTest() {
        String clearOutput = String.format("all content deleted from %1$s", testFileName);

        TextBuddy textBuddy = new TextBuddy(testFileName);
        textBuddy.setDataLines(testData1);

        //check clear output and internal data
        assertEquals(clearOutput, textBuddy.clearEntries());
        assertEquals(0, textBuddy.getDataLines().size());
    }

    @Test
    public void processClearCommandTest() {
        TextBuddy textBuddy = new TextBuddy(testFileName);
        textBuddy.setDataLines(testData1);
        
        //invalid clear command
        String invalidCommandOutput = "Invalid command parameter";
        TextBuddy.CommandObject invalidClearCommand = new TextBuddy.CommandObject(
                "Clear 3");
        assertEquals(invalidCommandOutput,
                textBuddy.processClearCommand(invalidClearCommand));
        
        //valid clear command
        String clearOutput = String.format("all content deleted from %1$s", testFileName);
        TextBuddy.CommandObject validClearCommmand = new TextBuddy.CommandObject("Clear");
        assertEquals(clearOutput, textBuddy.processClearCommand(validClearCommmand));
        assertEquals(0, textBuddy.getDataLines().size());

    }

    @Test
    public void displayEntriesTest() {
        TextBuddy textBuddy = new TextBuddy(testFileName);
        textBuddy.setDataLines(testData1);
        
        // empty
        String displayEmptyOutput = testFileName + " is empty";
        textBuddy.clearEntries();
        assertEquals(displayEmptyOutput, textBuddy.displayEntries());
        
        // non-empty
        textBuddy.setDataLines(testData1);
        String displayOutput = "1. " + testData1[0] + System.lineSeparator() + "2. "
                + testData1[1] + System.lineSeparator() + "3. " + testData1[2];
        assertEquals(displayOutput, textBuddy.displayEntries());

    }

    @Test
    public void processDisplayCommandTest() {
        TextBuddy textBuddy = new TextBuddy(testFileName);
        textBuddy.setDataLines(testData1);
        
        //invalid display command
        String invalidCommandOutput = "Invalid command parameter";
        TextBuddy.CommandObject invalidDisplayCommand = new TextBuddy.CommandObject(
                "Display 5");
        assertEquals(invalidCommandOutput,
                textBuddy.processDisplayCommand(invalidDisplayCommand));
        
        //valid display command
        String displayOutput = "1. " + testData1[0] + System.lineSeparator() + "2. "
                + testData1[1] + System.lineSeparator() + "3. " + testData1[2];
        TextBuddy.CommandObject validDisplayCommand = new TextBuddy.CommandObject(
                "Display");
        assertEquals(displayOutput, textBuddy.processDisplayCommand(validDisplayCommand));


    }

    @Test
    public void processInputTest() {
        ArrayList<String> entries = new ArrayList<String>();

        String output;
        String addOutput = String.format("added to %1$s: \"%2$s\"", testFileName, "five");
        String deleteOutput = String.format("deleted from %1$s: \"%2$s\"", testFileName,
                "test2");
        String clearOutput = String.format("all content deleted from %1$s", testFileName);
        String invalidOutput = "Invalid command";

        TextBuddy textBuddy = new TextBuddy(testFileName);
        textBuddy.setDataLines(entries);

        // add input
        textBuddy.processInput("Add five");
        output = outContent.toString();
        assertEquals(addOutput + System.lineSeparator(), output);
        outContent.reset();

        // intermediate
        textBuddy.addEntry("test1");
        textBuddy.addEntry("test2");

        // delete input
        textBuddy.processInput("Delete 3");
        output = outContent.toString();
        assertEquals(deleteOutput + System.lineSeparator(), output);
        outContent.reset();

        // clear input
        textBuddy.processInput("Clear");
        output = outContent.toString();
        assertEquals(clearOutput + System.lineSeparator(), output);
        outContent.reset();

        // intermediate
        textBuddy.addEntry("test1");
        textBuddy.addEntry("test2");

        // display input
        textBuddy.processInput("Display");
        output = outContent.toString();
        assertEquals(
                "1. test1" + System.lineSeparator() + "2. test2" + System.lineSeparator(),
                output);
        outContent.reset();

        // invalid input
        textBuddy.processInput("invalid");
        output = outContent.toString();
        assertEquals(invalidOutput + System.lineSeparator(), output);
        outContent.reset();

        // sort input
        String sortedOutput = String.format("%s sorted", testFileName);
        String[] unsortedArray = { "apple", "zebra", "pool" };
        String[] sortedArray = { "apple", "pool", "zebra" };
        textBuddy.setDataLines(unsortedArray);
        textBuddy.processInput("Sort");
        output = outContent.toString();
        assertEquals(sortedOutput + System.lineSeparator(), output);
        assertArrayEquals(sortedArray, textBuddy.getDataLines().toArray());
        outContent.reset();

        // search input
        String serachNotFoundOutput = "cat not found";
        textBuddy.processInput("Search cat");
        output = outContent.toString();
        assertEquals(serachNotFoundOutput + System.lineSeparator(), output);
        outContent.reset();

    }

    @Test
    public void sortEntriesTest() {
        ArrayList<String> entries = new ArrayList<String>();

        String sortEmptyOutput = String.format("%s is empty, nothing to sort",
                testFileName);

        TextBuddy textBuddy = new TextBuddy(testFileName);
        textBuddy.setDataLines(entries);

        // empty list
        assertEquals(sortEmptyOutput, textBuddy.sortEntries());
        assertEquals(0, textBuddy.getDataLines().size());

        // non-empty list
        textBuddy.addEntry("apple");
        textBuddy.addEntry("zebra");
        textBuddy.addEntry("pool");
        String sortedOutput = String.format("%s sorted", testFileName);
        assertEquals(sortedOutput, textBuddy.sortEntries());

        // check actual data
        String[] sortedArray = { "apple", "pool", "zebra" };
        assertArrayEquals(sortedArray, textBuddy.getDataLines().toArray());
        
        //non-empty mixed casing list
        textBuddy.addEntry("Mangoes");
        assertEquals(sortedOutput, textBuddy.sortEntries());
        String[] sortedArray2 = { "apple", "Mangoes", "pool", "zebra" };
        assertArrayEquals(sortedArray2, textBuddy.getDataLines().toArray());
        
    }

    @Test
    public void processSortCommandTest() {
        ArrayList<String> entries = new ArrayList<String>();
        TextBuddy textBuddy = new TextBuddy(testFileName);
        textBuddy.setDataLines(entries);

        // invalid command
        String invalidCommandOutput = "Invalid command parameter";
        TextBuddy.CommandObject invalidSortCommand = new TextBuddy.CommandObject("Sort 4");
        assertEquals(invalidCommandOutput,
                textBuddy.processSortCommand(invalidSortCommand));

        // valid command, empty data
        String sortEmptyOutput = String.format("%s is empty, nothing to sort",
                testFileName);
        TextBuddy.CommandObject validSortCommand = new TextBuddy.CommandObject("Sort");
        assertEquals(sortEmptyOutput, textBuddy.processSortCommand(validSortCommand));

        // further cases can be found under sortEntiresTest()
    }

    @Test
    public void searchEntriesTest() {
        ArrayList<String> entries = new ArrayList<String>();
        String emptyOutput = String
                .format("%s is empty, nothing to search", testFileName);

        TextBuddy textBuddy = new TextBuddy(testFileName);
        textBuddy.setDataLines(entries);

        // empty list
        assertEquals(emptyOutput, textBuddy.searchEntries("field"));

        // populate list
        entries.add("fox on a field");
        entries.add("People riding horses on a field");
        entries.add("apples");
        entries.add("green apples");
        textBuddy.setDataLines(entries);

        // non-empty, not found
        String notFoundOutput = "cat not found";
        assertEquals(notFoundOutput, textBuddy.searchEntries("cat"));

        // non-empty, found
        String foundOutput = "word: \"field\" found in 2 entries"
                + System.lineSeparator() + "1. fox on a field" + System.lineSeparator()
                + "2. People riding horses on a field";
        assertEquals(foundOutput, textBuddy.searchEntries("field"));
    }

    @Test
    public void processSearchCommandTest() {
        ArrayList<String> entries = new ArrayList<String>();
        TextBuddy textBuddy = new TextBuddy(testFileName);
        textBuddy.setDataLines(entries);

        // invalid command
        String invalidCommandOutput = "Invalid command parameter";
        TextBuddy.CommandObject invalidSearchCommand = new TextBuddy.CommandObject(
                "Search");
        assertEquals(invalidCommandOutput,
                textBuddy.processSearchCommand(invalidSearchCommand));

        // populate list
        entries.add("fox on a field");
        entries.add("People riding horses on a field");
        entries.add("apples");
        entries.add("green apples");
        textBuddy.setDataLines(entries);

        // valid command, non-empty data
        String foundOutput = "word: \"field\" found in 2 entries"
                + System.lineSeparator() + "1. fox on a field" + System.lineSeparator()
                + "2. People riding horses on a field";
        TextBuddy.CommandObject validSearchCommand = new TextBuddy.CommandObject(
                "Search field");
        assertEquals(foundOutput, textBuddy.processSearchCommand(validSearchCommand));

        // further cases can be found under searchEntriesTest()
    }

}
