(ns hype.core
  "Hypermedia functions for `ring` routers.

  `hype` currently provides support for:

    - generating paths or URLs,
    - including template parameters, and
    - converting between paths and URLs.

  `hype` includes built in backends for `bidi` and `reitit` and includes
  extension points allowing other routers to be supported."
  (:require
   [clojure.string :as string]

   [bidi.bidi :as bidi]
   [reitit.core :as reitit-core]
   [reitit.impl :as reitit-impl]

   [ring.util.codec :as codec]
   [camel-snake-kebab.core :as csk])
  (:import
   [clojure.lang PersistentVector]
   [reitit.core Router]))

(defprotocol Backend
  (path-for [_ route-name parameters]))

(defrecord BidiBackend [routing-context]
  Backend
  (path-for [_ route-name parameters]
    (apply bidi/path-for routing-context route-name
      (mapcat seq parameters))))

(defrecord ReititBackend [routing-context]
  Backend
  (path-for [_ route-name parameters]
    (let [match (reitit-core/match-by-name
                  routing-context route-name parameters)
          template (:template match)
          options (reitit-core/options routing-context)
          route (reitit-impl/parse template options)]
      (reitit-impl/path-for route parameters))))

(defprotocol BackendFactory
  (backend-for [thing]))

(extend-type Router
  BackendFactory
  (backend-for [routing-context]
    (->ReititBackend routing-context)))

(extend-type PersistentVector
  BackendFactory
  (backend-for [routing-context]
    (->BidiBackend routing-context)))

(defn- query-string-for
  [parameters {:keys [key-fn]
               :or   {key-fn csk/->camelCaseString}}]
  (if (seq parameters)
    (str "?"
      (codec/form-encode
        (reduce-kv
          (fn [acc k v] (assoc acc (key-fn k) v))
          {}
          parameters)))
    ""))

(defn- query-template-for
  [parameter-names {:keys [expansion-character key-fn]
                    :or   {expansion-character "?"
                           key-fn              csk/->camelCaseString}}]
  (if (seq parameter-names)
    (str "{" expansion-character
      (string/join "," (map key-fn parameter-names))
      "}")
    ""))

(defn- path-template-params-for
  [parameter-definitions {:keys [key-fn]
                          :or   {key-fn csk/->camelCaseString}}]
  (reduce-kv
    (fn [acc k v]
      (assoc acc k (str "{" (key-fn v) "}")))
    {}
    parameter-definitions))

(defn base-url
  "Returns the URL used to reach the server based on `request`.

  The `request` should be a ring request or equivalent. The URL is returned
  as a string."
  [request]
  (let [scheme (-> request :scheme name)
        host (get-in request [:headers "host"])]
    (format "%s://%s" scheme host)))

(defn absolute-path->absolute-url
  "Builds an absolute URL by appending `absolute-path` to the base URL of
  `request`.

  The `request` should be a ring request or equivalent. The URL is returned
  as a string."
  [request absolute-path]
  (str (base-url request) absolute-path))

(defn absolute-path-for
  "Builds an absolute path for `route` based on `router` and `params` where:

    - `router` is a `reitit` router or a `bidi` routes data structure,
    - `route` is a keyword identifying the name of the route for which to build
       a path,
    - `params` is an optional map which optionally includes any of:
      - `path-params`: parameters defined in the routes as route patterns,
        specified as a map,
      - `path-template-params`: parameters that should remain templatable in
        the resulting path, specified as a map from path parameter name to
        template variable name, as keywords.
      - `path-template-param-key-fn`: a function to apply to path template
        variable names before including in the path, camel casing by default.
      - `query-params`: parameters that should be appended to the path as query
        string parameters, specified as a map,
      - `query-param-key-fn`: a function to apply to query parameter keys before
        including in the path, camel casing by default,
      - `query-template-params`: parameters that should be appended to the path
        as query string template parameters, specified as a sequence of
        parameter names,
      - `query-template-param-key-fn`: a function to apply to query template
        parameter keys before including in the path, camel casing by default.

  The path is returned as a string.

  Examples:

      ;; bidi
      (def router
        [\"/\" {\"index.html\" :index
                \"articles/\" {\"index.html\" :article-index
                               [:id \"/article.html\"] :article}}])
      ;; or reitit
      (def router
        (reitit/router
          [[\"/index.html\" :index]
           [\"/articles/index.html\" :article-index]
           [\"/articles/:id/article.html\" :article]]))

      (absolute-path-for router :index)
      ; => \"/index.html\"

      (absolute-path-for router :article
        {:path-params {:id 10}})
      ; => \"/articles/10/article.html\"

      (absolute-path-for router :article
        {:path-template-params {:id :article-id}})
      ; => \"/articles/{articleId}/article.html\"

      (absolute-path-for router :article
        {:path-template-params {:id :articleID}
         :path-template-param-key-fn clojure.core/identity})
      ; => \"/articles/{articleID}/article.html\"

      (absolute-path-for router :article-index
        {:query-params {:latest true
                        :sort-direction \"descending\"}
         :query-template-params [:per-page :page]})
      ; => \"/articles/index.html?latest=true&sortDirection=descending{&perPage,page}\"

      (absolute-path-for router :article
        {:path-params {:id 10}
         :query-template-params [:include-author, :include-images]})
      ; => \"/articles/10/article.html{?includeAuthor,includeImages}\"

      (absolute-path-for router :article-index
        {:query-params {:latest true
                        :sort-direction \"descending\"}
         :query-param-key-fn clojure.core/identity
         :query-template-params [:per-page :page]
         :query-template-param-key-fn clojure.core/identity})
      ; => \"/articles/index.html?latest=true&sort-direction=descending{&per-page,page}\""
  ([router route] (absolute-path-for router route {}))
  ([router route
    {:keys [path-params
            path-template-params
            path-template-param-key-fn
            query-params
            query-param-key-fn
            query-template-params
            query-template-param-key-fn]
     :or   {path-params                 {}
            path-template-params        {}
            path-template-param-key-fn  csk/->camelCaseString
            query-params                {}
            query-param-key-fn          csk/->camelCaseString
            query-template-params       []
            query-template-param-key-fn csk/->camelCaseString}
     :as   params}]
   (str
     (path-for (backend-for router) route
       (merge
         (path-template-params-for
           path-template-params
           {:key-fn path-template-param-key-fn})
         path-params))
     (query-string-for
       query-params
       {:key-fn query-param-key-fn})
     (query-template-for
       query-template-params
       {:expansion-character (if (empty? query-params) "?" "&")
        :key-fn              query-template-param-key-fn}))))

(defn absolute-url-for
  "Builds an absolute URL for `route` based on `request`, `router` and
  `params` where:

    - `router` is a `reitit` router or a `bidi` routes data structure,
    - `route` is a keyword identifying the name of the route for which to build
       a path,
    - `params` is an optional map which optionally includes any of:
      - `path-params`: parameters defined in the routes as route patterns,
        specified as a map,
      - `path-template-params`: parameters that should remain templatable in
        the resulting path, specified as a map from path parameter name to
        template variable name, as keywords.
      - `path-template-param-key-fn`: a function to apply to path template
        variable names before including in the path, camel casing by default.
      - `query-params`: parameters that should be appended to the path as query
        string parameters, specified as a map,
      - `query-param-key-fn`: a function to apply to query parameter keys before
        including in the path, camel casing by default,
      - `query-template-params`: parameters that should be appended to the path
        as query string template parameters, specified as a sequence of
        parameter names,
      - `query-template-param-key-fn`: a function to apply to query parameter
        template keys before including in the path, camel casing by default.

  The `request` should be a ring request or equivalent. The URL is returned
  as a string.

  Examples:
      (require '[ring.mock.request :as ring-mock])

      (def request (ring-mock/request \"GET\" \"https://localhost:8080/help\"))

      ;; bidi
      (def router
        [\"/\" {\"index.html\" :index
                \"articles/\" {\"index.html\" :article-index
                               [:id \"/article.html\"] :article}}])
      ;; or reitit
      (def router
        (reitit/router
          [[\"/index.html\" :index]
           [\"/articles/index.html\" :article-index]
           [\"/articles/:id/article.html\" :article]]))

      (absolute-url-for request router :index)
      ; => \"https://localhost:8080/index.html\"

      (absolute-url-for request router :article
        {:path-params {:id 10}})
      ; => \"https://localhost:8080/articles/10/article.html\"

      (absolute-url-for request router :article
        {:path-template-params {:id :article-id}})
      ; => \"https://localhost:8080/articles/{articleId}/article.html\"

      (absolute-url-for request router :article
        {:path-template-params {:id :articleID}
         :path-template-param-key-fn clojure.core/identity})
      ; => \"https://localhost:8080/articles/{articleID}/article.html\"

      (absolute-url-for request router :article-index
        {:query-params {:latest true
                        :sort-direction \"descending\"}
         :query-template-params [:per-page :page]})
      ; => \"https://localhost:8080/articles/index.html?latest=true&sortDirection=descending{&perPage,page}\"

      (absolute-url-for request router :article
        {:path-params {:id 10}
         :query-template-params [:include-author :include-images]})
      ; => \"https://localhost:8080/articles/10/article.html{?includeAuthor,includeImages}\"

      (absolute-url-for request router :article-index
        {:query-params {:latest true
                        :sort-direction \"descending\"}
         :query-param-key-fn clojure.core/identity
         :query-template-params [:per-page :page]
         :query-template-param-key-fn clojure.core/identity})
      ; => \"https://localhost:8080/articles/index.html?latest=true&sort-direction=descending{&per-page,page}\""
  ([request router route]
   (absolute-url-for request router route {}))
  ([request router route params]
   (absolute-path->absolute-url
     request (absolute-path-for router route params))))
