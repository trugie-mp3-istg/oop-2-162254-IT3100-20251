package oop2.demo.api.net.dto;

import java.io.Serializable;


public class ServerPacket implements Serializable {
    private static final long serialVersionUID = 1L;

    private ServerCommand command;
    private Object data;

    public ServerPacket(ServerCommand command, Object data) {
        this.command = command;
        this.data = data;
    }

    public ServerPacket(ServerCommand command) {
        this(command, null);
    }

    public ServerCommand getCommand() {
        return command;
    }

    public Object getData() {
        return data;
    }
}