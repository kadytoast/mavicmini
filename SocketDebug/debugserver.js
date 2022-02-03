const http = require("http");
const server = http.createServer();

const { IOServer } = require("socket.io");
const io = new IOServer(server);