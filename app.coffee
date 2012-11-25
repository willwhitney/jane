xmpp = require 'simple-xmpp'
contacts = require './contacts'

console.log contacts.list
xmpp.on "online", ->
  console.log "Jane: I'm connected!"
  xmpp.send "wfwhitney@gmail.com", "tester is online"

xmpp.on "chat", (from, message) ->
  console.log "#{from}: #{message}"
  if (contacts.list.indexOf from) > -1
    xmpp.send from, "echo: " + message

xmpp.on "error", (err) ->
  console.error err

xmpp.connect
  jid: "wfwhitney.test@gmail.com"
  password: "willtest"
  host: "talk.google.com"
  port: 5222

loggify = (to, message) ->
  xmpp.send from, "echo: " + message
  
# debugEntryAndExit = (what) ->
#   (callback) ->
#     console.log 'debug', "entering #{what}"
#     value = callback.apply(this, arguments)
#     console.log 'debug', "leaving #{what}"
#     value

# xmpp.send = debugEntryAndExit(xmpp.send)