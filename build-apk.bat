@echo off
echo Building APK for Fincal App...
echo.

cd /d "%~dp0"

if exist "gradlew.bat" (
    echo Using Gradle Wrapper...
    call gradlew.bat assembleDebug
    if %ERRORLEVEL% EQU 0 (
        echo.
        echo ========================================
        echo APK Build Successful!
        echo ========================================
        echo.
        echo APK Location: app\build\outputs\apk\debug\app-debug.apk
        echo.
        if exist "app\build\outputs\apk\debug\app-debug.apk" (
            echo Opening APK location...
            explorer "app\build\outputs\apk\debug"
        )
    ) else (
        echo.
        echo Build failed! Please check the error messages above.
        echo Make sure you have Android Studio installed or Gradle in your PATH.
    )
) else (
    echo Gradle wrapper not found!
    echo.
    echo Please use one of these methods:
    echo 1. Open the project in Android Studio and build from there
    echo 2. Install Gradle and add it to your PATH
    echo.
    echo See BUILD_INSTRUCTIONS.md for more details.
)

pause

