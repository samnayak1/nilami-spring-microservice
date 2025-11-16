import axios from "axios";

export const registerWithEureka = (port: number) => {
  const eurekaHost = process.env.EUREKA_HOST ;
  const appName = process.env.APP_NAME ;
  const host = process.env.HOST_NAME;
  const instanceId = `${host}:${port}`;

  const register = async () => {
    try {
      await axios.post(
        `${eurekaHost}/eureka/apps/${appName}`,
        {
          instance: {
            instanceId,
            hostName: host,
            app: appName, //the name that it registers with the registry service with. We called it WEBSOCKET-SERVICE 
            ipAddr: host,
            vipAddress: appName, // The logical name clients use to find your service.  
            status: "UP",
            port: { "$": port, "@enabled": true },
            healthCheckUrl: `http://${host}:${port}/health`,
            statusPageUrl: `http://${host}:${port}`,
            dataCenterInfo: {
              "@class": "com.netflix.appinfo.InstanceInfo$DefaultDataCenterInfo",
              name: "MyOwn"
            }
          }
        },
        { headers: { "Content-Type": "application/json" } }
      );

      console.log("Registered with Eureka");
    } catch (err: any) {
      console.error("Failed to register:", err.message,JSON.stringify(err));
    }
  };

  const heartbeat = async () => {
    try {
      await axios.put(`${eurekaHost}/eureka/apps/${appName}/${instanceId}`);
      console.log("Heartbeat sent");
    } catch (err: any) {
      console.error("Heartbeat failed:", err.message);
    }
  };

  register();
  setInterval(heartbeat, 30000);

  const shutdown = async () => {
    try {
      await axios.delete(`${eurekaHost}/eureka/apps/${appName}/${instanceId}`);
      console.log("Deregistered from Eureka");
    } catch (err) {
      console.error("Failed to deregister");
    }
    process.exit();
  };

  process.on("SIGINT", shutdown);
  process.on("SIGTERM", shutdown);
};
