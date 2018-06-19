package org.pandora.control.display;

import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.util.concurrent.TimeUnit;

@Slf4j
public class DesktopApi {

    public static Integer browse(URI uri) throws Exception{
        Runtime r = Runtime.getRuntime();
        Process p = r.exec(String.format("explorer %s", uri.toString()));
        p.waitFor(10, TimeUnit.SECONDS);
        p.destroy();
        return p.exitValue();
    }

}
