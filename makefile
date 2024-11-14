JAVAC = javac
JAVA = java
MAIN_CLASS = GatorTicketMaster

#Defining input files
INPUT_FILE = input_sample_1.txt input_sample_2.txt input_1_test_case.txt input_2_test_case.txt input_3_test_case.txt

#Generating output file names by replacing .txt with _output_file.txt
OUTPUT_FILE = $(INPUT_FILE:.txt=_output_file.txt)

all: clean compile $(OUTPUT_FILE)

compile:
	$(JAVAC) *.java

# Pattern rule to generate output files
%_output_file.txt: %.txt
	$(JAVA) $(MAIN_CLASS) $<

# Optional: Run with a specific input file
# Usage: make run INPUT_FILE=input_1_test_case.txt
run:
	$(JAVA) $(MAIN_CLASS) $(INPUT_FILE)

clean:
	del /F /Q *.class *output_file.txt 2>nul

.PHONY: all compile run clean