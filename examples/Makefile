# examples/Makefile

# Prevent .tasm files from being deleted automatically
.SECONDARY: $(TASM_FILES)

# Get all subdirectories excluding "test/"
EXAMPLE_DIRS := $(filter-out test/,$(wildcard */))

# Find all .tile files in the allowed directories
TILE_FILES := $(foreach dir,$(EXAMPLE_DIRS),$(wildcard $(dir)*.tile))
TASM_FILES := $(TILE_FILES:.tile=.tasm)
BIN_FILES := $(TASM_FILES:.tasm=.bin)

all: $(BIN_FILES)

# Compile .tile to .tasm using top-level make
%.tasm: %.tile
	@echo "Compiling TILE: examples/$< -> examples/$@"
	@make -C .. app ARGS="examples/$< -o examples/$@"

# Compile .tasm to .bin using local path
%.bin: %.tasm
	@echo "Compiling TASM: $< -> $@"
	@tasm $< -o $@ -l raylib.dll

# Clean up only the .bin files, not the .tasm files
clean:
	rm -f $(BIN_FILES)
