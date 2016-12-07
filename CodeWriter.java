import org.omg.CosNaming.NamingContextExtPackage.StringNameHelper;

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

    /** arithmetic operations*/
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
    private static final String CONSTANT= "constant";
    private static final String LOCAL= "local";
    private static final String ARGUMENT= "argument";
    private static final String THAT= "that";
    private static final String THIS= "this";
    private static final String POINTER= "pointer";
    private static final String STATIC= "static";
    private static final String TEMP= "temp";

    /**program control operations*/
    private static final String IF_GOTO="if-goto";
    private static final String GOTO ="goto";

    private static final String FUNCTION ="function";

    public enum AssemblyFunction {
        CopyAToR13,
        CopyAToR14,
        CopyFromRamAddressInR14ToR13,
        CopyFromR13ToRamAddressInR14,
        LoadArgumentAddressToA,
        LoadLocalAddressToA,
        LoadStackAddressToA,
        LoadThatAddressLoA,
        LoadThisAddressToA,
        PopR13,
        PopToD,
        PushR13,
        PushD,
        AdvanceStack

    }


    /** the unique instance of CodeWriter*/
    private static CodeWriter codeWriter= null;

    /** this ArrayList signify the output asm code*/
    private ArrayList<String> asmLines;

    /** a hash map that contains asm functions*/
    private HashMap<AssemblyFunction, String>	codeFileMap;

    /** a label counter*/
    private int labelCounter;

    /** a hash map that contain the label and there addresses*/
    private HashMap<String, String> labelMap;

    /** a singleton constructor*/
    private CodeWriter(){
        this.asmLines= new ArrayList<>();
        labelCounter = 0;
        codeFileMap = new HashMap<>();
        labelMap= new HashMap<>();
        addFunctionsToHashMap();
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
        writeFunctionFromFile(	AssemblyFunction.PopToD					);
        writeFunctionFromFile(	AssemblyFunction.LoadStackAddressToA	);
        asm("A=A-1");
        asm("M=M+D");
        asm("//// ---add-end---");
    }
    /**
     * write the sub instruction in asm
     */
    private void writeSub(){
        asm("//// ---sub---");
        writeFunctionFromFile(	AssemblyFunction.PopToD					);
        writeFunctionFromFile(	AssemblyFunction.LoadStackAddressToA	);
        asm("A=A-1");
        asm("M=M-D");
        asm("//// ---sub-end---");
    }
    /**
     * write the negate instruction in asm
     */
    private void writeNeg(){
        asm("//// ---neg---");
        writeFunctionFromFile(AssemblyFunction.LoadStackAddressToA);
        asm("A=A-1");
        asm("M=-M");
        asm("//// ---neg-end---");
    }
    /**
     * write the and instruction in asm
     */
    private void writeAnd(){
        asm("//// ---and---");
        writeFunctionFromFile(	AssemblyFunction.PopToD					);
        writeFunctionFromFile(	AssemblyFunction.LoadStackAddressToA	);
        asm("A=A-1");
        asm("M=M&D");
        asm("//// ---and-end---");
    }
    /**
     * write the or instruction in asm
     */
    private void writeOr(){
        asm("//// ---or---");
        writeFunctionFromFile(	AssemblyFunction.PopToD					);
        writeFunctionFromFile(	AssemblyFunction.LoadStackAddressToA	);
        asm("A=A-1");
        asm("M=M|D");
        asm("//// ---or-end---");
    }
    /**
     * write the not instruction in asm
     */
    private void writeNot(){
        asm("//// ---not---");
        writeFunctionFromFile(AssemblyFunction.LoadStackAddressToA);
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
        writeSub();
        asm("@1");
        asm("A=-A");
        writeFunctionFromFile(AssemblyFunction.CopyAToR13);
        writeFunctionFromFile(AssemblyFunction.PopToD);
        asm("@label" + String.valueOf(this.labelCounter));
        asm(RuleOnD);//asm("D; JEQ");
        asm("@0");
        writeFunctionFromFile(AssemblyFunction.CopyAToR13);
        asm("(label" + String.valueOf(this.labelCounter) + ")");
        writeFunctionFromFile(AssemblyFunction.PushR13);
        this.labelCounter++;

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
     * @param address a string representing the address
     */
    public void writePushPop(String operation, String memory, int address){
        // todo is really a address input or data or both?
        if(operation.equals(PUSH)) {
            writePush(memory, address); //write push
        }
        else if(operation.equals(POP)){
            writePop(memory, address); //write pop
        }

    }

    /**
     * write push according to the memory segment
     * @param memory a string representing the memory segment
     * @param arg2 a data value
     */
    private void writePush(String memory, int arg2){
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
                writePushToStatic(arg2);
                break;

        }
    }

    //todo i dont understand what the function does, so omri please document this function
    private void pushSub_arg_const_this_that(){
        asm("A=A+D");
        asm("// A have now the address of the data to take from the ram");
        asm("D=M");
        asm("@R13");
        asm("M=D");
        asm("// R13 now have the data");

        writeFunctionFromFile(AssemblyFunction.LoadStackAddressToA);
        writeFunctionFromFile(AssemblyFunction.CopyAToR14);
        writeFunctionFromFile(AssemblyFunction.CopyFromR13ToRamAddressInR14);
        writeFunctionFromFile(AssemblyFunction.AdvanceStack);
    }

    /**
     * write pop operation in asm language
     * @param memory a string representing the memory segment
     * @param address a string representing the address
     */
    private void writePop(String memory, int address){
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
                writePopToStatic(address);
                break;
            //case (null):
            default:
                writeFunctionFromFile(AssemblyFunction.PopToD);
                break;

        }
    }

    /**
     * write the assembly function that is given as an input
     * @param function the name of the assembly function that need to be written
     */
    private void writeFunctionFromFile(AssemblyFunction function){
        String path = this.codeFileMap.get(function);
        File file= new File(path);
        if(file.exists()){
            try (FileReader asmFile = new FileReader(file);
                 BufferedReader reader =new BufferedReader(asmFile)){
                String line;
                while((line= reader.readLine())!=null) // add the lines to the container
                {
                    asm(line);
                }
            } catch (IOException e) {
                System.out.println("Files error   " + path);
                e.printStackTrace();
            }// define the BufferReader and BufferWriter

        }else{
            System.out.println("error! function file " + path + "not exist!!!");
        }
    }
    /**
     * add an asm line to asmLines
     */
    private void asm(String asmLine){
        this.asmLines.add(asmLine);
    }

    /**
     * write push to the constant segment
     * @param arg2 a data value
     */
    private void writePushToConstant(int arg2){
        asm("//// ----- push constant -------");
        writeInsertConstantToR13(arg2);
        writeFunctionFromFile(AssemblyFunction.PushR13);
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
     */
    private void writePushToStatic(int arg2){
        asm("//// ----- push static -------");
        asm("@" + String.valueOf(arg2));
        asm("D=A");
        asm("@16");
        pushSub_arg_const_this_that();
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
        writeFunctionFromFile(AssemblyFunction.LoadThatAddressLoA);
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
        writeFunctionFromFile(AssemblyFunction.LoadThisAddressToA);
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
        writeFunctionFromFile(AssemblyFunction.LoadLocalAddressToA);
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
        writeFunctionFromFile(AssemblyFunction.LoadArgumentAddressToA);
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
        writeFunctionFromFile(AssemblyFunction.CopyAToR14);
        writeFunctionFromFile(AssemblyFunction.PopR13);
        writeFunctionFromFile(AssemblyFunction.CopyFromR13ToRamAddressInR14);
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
        writeFunctionFromFile(AssemblyFunction.LoadThisAddressToA);
        asm("A=D+A");
        writeFunctionFromFile(AssemblyFunction.CopyAToR14);
        writeFunctionFromFile(AssemblyFunction.PopR13);
        writeFunctionFromFile(AssemblyFunction.CopyFromR13ToRamAddressInR14);
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
        writeFunctionFromFile(AssemblyFunction.LoadThatAddressLoA);
        asm("A=D+A");
        writeFunctionFromFile(AssemblyFunction.CopyAToR14);
        writeFunctionFromFile(AssemblyFunction.PopR13);
        writeFunctionFromFile(AssemblyFunction.CopyFromR13ToRamAddressInR14);
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
        writeFunctionFromFile(AssemblyFunction.CopyAToR14);
        writeFunctionFromFile(AssemblyFunction.PopR13);
        writeFunctionFromFile(AssemblyFunction.CopyFromR13ToRamAddressInR14);
        asm("//// -- pop pointer end -- ");
    }
    private void writePopToStatic(int address){
        asm("//// -- pop static -- ");
        asm("@" + String.valueOf(address));
        asm("D=A");
        asm("@16");
        asm("A=D+A");
        writeFunctionFromFile(AssemblyFunction.CopyAToR14);
        writeFunctionFromFile(AssemblyFunction.PopR13);
        writeFunctionFromFile(AssemblyFunction.CopyFromR13ToRamAddressInR14);
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
        writeFunctionFromFile(AssemblyFunction.LoadLocalAddressToA);
        asm("A=D+A");
        writeFunctionFromFile(AssemblyFunction.CopyAToR14);
        writeFunctionFromFile(AssemblyFunction.PopR13);
        writeFunctionFromFile(AssemblyFunction.CopyFromR13ToRamAddressInR14);
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
        writeFunctionFromFile(AssemblyFunction.LoadArgumentAddressToA);
        asm("A=D+A");
        writeFunctionFromFile(AssemblyFunction.CopyAToR14);
        writeFunctionFromFile(AssemblyFunction.PopR13);
        writeFunctionFromFile(AssemblyFunction.CopyFromR13ToRamAddressInR14);
        asm("//// -- pop argument end -- ");
    }

    /**
     * add the assembly functions to the hash map
     */
    private void addFunctionsToHashMap(){
        String dirPath = "assemblyCode";//"C:\\Users\\omri\\workspace\\ex7\\assemblyCode";
        codeFileMap.put(AssemblyFunction.CopyAToR13, dirPath + "\\CopyAToR13.asm");
        codeFileMap.put(AssemblyFunction.CopyAToR14, dirPath + "\\CopyAToR14.asm");
        codeFileMap.put(AssemblyFunction.CopyFromR13ToRamAddressInR14, dirPath + "\\CopyFromR13ToRamAddressInR14.asm");
        codeFileMap.put(AssemblyFunction.CopyFromRamAddressInR14ToR13, dirPath + "\\CopyFromRamAddressInR14ToR13.asm");
        codeFileMap.put(AssemblyFunction.LoadArgumentAddressToA, dirPath + "\\LoadArgumentAddressToA.asm");
        codeFileMap.put(AssemblyFunction.LoadLocalAddressToA,  dirPath + "\\LoadLocalAddressToA.asm");
        codeFileMap.put(AssemblyFunction.LoadStackAddressToA, dirPath + "\\LoadStackAddressToA.asm");
        codeFileMap.put(AssemblyFunction.LoadThatAddressLoA, dirPath + "\\LoadThatAddressToA.asm");
        codeFileMap.put(AssemblyFunction.LoadThisAddressToA, dirPath + "\\LoadThisAddressToA.asm");
        codeFileMap.put(AssemblyFunction.PopR13, dirPath + "\\PopR13.asm");
        codeFileMap.put(AssemblyFunction.PopToD, dirPath + "\\PopToD.asm");
        codeFileMap.put(AssemblyFunction.PushR13, dirPath + "\\pushR13.asm");
        codeFileMap.put(AssemblyFunction.PushD, dirPath + "\\PushD.asm");
        codeFileMap.put(AssemblyFunction.AdvanceStack, dirPath + "\\AdvanceStack.asm");
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
        writeFunctionFromFile(AssemblyFunction.PopToD);
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
                break;
        }
    }
    public void writeReturn (String operation, String name,int number){

    }

    /**
     * write a function declaration
     * @param name the function name
     * @param numLocal the number of local variables
     */
    private void writeFunction(String name, int numLocal){
        asm("// function declaration");
        asm("("+name+")");
        for(int i=0;i<numLocal;i++){
            asm("D=0"); //set d to zero
            // set a local address to A register
            writeFunctionFromFile(AssemblyFunction.LoadLocalAddressToA);

            if(i>0){
                asm("A=A+"+i);
            }
            asm("M=D");
            //push D into the global stack
            writeFunctionFromFile(AssemblyFunction.PushD);
        }
        asm("// end function declaration");
}
}