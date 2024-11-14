# Comppiler and runtime variables
JAVAC = javac
JAVA = java
MAIN_CLASS = GatorTicketMaster


# Defining input files for testing
# List of all test case files that will be processed
INPUT_FILE = input_sample_1.txt input_sample_2.txt input_1_test_case.txt input_2_test_case.txt input_3_test_case.txt


# Generate output filenames by substituting .txt with _output_file.txt
# Example: input_sample_1.txt becomes input_sample_1_output_file.txt
OUTPUT_FILE = $(INPUT_FILE:.txt=_output_file.txt)


# Default target that runs clean, compile and generates all output files
# Usage: make
# This will clean previous builds, compile the code, and process all input files
all: clean compile $(OUTPUT_FILE)


# Compiles all Java files in the current directory
# Usage: make compile
# This will compile all .java files in the current directory and creates .class files
compile:
	$(JAVAC) *.java


# Pattern rule to generate output files from input files
# $< represents the input file
# This rule is automatically used by 'make' command above
%_output_file.txt: %.txt
	$(JAVA) $(MAIN_CLASS) $<


# Target to run the program with a specific input file
# Usage: make run INPUT_FILE=input_1_test_case.txt
run: compile 
	$(JAVA) $(MAIN_CLASS) $(INPUT_FILE)


# Clean target to remove compiled classes and generated output files
# Usage: make clean
# This will remove all .class files and output files
clean:
ifeq ($(OS),Windows_NT)
# Windows environment command to delete files
	del /F /Q *.class 2>nul || true
	del /F /Q *output_file.txt 2>nul || true
else
# Unix environment command to delete files
	rm -f *.class *output_file.txt
endif


# Declare phony targets (targets that don't represent files)
# This prevents conflicts with files that might have the same names
.PHONY: all compile run clean


# Available commands:
# make          									- Runs 'all' target by default
# make all          							- Clean, compile and process all input files
# make compile      							- Only compile the Java files
# make clean        							- Remove all generated files
# make run INPUT_FILE=<filename> 	- Compiles Java files and run with a specific input file
# make help         							- Display help message
# Individual file processing is automatic when using 'make' or 'make all'


# Help target - displays available commands
# Usage: make help
help:
	@echo =====================================================================================
	@echo "GatorTicketMaster Makefile Help Section"
	@echo =====================================================================================
	@echo "Available targets:"
	@echo "  make      		- Runs 'all' target by default. Same as 'make all'"
	@echo "  make all      	- Clean, compile and process all input files"
	@echo "  make compile  	- Only compile the Java files"
	@echo "  make clean    	- Remove all generated files (*.class and *_output_file.txt)"
	@echo "  make run      	- Run with a specific input file"
	@echo "  make help     	- Displays this help message"
	@echo _____________________________________________________________________________________
	@echo "Usage examples:"
	@echo "  make all                                  - Process all input files"
	@echo "  make all                                  - Process all input files"
	@echo "  make run INPUT_FILE=input_sample_1.txt    - Run with specific input file"
	@echo _____________________________________________________________________________________
	@echo "Input files configured:"
	@for %%i in ($(INPUT_FILE)) do @echo "  %%i"
	@echo.
	@echo "Note: Output files will be generated with '_output_file.txt' suffix"
	@echo _____________________________________________________________________________________