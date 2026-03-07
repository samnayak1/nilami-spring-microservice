import { Socket } from "socket.io";

export interface ISocketManager<T> {
  init(server: any): void;
  toRoom(room: string, event: string, data: T): void;
  joinRoom(socketId: string, room: string): void;
  leaveRoom(socketId: string, room: string): void;

  onConnection(callback: (socket: Socket) => void): void;
}