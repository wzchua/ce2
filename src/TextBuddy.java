

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
    static class CommandObject {
        private String _command;
        private String _commandParameter;
        private int _commandParameterAsInteger;

        CommandObject(String commandMessage) {
            boolean isSingleWord = (commandMessage.indexOf(' ') == -1);
            if (isSingleWord) {
                _command = commandMessage.toLowerCase();
            } else {
                _command = commandMessage.substring(0, commandMessage.indexOf(' '));
                _command = _command.toLowerCase();
                _commandParameter = commandMessage.substring(commandMessage.indexOf(' ') + 1);
            }
        }

        boolean hasParameters() {
            return _commandParameter != null;
        }

        String getCommand() {
            return _command;
        }

        String getParameters() {
            return _commandParameter;
        }

        int getParameterAsInteger() {
            return _commandParameterAsInteger;
        }
        
        /**
         * Tries to convert the parameter into an Integer
         * @return true if successful, else false;
         */
        boolean processParameterAsInteger() {
            try {
                _commandParameterAsInteger = Integer.parseInt(_commandParameter);
                return true;
            } catch (Exception e) {
                return false;
            }
        }
    }
    

    /**
     * Creates a new TextBuddy instance that stores the fileName, 
     * initializes scanner and formats the welcome message
     * 
     * @param fileName - string of the file where data would be stored into
     */
    public TextBuddy(String fileName) {
        _fileName = fileName;
        _scanner = new Scanner(System.in);
        WELCOME_MSG = String.format(PRE_FORMATTED_WELCOME_MSG, _fileName);
    }    
    
    public void loadData(){
        _dataLines = getDataFromFile();        
    }
    
    public void setDataLines(ArrayList<String> data){
        if(_dataLines == null){
            _dataLines = new ArrayList<String>();
        }
        _dataLines.clear();
        for(String line : data){
            _dataLines.add(line);
        }
    }
    
    public void setDataLines(String[] data){
        if(_dataLines == null){
            _dataLines = new ArrayList<String>();
        }
        _dataLines.clear();
        for(String line : data){
            _dataLines.add(line);
        }
    }
    
    public ArrayList<String> getDataLines(){
        return _dataLines;
    }

    public void start() {
        loadData();
        printMessage(WELCOME_MSG);
        runCoreProcess();
    }

    void runCoreProcess() {
        while (!_canExit) {
            processInput(requestForInput());
        }
    }

    /**
     * This method does some tidying in preparation of the program closing.
     */
    void setupForExiting() {
        _canExit = true;
        _scanner.close();
        saveDataToFile(_dataLines);
    }
    
    /**
     * This method stores the data from the file into an ArrayList<String>
     * Terminates the program if there is an exception in the filestream
     * @return an array list of each line in the file
     */
    ArrayList<String> getDataFromFile() {
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
    void saveDataToFile(ArrayList<String> dataLines) {
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

    String requestForInput() {
        System.out.print(REQUEST_MSG);
        String input = _scanner.nextLine();
        return input;
    }

    void processInput(String input) {
        CommandObject cmd = new CommandObject(input);
        
        switch (cmd.getCommand()) {
            case COMMAND_EXIT :
                setupForExiting();
                break;
            case COMMAND_ADD :
                printMessage(processAddCommand(cmd));
                break;
            case COMMAND_DELETE :
                printMessage(processDeleteCommand(cmd));
                break;
            case COMMAND_CLEAR :
                printMessage(processClearCommand(cmd));
                break;
            case COMMAND_DISPLAY :
                printMessage(processDisplayCommand(cmd));
                break;
            default :
                printMessage(INVALID_COMMAND_MSG);
        }
    }

    String processAddCommand(CommandObject cmd) {
        if (cmd.hasParameters()) {
            return addEntry(cmd.getParameters());
        } else {
            return INVALID_COMMAND_PARAMETER_MSG;
        }
    }

    String processDeleteCommand(CommandObject cmd) {
        if (cmd.hasParameters() && cmd.processParameterAsInteger()) {
            int index = cmd.getParameterAsInteger() - 1;
            return deleteEntry(index);
        } else {
            return INVALID_COMMAND_PARAMETER_MSG;
        }
    }

    String processClearCommand(CommandObject cmd) {
        if (cmd.hasParameters()) {
            return INVALID_COMMAND_PARAMETER_MSG;
        } else {
            return clearEntries();
        }
    }

    String processDisplayCommand(CommandObject cmd) {
        if (cmd.hasParameters()) {
            return INVALID_COMMAND_PARAMETER_MSG;            
        } else {
            return displayEntries();
        }
    }

    String addEntry(String dataLine) {
        _dataLines.add(dataLine);
        return String.format(ADD_ENTRY_MSG, _fileName, dataLine);
    }

    String deleteEntry(int lineIndex) {
        if (lineIndex < 0 || lineIndex >= _dataLines.size()) {
            return INVALID_INDEX_MSG;
        } else {
            String lineDeleted = _dataLines.remove(lineIndex);
            return String.format(DELETE_ENTRY_MSG, _fileName, lineDeleted);
        }
    }

    String clearEntries() {
        _dataLines.clear();
        return String.format(CLEAR_ENTRIES_MSG, _fileName);
    }

    String displayEntries() {
        int length = _dataLines.size();
        String output;
        StringBuilder stringBuilder = new StringBuilder();
        if (length == 0) {
            output = String.format(NO_ENTRIES_MSG, _fileName);
        } else {
            for (int i = 0; i < length; i++) {
                stringBuilder.append(formatDataLine(i, _dataLines.get(i)));

                if(i != length - 1){
                    stringBuilder.append(System.lineSeparator());
                }
            }
            output = stringBuilder.toString();
        }
        return output;
    }

    String formatDataLine(int index, String dataLine) {        
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
