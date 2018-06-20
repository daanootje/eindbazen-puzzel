package org.pandora.control.display;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Slf4j
public class DesktopApi {

    private String fileName;

    public DesktopApi(String scriptsFolder) {
        this.fileName = String.format("%s/Ciklum-Kiosk.ps1", scriptsFolder);
    }

    public Integer openBrowser(String uri) {
        Runtime r = Runtime.getRuntime();
        String command = String.format("C:/Windows/System32/WindowsPowerShell/v1.0/powershell.exe -file %s %s", fileName, uri);
        Process p;
        try {
            p = r.exec(command);
            p.waitFor(10, TimeUnit.SECONDS);
            p.destroy();
            return p.exitValue();
        } catch (IOException e) {
            log.error(String.format("Failed to execute command: %s - %s", command, e.getMessage()));
        } catch (InterruptedException e) {
            log.error(String.format("Failed to end running process: command: %s - %s", command, e.getMessage()));
        }
        return -1;
    }

}
