const express = require('express')
const app = express()
const bodyParser = require('body-parser')
app.use(bodyParser.urlencoded({ extended: true }))

const ip = require('ip')
const port = 8080

app.post('/debug', (req, res) => {
    res.send("msg logged")
    console.log("body: ")
    console.log(req.body)
})

app.listen(port, () => {
    console.log(`Example app listening on ${ip.address()}:${port}`)
})