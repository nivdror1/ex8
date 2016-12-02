import com.sun.org.apache.bcel.internal.classfile.Code;
import com.sun.org.apache.regexp.internal.RE;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * parse the vm language into asm language
 */
public class Parser {
    private static final String ONE_LINER_COMMENT ="^/{2}";
    private static final String EMPTY_LINE= "^\\s*+$";
    private static final Pattern EMPTY_LINE_PATTERN= Pattern.compile(EMPTY_LINE);
    private static final Pattern COMMENT_PATTERN= Pattern.compile(ONE_LINER_COMMENT);

    private static final String PUSH_AND_POP ="\\b(push|pop)\\b";
    private static final Pattern PUSH_AND_POP_PATTERN = Pattern.compile(PUSH_AND_POP);


    private static final String PROGRAM_FLOW= "\\b(label|goto|if-goto)\\b";
    private static final Pattern PROGRAM_FLOW_PATTERN=Pattern.compile(PROGRAM_FLOW);

    private static final String NAME ="[a-zA-Z]{1}\\w*+";
    private static final Pattern NAME_PATTERN= Pattern.compile(NAME);

    private static final String FUNCTION_OR_CALL= "\\b(call|function)\\b";
    private static final Pattern FUNCTION_OR_CALL_PATTERN= Pattern.compile(FUNCTION_OR_CALL);

    private static final String RETURN="return";
    private static final Pattern RETURN_PATTERN=Pattern.compile(RETURN);

    private static final String MEMORY= "\\b(constant|local|argument|this|that|pointer|temp)\\b";
    private static final Pattern MEMORY_PATTERN= Pattern.compile(MEMORY);

    private static final String DECIMAL_NUMBER = "\\d++";
    private static final Pattern DECIMAL_NUMBER_PATTERN= Pattern.compile(DECIMAL_NUMBER);

    private static final String ARITHMETIC ="\\b(add|sub|neg|eq|gt|lt|and|or|not)\\b";
    private static final Pattern ARITHMETIC_PATTERN= Pattern.compile(ARITHMETIC);

    /** a string that represent the current arithmetic operation*/
    private String curArithmetic;
    /** A string that represent the current memory address*/
    private String curMemory;
    /** A string that represent the current number being processed*/
    private int curNumber=0;
    /** the current matcher*/
    private Matcher curMatcher;
    /**A string that represent the operation on the stack*/
    private String operation;
    /** this ArrayList contains all of the vm file lines*/
    private ArrayList<String> vmLines;
    /**a string that represent the current vm line*/
    private String curLine;
    /** a string that represent a name*/
    private String name;

    /** a constructor*/
    public Parser(){
        this.name=this.operation=this.curMemory=this.curArithmetic="";
        this.vmLines= new ArrayList<>();

    }

    public ArrayList<String> getVmLines(){
        return this.vmLines;
    }

    /**
     * parse the vm file
     */
    public void parseVmFile(){
        for(int i=0; i<vmLines.size();i++){
            this.curLine= vmLines.get(i); // assign the current line
            if(deleteOneLinerComment(this.curLine)||deleteBlankLines(this.curLine))
            { //skip if the row is a comment or a blank line
                continue;
            }
            if(parsePushOrPop()) // check which instruction to operate
            {
                signifyMemorySegment(); //check which memory segment
                insertDecimalNumber(); //assign a decimal number
                CodeWriter.getCodeWriter().translate(this.operation,
                        this.curMemory,this.curNumber); //translate the operation
            }
            else if(parseArithmetic())
            { //translate the arithmetic operation
                CodeWriter.getCodeWriter().translateArithmetic(this.operation);
            }
            else if (parseProgramFlow()){
                parseName();
                CodeWriter.getCodeWriter().writeProgramFlow(this.operation, this.name);
            }
            else if(parseFunctionOperation()){
                parseName();
                if(insertDecimalNumber()){
                    this.curNumber=0;
                }
                CodeWriter.getCodeWriter().writeFunction(this.operation, this.name,this.curNumber);
            }else{
                parseReturnOperation();
                CodeWriter.getCodeWriter().writeReturn(this.operation, this.name,this.curNumber);
            }
        }
    }

    /**
     * check for arithmetic operations
     * @return return true if is was an arithmetic operation
     */
    public boolean parseArithmetic(){
        this.curMatcher=ARITHMETIC_PATTERN.matcher(this.curLine);
        if(this.curMatcher.find())
        { //check for arithmetic operation
            this.operation=this.curLine.substring(this.curMatcher.start(),this.curMatcher.end());
            return true;
        }
        return false;
    }

    /**
     * parse the memory segment where the push/pop operations should address
     */
    private void signifyMemorySegment(){
        this.curMatcher= MEMORY_PATTERN.matcher(curLine);
        if(this.curMatcher.find())
        { //find the memory specified
            this.curMemory = curLine.substring(this.curMatcher.start(), this.curMatcher.end());
            this.curLine = this.curLine.substring(this.curMatcher.end()); //delete the prefix
        }
    }

    /** parse the decimal number*/
    private boolean insertDecimalNumber(){
        this.curMatcher=DECIMAL_NUMBER_PATTERN.matcher(this.curLine);
        if(this.curMatcher.find())
        { //find the data
            this.curLine = this.curLine.substring(this.curMatcher.start(), this.curMatcher.end());
            this.curNumber = Integer.parseInt(this.curLine); //convert into int
            return true;
        }
        return false;
    }

    /**
     * check if the line is blank
     * @param line a string that represent the specific line in the asm file
     * @return return true if it was a blank line, false otherwise
     */
    private boolean deleteBlankLines(String line){
        Matcher m=EMPTY_LINE_PATTERN.matcher(line);
        return m.find(); // check for an empty line

    }

    /**
     * check if the line is a comment
     * @param line a string that represent the specific line in the asm file
     * @return return true if it was a comment, false otherwise
     */
    private boolean deleteOneLinerComment(String line)
    {
        Matcher m= COMMENT_PATTERN.matcher(line);
        return m.lookingAt();
    }

    /**
     * check for a push or pop operation
     * @return return true if it was a push or pop operation else return false
     */
    private boolean parsePushOrPop() {
        this.curMatcher = PUSH_AND_POP_PATTERN.matcher(curLine);
        if (this.curMatcher.find()) { //check those operation
            this.operation = this.curLine.substring(this.curMatcher.start(), this.curMatcher.end());
            return true;
        }
        return false;
    }

    /**
     * check for a label definition or goto command or a if-goto command
     * @return return true if it was a program flow operation else return false
     */
    private boolean parseProgramFlow() {
        this.curMatcher = PROGRAM_FLOW_PATTERN.matcher(curLine);
        if (this.curMatcher.find()) { //check for a program flow operation
            this.operation = this.curLine.substring(this.curMatcher.start(), this.curMatcher.end());
            this.curLine=this.curLine.substring(this.curMatcher.end()); //delete the prefix
            return true;
        }
        return false;
    }

    /**
     * check for a name
     * @return return true if a name was found else return false
     */
    private boolean parseName() {
        this.curMatcher = NAME_PATTERN.matcher(curLine);
        if (this.curMatcher.find()) { //check for a name
            this.name = this.curLine.substring(this.curMatcher.start(), this.curMatcher.end());
            this.curLine=this.curLine.substring(this.curMatcher.end()); //delete the prefix
            return true;
        }
        return false;
    }

    /**
     * check for a function call or a function definition
     * @return return true if it was a function call else return false
     */
    private boolean parseFunctionOperation() {
        this.curMatcher = FUNCTION_OR_CALL_PATTERN.matcher(curLine);
        if (this.curMatcher.find()) { //check for a function call operation
            this.operation = this.curLine.substring(this.curMatcher.start(), this.curMatcher.end());
            this.curLine=this.curLine.substring(this.curMatcher.end()); //delete the prefix
            return true;
        }
        return false;
    }

    /**
     * check for a return operation
     * @return return true if it was a return operation else return false
     */
    private boolean parseReturnOperation() {
        this.curMatcher = RETURN_PATTERN.matcher(curLine);
        if (this.curMatcher.find()) { //check for a return operation
            this.operation = this.curLine.substring(this.curMatcher.start(), this.curMatcher.end());
            return true;
        }
        return false;
    }
}