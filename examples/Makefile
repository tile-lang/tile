# examples/Makefile

.SECONDARY: $(TASM_FILES)

# Explicitly list subdirs since wildcard */ is failing
EXAMPLE_DIRS := snake/ string/ forloop/
TILE_FILES := $(wildcard snake/*.tile pong/*.tile forloop/*.tile tic-tac-toe/*.tile)
TASM_FILES := $(TILE_FILES:.tile=.tasm)
BIN_FILES := $(TASM_FILES:.tasm=.bin)

# Debug output
$(info EXAMPLE_DIRS: $(EXAMPLE_DIRS))
$(info TILE_FILES: $(TILE_FILES))
$(info TASM_FILES: $(TASM_FILES))
$(info BIN_FILES: $(BIN_FILES))
$(info WILDCARD DIRS: $(wildcard */))
$(info WILDCARD TILES: $(wildcard snake/*.tile pong/*.tile forloop/*.tile tic-tac-toe/*.tile))

all: $(BIN_FILES)
	@echo "Done building: $(BIN_FILES)"

%.tasm: %.tile
	@echo "Compiling TILE: examples/$< -> examples/$@"
	@$(MAKE) -C .. app ARGS="examples/$< -o examples/$@ -l raylib.dll -gen-tasm"

%.bin: %.tasm
	@echo "Compiling TASM: $< -> $@"


# Clean up only the .bin files and the .tasm files
clean:
	@echo "Cleaning up..."
ifeq ($(OS),Windows_NT)
	del /f /q $(subst /,\,$(BIN_FILES)) $(subst /,\,$(TASM_FILES))
else
	rm -f $(subst /,\,$(BIN_FILES)) $(subst /,\,$(TASM_FILES))
endif

.PHONY: all clean
