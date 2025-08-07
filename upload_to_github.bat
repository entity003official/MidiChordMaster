@echo off
chcp 65001 >nul
cls
echo ==========================================
echo   MidiChordMaster GitHub Upload Script
echo ==========================================
echo.

echo 🚀 Preparing to upload MidiChordMaster to GitHub...
echo.

echo [Step 1] Checking Git installation...
git --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Git not found! Please install Git first:
    echo    Download: https://git-scm.com/download/windows
    echo.
    pause
    exit /b 1
)
echo ✅ Git is installed

echo.
echo [Step 2] GitHub Repository Setup Required
echo.
echo Please complete these steps on GitHub.com:
echo 1. Go to https://github.com and login
echo 2. Click '+' → 'New repository'
echo 3. Repository name: MidiChordMaster
echo 4. Description: 🎹 Professional Android MIDI Chord Recognition App
echo 5. Choose Public or Private
echo 6. DO NOT add README, .gitignore, or license (we have them)
echo 7. Click 'Create repository'
echo 8. Copy the repository URL (like: https://github.com/username/MidiChordMaster.git)
echo.

set /p repo_url="Paste your GitHub repository URL here: "
if "%repo_url%"=="" (
    echo ❌ Repository URL is required
    pause
    exit /b 1
)

echo.
echo [Step 3] Git Configuration
set /p git_name="Enter your GitHub username: "
set /p git_email="Enter your GitHub email: "

if "%git_name%"=="" (
    echo ❌ Username is required
    pause
    exit /b 1
)

if "%git_email%"=="" (
    echo ❌ Email is required
    pause
    exit /b 1
)

echo.
echo [Step 4] Initializing Git repository...
if exist ".git" (
    echo Repository already initialized
) else (
    git init
    if %errorlevel% neq 0 (
        echo ❌ Failed to initialize Git repository
        pause
        exit /b 1
    )
    echo ✅ Git repository initialized
)

echo.
echo [Step 5] Configuring Git user...
git config user.name "%git_name%"
git config user.email "%git_email%"
echo ✅ Git user configured

echo.
echo [Step 6] Adding remote repository...
git remote remove origin >nul 2>&1
git remote add origin %repo_url%
if %errorlevel% neq 0 (
    echo ❌ Failed to add remote repository
    pause
    exit /b 1
)
echo ✅ Remote repository added

echo.
echo [Step 7] Preparing files for upload...
echo Creating professional README and documentation files...
echo ✅ README.md - Comprehensive project documentation
echo ✅ .gitignore - Git ignore rules for Android projects
echo ✅ CONTRIBUTING.md - Contribution guidelines
echo ✅ CODE_OF_CONDUCT.md - Community guidelines
echo ✅ GITHUB_UPLOAD_GUIDE.md - Detailed upload instructions

echo.
echo [Step 8] Adding all files to Git...
git add .
if %errorlevel% neq 0 (
    echo ❌ Failed to add files
    pause
    exit /b 1
)
echo ✅ All files added to Git

echo.
echo [Step 9] Creating initial commit...
git commit -m "Initial commit: Professional MIDI Chord Recognition App with real-time processing, intelligent chord recognition, visual piano keyboard, audio synthesis, and modern Jetpack Compose UI. Ready-to-install APK included."
if %errorlevel% neq 0 (
    echo ❌ Failed to create commit
    pause
    exit /b 1
)
echo ✅ Initial commit created

echo.
echo [Step 10] Pushing to GitHub...
echo This may take a few minutes depending on your internet connection...
git branch -M main
git push -u origin main
if %errorlevel% neq 0 (
    echo ❌ Failed to push to GitHub
    echo.
    echo Possible solutions:
    echo 1. Check your internet connection
    echo 2. Verify the repository URL is correct
    echo 3. Make sure you have push permissions
    echo 4. You may need to authenticate with GitHub
    echo.
    pause
    exit /b 1
)

echo.
echo ==========================================
echo   🎉 SUCCESS! Upload Complete! 🎉
echo ==========================================
echo.
echo ✅ Your MidiChordMaster app is now on GitHub!
echo.
echo 📱 Repository URL: %repo_url%
echo.
echo 🎯 What's uploaded:
echo   📁 Complete Android project source code
echo   🎵 Professional MIDI chord recognition app
echo   📱 Ready-to-install APK (6.78MB)
echo   📚 Comprehensive documentation
echo   🛠️ Build scripts and configuration
echo   🎨 Modern Jetpack Compose UI
echo.
echo 🚀 Next steps:
echo   1. Visit your repository: %repo_url%
echo   2. Add repository topics: android, kotlin, midi, music
echo   3. Create a release with the APK file
echo   4. Share with the community!
echo.
echo 🎵 Your professional MIDI app is now live! 🎵

pause
