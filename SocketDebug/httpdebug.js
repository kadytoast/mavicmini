const express = require('express')
const app = express()
const port = 8080

app.get('/debug', (req, res) => {
    res.send('Hello World!')
    console.log("headers: ")
    console.log(req.headers)
    console.log("ip: ")
    console.log(req.ip)
    console.log("body: ")
    console.log(req.body)
    console.log("params: ")
    console.log(req.params)
})

app.listen(port, () => {
    console.log(`Example app listening on port ${port}`)
})