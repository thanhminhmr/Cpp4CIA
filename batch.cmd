@ECHO OFF
SET projectName=log4cplus
SET sourcePath=src
SET includePath=include
REM SET outputFile=true

ECHO. > config.txt
ECHO [Project] >> config.txt
ECHO projectName=%projectName% >> config.txt
IF DEFINED outputFile ECHO outputFile=Project-%projectName%.Project >> config.txt
ECHO exportDatabase=Project-%projectName%.sqLite >> config.txt

CD _git >> NUL 2>&1
git checkout --quiet master
SET next_rev=
FOR /F %%i IN ('git rev-list -n 50 master') DO CALL :REVISION %%i,next_rev

CD .. >> NUL 2>&1
EXIT /B 0

:REVISION
	ECHO Building %projectName% revision %1
	git checkout --quiet %1 >> NUL 2>&1
	MD ..\%1 >> NUL 2>&1
	
	CALL :XCOPY_DIR ..\%1,%sourcePath%
	CALL :XCOPY_DIR ..\%1,%includePath%
	
	ECHO. >> ..\config.txt
	ECHO [ProjectVersion-%1] >> ..\config.txt
	IF DEFINED outputFile ECHO outputFile=ProjectVersion-%1.ProjectVersion >> ..\config.txt
	ECHO versionName=%projectName%-%1 >> ..\config.txt
	ECHO projectRoot=%1 >> ..\config.txt
	
	FOR /F %%j IN ('DIR ..\%1\%sourcePath% /s /b /a-d') DO CALL :ECHO_FIX_PATH %%j,%1,projectFile,..\config.txt
	FOR /F %%j IN ('DIR ..\%1\%includePath% /s /b /ad') DO CALL :ECHO_FIX_PATH %%j,%1,includePath,..\config.txt
	
	IF DEFINED %2 CALL :ECHO_LAST_REV %1,%2
	
	SET "%2=%1"
EXIT /B 0

:XCOPY_DIR
	MD %1\%2 >> NUL 2>&1
	xcopy %2\*.c %1\%2 /s /y /q >> NUL 2>&1
	xcopy %2\*.cc %1\%2 /s /y /q >> NUL 2>&1
	xcopy %2\*.cpp %1\%2 /s /y /q >> NUL 2>&1
	xcopy %2\*.c++ %1\%2 /s /y /q >> NUL 2>&1
	xcopy %2\*.cxx %1\%2 /s /y /q >> NUL 2>&1
	xcopy %2\*.h %1\%2 /s /y /q >> NUL 2>&1
	xcopy %2\*.hh %1\%2 /s /y /q >> NUL 2>&1
	xcopy %2\*.hpp %1\%2 /s /y /q >> NUL 2>&1
	xcopy %2\*.h++ %1\%2 /s /y /q >> NUL 2>&1
	xcopy %2\*.hxx %1\%2 /s /y /q >> NUL 2>&1
EXIT /B 0

:ECHO_LAST_REV
	CALL SET "rev=%%%2%%%"
	ECHO. >> ..\config.txt
	ECHO [VersionDifference-%1-%rev%] >> ..\config.txt
	IF DEFINED outputFile ECHO outputFile=VersionDifference-%1-%rev%.VersionDifference >> ..\config.txt
	ECHO versionA=%projectName%-%1 >> ..\config.txt
	ECHO versionB=%projectName%-%rev% >> ..\config.txt
EXIT /B 0

:ECHO_FIX_PATH
	SET fix_path=%1
	CALL SET "fix_path=%%fix_path:*%2=%2%%%"
	ECHO %3=%fix_path:\=\\% >> %4
EXIT /B 0