@echo off
echo ========================================
echo Building Fincal APK
echo ========================================
echo.

cd /d "%~dp0"

echo Checking for Android Studio Gradle...
set GRADLE_PATH=%LOCALAPPDATA%\Android\Sdk\tools
if exist "%LOCALAPPDATA%\Android\Sdk" (
    echo Android SDK found!
    echo.
)

echo.
echo ========================================
echo RECOMMENDED: Use Android Studio
echo ========================================
echo.
echo 1. Open Android Studio
echo 2. File -^> Open -^> Select this folder
echo 3. Wait for Gradle sync
echo 4. Build -^> Build Bundle(s) / APK(s) -^> Build APK(s)
echo.
echo The APK will be at: app\build\outputs\apk\debug\app-debug.apk
echo.
echo ========================================
echo.

pause

