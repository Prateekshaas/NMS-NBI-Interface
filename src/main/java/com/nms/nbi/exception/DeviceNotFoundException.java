package com.nms.nbi.exception;

public class DeviceNotFoundException extends RuntimeException {
    public DeviceNotFoundException(String id) {
        super("Device not found: " + id);
    }
}
