@echo off
echo Building APK for Fynical App...
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
        echo APK Location: app\build\outputs\apk\debug\
        echo.
        if exist "app\build\outputs\apk\debug" (
            echo Opening APK location...
            explorer "app\build\outputs\apk\debug"
        )
    ) else (
        echo.
        echo Build failed! Please check the error messages above.
    )
) else (
    echo Gradle wrapper not found!
    echo Please open the project in Android Studio to build.
)

pause

