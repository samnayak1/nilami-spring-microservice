import express from "express";
import { registerWithEureka } from "./eureka";
import dotenv from "dotenv";
import { startConsumer } from "./kafka/kafkaConsumerSetup";
dotenv.config();

const app = express();
const PORT = 3001;

app.get("/health", (req, res) => res.send("OK"));

startConsumer((event) => {
  console.log("Consumer ran!");
  console.log(JSON.stringify(event));
});


app.listen(PORT, () => {
  console.log(`Server running on port ${PORT}`);
  registerWithEureka(PORT); // Call Eureka registration here
});
