@ECHO OFF
SETLOCAL ENABLEEXTENSIONS
SET projectName=log4cplus
ECHO. > config.txt
ECHO [Project] >> config.txt
ECHO projectName=%projectName% >> config.txt
ECHO outputFile=%projectName%.Project >> ..\config.txt
ECHO exportDatabase=. >> config.txt

CD _git >> NUL
git checkout --quiet master
SET next_rev=
FOR /F %%i IN ('git rev-list -n 50 master') DO CALL :REVISION %%i,next_rev

CD .. >> NUL
EXIT /B 0

:REVISION
	ECHO Building %projectName% revision %1
	git checkout --quiet %1 >> NUL
	MD ..\%1 >> NUL
	
	xcopy *.c ..\%1 /s /y /q >> NUL
	xcopy *.cc ..\%1 /s /y /q >> NUL
	xcopy *.cpp ..\%1 /s /y /q >> NUL
	xcopy *.c++ ..\%1 /s /y /q >> NUL
	xcopy *.cxx ..\%1 /s /y /q >> NUL
	xcopy *.h ..\%1 /s /y /q >> NUL
	xcopy *.hh ..\%1 /s /y /q >> NUL
	xcopy *.hpp ..\%1 /s /y /q >> NUL
	xcopy *.h++ ..\%1 /s /y /q >> NUL
	xcopy *.hxx ..\%1 /s /y /q >> NUL
	
	ECHO. >> ..\config.txt
	ECHO [ProjectVersion-%1] >> ..\config.txt
	ECHO outputFile=%1.ProjectVersion >> ..\config.txt
	ECHO versionName=%projectName%-%1 >> ..\config.txt
	ECHO projectRoot=%1 >> ..\config.txt
	
	FOR /F %%j IN ('DIR ..\%1\src /s /b') DO CALL :ECHO_FIX_PATH %%j,%1,projectFile,..\config.txt
	FOR /F %%j IN ('DIR ..\%1\include\log4cplus /s /b /ad') DO CALL :ECHO_FIX_PATH %%j,%1,includePath,..\config.txt
	
	IF DEFINED %2 CALL :ECHO_LAST_REV %1,%2
	
	SET "%2=%1"
EXIT /B 0

:ECHO_LAST_REV
	CALL SET "rev=%%%2%%%"
	ECHO. >> ..\config.txt
	ECHO [VersionDifference-%1-%rev%] >> ..\config.txt
	ECHO outputFile=%1-%rev%.VersionDifference >> ..\config.txt
	ECHO versionA=%projectName%-%1 >> ..\config.txt
	ECHO versionB=%projectName%-%rev% >> ..\config.txt
EXIT /B 0

:ECHO_FIX_PATH
	SET fix_path=%1
	CALL SET "fix_path=%%fix_path:*%2=%2%%%"
	ECHO %3=%fix_path:\=\\% >> %4
EXIT /B 0