const express = require('express')
const app = express()
const ip = require('ip')
const port = 8080

app.get('/debug', (req, res) => {
    res.send("msg logged")
    console.log("body: ")
    console.log(req.body)
})

app.listen(port, () => {
    console.log(`Example app listening on ${ip.address}:${port}`)
})