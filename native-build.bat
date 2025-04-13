:: Set classpath and native build dir
set BUILD_NATIVE_DIR=build-native
set CLASSPATH="..\lib\antlr-4.7-complete.jar;.;..\build-tile"
echo %CLASSPATH%

:: You should change VCVARS and GRAALVM_NATIVE_IMAGE to your paths
set VCVARS="C:\Program Files (x86)\Microsoft Visual Studio\2019\Community\VC\Auxiliary\Build\vcvarsall.bat"
set GRAALVM_NATIVE_IMAGE="C:\graalvm-jdk-24+36.1\bin\native-image.cmd"


cmd /C "if not exist %BUILD_NATIVE_DIR% mkdir %BUILD_NATIVE_DIR%"
cd %BUILD_NATIVE_DIR%

:: call vcvarsall.bat
call %VCVARS% x64

:: Ensure that MSVC (cl.exe) is used for compilation and disable checking the toolchain
%GRAALVM_NATIVE_IMAGE% -H:+UnlockExperimentalVMOptions -H:-CheckToolchain -cp %CLASSPATH% tile.app.Tile -o tile
