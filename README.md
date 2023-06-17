# hype

[![Clojars Project](https://img.shields.io/clojars/v/io.logicblocks/hype.svg)](https://clojars.org/io.logicblocks/hype)
[![Clojars Downloads](https://img.shields.io/clojars/dt/io.logicblocks/hype.svg)](https://clojars.org/io.logicblocks/hype)
[![GitHub Contributors](https://img.shields.io/github/contributors-anon/logicblocks/hype.svg)](https://github.com/logicblocks/hype/graphs/contributors)

Hypermedia functions for `ring` routers.

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

`hype` includes built in backends for `bidi` and `reitit` and includes
extension points allowing other routers to be supported.

### Examples

```clojure
(require '[ring.mock.request :as ring-mock])
(require '[hype.core :as hype])
(require '[reitit.core :as reitit])
(require '[bidi.bidi :as bidi])

(def request (ring-mock/request "GET" "https://localhost:8080/help"))

;; general
(hype/base-url request)
; => "https://localhost:8080"

(hype/absolute-path->absolute-url request "/articles/index.html")
; => "https://localhost:8080/articles"

;; reitit
(def router
  (reitit/router
    [["/" :index]
     ["/articles"
      ["" :articles]
      ["/:article-id"
       ["" :article]
       ["/sections/:section-id" :article-section]]]]))

(reitit/routes router)

;; or

;; bidi
(def router
  [""
   [["/" :index]
    ["/articles" :articles]
    [["/articles/" :article-id]
     [["" :article]
      [["/sections/" :section-id] :article-section]]]]])

;; then

(hype/absolute-path-for router :index)
; => "/"

(hype/absolute-path-for router :article
  {:path-params {:article-id 10}})
; => "/articles/10"

(hype/absolute-path-for router :article
  {:path-template-params {:article-id :article-id}})
; => "/articles/{articleId}"

(hype/absolute-path-for router :articles
  {:query-params          {:latest         true
                           :sort-direction "descending"}
   :query-template-params [:per-page :page]})
; => "/articles?latest=true&sortDirection=descending{&perPage,page}"

(hype/absolute-url-for request router :index)
; => "https://localhost:8080/"

(hype/absolute-url-for request router :article
  {:path-template-params       {:article-id :articleID}
   :path-template-param-key-fn clojure.core/identity})
; => "https://localhost:8080/articles/{articleID}"

(hype/absolute-url-for request router :article
  {:path-params           {:article-id 10}
   :query-template-params [:include-author :include-images]})
; => "https://localhost:8080/articles/10{?includeAuthor,includeImages}"

(hype/absolute-url-for request router :articles
  {:query-params                {:latest         true
                                 :sort-direction "descending"}
   :query-param-key-fn          clojure.core/identity
   :query-template-params       [:per-page :page]
   :query-template-param-key-fn clojure.core/identity})
; => "https://localhost:8080/articles?latest=true&sort-direction=descending{&per-page,page}"
```

See the [Getting Started](https://logicblocks.github.io/hype/getting-started.html) 
guide for more details.

## License

Copyright &copy; 2023 LogicBlocks Maintainers

Distributed under the terms of the
[MIT License](http://opensource.org/licenses/MIT).
