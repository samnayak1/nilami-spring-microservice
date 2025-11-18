
import { ISocketManager } from "./SocketManager";
import { Server } from "socket.io";
import http = require("http");




export class SocketManager<T> implements ISocketManager<T> {

    private io: Server | null = null;


    init(server: http.Server): void {

        this.io = new Server(server, {
            cors: { origin: "*" },
           // path:"/socket/bid"
        });

    }

    toRoom(room: string, event: string, data: T): void {
        this.io.to(room).emit(event, JSON.stringify(data));
    }


    joinRoom(socketId: string, room: string): void {

        //sockets has a map of current connections. You have to access sockets.sockets for some odd reason.
        const socket = this.io.sockets.sockets.get(socketId);
        if (!socket) return;

        socket.join(room);
    }


    leaveRoom(socketId: string, room: string): void {
        const socket = this.io.sockets.sockets.get(socketId);
        if (!socket) return;

        socket.leave(room);
    }

    //connection is an inbuilt socket.io event
    onConnection(callback: (socket: any) => void): void {
        this.io.on("connection", (socket) => {
            callback(socket);
        });
    }




}