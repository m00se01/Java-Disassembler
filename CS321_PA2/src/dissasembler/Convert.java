package dissasembler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

//@Authors: Brandon Chavez and Richard Bach
// NetIds: bac2 & rbach

public class Convert {
	// array indexes align with other arrays
	public static String codes[] = { "10001011000", // ADD
			"1001000100", // ADDI
			"10001010000", // AND
			"1001001000", // ANDI
			"000101", // B
			"100101", // BL
			"11010110000", // BR
			"10110101", // CBNZ
			"10110100", // CBZ
			"11001010000", // EOR
			"1101001000", // EORI
			"11111000010", // LDUR
			"11010011011", // LSL
			"11010011010", // LSR
			"10101010000", // ORR
			"1011001000", // ORRI
			"11111000000", // STUR
			"11001011000", // SUB
			"1101000100", // SUBI
			"1111000100", // SUBIS
			"11101011000", // SUBS
			"10011011000", // MUL
			"11111111101", // PRNT
			"11111111100", // PRNL
			"11111111110", // DUMP
			"11111111111" // HALT
	};
	public static String instNames[] = { "ADD", "ADDI", "AND", "ANDI", "B", "BL", "BR", "CBNZ", "CBZ", "EOR", "EORI",
			"LDUR", "LSL", "LSR", "ORR", "ORRI", "STUR", "SUB", "SUBI", "SUBIS", "SUBS", "MUL", "PRNT", "PRNL", "DUMP",
			"HALT" };

	public enum instType {
		R, I, D, B, CB, IW
	}

	public static instType instT[] = { instType.R, // ADD
			instType.I, // ADDI
			instType.R, // AND
			instType.I, // ANDI
			instType.B, // B
			instType.B, // BL
			instType.R, // BR
			instType.CB, // CBNZ
			instType.CB, // CBZ
			instType.R, // EOR
			instType.I, // EORI
			instType.D, // LDUR
			instType.R, // LSL
			instType.R, // LSR
			instType.R, // ORR
			instType.I, // ORRI
			instType.D, // STUR
			instType.R, // SUB
			instType.I, // SUBI
			instType.I, // SUBIS
			instType.R, // SUBS
			instType.R, // MUL
			instType.R, // PRNT
			instType.R, // PRNL
			instType.R, // DUMP
			instType.R // HALT
	};


	
	//parse the binary string into a decimal number
	public static String binaryToNum(String binary) {
		int t = Integer.parseInt(binary, 2);
		return Integer.toString(t);
	}

	// converts a given binary string into a decimal string used for the offsets of Branch Instructions
	public static int binaryToSigned(String binary) {
	    //Check if the number is negative
	    if (binary.charAt(0) == '1') {
	        //invert all the bits and then add 1 for twos compliment
	        String invertedInt = invertBits(binary);
	        int result = Integer.parseInt(invertedInt, 2);
	        result = (result + 1) * -1;
	        return result;
	    } else {
	        return Integer.parseInt(binary, 2);
	    }
	}

	//Invert the bits for twos compliment signed numbers
	public static String invertBits(String binary) {
	    String result = binary;
	    //Swap all the 0' and 1's 
	    result = result.replace("0", " ");
	    result = result.replace("1", "0");
	    result = result.replace(" ", "1");
	    return result;
	}
	
	
	
	
	public static String ToInstruction(String binary) {
		// processing variables
		CharSequence OPCode;
		CharSequence temp;
		String opcodeToString;
		// variables representing bit strings in the different instruction formats
		String Rm;
		String shamt;
		String Rn;
		String Rd;
		String immediate;
		String offset;
		String op;
		String Rt;

		int index = 0; // used to store the index corresponding to the three arrays we use above.
		int curLine = 0; // keeps track of current line for the label process.

		String output = ""; // used in the process of storing lines into the array lists as well as serving
		// the variables we dump everything into at the end of the method as one string.

		// stores the instruction lines
		ArrayList<String> lineList = new ArrayList<String>();
		// stores the lines where labels are placed.
		ArrayList<Integer> labels = new ArrayList<Integer>();
		for (int j = 0; j < binary.length(); j += 32) {
			// figures out the OPCode and stores its corresponding index in the variable
			// index
			for (int i = 0; i < codes.length; i++) {
				OPCode = binary.subSequence(j, j + (codes[i]).length());
				opcodeToString = (codes[i]);
				if (OPCode.toString().equals(opcodeToString)) {
					index = i;
					i = codes.length + 10; // exits out of for loop
				}
			}
			// appends instruction name to line
			output += instNames[index];
			// given the OPCode's corresponding instruction format, we format and append the
			// rest of the
			// instruction to the output string.
			switch (instT[index]) {
			case R:
				// R-Type - opcode: 11 Rm: 5 shamt: 6 Rn: 5 Rd: 5

				// divides the string into substrings based on the sizes of each attribute above
				// stores substrings in appropriately named variables
				temp = binary.subSequence(j + 10 + 1, j + 11 + 5);
				Rm = temp.toString();
				temp = binary.subSequence(j + 15 + 1, j + 16 + 6);
				shamt = temp.toString();
				temp = binary.subSequence(j + 21 + 1, j + 22 + 5);
				Rn = temp.toString();
				temp = binary.subSequence(j + 26 + 1, j + 27 + 5);
				Rd = temp.toString();

				// appends register names to the instruction
				output += " X" + binaryToNum(Rd) + ",";
				output += " X" + binaryToNum(Rn) + ",";
				// determines what to append depending on whether shamt or Rm is equal to 0
				if (binaryToNum(shamt).equals("0"))
					output += " X" + binaryToNum(Rm);
				else
					output += " #" + binaryToNum(shamt);
				break;
			case I:
				// I-Type - opcode: 10 immediate: 12 Rn: 5 Rd: 5

				// divides the string into substrings based on the sizes of each attribute above
				// stores substrings in appropriately named variables
				temp = binary.subSequence(j + 9 + 1, j + 10 + 12);
				immediate = temp.toString();
				temp = binary.subSequence(j + 21 + 1, j + 22 + 5);
				Rn = temp.toString();
				temp = binary.subSequence(j + 26 + 1, j + 27 + 5);
				Rd = temp.toString();

				// appends register names to the instruction
				output += " X" + binaryToNum(Rd) + ",";
				output += " X" + binaryToNum(Rn) + ",";
				output += " #" + binaryToNum(immediate);
				break;
			case D:
				// D-Type - opcode: 11 offset: 9 op: 2 Rn: 5 Rt: 5

				// divides the string into substrings based on the sizes of each attribute above
				// stores substrings in appropriately named variables
				temp = binary.subSequence(j + 10 + 1, j + 11 + 9);
				offset = temp.toString();
				temp = binary.subSequence(j + 19 + 1, j + 20 + 2);
				op = temp.toString();
				temp = binary.subSequence(j + 21 + 1, j + 22 + 5);
				Rn = temp.toString();
				temp = binary.subSequence(j + 26 + 1, j + 27 + 5);
				Rt = temp.toString();

				// appends register names to the instruction
				output += " X" + binaryToNum(Rt);
				output += ", [X" + binaryToNum(Rn) + ", #" + binaryToNum(offset) + "]";
				break;
			case B:
				// B-Type - opcode: 6 offset: 26

				// divides the string into substrings based on the sizes of each attribute above
				// stores substrings in appropriately named variables
				temp = binary.subSequence(j + 5 + 1, j + 6 + 26);
				offset = temp.toString();

				// calculates and adds the location of the label to labels (ArrayList) for later
				// use

				// appends register names to the instruction
				output += " " + binaryToSigned(offset);
				break;
			case CB:
				// CB-Type - opcode: 8 offset: 19 Rt: 5

				// divides the string into substrings based on the sizes of each attribute above
				// stores substrings in appropriately named variables
				temp = binary.subSequence(j + 7 + 1, j + 8 + 19);
				offset = temp.toString();
				temp = binary.subSequence(j + 26 + 1, j + 27 + 5);
				Rt = temp.toString();

				// calculates and adds the location of the label to labels (ArrayList) for later
				// appends register names to the instruction
				output += " X" + binaryToNum(Rt);
				output += " " + binaryToSigned(offset);
				break;
			default:
				break;
			}
			
			// at the end of each iteration, the contents of output get stored in the
			// lineList array list
			lineList.add(output);
			// output is emptied every iteration
			output = "";
			// curLine is incremented, representing that the algorithm moves onto the next
			// line
			curLine++;
		}
		// iterates through lineList and stores its contents into output for returning.
		// if the for loop
		// is currently on a line that is contained in labels (ArrayList), the label is
		// printed above
		// the corresponding instruction line.
		for (int i = 0; i < lineList.size(); i++) {
			if (labels.contains(i))
				output += "label " + labels.indexOf(i) + ":\n";
			output += lineList.get(i) + "\n";
		}
		return output;
	}

	public static void main(String[] args) throws IOException {
		
		Scanner sc = new Scanner(System.in);
		System.out.println("Please enter the filepath to wherever your .machine file is stored: ");
		String machineFile = sc.next();
		File myFile = new File(machineFile);
		sc.close();
		
		//Byte Stream reader
		FileInputStream in = null;
		in = new FileInputStream(myFile);

		int totalNumInstructions = in.available();
		String result = "";
		String finalResult = "";

		for (int j = 0; j < totalNumInstructions; j++) {
			result = Integer.toBinaryString(in.read());
			String temp = formatResult(result);
			finalResult = finalResult.concat(temp);
		}

		System.out.println(ToInstruction(finalResult));
		in.close();
	}

	//Format the binary string to include the 0's in the string
	public static String formatResult(String s) {
		return String.format("%8s", s).replaceAll(" ", "0");
	}

}
