# hype

[![Clojars Project](https://img.shields.io/clojars/v/io.logicblocks/hype.svg)](https://clojars.org/io.logicblocks/hype)
[![Clojars Downloads](https://img.shields.io/clojars/dt/io.logicblocks/hype.svg)](https://clojars.org/io.logicblocks/hype)
[![GitHub Contributors](https://img.shields.io/github/contributors-anon/logicblocks/hype.svg)](https://github.com/logicblocks/hype/graphs/contributors)

Hypermedia functions for `bidi` and `ring`.

## Install

Add the following to your `project.clj` file:

```clj
[io.logicblocks/hype "1.1.0"]
```

## Documentation

* [API Docs](http://logicblocks.github.io/hype)

## Usage

`hype` currently provides support for:

- generating paths or URLs,
- including URI template parameters, and
- converting between paths and URLs.

For example:

```clojure
(require '[ring.mock.request :as ring-mock])
(require '[hype.core :as hype])

(def request (ring-mock/request "GET" "https://localhost:8080/help"))
(def routes 
  [""
   [["/" :index]
    ["/articles" :articles]
    [["/articles/" :article-id] 
     [["" :article]
      [["/sections/" :article-section-id] :article-section]]]]])

(hype/base-url-for request)
; => "https://localhost:8080"

(absolute-path-for routes :index)
; => "/"

(absolute-path-for routes :article
  {:path-params {:article-id 10}})
; => "/articles/10"

(absolute-path-for routes :article
  {:path-template-params {:article-id :article-id}})
; => "/articles/{articleId}"

(absolute-path-for routes :article-index
  {:query-params {:latest true
                  :sort-direction "descending"}
   :query-template-params [:per-page :page]})
; => "/articles?latest=true&sortDirection=descending{&perPage,page}"

(absolute-url-for request routes :index)
; => "https://localhost:8080/"

(absolute-url-for request routes :article
  {:path-template-params {:article-id :articleID}
   :path-template-param-key-fn clojure.core/identity})
; => "https://localhost:8080/articles/{articleID}"

(absolute-url-for request routes :article
  {:path-params {:article-id 10}
   :query-template-params [:include-author :include-images]})
; => "https://localhost:8080/articles/10{?includeAuthor,includeImages}"

(absolute-url-for request routes :article-index
  {:query-params {:latest true
                  :sort-direction "descending"}
   :query-param-key-fn clojure.core/identity
   :query-template-params [:per-page :page]
   :query-template-param-key-fn clojure.core/identity})
; => "https://localhost:8080/articles?latest=true&sort-direction=descending{&per-page,page}"

(absolute-path->absolute-url request "/articles/index.html")
; => "https://localhost:8080/articles"
```

See the [Getting Started](https://logicblocks.github.io/hype/getting-started.html) 
guide for more details.

## License

Copyright &copy; 2023 LogicBlocks Maintainers

Distributed under the terms of the
[MIT License](http://opensource.org/licenses/MIT).
