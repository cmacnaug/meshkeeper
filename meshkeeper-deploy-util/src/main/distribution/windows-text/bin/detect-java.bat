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
rem check for correct java version
rem


rem this is what is on slider now
set MIN_VERSION="1.6"

:start_java_version

if not "%JAVACMD%" == "" goto check_java_version

:check_java_home
if not "%JAVA_HOME%" == "" goto have_java_home
set JAVACMD="%JAVA_HOME%\bin\java.exe"
goto check_MESHKEEPER_HOME

:have_java_home
set JAVACMD="%JAVA_HOME%\bin\java"
goto check_java_version

:no_java_home
set JAVACMD=java
goto check_java_version

:check_java_version
%JAVACMD% -version 2> jverchk.txt
for %%V in (1.5 1.4 1.3) DO ^
findstr.exe "%%V" jverchk.txt && goto wrong_java_version

goto end_java_version

:wrong_java_version
echo %JAVACMD% is wrong version %MIN_VERSION% or higher required. Please set fix your JAVA_HOME or path and try again.
set JAVACMD=
del jverchk.txt
if "%MESHKEEPER_BATCH_PAUSE%" == "on" pause
exit 1
goto end_java_version

:end_java_version
del jverchk.txt

