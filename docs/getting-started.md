# Getting Started

`hype` provides a number of functions to assist in using hypermedia in APIs.
Primarily, `hype` uses reverse routing to build URLs and paths. Currently, 
`hype` expects `ring` compatible requests and provides support for the `reitit` 
and `bidi` routing libraries with extension points allowing others to be
supported. 

`hype` can build absolute paths (i.e., paths that start from `/`) and
absolute URLs (i.e., including scheme, host and port etc.) and provides some 
facilities for converting between the two.

`hype` can build URLs and paths including any or all of:

* path parameters,
* query parameters,
* [URI templates](https://tools.ietf.org/html/rfc6570).

## <a name="requiring-hype"></a> Requiring `hype`

`hype` can be required with:

```clojure
(require '[hype.core :as hype])
```

## <a name="generating-paths"></a> Generating paths

Given a set of `bidi` routes:

```clojure
(def router 
  [""
   [["/" :index]
    ["/articles" :articles]
    [["/articles/" :article-id] 
     [["" :article]
      [["/sections/" :section-id] :article-section]]]]])
```

or an equivalent `reitit` router:

```clojure
(def router
  (reitit/router
    [["/" :index]
     ["/articles"
      ["" :articles]
      ["/:article-id"
       ["" :article]
       ["/sections/:section-id" :article-section]]]]))
```

an absolute path, say for `:articles`, can be generated as follows:

```clojure
(hype/absolute-path-for router :articles)
; => "/articles"
```

If the route requires a path parameter, such as in the `:article` route above:

```clojure
(hype/absolute-path-for router :article
  {:path-params {:article-id 26}})
; => "/articles/26"
```

To leave the path parameter templated for use in URI templates:

```clojure
(hype/absolute-path-for router :article
  {:path-template-params {:article-id :article-id}})
; => "/articles/{articleId}"
```

Path template parameters are automatically converted to camel case. This 
behaviour can be overridden as follows:

```clojure
(hype/absolute-path-for router :article
  {:path-template-params {:article-id :articleID}
   :path-template-param-key-fn clojure.core/identity})
; => "/articles/{articleID}"
```

If the route requires multiple path parameters, such as in the 
`:article-section` route above:

```clojure
(hype/absolute-path-for router :article-section
  {:path-params {:article-id 26
                 :section-id 1}})
; => "/articles/26/sections/1
```

Query parameters are also supported when generating a path:

```clojure
(hype/absolute-path-for router :articles
  {:query-params {:page 2
                  :per-page 10}})
; => "/articles?page=2&perPage=10"
```

As for path template parameters, query parameter keys are automatically 
converted to camel case. This behaviour can be overridden as follows:

```clojure
(hype/absolute-path-for router :articles
  {:query-params {:page 2
                  :per-page 10}
   :query-param-key-fn clojure.core/identity})
; => "/articles?page=2&per-page=10"
```

Both path and query parameters can be provided together:

```clojure
(hype/absolute-path-for router :article
  {:path-params {:article-id 26}
   :query-params {:include-all-sections true}})
; => "/articles/26?includeAllSections=true"
```

When paths need to include query string template parameters:

```clojure
(hype/absolute-path-for router :articles
  {:query-template-params [:page :per-page]})
; => "/articles{?page,perPage}"
```

Again, query template parameter keys are converted to camel case by default.
This behaviour can be overridden as follows:

```clojure
(hype/absolute-path-for router :articles
  {:query-template-params [:page :per-page]
   :query-template-param-key-fn clojure.core/identity})
; => "/articles{?page,per-page}"
```

Query string template parameters can be used in addition to path parameters
and other query string parameters:

```clojure
(hype/absolute-path-for router :article
  {:path-params {:article-id 26}
   :query-params {:include-all-sections true}
   :query-template-params [:include-summary]})
; => "/articles/26?includeAllSections=true{&includeSummary}"
```

Currently, there is no support for relative paths.

## <a name="generating-urls"></a> Generating URLs

Given one of the routers defined in [Generating paths](#generating-paths) and
a `ring` `request` such as that produced by:

```clojure
(require '[ring.mock.request :as ring-mock])

(def request (ring-mock/request "GET" "https://localhost:8080/help"))
```

an absolute URL, say for `:articles`, can be generated as:

```clojure
(hype/absolute-url-for request routes :articles)
; => "https://localhost:8080/articles"
```

All parameters that can be passed to [[absolute-path-for]] can also be passed
to [[absolute-url-for]], for example:

```clojure
(hype/absolute-url-for request routes :article
  {:path-params {:article-id 26}})
; => "https://localhost:8080/articles/26"

(hype/absolute-url-for request routes :article
  {:path-template-params {:article-id :articleID}
   :path-template-param-key-fn clojure.core/identity})
; => "https://localhost:8080/articles/{articleID}"

(hype/absolute-url-for request routes :articles
  {:query-template-params [:page :per-page]})
; => "https://localhost:8080/articles{?page,perPage}"

(hype/absolute-url-for request routes :article
  {:path-params {:article-id 26}
   :query-params {:include-all-sections true}
   :query-template-params [:include-summary]})
; => "https://localhost:8080/articles/26?includeAllSections=true{&includeSummary}"
```

## <a name="converting-paths-and-urls"></a> Converting between paths and URLs

Given the request defined in [Generating URLs](#generating-urls) and an absolute
path, such as:

```clojure
(def absolute-path "/articles/26/sections/3")
```

an absolute URL relative to the request can be generated as:

```clojure
(hype/absolute-path->absolute-url request absolute-path)
; => "https://localhost:8080/articles/26/sections/3"
``` 
