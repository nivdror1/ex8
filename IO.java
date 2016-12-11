import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.*;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** the purpose of this class is to read and write the IO*/
public class IO {
    private static final String UNREADABLE_FILE= "file could not been read";
    private static final String UNWRITTENABLE_FILE= "file could not been written into";
    private static final String FILE_NOT_EXISTS = "enter another file, since the path was wrong";
    private static final String ASM = ".asm";
    private static final String DOT = "\\w++\\.";
    private static final String VM= DOT+"vm";
    private static final Pattern VM_PATTERN =Pattern.compile(VM);
    private static final int THREE =3;
    private static final String SLASH="\\";


    /**
     * the main method which control the progress of the assembler
     */
    public static void main(String[] args) {
        File file = new File(args[0]);
        if (file.exists()) // check if the file exists
        {
            //check if it is a file or a directory and get all the appropriate files
            ArrayList<File> vmList =fileOrDirectory(file);
            String outputFileName = setOutputFileName(vmList.size(),file);

            readAllVmFiles(vmList);
            writeToASingleAsmFile(outputFileName);
        } else {
            System.out.println(FILE_NOT_EXISTS);
        }
    }

    /**
     * get the name of the asm file and exchange it to asm file
     * @param fileCount the number of files to parse
     * @param file the input file
     * @return the file name but with a suffix of a asm file
     */
    private static String setOutputFileName(int fileCount, File file ) {
        String location= file.getAbsolutePath();
        if(fileCount==1){ // if the there is only one file
            location=location.substring(0,location.length()-THREE);
            location+= ASM;
        }
        else{ //if it is a directory
            location+=SLASH+file.getName()+ASM;
        }
        return location;
    }

    /**
     * read and parse a specific file
     * @param reader a bufferReader
     * @throws IOException
     */
    private static void readAndParse(BufferedReader reader) throws IOException {
        Parser parser = new Parser(); //define a new parser
        String text;
        while ((text = reader.readLine()) != null) // add the lines to the container
        {
            parser.getVmLines().add(text);
        }
        parser.parseVmFile(); // parse the vm text
    }

    /**
     * check if the input file is an actual file or a directory - filter only the files that ends with ".vm"
     * @param file a file
     * @return a array list of the files that were found
     */
    private static ArrayList<File> fileOrDirectory(File file) {
        File[] filesArray;
        ArrayList<File> vmFiles = new ArrayList<>();
        // check if it a file
        if (file.isFile())
        {
            vmFiles.add(file);
        } else if (file.isDirectory()) //check for a directory
        {
            //create a filter that specifies the suffix of the file
            filesArray = file.listFiles(); // create an array of all the files in the directory

            for (int i = 0; i < filesArray.length; i++) {
                Matcher m = VM_PATTERN.matcher(filesArray[i].getName());
                if (m.lookingAt()) { // find the dot char
                    vmFiles.add(filesArray[i]);
                }
            }
        }
        return vmFiles;
    }

    /**
     * read all of the vm files
     * @param vmList an arrat list of vm files
     */
    private static void readAllVmFiles(ArrayList <File> vmList){
        for (int j=0;j<vmList.size();j++) {
            try (FileReader vmFile = new FileReader(vmList.get(j));// define the BufferReader and BufferWriter
                 BufferedReader reader = new BufferedReader(vmFile))
            {
                //read the file and parse it
                readAndParse(reader);

            } catch (IOException e) {
                System.out.println(UNREADABLE_FILE);
            }
        }
    }

    /**
     * write in the output file the asm text that been translated
     * @param outputFileName a string that represents a output file name
     */
    private static void writeToASingleAsmFile(String outputFileName){
        try(FileWriter asmFile = new FileWriter(outputFileName);
            BufferedWriter writer = new BufferedWriter(asmFile)){

            //write the binary code to an output file
            for (int i = 0; i < CodeWriter.getCodeWriter().getAsmLines().size(); i++) {
                writer.write(CodeWriter.getCodeWriter().getAsmLines().get(i) + "\n");
            }
        }catch(IOException e2){
            System.out.println(UNWRITTENABLE_FILE);
        }
    }
}