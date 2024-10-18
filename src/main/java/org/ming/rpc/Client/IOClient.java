package org.ming.rpc.Client;

import org.ming.rpc.common.Message.RpcRequest;
import org.ming.rpc.common.Message.RpcResponse;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class IOClient {
    public static RpcResponse send(String host, int port, RpcRequest request) {
        try {
            Socket socket = new Socket(host, port);
            System.out.println("客户端建立连接");
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            oos.writeObject(request);
            oos.flush();
            RpcResponse response = (RpcResponse) ois.readObject();
            System.out.println("发送信息成功");
            return response;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
