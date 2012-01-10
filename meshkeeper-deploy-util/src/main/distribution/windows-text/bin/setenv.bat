@REM
@REM  Copyright (C) 2009 Progress Software, Inc. All rights reserved.
@REM  http://fusesource.com
@REM
@REM  Licensed under the Apache License, Version 2.0 (the "License");
@REM  you may not use this file except in compliance with the License.
@REM  You may obtain a copy of the License at
@REM
@REM         http://www.apache.org/licenses/LICENSE-2.0
@REM
@REM  Unless required by applicable law or agreed to in writing, software
@REM  distributed under the License is distributed on an "AS IS" BASIS,
@REM  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@REM  See the License for the specific language governing permissions and
@REM  limitations under the License.
@REM
@if "%DEBUG%" == "" @echo off

:begin
@REM Pause on errors is on by default
if "%MESHKEEPER_BATCH_PAUSE%" == "" set MESHKEEPER_BATCH_PAUSE=on

set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.\

:check_JAVACMD
CALL %DIRNAME%detect-java.bat
goto check_MESHKEEPER_HOME

:check_MESHKEEPER_HOME
if "%MESHKEEPER_HOME%" == "" set MESHKEEPER_HOME=%DIRNAME%..

:init
@REM Get command-line arguments, handling Windowz variants
if not "%OS%" == "Windows_NT" goto win9xME_args
if "%eval[2+2]" == "4" goto 4NT_args

@REM Regular WinNT shell
set ARGS=%*
goto set_classpath

:win9xME_args
@REM Slurp the command line arguments.  This loop allows for an unlimited number
set ARGS=

:win9xME_args_slurp
if "x%1" == "x" goto set_classpath
set ARGS=%ARGS% %1
shift
goto win9xME_args_slurp

:4NT_args
@REM Get arguments from the 4NT Shell from JP Software
set ARGS=%$

:set_classpath
rem add conf directory to pick up log4j.properties:
set MESHKEEPER_CLASSPATH=%MESHKEEPER_HOME%\lib\meshkeeper-api.jar;%MESHKEEPER_HOME%\conf

:end

