
import { ISocketManager } from "./SocketManager";
import { Server } from "socket.io";
import http = require("http");
import Redis from "ioredis/built/Redis";
import { createAdapter } from "@socket.io/redis-adapter";




export class SocketManager<T> implements ISocketManager<T> {

    private io: Server | null = null;


    init(server: http.Server): void {

        this.io = new Server(server, {

            cors: { origin: "*" },
            path: "/ws"
        });

        const pubClient = new Redis({
            host: process.env.REDIS_HOST,
            port: 6379,
            password: process.env.REDIS_PASSWORD
        });
        const subClient = pubClient.duplicate(); //duplicate() creates a new instance with the same options as the previous one.
        this.io.adapter(createAdapter(pubClient, subClient));

    }



     toRoom(room: string, event: string, data: T): void {
        this.io && this.io.to(room).emit(event, JSON.stringify(data));
    }


     joinRoom(socketId: string, room: string): void {

        if (!this.io) return;
        this.io.in(socketId).socketsJoin(room);
    }



    leaveRoom(socketId: string, room: string): void {
        if (!this.io) return;
        this.io.in(socketId).socketsLeave(room);
    }

    //connection is an inbuilt socket.io event
    onConnection(callback: (socket: any) => void): void {
        this.io && this.io.on("connection", (socket) => {
            callback(socket);
        });
    }




}