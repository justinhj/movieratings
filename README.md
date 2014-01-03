# movieratings

Clojure code to grab movie ratings from Rotten Tomatoes

## Usage

To use the Rotten Tomatoes API you need to get an API key and then set 
it as an environment variable on your system. For example:

export ROTTEN_TOMATOES_API_KEY="your key"

To find movie ratings for all movies with the text 'jaws' in the title

    lein run "jaws"

If no api key is found then the program falls back to grabbing the HTML content 
and scraping the desired values.

## Installation

Requires lein 2

    lein deps
    lein uberjar 

## Using from emacs

Currently I use cider from emacs packages

    M-x cider-jack-in

    (in-ns 'movieratings.core)
    (-main "aliens")

## License

The MIT License (MIT)

Copyright (C) 2012 Justin Heyes-Jones

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.

