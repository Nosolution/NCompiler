package lex;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

/**
 * Generate lex lexical parser in java language basing on given information
 *
 * @author Nosolution
 * @version 1.0
 * @since 2019/12/13
 */
public class CodeGenerator {
    private final static String target = "lex.yy.java";

    /**
     * Generate lexical parser code in java by given information
     *
     * @param arrays     arrays converted by constructed dfa, whose size is assumed to be 4
     * @param actions    action list
     * @param startState the start state
     * @param rawDefs    user-specified codes outside the java class
     * @param localDefs  user-specified codes inside the yylex() method
     * @param userCode   user-specified codes outside the yylex() method
     */
    public static void generate(List<int[]> arrays, List<String> actions, int startState, List<String> rawDefs, List<String> localDefs, List<String> userCode) {
        BufferedWriter writer;
        try {
            writer = new BufferedWriter(new FileWriter(target));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        assert (arrays.size() == 4);

        try {
            for (String def : rawDefs) {
                writer.write(def);
            }

            writer.write("public class Lex {\n");
            writer.write("  private final static int START_STATE = " + startState + ";\n");
            writeIntArray(writer, "yy_ec", arrays.get(0));
            writeIntArray(writer, "yy_base", arrays.get(1));
            writeIntArray(writer, "yy_next", arrays.get(2));
            writeIntArray(writer, "yy_accept", arrays.get(3));


            writer.write("public static void yylex(String[] args) {\n");

            writer.write("  assert(args.length == 2)\n");

            writer.write("  int yy_cur_st = START_STATE;\n");
            writer.write("  int yy_last_accept_st = -1;");
            writer.write("  int yy_cidx = 0;\n");
            writer.write("  int yy_last_accept_idx = -1;\n");
            writer.write("  int yy_act = 0;\n");
            writer.write("  int isEnd = 0;\n");
            writer.write("  int yy_c = -1;\n");
            writer.write("  int correct = 1;\n");

            for (String def : localDefs) {
                writer.write(def);
            }

            writer.write("  char[] src;\n");
            writer.write("  try{\n");
            writer.write("      src = readSourceFile(args[1])\n");
            writer.write("  } catch(IOException e){\n");
            writer.write("      e.printStacktrace();\n");
            writer.write("      return;\n");
            writer.write("  }");

            writer.write("  while(yy_cidx < src.length){\n");
            writer.write("      yy_c = yy_ec[src[yy_cidx]];\n");
            writer.write("      if (yy_accept[yy_cur_st]){\n");
            writer.write("          yy_last_accept_st = yy_cur_st;\n");
            writer.write("          yy_last_accept_idx = yy_cidx;\n");
            writer.write("      }\n");
            writer.write("      if (yy_next[yy_base[yy_cur_st] + yy_c] == -1 && yy_last_accept_st != -1){\n");
            writer.write("          yy_cur_st = yy_last_accept_st;\n");
            writer.write("          yy_cidx = yy_last_accept_idx;\n");
            writer.write("          yy_act = yy_accept[yy_cur_st];\n");
            writer.write("          perform(yy_act);\n");
            writer.write("          System.out.print(\" \");\n");
            writer.write("          yy_cur_st = START_STATE;\n");
            writer.write("          yy_last_accept_st = -1;\n");
            writer.write("          yy_cidx++;\n");
            writer.write("          yy_cur_st = yy_next[yy_base[yy_cur_st] + yy_c];\n");
            writer.write("          continue;\n");
            writer.write("      }\n");
            writer.write("      if (yy_next[yy_base[yy_cur_st] + yy_c] == -1 && yy_last_accept_st == -1) {\n");
            writer.write("          System.out.print(\"ERROR DETECTED IN INPUT FILE !\");\n");
            writer.write("          return -1;\n");
            writer.write("      }\n");
            writer.write("      if (yy_next[yy_base[yy_cur_st] + yy_c] != -1) {\n");
            writer.write("          yy_cur_st = yy_next[yy_base[yy_cur_st] + yy_c];\n");
            writer.write("          yy_cidx++;\n");
            writer.write("      }\n");
            writer.write("  }\n");
            writer.write("  if (yy_cidx == src.length) {\n");
            writer.write("      isEnd = 1;\n");
            writer.write("      if (yy_accept[yy_cur_st] && yy_cidx == yy_last_accept_idx + 1) {\n");
            writer.write("          yy_act = yy_accept[yy_cur_st];\n");
            writer.write("          perform(yy_act);\n");
            writer.write("      } else {\n");
            writer.write("          System.out.println(\"ERROR DETECTED IN INPUT FILE !\");\n");
            writer.write("          correct = 0;\n");
            writer.write("      }\n");
            writer.write("  }\n");
            writer.write("  return 0;\n");
            writer.write("}\n");

            //main entry
            writer.write("public static void main(String[] arg) {\n");
            writer.write("  yylex(args);\n");
            writer.write("}\n");

            //perform action
            writer.write("private static int perform(int act_idx) {\n");
            writer.write("  switch(act_idx) {\n");
            writer.write("      case 0: {\n");
            //writer.write("...\n");
            writer.write("          break;\n");
            writer.write("  }\n");
            for (int i = 0; i < actions.size(); i++) {
                writer.write("      case " + (i + 1) + ": ");
                writer.write(actions.get(i));
                writer.write("          break;\n");
            }
            writer.write("      default:");
            writer.write("          break;\n");
            writer.write("  }\n");
            writer.write("  return 0;\n");
            writer.write("}\n");

            //read source file into array
            writer.write("private static char[] readSourceFile(String filePath) throws IOException {\n");
            writer.write("  File file = new File(filePath);\n");
            writer.write("  int size = file.length();\n");
            writer.write("  char[] buffer = new char[size];\n");
            writer.write("  FileInputStream input = new FileInputStream(file);\n");
            writer.write("  int offset = 0;\n");
            writer.write("  int readCnt = 0;\n");
            writer.write("  while(offset < size && (readCnt = input.read(buffer, offset, size - offset)) >= 0)\n");
            writer.write("      offset += readCnt;\n");
            writer.write("  if(offset != size)\n");
            writer.write("      throw new IOException(\"Cannot read file: \" + filePath);\n");
            writer.write("  input.close();");
            writer.write("  return buffer");

            for (String line : userCode) {
                writer.write(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Output int array
     *
     * @param writer provided writer
     * @param name   specified array name
     * @param array  array content
     */
    private static void writeIntArray(Writer writer, String name, int[] array) throws IOException {
        writer.write("private final static int[] " + name + " = new int[]{ ");
        for (int i = 0; i < array.length; i++) {
            writer.write(array[i]);
            if (i < array.length - 1)
                writer.write(", ");
        }
        writer.write("};\n");
    }
}
