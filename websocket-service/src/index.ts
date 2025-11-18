import express from "express";
import { registerWithEureka } from "./eureka";
import dotenv from "dotenv";
import { startBidConsumer } from "./kafka/kafkaBidConsumerSetup";
import { SocketManager } from "./websockets/WebsocketGatewayManager";
import { EventEnums,  SocketRooms } from "./websockets/enums/SocketEnums";
import { JoinItemPayload } from "./websockets/validators/joinRoomPayload";
dotenv.config();

const app = express();
const PORT = 3001;

app.get("/health", (req, res) => res.send("OK"));

startBidConsumer((event) => {
    try {
        console.debug("Consumer ran!");
        console.debug(JSON.stringify(event));

        socketManager.toRoom(`${SocketRooms.ItemBids}-${event.itemId}`, EventEnums.BidPlaced, event);

    } catch (error) {
  
        console.error("Error processing event in Bid Consumer:", error);
        console.error("Failing Event Data:", JSON.stringify(event));
    
    }
});
const httpServer=app.listen(PORT, () => {
  console.log(`Server running on port ${PORT}`);
  registerWithEureka(PORT); 
});

const socketManager = new SocketManager();
socketManager.init(httpServer);
socketManager.onConnection((socket) => {

    socket.on(EventEnums.JoinRoom, (req: JoinItemPayload) => {
        try {
           const data = typeof req === 'string' ? JSON.parse(req) : req;
            if (!data.itemId) {
                throw new Error("Missing itemId for JoinRoom");
            }
            socket.join(`${SocketRooms.ItemBids}-${data.itemId}`);
            console.log(`Socket ${socket.id} joined room ${data.itemId}`);

        } catch (error) {
            console.error(`Error handling JoinRoom for socket ${socket.id}:`, error);
        
            socket.emit(EventEnums.Error, { message: "Failed to join room." });
        }
    });


    socket.on(EventEnums.LeaveRoom, (req: JoinItemPayload) => {
        try {
          const data = typeof req === 'string' ? JSON.parse(req) : req;
            if (!data.itemId) {
                throw new Error("Missing itemId for LeaveRoom");
            }
            socket.leave(`${SocketRooms.ItemBids}-${data.itemId}`);
            console.log(`Socket ${socket.id} left room ${data.itemId}`);

        } catch (error) {
            console.error(`Error handling LeaveRoom for socket ${socket.id}:`, error);
          
            socket.emit(EventEnums.Error, { message: "Failed to leave room." });
        }
    });
});