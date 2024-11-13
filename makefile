JAVAC = javac
JAVA = java
MAIN_CLASS = GatorTicketMaster
INPUT_FILE = input_1_test_case.txt
OUTPUT_FILE = $(subst .txt,_output.txt,$(INPUT_FILE))

all: compile run

compile:
	$(JAVAC) *.java

run:
	$(JAVA) $(MAIN_CLASS) $(INPUT_FILE)

clean:
	rm -f *.class $(OUTPUT_FILE)

.PHONY: all compile run clean