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
        String fileName ="test" + rng.nextInt() + ".txt";
        File file;
        try {
            file = new File(fileName);
            while(file.exists()){
                fileName ="test" + rng.nextInt() + ".txt";
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
    
    public void deleteDummyFile(String fileName){
        File file;
        //cleaning the test file
        try {
            file = new File(fileName);
            file.delete();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    
    @Test
    public void fileReadTest(){
        //create a dummy file to read
       String fileName = initializeDummyFile(testData1);
               
       TextBuddy textBuddy = new TextBuddy(fileName);
       textBuddy.loadData();
       
       assertArrayEquals(testData1, textBuddy.getDataFromFile().toArray());
   
       deleteDummyFile(fileName);
    }

    @Test
    public void commandObjectNoParameterTest(){
        String message = "Clear";
        
        TextBuddy.CommandObject cmdObj = new TextBuddy.CommandObject(message);
        
        //check case insensitivity
        assertEquals(message.toLowerCase(), cmdObj.getCommand()); 
        
        assertFalse(cmdObj.hasParameters());
        
        assertEquals(null, cmdObj.getParameters());
    }
    
    @Test
    public void commandObjectWithStringParameter(){
        String message = "Add ten pies";
        
        TextBuddy.CommandObject cmdObj = new TextBuddy.CommandObject(message);
        
        assertEquals("add", cmdObj.getCommand());
        assertTrue(cmdObj.hasParameters());
        assertEquals("ten pies", cmdObj.getParameters());
        assertFalse(cmdObj.processParameterAsInteger());
    }
    
    @Test
    public void commandObjectWithIntegerParameter(){
        String message = "Delete 1000";
        
        TextBuddy.CommandObject cmdObj = new TextBuddy.CommandObject(message);
        
        assertEquals("delete", cmdObj.getCommand());
        assertTrue(cmdObj.hasParameters());
        assertEquals("1000", cmdObj.getParameters());
        assertTrue(cmdObj.processParameterAsInteger());
        assertEquals(1000, cmdObj.getParameterAsInteger());
    }
    
    @Test
    public void printMessageTest(){
        String message = "hello";
        
        TextBuddy.printMessage(message);
        
        assertEquals(message + System.lineSeparator(), outContent.toString());
    }
    
    @Test
    public void arguementNotOneTest(){
        String[] args = {"0", "1" };
        
        assertFalse(TextBuddy.isOfOneArgument(args));
    }    

    @Test
    public void addEntryTest(){
        String addedLine = "fourth line";
        String addOutput = String.format("added to %1$s: \"%2$s\"", testFileName, addedLine);
        
        String[] dataAfterAdd = {testData1[0], testData1[1], testData1[2], addedLine};

        TextBuddy textBuddy = new TextBuddy(testFileName);
        textBuddy.setDataLines(testData1);
        
        assertArrayEquals(testData1, textBuddy.getDataLines().toArray());
        assertEquals(addOutput, textBuddy.addEntry(addedLine));
        assertArrayEquals(dataAfterAdd, textBuddy.getDataLines().toArray());
    }
    
    @Test
    public void processAddCommandTest(){
        String addedLine = "fourth line";
        String addOutput = String.format("added to %1$s: \"%2$s\"", testFileName, addedLine);
        String invalidCommandOutput = "Invalid command parameter";
        
        String[] dataAfterAdd = {testData1[0], testData1[1], testData1[2], addedLine};
        
        TextBuddy.CommandObject validDeleteCommand = new TextBuddy.CommandObject("Add " + addedLine);
        TextBuddy.CommandObject invalidDeleteCommand  = new TextBuddy.CommandObject("Add");
        
        TextBuddy textBuddy = new TextBuddy(testFileName);
        textBuddy.setDataLines(testData1);
        
        assertArrayEquals(testData1, textBuddy.getDataLines().toArray());
        assertEquals(addOutput, textBuddy.processAddCommand(validDeleteCommand));
        assertArrayEquals(dataAfterAdd, textBuddy.getDataLines().toArray());
        
        assertEquals(invalidCommandOutput, textBuddy.processDeleteCommand(invalidDeleteCommand));
    }
    
    @Test
    public void deleteEntryTest(){
        String deleteOutput = String.format("deleted from %1$s: \"%2$s\"", testFileName, "Second line");
        String invalidIndexOutput = "Invalid index";
        
        String[] dataAfterDelete = { "First line", "Third line" };
        
        TextBuddy textBuddy = new TextBuddy(testFileName);
        textBuddy.setDataLines(testData1);
        
        assertArrayEquals(testData1, textBuddy.getDataLines().toArray());
        assertEquals(deleteOutput, textBuddy.deleteEntry(1));
        assertArrayEquals(dataAfterDelete, textBuddy.getDataLines().toArray());   
        
        assertEquals(invalidIndexOutput, textBuddy.deleteEntry(10));
    }
    
    @Test
    public void processDeleteCommandTest(){
        String deleteOutput = String.format("deleted from %1$s: \"%2$s\"", testFileName, "Second line");
        String invalidIndexOutput = "Invalid index";
        String invalidCommandOutput = "Invalid command parameter";
        
        String[] dataAfterDelete = { "First line", "Third line" };
        
        TextBuddy.CommandObject validDeleteCommand = new TextBuddy.CommandObject("Delete 2");
        TextBuddy.CommandObject invalidDeleteCommand1  = new TextBuddy.CommandObject("Delete");
        TextBuddy.CommandObject invalidDeleteCommand2  = new TextBuddy.CommandObject("Delete 10");

        TextBuddy textBuddy = new TextBuddy(testFileName);
        textBuddy.setDataLines(testData1);
        
        assertArrayEquals(testData1, textBuddy.getDataLines().toArray());
        assertEquals(deleteOutput, textBuddy.processDeleteCommand(validDeleteCommand));
        assertArrayEquals(dataAfterDelete, textBuddy.getDataLines().toArray());
        
        assertEquals(invalidCommandOutput, textBuddy.processDeleteCommand(invalidDeleteCommand1));
        
        assertEquals(invalidIndexOutput, textBuddy.processDeleteCommand(invalidDeleteCommand2));        
    }
    
    @Test
    public void clearEntriesTest(){
        String clearOutput = String.format("all content deleted from %1$s", testFileName);
        
        TextBuddy textBuddy = new TextBuddy(testFileName);
        textBuddy.setDataLines(testData1);
        
        assertEquals(clearOutput, textBuddy.clearEntries());        
    }
    
    @Test
    public void processClearCommandTest(){
        String invalidCommandOutput = "Invalid command parameter";
        String clearOutput = String.format("all content deleted from %1$s", testFileName);
        
        TextBuddy.CommandObject validClearCommmand = new TextBuddy.CommandObject("Clear");
        TextBuddy.CommandObject invalidClearCommand = new TextBuddy.CommandObject("Clear 3");
        
        TextBuddy textBuddy = new TextBuddy(testFileName);
        textBuddy.setDataLines(testData1);
        
        assertEquals(clearOutput, textBuddy.processClearCommand(validClearCommmand));
        
        assertEquals(invalidCommandOutput, textBuddy.processClearCommand(invalidClearCommand));
    }
    
    @Test
    public void displayEntriesTest(){
        String displayOutput = "1. " + testData1[0] + System.lineSeparator() + "2. " 
                                + testData1[1] + System.lineSeparator() + "3. " + testData1[2];
        String displayEmptyOutput = testFileName + " is empty";
        
        TextBuddy textBuddy = new TextBuddy(testFileName);
        textBuddy.setDataLines(testData1);
        
        assertEquals(displayOutput, textBuddy.displayEntries());
        
        textBuddy.clearEntries();
        assertEquals(displayEmptyOutput, textBuddy.displayEntries());        
    }
    
    @Test
    public void processDisplayCommandTest(){
        String invalidCommandOutput = "Invalid command parameter";
        String displayOutput = "1. " + testData1[0] + System.lineSeparator() + "2. " 
                + testData1[1] + System.lineSeparator() + "3. " + testData1[2];
        String displayEmptyOutput = testFileName + " is empty";
        
        TextBuddy.CommandObject validDisplayCommand = new TextBuddy.CommandObject("Display");
        TextBuddy.CommandObject invalidDisplayCommand  = new TextBuddy.CommandObject("Display 5");
        
        TextBuddy textBuddy = new TextBuddy(testFileName);
        textBuddy.setDataLines(testData1);
        
        assertEquals(displayOutput, textBuddy.processDisplayCommand(validDisplayCommand));
        
        assertEquals(invalidCommandOutput, textBuddy.processDisplayCommand(invalidDisplayCommand));
        
        textBuddy.clearEntries();
        assertEquals(displayEmptyOutput, textBuddy.processDisplayCommand(validDisplayCommand));        
    }
    @Test
    public void processInputTest(){
        ArrayList<String> entries = new ArrayList<String>();
        
        String output;
        String addOutput = String.format("added to %1$s: \"%2$s\"", testFileName, "five");
        String deleteOutput = String.format("deleted from %1$s: \"%2$s\"", testFileName, "test2");
        String clearOutput = String.format("all content deleted from %1$s", testFileName);
        String invalidOutput = "Invalid command";

        TextBuddy textBuddy = new TextBuddy(testFileName);
        textBuddy.setDataLines(entries);        
        
        //add input
        textBuddy.processInput("Add five");
        output = outContent.toString();
        assertEquals(addOutput + System.lineSeparator(), output);
        outContent.reset();
        
        //intermediate
        textBuddy.addEntry("test1");
        textBuddy.addEntry("test2");
        
        //delete input
        textBuddy.processInput("Delete 3");
        output = outContent.toString();
        assertEquals(deleteOutput + System.lineSeparator(), output);
        outContent.reset();
        
        //clear input
        textBuddy.processInput("Clear");
        output = outContent.toString();
        assertEquals(clearOutput + System.lineSeparator(), output);
        outContent.reset();
        
        //intermediate
        textBuddy.addEntry("test1");
        textBuddy.addEntry("test2");
        
        //display input
        textBuddy.processInput("Display");
        output = outContent.toString();
        assertEquals("1. test1" +System.lineSeparator() + 
                        "2. test2" + System.lineSeparator(), output);
        outContent.reset();
        
        //invalid input
        textBuddy.processInput("invalid");
        output = outContent.toString();
        assertEquals(invalidOutput + System.lineSeparator(), output);
        outContent.reset();
    }

}
