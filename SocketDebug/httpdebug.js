// instantiate server constants
const express = require('express')
const app = express()
const bodyParser = require('body-parser')
app.use(bodyParser.urlencoded({ extended: true }))

// instantiate address constants
const ip = require('ip')
const port = 8080

// instantiate date variables
let date_ob = new Date();
let date = ("0" + date_ob.getDate()).slice(-2);
let month = ("0" + (date_ob.getMonth() + 1)).slice(-2);
let year = date_ob.getFullYear();

// instantiate filesystem variables
const fs = require('fs')
var logstream = fs.createWriteStream("mavic_logs/" + month + "-" + date + "-" + year, 
    {flags: "a", autoClose: true})

app.post('/debug', (req, res) => {
    res.send("msg logged")
    console.log(req.body["time"] + " : " + req.body["msg"])
    logstream.write(req.body["time"] + " : " + req.body["msg"])
})

app.listen(port, () => {
    console.log(`Example app listening on ${ip.address()}:${port}`)
})