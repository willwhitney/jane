express = require 'express'

app = express()
app.configure ->
  # app.set 'views', __dirname + '/views'
  # app.set 'view engine', 'jade'
  # app.use("/styles", lessMiddleware({ src: __dirname + '/styles'}))
  app.use("/", express.static(__dirname + '/static'))
  app.use("/static", express.static(__dirname + '/static'))

# is_mobile = (req) ->
#   ua = req.header('user-agent')
#   if (/mobile/i.test(ua))
#     return true
#   else return false

app.get '/foursquare_reg', (req, res) ->
  console.log req.query
  # req.writeHead 200
  # res.send req.query.foursquare_token
  res.writeHead(200, {'Content-Type': 'text/plain'});
  res.end('Thanks!\n');
  

port = process.env.PORT || 8000
app.listen port