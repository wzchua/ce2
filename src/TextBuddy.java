

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * This class is used for interacting with a user to add, delete, display or clear entries.
 * It will also load from and save to a file.
 * The file is saved on exit.
 * 
 * Assumptions made: Single word commands only works if they are inputed as single words.
 * @author Wz
 *
 */
public class TextBuddy {
    private static final String PRE_FORMATTED_WELCOME_MSG = "Welcome to TextBuddy. %1$s is ready for use";
    private static final String DATA_LINE_MSG = "%1$s. %2$s";
    private static final String NO_ENTRIES_MSG = "%1$s is empty";
    private static final String CLEAR_ENTRIES_MSG = "all content deleted from %1$s";
    private static final String DELETE_ENTRY_MSG = "deleted from %1$s: \"%2$s\"";
    private static final String ADD_ENTRY_MSG = "added to %1$s: \"%2$s\"";
    private static final String ARGUMENT_ERROR_MSG = "Error, this program expects only 1 argument" 
                                                    + " as the filename";
    private static final String INVALID_INDEX_MSG = "Invalid index";
    private static final String INVALID_COMMAND_PARAMETER_MSG = "Invalid command parameter";
    private static final String INVALID_COMMAND_MSG = "Invalid command";
    private static final String REQUEST_MSG = "command: ";
    
    private static final String COMMAND_EXIT = "exit";
    private static final String COMMAND_ADD = "add";
    private static final String COMMAND_DELETE = "delete";
    private static final String COMMAND_CLEAR = "clear";
    private static final String COMMAND_DISPLAY = "display";

    private final String WELCOME_MSG;
    
    private String _fileName;
    private ArrayList<String> _dataLines;
    private Scanner _scanner;
    private boolean _canExit = false;

    /**
     * This class is for processing command inputs into two elements: 
     * the command and its parameter
     * 
     * @author Wz
     *
     */
    private class CommandObject {
        private String _command;
        private String _commandParameter;
        private int _commandParameterAsInteger;

        private CommandObject(String commandMessage) {
            boolean isSingleWord = (commandMessage.indexOf(' ') == -1);
            if (isSingleWord) {
                _command = commandMessage;
            } else {
                _command = commandMessage.substring(0, commandMessage.indexOf(' '));
                _commandParameter = commandMessage.substring(commandMessage.indexOf(' ') + 1);
            }
        }

        private boolean hasParameters() {
            return _commandParameter != null;
        }

        private String getCommand() {
            return _command;
        }

        private String getParameters() {
            return _commandParameter;
        }

        private int getParameterAsInteger() {
            return _commandParameterAsInteger;
        }
        
        /**
         * Tries to convert the parameter into an Integer
         * @return true if successful, else false;
         */
        private boolean processParameterAsInteger() {
            try {
                _commandParameterAsInteger = Integer.parseInt(_commandParameter);
                return true;
            } catch (Exception e) {
                return false;
            }
        }
    }
    
    /**
     * Creates a new TextBuddy instance that stores the fileName, loads the file data, 
     * initializes scanner and formats the welcome message
     * 
     * @param fileName - string of the file where data would be stored into
     */
    public TextBuddy(String fileName) {
        _fileName = fileName;
        _dataLines = getDataFromFile();
        _scanner = new Scanner(System.in);
        WELCOME_MSG = String.format(PRE_FORMATTED_WELCOME_MSG, _fileName);        
    }

    public void start() {
        printMessage(WELCOME_MSG);
        runCoreProcess();
    }

    private void runCoreProcess() {
        while (!_canExit) {
            processInput(requestForInput());
        }
    }

    /**
     * This method does some tidying in preparation of the program closing.
     */
    private void setupForExiting() {
        _canExit = true;
        _scanner.close();
        saveDataToFile(_dataLines);
    }
    
    /**
     * This method stores the data from the file into an ArrayList<String>
     * Terminates the program if there is an exception in the filestream
     * @return an array list of each line in the file
     */
    private ArrayList<String> getDataFromFile() {
        ArrayList<String> dataLines = new ArrayList<String>();
        try {
            File file = new File(_fileName);
            
            if (!file.exists()) {
                file.createNewFile();
            }
            
            FileInputStream fs = new FileInputStream(_fileName);
            BufferedReader br = new BufferedReader(new InputStreamReader(fs));
            String line;
            
            while ((line = br.readLine()) != null) {
                dataLines.add(line);
            }
            
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        return dataLines;
    }
    
    /**
     * This methods takes the contents of an ArrayList<String> and saves it into the file
     * Terminates the program if there is an exception in the filestream
     * @param dataLines
     *          is the array of data to be saved in the file
     */
    private void saveDataToFile(ArrayList<String> dataLines) {
        try {
            FileWriter fw = new FileWriter(_fileName);
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

    }

    private String requestForInput() {
        System.out.print(REQUEST_MSG);
        String input = _scanner.nextLine();
        return input;
    }

    private void processInput(String input) {
        CommandObject cmd = new CommandObject(input);
        
        switch (cmd.getCommand()) {
            case COMMAND_EXIT :
                setupForExiting();
                break;
            case COMMAND_ADD :
                processAddCommand(cmd);
                break;
            case COMMAND_DELETE :
                processDeleteCommand(cmd);
                break;
            case COMMAND_CLEAR :
                processClearCommand(cmd);
                break;
            case COMMAND_DISPLAY :
                processDisplayCommand(cmd);
                break;
            default :
                printMessage(INVALID_COMMAND_MSG);
        }
    }

    private void processDisplayCommand(CommandObject cmd) {
        if (cmd.hasParameters()) {
            printMessage(INVALID_COMMAND_PARAMETER_MSG);            
        } else {
            displayEntries();
        }
    }

    private void processClearCommand(CommandObject cmd) {
        if (cmd.hasParameters()) {
            printMessage(INVALID_COMMAND_PARAMETER_MSG);
        } else {
            clearEntries();
        }
    }

    private void processDeleteCommand(CommandObject cmd) {
        if (cmd.hasParameters() && cmd.processParameterAsInteger()) {
            int index = cmd.getParameterAsInteger() - 1;
            deleteEntry(index);
        } else {
            printMessage(INVALID_COMMAND_PARAMETER_MSG);
        }
    }

    private void processAddCommand(CommandObject cmd) {
        if (cmd.hasParameters()) {
            addEntry(cmd.getParameters());
        } else {
            printMessage(INVALID_COMMAND_PARAMETER_MSG);
        }
    }

    private void addEntry(String dataLine) {
        _dataLines.add(dataLine);
        printMessage(String.format(ADD_ENTRY_MSG, _fileName, dataLine));
    }

    private void deleteEntry(int lineIndex) {
        if (lineIndex < 0 || lineIndex >= _dataLines.size()) {
            printMessage(INVALID_INDEX_MSG);
        } else {
            String lineDeleted = _dataLines.remove(lineIndex);
            printMessage(String.format(DELETE_ENTRY_MSG, _fileName, lineDeleted));
        }
    }

    private void clearEntries() {
        _dataLines.clear();
        printMessage(String.format(CLEAR_ENTRIES_MSG, _fileName));
    }

    private void displayEntries() {
        int length = _dataLines.size();
        if (length == 0) {
            printMessage(String.format(NO_ENTRIES_MSG, _fileName));
        }
        for (int i = 0; i < length; i++) {
            printMessage(formatDataLine(i, _dataLines.get(i)));
        }
    }

    private String formatDataLine(int index, String dataLine) {        
        String formatted = String.format(DATA_LINE_MSG, (index + 1), dataLine);
        return formatted;
    }

    public static void main(String[] args) {
        if (isOfOneArgument(args)) {
            TextBuddy textBuddy = new TextBuddy(args[0]);
            textBuddy.start();
        } else {
            printMessage(ARGUMENT_ERROR_MSG);
        }

    }

    public static void printMessage(String message) {
        System.out.println(message);
    }

    public static boolean isOfOneArgument(String[] args) {
        return args.length == 1;
    }
}
