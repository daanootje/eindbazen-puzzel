######################################################
##########            Kiosk Mode          ############
######################################################
#
# Runs chrome and other apps in full-screen mode 
# on predefined screens
# See https://alextomin.wordpress.com/2015/04/10/kiosk-mode-in-windows-chrome-on-multiple-displays/
# ----------------------------------------------------
Stop-Process -Name chrome

$chromePath = 'C:\Program Files (x86)\Google\Chrome\Application\chrome.exe'
$chromeArguments = '--new-window'
# if Window not moved (especially on machine start) - try increaing the delay. 
$ChromeStartDelay = 3

Set-Location $PSScriptRoot
. .\HelperFunctions.ps1

# Kill all running instances
# &taskkill /im chrome* /F

Chrome-Kiosk 'http://google.com' -MonitorNum 1 
Chrome-Kiosk 'http://www.nu.nl' -MonitorNum 2