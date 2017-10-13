package cs49jproject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Compiler {

	public static void main(String[] args) {

		// name of txt file
		final String INPUT_FILE_NAME = "ClassA.txt";

		// Second Class
		final String SECOND_INPUT_FILE = "ClassB.txt";

		// getting the name of the class
		final String CLASS_NAME = INPUT_FILE_NAME.split("\\.")[0];

		// Getting name for the other class
		final String OTHER_CLASS_NAME = SECOND_INPUT_FILE.split("\\.")[0];

		// READING SECOND CLASS
		File otherclass = new File(SECOND_INPUT_FILE);
		String otherclassLines = "";
		BufferedReader otherbr = null;
		ArrayList<String> storedLines = new ArrayList<String>();

		// READING FIRST CLASS
		File newfile = new File(INPUT_FILE_NAME);
		String lines = "";
		BufferedReader br = null;
		ArrayList<String> lines2 = new ArrayList<String>();

		try {
			FileReader fr = new FileReader(newfile);
			br = new BufferedReader(fr);
			while ((lines = br.readLine()) != null) {
				lines2.add(lines);
			}

			FileReader fr2 = new FileReader(otherclass);
			otherbr = new BufferedReader(fr2);
			while ((otherclassLines = otherbr.readLine()) != null) {
				storedLines.add(otherclassLines);
			}

		} catch (FileNotFoundException e) {
			System.out.println(e.getMessage());
		} catch (IOException e) {

			System.out.println(e.getMessage());
		}

		//The lines of the classes stored in Arrays
		String[] allLines = lines2.toArray(new String[lines2.size()]);
		String[] otherClassLines = storedLines.toArray(new String[storedLines.size()]);

		// THE REGEX'S
		Pattern keywordRegex = Pattern.compile("\\s*(\\{|\\})?\\s*(\\w+|\\})");
		Pattern packageRegex = Pattern.compile("(\\s*package\\s+([\\w\\.]+);)");
		Pattern importRegex = Pattern.compile("\\s*import\\s+([\\w\\.]+)(\\.\\*)?;");
		Pattern classRegex = Pattern.compile("\\s*(public\\s|abstract\\s|final\\s|\\class\\s){0,1}\\s*class\\s+(\\w+)\\s*\\{*");
		Pattern instanceVariableRegex = Pattern.compile("\\s*(int|float|char|String){1}\\s+(\\w+)\\s*(\\=\\s*([\"*\\w\\d\"*]+))?;");
		Pattern constructorRegex = Pattern.compile("\\s*(public\\s+|private\\s+|protected\\s+){0,1}" + CLASS_NAME + "\\s*\\((.*)\\)\\s*\\{*");
		Pattern constructorRegex2 = Pattern.compile("\\s*(public\\s+|private\\s+|protected\\s+){0,1}" + OTHER_CLASS_NAME + "\\s*\\((.*)\\)\\s*\\{*");
		Pattern paramRegex = Pattern.compile("(\\s*(int|float|String|char|\\w+){1}\\s+\\b(\\w*) ?)*");
		Pattern methodRegex = Pattern.compile("\\s*(public\\s+|private\\s+|protected\\s+){0,1}(void\\s+|String\\s+|int\\s+|char\\s+){1}\\s*(\\w+)\\s*\\((.*)\\)\\s*\\{*");
		Pattern objRegex = Pattern.compile("\\s*(" + OTHER_CLASS_NAME + ")\\s*(\\w+)\\s*(\\=\\s*(new)\\s*("+ OTHER_CLASS_NAME + ")\\s*(\\(+)\\s*([\\w\\d])*(\\)+)\\s*(\\;+))");
		Pattern objRegex2 = Pattern.compile("\\s*(" + CLASS_NAME + ")\\s*(\\w+)\\s*(\\=\\s*(new)\\s*(" + CLASS_NAME+ ")\\s*(\\(+)\\s*([\\w\\d])*(\\)+)\\s*(\\;+))");

		ArrayList<Import> imports = new ArrayList<>();
		ArrayList<Import> imports2 = new ArrayList<>();

		// instance of class for the actual class
		Class actualClass = new Class();

		// instance of class for second class
		Class otherClass = new Class();

		Matcher m;
		int errors = 0;
		int otherClassErr = 0;
		int linenumber = 0;
		int otherClassln = 0;
		int opencurly = 0;
		int closecurly = 0;
		int opencurlyb = 0;
		int closecurlyb = 0;

		// Reading the text file by iterating over allLines array(for the actualclass)
		for (String currentString : allLines) {

			linenumber++;

			if (currentString.length() == 0) {
				continue;
			}

			// Adding number of brackets
			if (currentString.contains("{")) {
				opencurly++;
			}
			if (currentString.contains("}")) {
				closecurly++;
			}

			// matcher to match keywords
			m = keywordRegex.matcher(currentString);
			boolean keywordFound = m.find();

			if (keywordFound) {
				if (m.group(1) != null && m.group(1).equals("}")) {
					continue;
				}

				String keyword = m.group(2);

				// checking for actual class package
				if (keyword.equals("package")) {
					if (imports.size() > 0 || actualClass.getClassName() != null) {
						System.err.println(CLASS_NAME + ":\tError at line: " + linenumber + " package should"
								+ " be at the beginning of the file");
						errors++;
					}

					m = packageRegex.matcher(currentString);

					boolean found = m.find();

					// if package syntax doesn't match then the syntax is incorrect
					if (found == false) {
						System.err.println(CLASS_NAME + ":\tIncorrect package syntax at line: " + linenumber);
						errors++;
					}

					while (found) {

						if (actualClass.getPackage() == null) {
							// if package is found set its name
							actualClass.setPackageName(m.group(2));
							System.out.println(CLASS_NAME + ":\tPackage found: " + m.group(2));

						} else {
							// if package name is already set throw duplicate package error
							System.err.println(
									CLASS_NAME + ":\tError found at line: " + linenumber + " multiple packages");
							errors++;
						}
						found = m.find();
					}
				}

				else if (keyword.equals("import")) {
					if (actualClass.getClassName() == CLASS_NAME) {
						System.err.println(CLASS_NAME + ":\timport should be before class");
						errors++;
					}

					m = importRegex.matcher(currentString);
					boolean found = m.find();
					if (found == false) {
						System.err.println(CLASS_NAME + ":\tIncorrect import syntax at line: " + linenumber);
						errors++;
					}
					while (found) {
						if (m.group(1).length() > 0) {
							imports.add(new Import(m.group(1), linenumber));
						}
						System.out.println(CLASS_NAME + ":\timported: " + m.group(1));

						found = m.find();
					}
				}

				else if (keyword.equals("public") || keyword.equals("abstract") || keyword.equals("final")
						|| keyword.equals("class")) {

					m = classRegex.matcher(currentString);
					boolean found = m.find();

					if (!found) {
						System.err.println(
								CLASS_NAME + ":\tIncorrect syntax for the class definition line: " + linenumber);
						errors++;
						System.exit(0);
					}
					while (found) {
						if (actualClass.getClassName() != null) {
							System.err.println(CLASS_NAME + ":\tError at line " + linenumber + "\nthe class "
									+ actualClass.getClassName() + " already been defined");
							errors++;
						}
						if (!m.group(2).equals(CLASS_NAME)) {
							System.err.println(CLASS_NAME + ":\tthe public type " + m.group(2)
									+ " must be defined in its own file");
							errors++;
						}

						if (m.group(1) != null)
							actualClass.setAccessSpecifier(m.group(1));
						actualClass.setClassName(m.group(2));
						actualClass.setStartingLine(linenumber);

						System.out.println(
								"\n" + CLASS_NAME + ":\tClass found\t class name: " + actualClass.getClassName()
										+ "\tAccess specifier: " + actualClass.getAccesSpecifier() + "\n");

						found = m.find();
					}
				} else {
					System.err.println(CLASS_NAME + ":\tkeyword " + keyword + " not rec at line " + linenumber);
					errors++;
					continue;
				}
				keywordFound = m.find();
			}

			else {
				System.err.println(CLASS_NAME + ":\tError at line " + linenumber + ": " + currentString);
				errors++;
			}

			// if class has already been defined continue to analyzing the
			// material inside the class
			if (actualClass.getClassName() != null) {
				break;
			}

		}

		
		//Analyzing the other class.
		for (String currentString : otherClassLines) {
			otherClassln++;

			if (currentString.length() == 0) {
				continue;
			}

			// matcher to match keywords
			m = keywordRegex.matcher(currentString);
			boolean keywordFound = m.find();

			if (keywordFound) {

				if (m.group(1) != null && m.group(1).equals("}")) {
					continue;
				}
				String keyword = m.group(2);

				if (keyword.equals("package")) {
					if (imports2.size() > 0 || otherClass.getClassName() != null) {
						System.err.println(OTHER_CLASS_NAME + ":\t Error at line: " + otherClassln + " package should"
								+ " be at the beginning of the file");
						otherClassErr++;
					}

					m = packageRegex.matcher(currentString);

					boolean found = m.find();

					if (found == false) {
						System.err.println(OTHER_CLASS_NAME + ":\t Incorrect package syntax at line: " + otherClassln);
						otherClassErr++;
					}

					while (found) {

						if (otherClass.getPackage() == null) {
							// if package is is found set its name
							otherClass.setPackageName(m.group(2));
							System.out.println(OTHER_CLASS_NAME + ":\tPackage found: " + m.group(2));

						} else {
							// if package name is already set throw duplicate
							// package error
							System.err.println(OTHER_CLASS_NAME + ":\t Error found at line: " + otherClassln
									+ " multiple packages");
							otherClassErr++;
						}
						found = m.find();
					}
				}

				else if (keyword.equals("import")) {
					if (otherClass.getClassName() == OTHER_CLASS_NAME) {
						System.err.println(OTHER_CLASS_NAME + ":\t import should be before class");
						otherClassErr++;
					}

					m = importRegex.matcher(currentString);
					boolean found = m.find();
					if (found == false) {
						System.err.println(OTHER_CLASS_NAME + ":\t Incorrect import syntax at line: " + otherClassln);
						otherClassErr++;
					}
					while (found) {
						if (m.group(1).length() > 0) {
							imports2.add(new Import(m.group(1), otherClassln));
						}
						System.out.println(OTHER_CLASS_NAME + ":\timported: " + m.group(1));
						found = m.find();
					}
				}

				else if (keyword.equals("public") || keyword.equals("abstract") || keyword.equals("final")
						|| keyword.equals("class")) {
					m = classRegex.matcher(currentString);
					boolean found = m.find();
					if (found == false) {
						System.err.println(OTHER_CLASS_NAME + ":\t Incorrect syntax for class definition at line: "
								+ otherClassln);
						otherClassErr++;
					}
					while (found) {
						if (otherClass.getClassName() != null) {
							System.err.println(OTHER_CLASS_NAME + ":\t Error at line " + otherClassln + "\nthe class "
									+ otherClass.getClassName() + " already been defines");
							otherClassErr++;
						}
						if (!m.group(2).equals(OTHER_CLASS_NAME)) {
							System.err.println(OTHER_CLASS_NAME + ":\t File " + INPUT_FILE_NAME + " can only contain "
									+ "class with the name " + OTHER_CLASS_NAME + " only");
							otherClassErr++;

						}
						if (m.group(1) != null)
							otherClass.setAccessSpecifier(m.group(1));
						otherClass.setClassName(m.group(2));
						otherClass.setStartingLine(otherClassln);

						found = m.find();
					}

				} else {
					System.err.println(OTHER_CLASS_NAME + ":\t keyword" + keyword + " not rec at line " + otherClassln);
					otherClassErr++;
					continue;
				}
				keywordFound = m.find();
			} else {
				System.err.println(OTHER_CLASS_NAME + ":\t Error at line " + otherClassln + ": " + currentString);
				otherClassErr++;
			}

			// if class has already been defined continue to analyzing the
			// material inside the class
			if (otherClass.getClassName() != null) {
				break;
			}
		}

		
		
		// checking the other class's contents
		for (int i = otherClass.getStartingLine(); i < otherClassLines.length; i++) {
			otherClassln++;

			if (otherClassLines[i].length() == 0)
				continue;

			if (otherClassLines[i].contains("{")) {
				opencurlyb++;
			}
			if (otherClassLines[i].contains("}")) {
				closecurlyb++;
			}

			m = keywordRegex.matcher(otherClassLines[i]);
			boolean keywordFound = m.find();

			if (keywordFound) {
				if (m.group(1) != null && m.group(1).equals("}")) {
					continue;
				}
				String keyword = m.group(2);

				if (keyword.equals("package")) {
					System.err.println(OTHER_CLASS_NAME + ":\tError at line: " + otherClassln
							+ " package should be at the beginning of the class");
					otherClassErr++;
				}

				if (keyword.equals("import")) {
					System.err.println(OTHER_CLASS_NAME + ":\tError at line: " + otherClassln
							+ " imports should come before class");
					otherClassErr++;
				}

				// To Detect Instance Variables
				if (keyword.equals("int") || keyword.equals("float") || keyword.equals("String")) {
					m = instanceVariableRegex.matcher(otherClassLines[i]);
					boolean found = m.find();

					if (!found) {
						System.err.println(OTHER_CLASS_NAME + ":\tSyntax Error on line: " + otherClassln);
						otherClassErr++;
					}

					/// Erro if the instance variables don't end in a semicolon
					if (!(otherClassLines[i].endsWith(";"))) {
						System.err.println(OTHER_CLASS_NAME + ":\tSyntax Error missing semi-colon: " + otherClassln);
						otherClassErr++;
					}

					if (found) {
						while (found) {
							if (m.groupCount() == 4)
							{
								otherClass.addInstanceVariable(m.group(1), m.group(2), m.group(4));
							}

							System.out.println(OTHER_CLASS_NAME + ":\tInstance variable: " + m.group(2)
									+ "\tdata type: " + m.group(1) + "\tvalue: " + m.group(3));
							found = m.find();
						}
					}

				}

				// For Class Constructor
				if (keyword.equals(OTHER_CLASS_NAME)) {

					m = constructorRegex2.matcher(otherClassLines[i]);
					boolean wordFound = m.find();

					if (wordFound == false) {
						System.err.println(OTHER_CLASS_NAME + ":\tSyntax Error in constructor ");
						otherClassErr++;
					}

					System.out.println("\n" + OTHER_CLASS_NAME + ":\tConstructor found: " + OTHER_CLASS_NAME);
					otherClass.setConstructor(OTHER_CLASS_NAME);

					int nParameter2 = 1;
					// System.out.println("ALLLLL" + allLines[i]);
					Matcher m2 = paramRegex.matcher(otherClassLines[i]);
					boolean found = m2.find();

					if (found == false) {
						System.err.println(OTHER_CLASS_NAME + ":\tsyntax error");
						otherClassErr++;
					}

					if (found) {

						while (found) {

							if (m2.group().length() != 0) {
								if (m.matches()) {
									otherClass.setConstructorParameter(m.group(2));

								}

								if (((m2.group(2).equals("String"))) || ((m2.group(2).equals("int")))
										|| ((m2.group(2).equals("float"))) || ((m2.group(2).equals("char")))) {
									System.out.println("\tParameter " + nParameter2 + " for constructor:" + "\tType: "
											+ m2.group(2) + "\tName: " + m2.group(3));
									otherClass.addParamType(m2.group(2));
									nParameter2++;

								} else {
									System.err.println(OTHER_CLASS_NAME + ":\tSyntax error on parameter " + nParameter2
											+ " of constructor: " + m2.group(2) + " not recognized");
									otherClassErr++;
									System.out.println();
									nParameter2++;
								}
							}

							found = m2.find();
						}

					}

				}

				// Analyze the Methods in the Class
				if (keyword.equals("private") || keyword.equals("protected") || keyword.equals("public")) {

					// System.out.println("METHOD REGEX" + allLines[i]);
					m = methodRegex.matcher(otherClassLines[i]);

					// System.out.println("METHOD REGEX" + allLines[i]);
					boolean foundmethod = m.find();

					if (foundmethod == false) {
						System.err.println(OTHER_CLASS_NAME + ":\tError on line " + otherClassln
								+ ":\tsyntax error or return type missing");
						otherClassErr++;
					}

					if (foundmethod) {
						while (foundmethod) {
							System.out.println("\n" + OTHER_CLASS_NAME + ":\tMethod found: " + m.group(3)
									+ "\t\tReturn type: " + m.group(2));
							foundmethod = m.find();
						}
					}

					// Checking for parameters inside the Methods
					Matcher m2 = paramRegex.matcher(otherClassLines[i]);
					boolean found = m2.find();

					if (!found) {
						System.err.println(OTHER_CLASS_NAME + ":\tSyntax error: " + otherClassln);
						otherClassErr++;
					}
				}

				// CHECKING OTHER CLASS'S PACKAGE AND CONSTRUCTOR TO COMPARE
				// WITH THE ACTUAL CLASS
				String imported2 = "";
				if (keyword.equals(CLASS_NAME)) 
				{
					// matching object(Instance of the other class)
					Matcher mc = objRegex2.matcher(otherClassLines[i]);
					boolean found = mc.find();

					if (!found) {
						System.err.println(OTHER_CLASS_NAME + ":\tSyntax Error on line: " + otherClassln);
						otherClassErr++;
					}

					System.out.println("\n" + OTHER_CLASS_NAME + ":\tInstance of " + CLASS_NAME + " found at line "
							+ otherClassln);
					for (Import imp : imports2) {
						// if one of the imports contains the other class name save it.
						if (actualClass.getPackage() != null) {
							if (imp.getName().contains(actualClass.getPackage())) {
								imported2 = imp.getName();
							}
						}
					}

					/*	if the Object created in the class is not in the same package
						check if the import for instance of other class exist
						else this will be an error
					 */
					if (!(otherClass.getPackage().equals(actualClass.getPackage()))) {
						if (!imported2.equals(actualClass.getPackage() + "." + CLASS_NAME)) {
							System.err.println(OTHER_CLASS_NAME + ":\tError at line " + otherClassln + ": must import "
									+ CLASS_NAME);
							otherClassErr++;
						}
					}
				}
			}
		}

		
		// Checking the actual class contents for errors
		for (int i = actualClass.getStartingLine(); i < allLines.length; i++) {
			linenumber++;

			if (allLines[i].length() == 0)
				continue;

			if (allLines[i].contains("{")) {
				opencurly++;
			}
			if (allLines[i].contains("}")) {
				closecurly++;
			}

			m = keywordRegex.matcher(allLines[i]);
			boolean keywordFound = m.find();

			if (keywordFound) {
				if (m.group(1) != null && m.group(1).equals("}")) {
					continue;
				}
				String keyword = m.group(2);

				if (keyword.equals("package")) {
					System.err.println(CLASS_NAME + ":\tError at line: " + linenumber
							+ " package should be at the beginning of the class");
					errors++;
				}

				if (keyword.equals("import")) {
					System.err.println(
							CLASS_NAME + ":\tError at line: " + linenumber + " imports should come before class");
					errors++;
				}

				// To Detect Instance Variables
				if (keyword.equals("int") || keyword.equals("float") || keyword.equals("String")) {
					m = instanceVariableRegex.matcher(allLines[i]);
					boolean found = m.find();

					if (!found) {
						System.err.println(CLASS_NAME + ":\tSyntax Error on line: " + linenumber);
						errors++;
					}

					/// Error if the instance variable doesnt end with a semicolon
					if (!(allLines[i].endsWith(";"))) {
						System.err.println(CLASS_NAME + ":\tSyntax Error missing semi-colon: " + linenumber);
						errors++;
					}

					if (found) {
						while (found) {
							if (m.groupCount() == 4) 
							{
								actualClass.addInstanceVariable(m.group(1), m.group(2), m.group(4));
							}

							System.out.println(CLASS_NAME + ":\tInstance variable: " + m.group(2) + "\tdata type: "
									+ m.group(1) + "\tvalue: " + m.group(3));
							found = m.find();
						}
					}

				}

				// For Class Constructor
				if (keyword.equals(CLASS_NAME)) {

					m = constructorRegex.matcher(allLines[i]);
					boolean wordFound = m.find();

					if (wordFound == false) {
						System.err.println(CLASS_NAME + ":\tSyntax Error in constructor ");
						errors++;
					}

					System.out.println("\n" + CLASS_NAME + ":\tConstructor found: " + CLASS_NAME);
					actualClass.setConstructor(CLASS_NAME);

					int nParameter = 1;

					Matcher m2 = paramRegex.matcher(allLines[i]);
					boolean found = m2.find();

					if (found == false) {
						System.err.println(CLASS_NAME + ":\tsyntax error");
						errors++;
					}

					if (found) {

						while (found) {

							if (m2.group().length() != 0) {
								if (m.matches()) {
									actualClass.setConstructorParameter(m.group(2));
								}

								if (((m2.group(2).equals("String"))) || ((m2.group(2).equals("int")))
										|| ((m2.group(2).equals("float"))) || ((m2.group(2).equals("char")))) {
									System.out.println("\tParameter " + nParameter + " for constructor:" + "\tType: "
											+ m2.group(2) + "\tName: " + m2.group(3));
									actualClass.addParamType(m2.group(2));
									nParameter++;

								} else {
									System.err.println(CLASS_NAME + ":\tSyntax error on parameter " + nParameter
											+ " of constructor: " + m2.group(2) + " not recognized");
									errors++;
									System.out.println();
									nParameter++;
								}
							}

							found = m2.find();
						}
					}

				}

				// Analyze the Methods in the Class
				if (keyword.equals("private") || keyword.equals("protected") || keyword.equals("public")) {

					// System.out.println("METHOD REGEX" + allLines[i]);
					m = methodRegex.matcher(allLines[i]);

					// System.out.println("METHOD REGEX" + allLines[i]);
					boolean foundmethod = m.find();

					if (foundmethod == false) {
						System.err.println(CLASS_NAME + ":\tError on line " + linenumber
								+ ":\tsyntax error or return type missing");
						errors++;
					}

					if (foundmethod) {
						while (foundmethod) {
							System.out.println("\n" + CLASS_NAME + ":\tMethod found: " + m.group(3)
									+ "\t\tReturn type: " + m.group(2));
							foundmethod = m.find();
						}
					}

					// Checking for parameters inside the Methods
					Matcher m2 = paramRegex.matcher(allLines[i]);
					boolean found = m2.find();

					if (!found) {
						System.err.println(CLASS_NAME + ":\tSyntax error: " + linenumber);
						errors++;
					}
				}

				// CHECKING OTHER CLASS'S PACKAGE AND CONSTRUCTOR TO COMPARE
				// WITH THE ACTUAL CLASS
				String imported = "";
				if (keyword.equals(OTHER_CLASS_NAME)) {
					// matching object(Instance of the other class)
					Matcher mc = objRegex.matcher(allLines[i]);
					boolean found = mc.find();

					if (!found) {
						System.out.println(CLASS_NAME + ":\tSyntax Error on line: " + linenumber);
					}

					if (found) {
						while (found) {

							// To check if the constructor needs a parameter
							// passed in.
							if (otherClass.getConstructorParameter() != null && (mc.group(7) == null)) {
								System.err.println(CLASS_NAME + ":\tError Line: " + linenumber + "\tThe Constructor "
										+ OTHER_CLASS_NAME + "() is undefined");
								errors++;
								break;
							} else if (otherClass.getConstructorParameter() == null && (mc.group(7) != null)) {
								// error if there is a parameter passed in to
								// the constructor without it having any
								// parameters
								System.err.println(CLASS_NAME + ":\tError Line: " + linenumber + "\tThe constructor "
										+ OTHER_CLASS_NAME + " is undefined");
								errors++;
								break;
							}
							found = m.find();
						}
					}
					System.out.println(
							"\n" + CLASS_NAME + ":\tInstance of " + OTHER_CLASS_NAME + " found at line " + linenumber);
					for (Import imp : imports) {
						// if one of the imports contains the other class name
						// save it.
						if (otherClass.getPackage() != null) {
							if (imp.getName().contains(otherClass.getPackage())) {
								imported = imp.getName();
							}
						}
					}

					// if the Object created in the class is not in the same
					// package
					// check if the import for instance of other class exist
					// else this will be an error
					if (!(actualClass.getPackage().equals(otherClass.getPackage()))) {
						if (!imported.equals(otherClass.getPackage() + "." + OTHER_CLASS_NAME)) {
							System.err.println(CLASS_NAME + ":\tError at line " + linenumber + ": must import "
									+ OTHER_CLASS_NAME);
							errors++;
						}
					}
				}
			}

		} // for loop ends here for analyzing the classes

		
		// to check for any possible missing curly brackets
		if ((opencurly + closecurly) % 2 != 0) 
		{
			if (opencurly < closecurly) 
			{
				System.err.println("Syntax error need '{' to complete classbody");
			} else
				System.err.println("Syntax error need '}' to complete classbody");
			errors++;
		}

		if ((opencurlyb + closecurlyb) % 2 != 0) 
		{
			if (opencurlyb < closecurlyb) {
				System.err.println(OTHER_CLASS_NAME + ":Syntax error need '{' to complete classbody");
			} else
				System.err.println(OTHER_CLASS_NAME + ":Syntax error need '}' to complete classbody");
			otherClassErr++;
		}

		// To check if the class has any errors
		if (errors == 0) 
		{
			System.out.println("\n" + CLASS_NAME + ":\tThe Class is ok");
		} else {
			System.out.println(CLASS_NAME + ":\tThe Class has Errors");
		}

		if (otherClassErr == 0)
		{
			System.out.println("\n" + OTHER_CLASS_NAME + ":\tThe Class is ok");
		} else {
			System.out.println(OTHER_CLASS_NAME + ":\tThe Class has Errors");
		}

	}
}