@echo off
cd /d "%~dp0www"
echo Stretch running at http://localhost:8080  (close this window to stop)
start "" http://localhost:8080
python -m http.server 8080
