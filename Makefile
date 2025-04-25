# variables
BUILD = build-tile
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
	MKDIR_P = cmd /C "if not exist $(BUILD) mkdir $(BUILD)"
else
    CLASSPATH_SEP = :
	MKDIR_P = mkdir -p $(BUILD)
endif


antlr: gen build

gen:
	java -jar $(ANTLR) -Dlanguage=Java -visitor -package $(PACKAGE) ./$(LANG_PARSER).g4 ./$(LANG_LEXER).g4 -o $(PACKAGE_PATH)

# $(MKDIR_P)
build:
	$(MKDIR_P)
	javac -cp $(ANTLR) $(PACKAGE_PATH)/*.java src/tile/*.java src/tile/app/*.java src/tile/ast/base/*.java src/tile/ast/stmt/*.java src/tile/ast/expr/*.java src/tile/ast/types/*.java src/tile/sym/*.java src/tile/err/*.java -d $(BUILD)

run:
	java -cp "$(ANTLR)$(CLASSPATH_SEP).$(CLASSPATH_SEP)$(BUILD)" org.antlr.v4.gui.TestRig $(PACKAGE).$(LANG) $(RULE) -gui ./examples/test.tile

app:
	@java -cp "$(ANTLR)$(CLASSPATH_SEP).$(CLASSPATH_SEP)$(BUILD)" tile.app.Tile $(ARGS)
