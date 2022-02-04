const port = 8080;

const { Server } = require("socket.io");
const io = new Server(port);

io.on('connection', (socket) => {
    console.log ('socket connected from ' + socket.handshake.address);

    socket.on('debug', (message) => {
        console.log(message);
    })

    socket.on('disconnect', () => {
        console.log('socket disconnected');
    })
})

io.engine.on("connection_error", (err) => {  
    console.log(err.req);      // the request object  
    console.log(err.code);     // the error code, for example 1  
    console.log(err.message);  // the error message,  example "Session ID unknown"  
    console.log(err.context);  // some additional error context
});

console.log (`listening on port ${port}`);
