package org.pandora.control.serialcomm;


import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TooManyListenersException;

@Slf4j
abstract class SerialCommunicator implements SerialPortEventListener {

    private CommPortIdentifier selectedPortIdentifier;
    private SerialPort serialPort;
    private OutputStream output;
    private Map<String,CommPortIdentifier> portMap = new HashMap<>();
    InputStream input;

    private Boolean connected = false;
    private final static Integer TIMEOUT = 2000;
    private final static Integer SPACE_ASCII = 32;
    private final static Integer DASH_ASCII = 45;
    private final static Integer NEW_LINE_ASCII = 10;

    SerialCommunicator() {
        this.searchForPorts();
    }

    public void initialize(String port) {
        try {
            this.connect(port);
            this.initIOStream();
            this.initListener();
        } catch (PortInUseException e) {
            log.error(String.format("Failed to connect, port in use - %s", e.getMessage()));
        } catch (IOException e) {
            log.error(String.format("Failed to initialize I/O stream - %s", e.getMessage()));
        } catch (TooManyListenersException e) {
            log.error("Failed to initialize serial event listener, too many listeners present - %s", e.getMessage());
        }
    }

    public Set<String> getAvailablePorts() {
        return portMap.keySet();
    }

    public Boolean isConnected() {
        return connected;
    }

    void writeData(int leftThrottle, int rightThrottle) {
        if (isConnected()) {
            try {
                output.write(leftThrottle);
                output.flush();

                output.write(DASH_ASCII);
                output.flush();

                output.write(rightThrottle);
                output.flush();

                output.write(SPACE_ASCII);
                output.flush();
            } catch (IOException e) {
                log.error(String.format("Failed to write data - %s", e.getMessage()));
            }
        }
    }

    abstract public void serialEvent(SerialPortEvent evt);

    public void disconnect() {
        try {
            writeData(0, 0);
            serialPort.removeEventListener();
            serialPort.close();
            input.close();
            output.close();
            setConnected(false);
        } catch (IOException e) {
            log.error(String.format("Failed to close %s - %s", serialPort.getName(), e.getMessage()));
        }
    }

    private void connect(String port) throws PortInUseException {
        selectedPortIdentifier = portMap.get(port);
        CommPort commPort;
        commPort = selectedPortIdentifier.open("TigerControlPanel", TIMEOUT);
        serialPort = (SerialPort)commPort;

        setConnected(true);

        //CODE ON SETTING BAUD RATE ETC OMITTED
        //XBEE PAIR ASSUMED TO HAVE SAME SETTINGS ALREADY
    }

    private void initIOStream() throws IOException {
        input = serialPort.getInputStream();
        output = serialPort.getOutputStream();
        writeData(0, 0);
    }

    private void initListener() throws TooManyListenersException {
        serialPort.addEventListener(this);
        serialPort.notifyOnDataAvailable(true);
    }

    private void searchForPorts() {
        Enumeration ports = CommPortIdentifier.getPortIdentifiers();
        while (ports.hasMoreElements()) {
            CommPortIdentifier curPort = (CommPortIdentifier) ports.nextElement();
            if (curPort.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                portMap.put(curPort.getName(), curPort);
            }
        }
    }

    private void setConnected(Boolean state) {
        connected = state;
    }

}
