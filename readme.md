# Jane

Jane is an intelligent voice assistant for Android.

## Usage

Jane works in the background. Whenever you push the button on a headset, she starts listening.

If you have the app in the foreground, you can use one of the phone's volume buttons or the button onscreen.

### Commands

- If you say `tell me about <x>`, Jane will read you a brief summary of the topic. **WARNING:** the Wikipedia scraper that powers this is currently running off my (Will's) computer via [localtunnel](http://progrium.com/localtunnel/). If mine's off, you can run your own from the `go-wikipedia-scraper` folder and change the constant in JaneService. Requires [goquery](https://github.com/PuerkitoBio/goquery). At some point this will probably be running on an EC2 instance, or just ported to Node and run on Heroku.

- If you say `take a note`, Jane will tell you when she's ready, then transcribe your statement and put it in a new email draft addressed to you.
