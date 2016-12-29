import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.lang.String;


/**
 * this class translates the vm code into asm code
 */
public class CodeWriter {

    /** logic operations*/
    private static final String ADD= "add";
    private static final String SUB="sub";
    private static final String EQ="eq";
    private static final String LT="lt";
    private static final String GT="gt";
    private static final String NEG="neg";
    private static final String AND="and";
    private static final String OR="or";
    private static final String NOT="not";
    private static final String PUSH="push";
    private static final String POP="pop";

    /** the memory segment*/
    private static final String SP = "SP";
    private static final String CONSTANT= "constant";
    private static final String LOCAL= "local";
    private static final String ARGUMENT= "argument";
    private static final String THAT= "that";
    private static final String THIS= "this";
    private static final String POINTER= "pointer";
    private static final String STATIC= "static";
    private static final String TEMP= "temp";

    /**program flow operations*/
    private static final String IF_GOTO="if-goto";
    private static final String GOTO ="goto";

    /** program control operations*/
    private static final String FUNCTION ="function";
    
    /**	signify a space	*/
    private static final String SPACE="\\s";


    /** the unique instance of CodeWriter*/
    private static CodeWriter codeWriter= null;

    /** this ArrayList signify the output asm code*/
    private ArrayList<String> asmLines;


    /** a label counter*/
    private int labelCounter;
    
    /** a line counter*/
    private int lineCounter;
    
    private int returnCounter;



    /** a singleton constructor*/
    private CodeWriter(){
        this.asmLines= new ArrayList<>();
        labelCounter = lineCounter = returnCounter= 0;
    }

    
    /**
     * get the instance of CodeWriter
     * @return return codeWriter
     */
    public static CodeWriter getCodeWriter(){
        if(codeWriter==null) // if the instance hasn't been made then create it
        {
            codeWriter =new CodeWriter();
        }
        return codeWriter;
    }

    /**
     * get asmLines
     * @return return the translated asm text
     */
    public  ArrayList<String> getAsmLines(){
        return this.asmLines;
    }


    /** signify arithmetic instructions and logic operations
     * @param operation a string representing the arithmetic operation
     */
    public void translateArithmetic(String operation) {
        if (operation.equals(EQ)|| operation.equals(GT)|| operation.equals(LT)){
            writeBoolean(operation); // write boolean operations
        }else{
            writeArithmetic(operation); // write arithmetic operation
        }
    }

    // ---------------------------arithmetic----------------------------

    /**
     * write arithmetic operation in asm language
     * @param operation  a string representing the arithmetic operation
     */
    public void writeArithmetic(String operation){
        switch (operation){
            case ADD:
                writeAdd();
                break;
            case SUB:
                writeSub();
                break;
            case NEG:
                writeNeg();
                break;
            case AND:
                writeAnd();
                break;
            case OR:
                writeOr();
                break;
            case NOT:
                writeNot();
                break;
        }
    }

    /**
     * write the add instruction in asm
     */
    private void writeAdd(){
            asm("// ---add---");
            popToD();
            loadStackAddressToA();
            asm("A=A-1");
            asm("M=M+D");
            asm("//// ---add-end---");
        }
    /**
     * write the sub instruction in asm
     */
    private void writeSub(){
        asm("//// ---sub---");
        popToD();
        loadStackAddressToA();
        asm("A=A-1");
        asm("M=M-D");
        asm("//// ---sub-end---");
    }
    /**
     * write the negate instruction in asm
     */
    private void writeNeg(){
        asm("//// ---neg---");
        loadStackAddressToA();
        asm("A=A-1");
        asm("M=-M");
        asm("//// ---neg-end---");
    }
    /**
     * write the and instruction in asm
     */
    private void writeAnd(){
        asm("//// ---and---");
        popToD();
        loadStackAddressToA();
        asm("A=A-1");
        asm("M=M&D");
        asm("//// ---and-end---");
    }
    /**
     * write the or instruction in asm
     */
    private void writeOr(){
        asm("//// ---or---");
        popToD();
        loadStackAddressToA();
        asm("A=A-1");
        asm("M=M|D");
        asm("//// ---or-end---");
    }
    /**
     * write the not instruction in asm
     */
    private void writeNot(){
        asm("//// ---not---");
        loadStackAddressToA();
        asm("A=A-1");
        asm("M=!M");
        asm("//// ---not-end---");
    }

   // ---------------------------boolean-------------------------------

    /**
     * check which logic operation to translate
     * @param operation a string representing the arithmetic operation
     */
    public void writeBoolean(String operation){
        switch (operation){
            case EQ:
                writeLogic("D; JEQ");
                break;
            case LT:
                writeLogic("D; JLT");
                break;
            case GT:
                writeLogic("D; JGT");
                break;
        }
    }

    /**
     * write the jump operation according to the input RuleOnD
     * @param RuleOnD the jump condition
     */
    private void writeLogic(String RuleOnD){

        String endingLabel = "label" + this.labelCounter;
        String xPos = "XG0_" + this.labelCounter;
        String xNeg = "XL0_" + this.labelCounter;

        String sameSign = "sameSign" + this.labelCounter;
        String diffSign = "diffSign" + this.labelCounter;

        String _false = "false" + this.labelCounter;
        String _true = "true" + this.labelCounter;

        this.labelCounter++;


//        asm("@SP");
//        asm("A=A-1");
//        asm("A=A-1");
//        asm("A=M");
//        copyAToR13();


        asm("//overflow check");

        loadStackAddressToA();
        asm("A=A-1");
        asm("D=M");

        asm("@" + xPos);
        asm("D; JGT");

        loadStackAddressToA();
        asm("A=A-1");
        asm("D=M");

        asm("@" + xNeg);
        asm("D; JLT");


        asm(label(xPos));
        loadStackAddressToA();
        asm("A=A-1");
        asm("A=A-1");
        asm("D=M");

        asm("@14");
        asm("M=1");
        asm("M=-M");
        asm("@" + diffSign);
        asm("D; JLT");
        asm("@" + sameSign);
        asm("0; JMP");


        asm(label(xNeg));

        loadStackAddressToA();
        asm("A=A-1");
        asm("A=A-1");
        asm("D=M");

        asm("@14");
        asm("M=1");
        //asm("M=-M");

        asm(loadLabel(diffSign));
        asm("D; JGT");
        asm(loadLabel(sameSign));
        asm("0; JMP");


        asm(label(diffSign));
        popToD();
        popToD();
        asm("@14");
        asm("D=M");
        asm(loadLabel(_true));
        asm(RuleOnD);
        asm(loadLabel(_false));
        asm("0; JMP");



        asm(label(sameSign));


        writeSub();
        popToD();
        asm(loadLabel(_true));
        asm(RuleOnD);//asm("D; JEQ");
        asm(loadLabel(_false));
        asm("0; JMP");



        asm(label(_true));

        asm("@1");
        asm("A=-A");
        copyAToR13();

        asm(loadLabel(endingLabel));
        asm("0; JMP");

        asm("@0");
        copyAToR13();
        asm(label(_false));

        asm(label(endingLabel));
        pushR13();

    }

    private static String label(String str){
        return "(" + str + ")";
    }
    private static String loadLabel(String str){
        return "@" + str;
    }
    // ---------------------------memory access functions --------------

    /**
     * insert a constant to R13 which is a data register
     * @param value the constant value
     */
    private void writeInsertConstantToR13(int value){
        asm("@" + String.valueOf(value));
        asm("D=A");
        asm("@R13");
        asm("M=D");

    }

    /**
     * write push or pop operation
     * @param operation a string representing the arithmetic operation
     * @param memory a string representing the memory segment
     * @param arg2 a string representing the address or the data
     * @param className the current class name
     */
    public void writePushPop(String operation, String memory, int arg2, String className){
        if(operation.equals(PUSH)) {
            writePush(memory, arg2,className); //write push
        }
        else if(operation.equals(POP)){
            writePop(memory, arg2,className); //write pop
        }

    }

    /**
     * write push according to the memory segment
     * @param memory a string representing the memory segment
     * @param arg2 a data value
     * @param className the current class name
     */
    private void writePush(String memory, int arg2, String className){
        switch (memory){
            case CONSTANT:
                writePushToConstant(arg2);
                break;
            case ARGUMENT:
                writePushToArgument(arg2);
                break;
            case LOCAL:
                writePushToLocal(arg2);
                break;
            case THIS:
                writePushToThis(arg2);
                break;
            case THAT:
                writePushToThat(arg2);
                break;
            case TEMP:
                writePushToTemp(arg2);
                break;
            case POINTER:
                writePushToPointer(arg2);
                break;
            case STATIC:
                writePushToStatic(arg2,className);
                break;

        }
    }

    /**
     * push the ~content~ of the SP/ARG/LOCAL/THIS/THAT
     * == push from R0-R4
     */
    private void writePushAddresses(String segment){
    	switch (segment){
    	case SP:
    		asm("@SP");
    		break;
    	case LOCAL:
        	asm("@LCL");
    		break;
    	case ARGUMENT:
    		asm("@ARG");
    		break;
    	case THIS:
    		asm("@THIS");
    		break;
    	case THAT:
    		asm("@THAT");
    		break;
    	}
    	asm("A=M");
        copyAToR13();
        pushR13();
    }

    /**
     * push operation helper
     */
    private void pushSub_arg_const_this_that(){
        asm("A=A+D");
        asm("// A have now the address of the data to take from the ram");
        asm("D=M");
        asm("@R13");
        asm("M=D");
        asm("// R13 now have the data");

        loadStackAddressToA();
        copyAToR14();
        copyFromR13ToRamAddressInR14();
        advanceStack();
    }

    /**
     * write pop operation in asm language
     * @param memory a string representing the memory segment
     * @param address a string representing the address
     * @param className the current className
     */
    private void writePop(String memory, int address, String className){
        switch (memory){
            case ARGUMENT:
                writePopToArgument(address);
                break;
            case LOCAL:
                writePopToLocal(address);
                break;
            case THIS:
                writePopToThis(address);
                break;
            case THAT:
                writePopToThat(address);
                break;
            case TEMP:
                writePopToTemp(address);
                break;
            case POINTER:
                writePopToPointer(address);
                break;
            case STATIC:
                writePopToStatic(address,className);
                break;
            default:
                popToD();
                break;
        }
    }


    /**
     * add an asm line to asmLines
     */
    private void asm(String asmLine){
        this.asmLines.add(asmLine + "       //" + this.lineCounter);
        //	increment lineCounter
        String cleanLine = asmLine.replaceAll(SPACE,""); 
        if (!(	(cleanLine.charAt(0) == '/') ||
        		(cleanLine.charAt(0) == '(') ||
        		(cleanLine.length() == 0)		)){
        	// count line
        	this.lineCounter ++ ;
        }
    }

    /**
     * write push to the constant segment
     * @param arg2 a data value
     */
    private void writePushToConstant(int arg2){
        asm("//// ----- push constant -------");
        writeInsertConstantToR13(arg2);
        pushR13();
        asm("//// ----- push constant end -------");
    }
    /**
     * write push to the temp segment
     * @param arg2 a data value
     */
    private void writePushToTemp(int arg2){
        asm("//// ----- push temp -------");
        asm("@" + String.valueOf(arg2));
        asm("D=A");
        asm("@5");
        pushSub_arg_const_this_that();
        asm("//// ----- push temp end-------");
    }
    /**
     * write push to the static segment
     * @param arg2 a data value
     * @param className the current class name
     */
    private void writePushToStatic(int arg2,String className){
        asm("//// ----- push static -------");
        asm("@" + String.valueOf(arg2));
        asm("D=A");
        asm("@"+className+arg2);
        asm("D=M");
        asm("@R13");
        asm("M=D");
        asm("// R13 now have the data");

        loadStackAddressToA();
        copyAToR14();
        copyFromR13ToRamAddressInR14();
        advanceStack();
        asm("//// ----- push static end-------");
    }

    /**
     * write push to the "that" segment
     * @param arg2 a data value
     */
    private void writePushToThat(int arg2){
        asm("//// ----- push that -------");
        asm("@" + String.valueOf(arg2));
        asm("D=A");
        loadThatAddressToA();
        pushSub_arg_const_this_that();
        asm("//// ----- push that end-------");
    }
    /**
     * write push to the "this" segment
     * @param arg2 a data value
     */
    private void writePushToThis(int arg2){
        asm("//// ----- push this -------");
        asm("@" + String.valueOf(arg2));
        asm("D=A");
        loadThisAddressToA();
        pushSub_arg_const_this_that();
        asm("//// ----- push this end-------");
    }
    /**
     * write push to the local segment
     * @param arg2 a data value
     */
    private void writePushToLocal(int arg2){
        asm("//// ----- push local -------");
        asm("@" + String.valueOf(arg2));
        asm("D=A");
        loadLocalAddressToA();
        pushSub_arg_const_this_that();
        asm("//// ----- push local end-------");
    }
    /**
     * write push to the argument segment
     * @param arg2 a data value
     */
    private void writePushToArgument(int arg2){
        asm("//// ----- push argument -------");
        asm("@" + String.valueOf(arg2));
        asm("D=A");
        loadArgumentAddressToA();
        pushSub_arg_const_this_that();
        asm("//// ----- push argument end-------");
    }
    /**
     * write push to the pointer segment
     * @param arg2 a data value
     */
    private void writePushToPointer(int arg2){
        asm("//// ----- push pointer -------");
        asm("@" + String.valueOf(arg2));
        asm("D=A");
        asm("@3");
        pushSub_arg_const_this_that();
        asm("//// ----- push pointer end-------");
    }

    /**
     * write pop to the temp segment
     * @param address an address in the memory
     */
    private void writePopToTemp(int address){
        asm("//// -- pop temp -- ");
        asm("@" + String.valueOf(address));
        asm("D=A");
        asm("@5");
        asm("A=D+A");
        copyAToR14();
        popR13();
        copyFromR13ToRamAddressInR14();
        asm("//// -- pop temp end -- ");
    }
    /**
     * write pop to the "this" segment
     * @param address an address in the memory
     */
    private void writePopToThis(int address){
        asm("//// -- pop this -- ");
        asm("@" + String.valueOf(address));
        asm("D=A");
        loadThisAddressToA();
        asm("A=D+A");
        copyAToR14();
        popR13();
        copyFromR13ToRamAddressInR14();
        asm("//// -- pop this end -- ");
    }
    /**
     * write pop to the "that" segment
     * @param address an address in the memory
     */
    private void writePopToThat(int address){
        asm("//// -- pop that -- ");
        asm("@" + String.valueOf(address));
        asm("D=A");
        loadThatAddressToA();
        asm("A=D+A");
        copyAToR14();
        popR13();
        copyFromR13ToRamAddressInR14();
        asm("//// -- pop that end -- ");
    }
    /**
     * write pop to the pointer segment
     * @param address an address in the memory
     */
    private void writePopToPointer(int address){
        asm("//// -- pop pointer -- ");
        asm("@" + String.valueOf(address));
        asm("D=A");
        asm("@3");
        asm("A=D+A");
        copyAToR14();
        popR13();
        copyFromR13ToRamAddressInR14();
        asm("//// -- pop pointer end -- ");
    }

    /**
     * write pop to the static segment
     * @param address an address in the memory
     * @param className the current class name
     */
    private void writePopToStatic(int address, String className){
        asm("//// -- pop static -- ");
        asm("@" + String.valueOf(address));
        asm("D=A");
        asm("@"+className+address);
        copyAToR14();
        popR13();
        copyFromR13ToRamAddressInR14();
        asm("//// -- pop static end -- ");
    }

    /**
     * write pop to the local segment
     * @param address an address in the memory
     */
    private void writePopToLocal(int address){
        asm("//// -- pop local -- ");
        asm("@" + String.valueOf(address));
        asm("D=A");
        loadLocalAddressToA();
        asm("A=D+A");
        copyAToR14();
        popR13();
        copyFromR13ToRamAddressInR14();
        asm("//// -- pop local end -- ");
    }
    /**
     * write pop to the argument segment
     * @param address an address in the memory
     */
    private void writePopToArgument(int address){
        asm("//// -- pop argument -- ");
        asm("@" + String.valueOf(address));
        asm("D=A");
        loadArgumentAddressToA();
        asm("A=D+A");
        copyAToR14();
        popR13();
        copyFromR13ToRamAddressInR14();
        asm("//// -- pop argument end -- ");
    }



    /**
     * write the program flow operations
     * @param operation a string that represent the operation
     * @param name a name
     */
    public void writeProgramFlow(String operation ,String name){
        switch(operation){
            case IF_GOTO:
                writeIf(name);
                break;
            case GOTO:
                writeGoto(name);
                break;
            default:
                writeLabel(name);
                break;
        }
    }

    /**
     * write a conditional jump , if the result isn't zero jump else continue to the next instruction
     * @param name a label name for the jump
     */
    private void writeIf(String name){
        asm("// if_goto");
        popToD();
        asm("@"+name);
        asm("D;JNE");
        asm("// end if_goto");
    }

    /**
     * write a unconditional jump
     * @param name a label name for the jump
     */
    private void writeGoto(String name){
        asm("//goto");
        asm("@"+name);
        asm("0;JMP");
        asm("// end goto");
    }

    /**
     * write in asm the label definition
     * @param name the label name
     */
    private void writeLabel(String name){
        asm("// add label");
        asm("("+name+")");

    }

    /**
     * signify which operation is it - a function declaration or a function call
     * @param operation a string that represent the operation
     * @param name the function name
     * @param numLocal the number of local variables
     */
    public void writeFunctionOrCall(String operation, String name,int numLocal){

        switch(operation){
            case FUNCTION:
                writeFunction(name, numLocal); //write a function declaration
                break;
            default:
            	writeCall(name, numLocal);
                break;
        }
    }

    /**
     * write a function declaration
     * hay niv, there is some assembly problem here. i changed it a bit.
     * @param name the function name
     * @param numLocal the number of local variables
     */
    private void writeFunction(String name, int numLocal){
        asm("// ---function declaration---");
        asm("("+name+")");
        for(int i=0;i<numLocal;i++){
        	asm("@0");
        	asm("D=A");
            pushD();
        }
        asm("// ---end function declaration---");
}
    private void writeCall(String functionName, int argc){
    	String returnSymbol = "@" + "return" + returnCounter;
    	String returnLabel = "(" + "return" + returnCounter + ")";
    	returnCounter++;
    	
    	asm("// ----- call " + functionName + "  Line number = " + this.lineCounter);

    	asm(returnSymbol);
    	asm("D=A");
    	pushD();
    	
    	
    	asm("// push LCL");
    	writePushAddresses(LOCAL);
        
    	asm("// push ARG");
    	writePushAddresses(ARGUMENT);      
        
    	asm("// push THIS");
        writePushAddresses(THIS);
    	
    	asm("// push THAT");
        writePushAddresses(THAT);
        
        asm("// ARG = SP - n - 5");
        
        asm("// load and push SP");
        loadStackAddressToA();
        copyAToR13();
        pushR13();
        
        asm("// push n; push 5; add");
        writePushToConstant(5);
        writePushToConstant(argc);
        writeAdd();
        asm("// sub");
        writeSub();
        asm("// pop to ARG");
        popToD();
        asm("@ARG");
        asm("M=D");
        
        asm("@SP");
        asm("D=M");
        asm("@LCL");
        asm("M=D");

    	// goto f
    	asm("@" + functionName);
    	asm("0; JMP");
    	asm(returnLabel);
    }
    
    public void writeReturn(){

    	asm("//----------------- return! ---------------");
    	//----------------------
    	asm("@LCL");
    	asm("D=M");

    	asm("@R14");
    	asm("M=D");

    	asm("@5");
    	asm("A=D-A");
    	asm("D=M");
    	asm("@R15");
    	asm("M=D");

    	// ----------------------
    	asm("// manage returned value");
    	
    	popR13();
    	asm("@R13");
    	asm("D=M");
    	loadArgumentAddressToA();
    	asm("M=D");
    	    	
    	asm("// restore SP" + this.lineCounter);
    	asm("@ARG");
    	asm("D=M+1");
    	asm("@SP");
    	asm("M=D");
    	
    	asm("// restore THAT" + this.lineCounter);
    	asm("@R14");
    	asm("M=M-1");
    	asm("A=M");
    	asm("D=M");
    	
    	asm("@THAT");
    	asm("M=D");

    	asm("// restore THIS");
    	asm("@R14");
    	asm("M=M-1");
    	asm("A=M");
    	asm("D=M");

    	asm("@THIS");
    	asm("M=D");
    	
    	asm("// restore ARG");
    	asm("@R14");
    	asm("M=M-1");
    	asm("A=M");
    	asm("D=M");

    	asm("@ARG");
    	asm("M=D");
    	
    	asm("// restore local");
    	asm("@R14");
    	asm("M=M-1");
    	asm("A=M");
    	asm("D=M");

    	asm("@LCL");
    	asm("M=D");
    	
    	asm("@R15");
    	asm("A=M");
    	asm("0; JMP");
    	
    }

    /**
     * set sp as RAM[256] and call the function Sys.init
     */
    public void startFile(){
    	asm("@256");
    	asm("D=A");
    	asm("@SP");
    	asm("M=D");
    	writeCall("Sys.init", 0);

    	
    }



    /**
     * advance SP
     */
    private void advanceStack(){
        asm("//// AdvanceStack");
        asm("@SP");
        asm("M=M+1");
    }

    /**
     * set R13 as the address
     */
    private void copyAToR13(){
        asm("//	Copy A To R13");
        asm("D=A");
        asm("@R13");
        asm("M=D");
    }

    /**
     * set R14 as the address
     */
    private void copyAToR14(){
        asm("//Copy A To R14");
        asm("D=A");
        asm("@R14");
        asm("M=D");
    }

    /**
     * set R14 as R13
     */
    private void copyFromR13ToRamAddressInR14(){
        asm("// copy from r13 to ram address in ram14");
        asm("@13");
        asm("D=M");
        asm("@14");
        asm("A=M");
        asm("M=D");
    }


    /**
     * load arg address to A
     */
    private void loadArgumentAddressToA(){
        asm("@ARG");
        asm("A=M");
    }

    /**
     * load local address to A
     */
    private void loadLocalAddressToA(){
        asm("@LCL");
        asm("A=M");
    }

    /**
     * load stack address to A
     */
    private void loadStackAddressToA(){
        asm("@SP");
        asm("A=M");
    }

    /**
     * load that address to A
     */
    private void loadThatAddressToA(){
        asm("@THAT");
        asm("A=M");
    }

    /**
     * load this address to A
     */
    private void loadThisAddressToA(){
        asm("@THIS");
        asm("A=M");
    }

    /**
     * pop R13
     */
    private void popR13(){
        asm("//pop to R13");
        asm("// first stage - pop to D");
        asm("@SP");
        asm("M=M-1");
        asm("A=M");
        asm("D=M");
        asm("//second stage - write D to R13");
        asm("@R13");
        asm("M=D");
    }

    /**
     * pop to D
     */
    private void popToD (){
        asm("// popToD");
        asm("@0");
        asm("M=M-1");
        asm("A=M");
        asm("D=M");
    }


    /**
     * push R13
     */
    private void pushR13(){
        asm("// PushR13");
        asm("@R13");
        asm("D=M");
        asm("@SP");
        asm("A=M");
        asm("M=D");
        asm("@SP");
        asm("M=M+1");
    }

    /**
     * push D
     */
    private void pushD() {
        asm("//Push D");
        asm("@SP");
        asm("A=M");
        asm("M=D");
        asm("@SP");
        asm("M=M+1");
    }



}
