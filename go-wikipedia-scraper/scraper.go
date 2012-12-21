package main

import (
	"fmt"
	"net/http"
	"encoding/json"
	"io/ioutil"
	"github.com/PuerkitoBio/goquery"
	"regexp"
	"net/url"
)

// if the given error exists, print it and panic
func errorify(err error) {
	if err != nil {
		fmt.Printf("%s", err)
		panic(err)
	}
}

func findArticle(title string) (articles []string) {
	title = url.QueryEscape(title)
	resp, err := http.Get("http://en.wikipedia.org/w/api.php?action=opensearch&search=" + title)
	errorify(err)
	defer resp.Body.Close()
	body, err := ioutil.ReadAll(resp.Body)
	errorify(err)
	
	var dataInterface interface{}
	json.Unmarshal(body, &dataInterface)
	
	data := dataInterface.([]interface{})[1].([]interface{})
	for _, item := range data {
		articles = append(articles, item.(string))
	}
	return
}

func scrape(title string) (paragraph string) {
	replaceRegex, _ := regexp.Compile(" ")
	urlTitle := replaceRegex.ReplaceAllString(title, "_")
	url := "https://en.wikipedia.org/wiki/" + urlTitle

	doc, err := goquery.NewDocument(url)
	errorify(err)

	selection := doc.Find("#mw-content-text > p").First()
	paragraph = selection.Text()
	
	deParenRegex, err := regexp.Compile("\\([^)]*\\)+")
	errorify(err)
	paragraph = deParenRegex.ReplaceAllString(paragraph, "")
	deBracketRegex, err := regexp.Compile("\\[[^)]*\\]+")
	errorify(err)
	paragraph = deBracketRegex.ReplaceAllString(paragraph, "")
	
	return
}

func handler(w http.ResponseWriter, r *http.Request) {
	articles := findArticle(r.URL.Path[1:])
	if len(articles) > 0 {
		fmt.Fprintf(w, scrape(articles[0]))
	}
}

func main() {
	scrape("falafel")
	http.HandleFunc("/", handler)
	http.ListenAndServe(":8080", nil)
}















