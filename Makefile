# variables
BUILD = ./build-tile
LANG = tile
LANG_PARSER = $(LANG)Parser
LANG_LEXER = $(LANG)Lexer
PACKAGE = gen.antlr.tile
PACKAGE_PATH = ./src/gen/antlr/tile
RULE = program
ANTLR = ./lib/antlr-4.7-complete.jar


# Detect OS to set classpath separator
ifeq ($(OS), Windows_NT)
    CLASSPATH_SEP = ;
else
    CLASSPATH_SEP = :
endif


antlr: dir gen build

dir:
ifeq ($(OS), Windows_NT)
	if not exist $(BUILD) mkdir $(BUILD)
else
	mkdir -p $(BUILD)
endif

gen:
	java -jar $(ANTLR) -Dlanguage=Java -visitor -package $(PACKAGE) ./$(LANG_PARSER).g4 ./$(LANG_LEXER).g4 -o $(PACKAGE_PATH)

build:
	javac -cp $(ANTLR) $(PACKAGE_PATH)/*.java src/tile/*.java src/tile/app/*.java src/tile/ast/base/*.java src/tile/ast/stmt/*.java src/tile/ast/expr/*.java -d $(BUILD)

run:
	java -cp "$(ANTLR)$(CLASSPATH_SEP).$(CLASSPATH_SEP)$(BUILD)" org.antlr.v4.gui.TestRig $(PACKAGE).$(LANG) $(RULE) -gui ./examples/test.tile

app:
	java -cp "$(ANTLR)$(CLASSPATH_SEP).$(CLASSPATH_SEP)$(BUILD)" tile.app.Tile
