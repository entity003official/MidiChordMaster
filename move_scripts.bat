@echo off
echo Moving development bat files to scripts folder...

move network_fix_upload.bat scripts\
move monitor_build_en.bat scripts\
move monitor_build.bat scripts\
move direct_upload.bat scripts\
move check_java_env.bat scripts\
move check_apk.bat scripts\
move build_with_jdk.bat scripts\
move build_theme_fixed.bat scripts\
move build_source_fixed.bat scripts\
move build_no_hang.bat scripts\
move build_midi_api_fixed.bat scripts\
move build_fixed.bat scripts\
move build_final_fixed.bat scripts\
move build_final.bat scripts\
move build_dependencies_fixed.bat scripts\

echo Completed moving files to scripts folder!
echo.
echo Files kept in root (for GitHub):
echo - gradlew.bat (Gradle wrapper - required)
echo - build_apk.bat (Main APK build script)
echo - quick_solution.bat (User-friendly solution selector)
echo.
pause
