const express = require('express')
const app = express()
const port = 8080

app.get('/debug', (req, res) => {
    res.send("msg logged")
    console.log("query: ")
    console.log(req.query)
})

app.listen(port, () => {
    console.log(`Example app listening on port ${port}`)
})