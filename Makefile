# variables
BUILD = ./build-tile
LANG = tile
LANG_PARSER = $(LANG)Parser
LANG_LEXER = $(LANG)Lexer
PACKAGE = gen.antlr.tile
PACKAGE_PATH = ./src/gen/antlr/tile
RULE = program
ANTLR = ./lib/antlr-4.7-complete.jar


antlr: gen build

gen:
	java -jar $(ANTLR) -Dlanguage=Java -visitor -package $(PACKAGE) ./$(LANG_PARSER).g4 ./$(LANG_LEXER).g4 -o $(PACKAGE_PATH)

build:
	javac -cp $(ANTLR) $(PACKAGE_PATH)/*.java src/tile/*.java src/tile/app/*.java src/tile/ast/base/*.java src/tile/ast/stmt/*.java -d $(BUILD)
#src/tile/ast/expr/*.java

run:
	java -cp "$(ANTLR);.;$(BUILD)" org.antlr.v4.gui.TestRig $(LANG) $(RULE) -gui ./examples/sum.tile

app:
	java -cp "$(ANTLR);.;$(BUILD)" tile.app.Tile
