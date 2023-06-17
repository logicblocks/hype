(ns hype.core-test
  (:require
   [clojure.test :refer :all]

   [ring.mock.request :as ring]
   [reitit.core :as reitit]
   [camel-snake-kebab.core :as csk]

   [hype.core :as hype]))

(deftest base-url-for-returns-the-domain-name-for-a-url
  (is (= "https://example.com"
        (hype/base-url
          (ring/request "GET" "https://example.com/some/thing"))))
  (is (= "http://another.example.com"
        (hype/base-url
          (ring/request "GET" "http://another.example.com/some/thing")))))

(deftest absolute-path-for-returns-the-absolute-path-for-a-route
  (testing "bidi routing"
    (let [routes [""
                  [["/" :root]
                   ["/examples" :examples]]]]
      (is (= "/examples"
            (hype/absolute-path-for routes :examples)))))

  (testing "reitit routing"
    (let [router (reitit/router
                   [["/" :root]
                    ["/examples" :examples]])]
      (is (= "/examples"
            (hype/absolute-path-for router :examples))))))

(deftest absolute-path-for-expands-a-single-path-parameter
  (testing "bidi routing"
    (let [routes [""
                  [["/" :root]
                   [["/examples/" :example-id] :example]]]]
      (is (= "/examples/123"
            (hype/absolute-path-for routes :example
              {:path-params {:example-id 123}})))))

  (testing "reitit routing"
    (let [router (reitit/router
                   [["/" :root]
                    ["/examples/:example-id" :example]])]
      (is (= "/examples/123"
            (hype/absolute-path-for router :example
              {:path-params {:example-id 123}}))))))

(deftest absolute-path-for-expands-multiple-path-parameters
  (testing "bidi routing"
    (let [routes [""
                  [["/" :root]
                   [["/examples/" :example-id "/thing/" :thing-id] :example]]]]
      (is (= "/examples/123/thing/456"
            (hype/absolute-path-for routes :example
              {:path-params {:example-id 123
                             :thing-id   456}})))))

  (testing "reitit routing"
    (let [router (reitit/router
                   [["/" :root]
                    ["/examples/:example-id/thing/:thing-id" :example]])]
      (is (= "/examples/123/thing/456"
            (hype/absolute-path-for router :example
              {:path-params {:example-id 123
                             :thing-id   456}}))))))

(deftest absolute-path-for-expands-a-single-path-template-parameter
  (testing "bidi routing"
    (let [routes [""
                  [["/" :root]
                   [["/examples/" :example-id] :example]]]]
      (is (= "/examples/{exampleId}"
            (hype/absolute-path-for routes :example
              {:path-template-params {:example-id :example-id}})))))

  (testing "reitit routing"
    (let [router (reitit/router
                   [["/" :root]
                    ["/examples/:example-id" :example]])]
      (is (= "/examples/{exampleId}"
            (hype/absolute-path-for router :example
              {:path-template-params {:example-id :example-id}}))))))

(deftest absolute-path-for-expands-multiple-path-template-parameters
  (testing "bidi routing"
    (let [routes [""
                  [["/" :root]
                   [["/examples/" :example-id]
                    [["" :example]
                     [["/subexamples/" :sub-example-id] :sub-example]]]]]]
      (is (= "/examples/{exampleId}/subexamples/{subExampleId}"
            (hype/absolute-path-for routes :sub-example
              {:path-template-params {:example-id     :example-id
                                      :sub-example-id :sub-example-id}})))))

  (testing "reitit routing"
    (let [router (reitit/router
                   [["/" :root]
                    ["/examples/:example-id" :example]
                    ["/examples/:example-id/subexamples/:sub-example-id"
                     :sub-example]])]
      (is (= "/examples/{exampleId}/subexamples/{subExampleId}"
            (hype/absolute-path-for router :sub-example
              {:path-template-params {:example-id     :example-id
                                      :sub-example-id :sub-example-id}}))))))

(deftest absolute-path-for-uses-path-template-parameter-key-function
  (testing "bidi routing"
    (let [routes [""
                  [["/" :root]
                   [["/examples/" :example-id]
                    [["" :example]
                     [["/subexamples/" :sub-example-id] :sub-example]]]]]]
      (is (= "/examples/{example_id}/subexamples/{sub_example_id}"
            (hype/absolute-path-for routes :sub-example
              {:path-template-params       {:example-id     :example-id
                                            :sub-example-id :sub-example-id}
               :path-template-param-key-fn csk/->snake_case_string})))))

  (testing "reitit routing"
    (let [router (reitit/router
                   [["/" :root]
                    ["/examples/:example-id" :example]
                    ["/examples/:example-id/subexamples/:sub-example-id"
                     :sub-example]])]
      (is (= "/examples/{example_id}/subexamples/{sub_example_id}"
            (hype/absolute-path-for router :sub-example
              {:path-template-params       {:example-id     :example-id
                                            :sub-example-id :sub-example-id}
               :path-template-param-key-fn csk/->snake_case_string}))))))

(deftest absolute-path-for-expands-a-single-query-parameter
  (testing "bidi routing"
    (let [routes [""
                  [["/" :root]
                   ["/examples" :example]]]]
      (is (= "/examples?key=value"
            (hype/absolute-path-for routes :example
              {:query-params {:key "value"}})))))

  (testing "reitit routing"
    (let [router (reitit/router
                   [["/" :root]
                    ["/examples" :example]])]
      (is (= "/examples?key=value"
            (hype/absolute-path-for router :example
              {:query-params {:key "value"}}))))))

(deftest absolute-path-for-expands-multiple-query-parameters
  (testing "bidi routing"
    (let [routes [""
                  [["/" :root]
                   ["/examples" :example]]]]
      (is (= "/examples?key1=value1&key2=value2"
            (hype/absolute-path-for routes :example
              {:query-params {:key1 "value1"
                              :key2 "value2"}})))))

  (testing "reitit routing"
    (let [router (reitit/router
                   [["/" :root]
                    ["/examples" :example]])]
      (is (= "/examples?key1=value1&key2=value2"
            (hype/absolute-path-for router :example
              {:query-params {:key1 "value1"
                              :key2 "value2"}}))))))

(deftest absolute-path-for-uses-camel-case-for-query-parameters-by-default
  (testing "bidi routing"
    (let [routes [""
                  [["/" :root]
                   ["/examples" :example]]]]
      (is (= "/examples?perPage=10&sortDirection=descending"
            (hype/absolute-path-for routes :example
              {:query-params {:per-page       10
                              :sort-direction "descending"}})))))

  (testing "reitit routing"
    (let [router (reitit/router
                   [["/" :root]
                    ["/examples" :example]])]
      (is (= "/examples?perPage=10&sortDirection=descending"
            (hype/absolute-path-for router :example
              {:query-params {:per-page       10
                              :sort-direction "descending"}}))))))

(deftest absolute-path-for-uses-query-parameter-key-function
  (testing "bidi routing"
    (let [routes [""
                  [["/" :root]
                   ["/examples" :example]]]]
      (is (= "/examples?per_page=10&sort_direction=descending"
            (hype/absolute-path-for routes :example
              {:query-params       {:per-page       10
                                    :sort-direction "descending"}
               :query-param-key-fn csk/->snake_case_string})))))

  (testing "reitit routing"
    (let [routes (reitit/router
                   [["/" :root]
                    ["/examples" :example]])]
      (is (= "/examples?per_page=10&sort_direction=descending"
            (hype/absolute-path-for routes :example
              {:query-params       {:per-page       10
                                    :sort-direction "descending"}
               :query-param-key-fn csk/->snake_case_string}))))))

(deftest absolute-path-for-expands-a-single-query-template-parameter
  (testing "bidi routing"
    (let [routes [""
                  [["/" :root]
                   ["/examples" :example]]]]
      (is (= "/examples{?key}"
            (hype/absolute-path-for routes :example
              {:query-template-params #{:key}})))))

  (testing "reitit routing"
    (let [routes (reitit/router
                   [["/" :root]
                    ["/examples" :example]])]
      (is (= "/examples{?key}"
            (hype/absolute-path-for routes :example
              {:query-template-params #{:key}}))))))

(deftest absolute-path-for-expands-multiple-query-template-parameters
  (testing "bidi routing"
    (let [routes [""
                  [["/" :root]
                   ["/examples" :example]]]]
      (is (= "/examples{?key1,key2}"
            (hype/absolute-path-for routes :example
              {:query-template-params [:key1 :key2]})))))

  (testing "reitit routing"
    (let [router (reitit/router
                   [["/" :root]
                    ["/examples" :example]])]
      (is (= "/examples{?key1,key2}"
            (hype/absolute-path-for router :example
              {:query-template-params [:key1 :key2]}))))))

(deftest
  absolute-path-for-appends-query-template-parameters-after-query-parameters
  (testing "bidi routing"
    (let [routes [""
                  [["/" :root]
                   ["/examples" :example]]]]
      (is (= "/examples?key0=value0{&key1,key2}"
            (hype/absolute-path-for routes :example
              {:query-params          {:key0 "value0"}
               :query-template-params [:key1 :key2]})))))

  (testing "reitit routing"
    (let [router (reitit/router
                   [["/" :root]
                    ["/examples" :example]])]
      (is (= "/examples?key0=value0{&key1,key2}"
            (hype/absolute-path-for router :example
              {:query-params          {:key0 "value0"}
               :query-template-params [:key1 :key2]}))))))

(deftest
  absolute-path-for-uses-camel-case-for-query-template-parameters-by-default
  (testing "bidi routing"
    (let [routes [""
                  [["/" :root]
                   ["/examples" :example]]]]
      (is (= "/examples{?perPage,sortDirection}"
            (hype/absolute-path-for routes :example
              {:query-template-params [:per-page :sort-direction]})))))

  (testing "reitit routing"
    (let [router (reitit/router
                   [["/" :root]
                    ["/examples" :example]])]
      (is (= "/examples{?perPage,sortDirection}"
            (hype/absolute-path-for router :example
              {:query-template-params [:per-page :sort-direction]}))))))

(deftest absolute-path-for-uses-query-template-parameter-key-function
  (testing "bidi routing"
    (let [routes [""
                  [["/" :root]
                   ["/examples" :example]]]]
      (is (= "/examples{?per_page,sort_direction}"
            (hype/absolute-path-for routes :example
              {:query-template-params       [:per-page :sort-direction]
               :query-template-param-key-fn csk/->snake_case_string})))))

  (testing "reitit routing"
    (let [router (reitit/router
                   [["/" :root]
                    ["/examples" :example]])]
      (is (= "/examples{?per_page,sort_direction}"
            (hype/absolute-path-for router :example
              {:query-template-params       [:per-page :sort-direction]
               :query-template-param-key-fn csk/->snake_case_string}))))))

(deftest absolute-url-for-returns-the-absolute-url-for-a-route
  (testing "bidi routing"
    (let [request (ring/request "GET" "https://example.com/some/thing")
          routes [""
                  [["/" :root]
                   ["/examples" :examples]]]]
      (is (= "https://example.com/examples"
            (hype/absolute-url-for request routes :examples)))))

  (testing "reitit routing"
    (let [request (ring/request "GET" "https://example.com/some/thing")
          router (reitit/router
                   [["/" :root]
                    ["/examples" :examples]])]
      (is (= "https://example.com/examples"
            (hype/absolute-url-for request router :examples))))))

(deftest absolute-url-for-expands-a-single-path-parameter
  (testing "bidi routing"
    (let [request (ring/request "GET" "https://example.com/some/thing")
          routes [""
                  [["/" :root]
                   [["/examples/" :example-id] :example]]]]
      (is (= "https://example.com/examples/123"
            (hype/absolute-url-for request routes :example
              {:path-params {:example-id 123}})))))

  (testing "reitit routing"
    (let [request (ring/request "GET" "https://example.com/some/thing")
          router (reitit/router
                   [["/" :root]
                    ["/examples/:example-id" :example]])]
      (is (= "https://example.com/examples/123"
            (hype/absolute-url-for request router :example
              {:path-params {:example-id 123}}))))))

(deftest absolute-url-for-expands-multiple-path-parameters
  (testing "bidi routing"
    (let [request (ring/request "GET" "https://example.com/some/thing")
          routes [""
                  [["/" :root]
                   [["/examples/" :example-id "/thing/" :thing-id] :example]]]]
      (is (= "https://example.com/examples/123/thing/456"
            (hype/absolute-url-for request routes :example
              {:path-params {:example-id 123
                             :thing-id   456}})))))

  (testing "reitit routing"
    (let [request (ring/request "GET" "https://example.com/some/thing")
          router (reitit/router
                   [["/" :root]
                    ["/examples/:example-id/thing/:thing-id" :example]])]
      (is (= "https://example.com/examples/123/thing/456"
            (hype/absolute-url-for request router :example
              {:path-params {:example-id 123
                             :thing-id   456}}))))))

(deftest absolute-url-for-expands-a-single-path-template-parameter
  (testing "bidi routing"
    (let [request (ring/request "GET" "https://example.com/some/thing")
          routes [""
                  [["/" :root]
                   [["/examples/" :example-id] :example]]]]
      (is (= "https://example.com/examples/{exampleId}"
            (hype/absolute-url-for request routes :example
              {:path-template-params {:example-id :example-id}})))))

  (testing "reitit routing"
    (let [request (ring/request "GET" "https://example.com/some/thing")
          router (reitit/router
                   [["/" :root]
                    ["/examples/:example-id" :example]])]
      (is (= "https://example.com/examples/{exampleId}"
            (hype/absolute-url-for request router :example
              {:path-template-params {:example-id :example-id}}))))))

(deftest absolute-url-for-expands-multiple-path-template-parameter
  (testing "bidi routing"
    (let [request (ring/request "GET" "https://example.com/some/thing")
          routes [""
                  [["/" :root]
                   [["/examples/" :example-id]
                    [["" :example]
                     [["/subexamples/" :sub-example-id] :sub-example]]]]]]
      (is
        (= "https://example.com/examples/{exampleId}/subexamples/{subExampleId}"
          (hype/absolute-url-for request routes :sub-example
            {:path-template-params {:example-id     :example-id
                                    :sub-example-id :sub-example-id}})))))

  (testing "reitit routing"
    (let [request (ring/request "GET" "https://example.com/some/thing")
          router (reitit/router
                   [["/" :root]
                    ["/examples/:example-id" :example]
                    ["/examples/:example-id/subexamples/:sub-example-id"
                     :sub-example]])]
      (is
        (= "https://example.com/examples/{exampleId}/subexamples/{subExampleId}"
          (hype/absolute-url-for request router :sub-example
            {:path-template-params {:example-id     :example-id
                                    :sub-example-id :sub-example-id}}))))))

(deftest absolute-url-for-uses-path-template-parameter-key-function
  (testing "bidi routing"
    (let [request (ring/request "GET" "https://example.com/some/thing")
          routes [""
                  [["/" :root]
                   [["/examples/" :example-id]
                    [["" :example]
                     [["/subexamples/" :sub-example-id] :sub-example]]]]]]
      (is
        (= (str "https://example.com/examples/{example_id}/subexamples/"
             "{sub_example_id}")
          (hype/absolute-url-for request routes :sub-example
            {:path-template-params       {:example-id     :example-id
                                          :sub-example-id :sub-example-id}
             :path-template-param-key-fn csk/->snake_case_string})))))

  (testing "reitit routing"
    (let [request (ring/request "GET" "https://example.com/some/thing")
          routes (reitit/router
                   [["/" :root]
                    ["/examples/:example-id" :example]
                    ["/examples/:example-id/subexamples/:sub-example-id"
                     :sub-example]])]
      (is
        (= (str "https://example.com/examples/{example_id}/subexamples/"
             "{sub_example_id}")
          (hype/absolute-url-for request routes :sub-example
            {:path-template-params       {:example-id     :example-id
                                          :sub-example-id :sub-example-id}
             :path-template-param-key-fn csk/->snake_case_string}))))))

(deftest absolute-url-for-expands-a-single-query-parameter
  (testing "bidi routing"
    (let [request (ring/request "GET" "https://example.com/some/thing")
          routes [""
                  [["/" :root]
                   ["/examples" :example]]]]
      (is (= "https://example.com/examples?key=value"
            (hype/absolute-url-for request routes :example
              {:query-params {:key "value"}})))))

  (testing "reitit routing"
    (let [request (ring/request "GET" "https://example.com/some/thing")
          router (reitit/router
                   [["/" :root]
                    ["/examples" :example]])]
      (is (= "https://example.com/examples?key=value"
            (hype/absolute-url-for request router :example
              {:query-params {:key "value"}}))))))

(deftest absolute-url-for-expands-multiple-query-parameters
  (testing "bidi routing"
    (let [request (ring/request "GET" "https://example.com/some/thing")
          routes [""
                  [["/" :root]
                   ["/examples" :example]]]]
      (is (= "https://example.com/examples?key1=value1&key2=value2"
            (hype/absolute-url-for request routes :example
              {:query-params {:key1 "value1"
                              :key2 "value2"}})))))

  (testing "reitit routing"
    (let [request (ring/request "GET" "https://example.com/some/thing")
          router (reitit/router
                   [["/" :root]
                    ["/examples" :example]])]
      (is (= "https://example.com/examples?key1=value1&key2=value2"
            (hype/absolute-url-for request router :example
              {:query-params {:key1 "value1"
                              :key2 "value2"}}))))))

(deftest absolute-url-for-uses-camel-case-for-query-parameters-by-default
  (testing "bidi routing"
    (let [request (ring/request "GET" "https://example.com/some/thing")
          routes [""
                  [["/" :root]
                   ["/examples" :example]]]]
      (is (= "https://example.com/examples?perPage=10&sortDirection=descending"
            (hype/absolute-url-for request routes :example
              {:query-params {:per-page       10
                              :sort-direction "descending"}})))))

  (testing "reitit routing"
    (let [request (ring/request "GET" "https://example.com/some/thing")
          routes (reitit/router
                   [["/" :root]
                    ["/examples" :example]])]
      (is (= "https://example.com/examples?perPage=10&sortDirection=descending"
            (hype/absolute-url-for request routes :example
              {:query-params {:per-page       10
                              :sort-direction "descending"}}))))))

(deftest absolute-url-for-uses-query-parameter-key-function
  (testing "bidi routing"
    (let [request (ring/request "GET" "https://example.com/some/thing")
          routes [""
                  [["/" :root]
                   ["/examples" :example]]]]
      (is
        (= "https://example.com/examples?per_page=10&sort_direction=descending"
          (hype/absolute-url-for request routes :example
            {:query-params       {:per-page       10
                                  :sort-direction "descending"}
             :query-param-key-fn csk/->snake_case_string})))))

  (testing "reitit routing"
    (let [request (ring/request "GET" "https://example.com/some/thing")
          router (reitit/router
                   [["/" :root]
                    ["/examples" :example]])]
      (is
        (= "https://example.com/examples?per_page=10&sort_direction=descending"
          (hype/absolute-url-for request router :example
            {:query-params       {:per-page       10
                                  :sort-direction "descending"}
             :query-param-key-fn csk/->snake_case_string}))))))

(deftest absolute-url-for-expands-a-single-query-template-parameter
  (testing "bidi routing"
    (let [request (ring/request "GET" "https://example.com/some/thing")
          routes [""
                  [["/" :root]
                   ["/examples" :example]]]]
      (is (= "https://example.com/examples{?key}"
            (hype/absolute-url-for request routes :example
              {:query-template-params #{:key}})))))

  (testing "reitit routing"
    (let [request (ring/request "GET" "https://example.com/some/thing")
          router (reitit/router
                   [["/" :root]
                    ["/examples" :example]])]
      (is (= "https://example.com/examples{?key}"
            (hype/absolute-url-for request router :example
              {:query-template-params #{:key}}))))))

(deftest absolute-url-for-expands-multiple-query-template-parameter
  (testing "bidi routing"
    (let [request (ring/request "GET" "https://example.com/some/thing")
          routes [""
                  [["/" :root]
                   ["/examples" :example]]]]
      (is (= "https://example.com/examples{?key1,key2}"
            (hype/absolute-url-for request routes :example
              {:query-template-params [:key1 :key2]})))))

  (testing "reitit routing"
    (let [request (ring/request "GET" "https://example.com/some/thing")
          router (reitit/router
                   [["/" :root]
                    ["/examples" :example]])]
      (is (= "https://example.com/examples{?key1,key2}"
            (hype/absolute-url-for request router :example
              {:query-template-params [:key1 :key2]}))))))

(deftest
  absolute-url-for-appends-query-template-parameters-after-query-parameters
  (testing "bidi routing"
    (let [request (ring/request "GET" "https://example.com/some/thing")
          routes [""
                  [["/" :root]
                   ["/examples" :example]]]]
      (is (= "https://example.com/examples?key0=value0{&key1,key2}"
            (hype/absolute-url-for request routes :example
              {:query-params          {:key0 "value0"}
               :query-template-params [:key1 :key2]})))))

  (testing "reitit routing"
    (let [request (ring/request "GET" "https://example.com/some/thing")
          router (reitit/router
                   [["/" :root]
                    ["/examples" :example]])]
      (is (= "https://example.com/examples?key0=value0{&key1,key2}"
            (hype/absolute-url-for request router :example
              {:query-params          {:key0 "value0"}
               :query-template-params [:key1 :key2]}))))))

(deftest
  absolute-url-for-uses-camel-case-for-query-template-parameters-by-default
  (testing "bidi routing"
    (let [request (ring/request "GET" "https://example.com/some/thing")
          routes [""
                  [["/" :root]
                   ["/examples" :example]]]]
      (is (= "https://example.com/examples{?perPage,sortDirection}"
            (hype/absolute-url-for request routes :example
              {:query-template-params [:per-page :sort-direction]})))))

  (testing "reitit routing"
    (let [request (ring/request "GET" "https://example.com/some/thing")
          router (reitit/router
                   [["/" :root]
                    ["/examples" :example]])]
      (is (= "https://example.com/examples{?perPage,sortDirection}"
            (hype/absolute-url-for request router :example
              {:query-template-params [:per-page :sort-direction]}))))))

(deftest absolute-url-for-uses-query-template-parameter-key-function
  (testing "bidi routing"
    (let [request (ring/request "GET" "https://example.com/some/thing")
          routes [""
                  [["/" :root]
                   ["/examples" :example]]]]
      (is (= "https://example.com/examples{?per_page,sort_direction}"
            (hype/absolute-url-for request routes :example
              {:query-template-params       [:per-page :sort-direction]
               :query-template-param-key-fn csk/->snake_case_string})))))

  (testing "reitit routing"
    (let [request (ring/request "GET" "https://example.com/some/thing")
          router (reitit/router
                   [["/" :root]
                    ["/examples" :example]])]
      (is (= "https://example.com/examples{?per_page,sort_direction}"
            (hype/absolute-url-for request router :example
              {:query-template-params       [:per-page :sort-direction]
               :query-template-param-key-fn csk/->snake_case_string}))))))

(deftest absolute-path->absolute-url-converts-absolute-path-to-url-for-request
  (let [request (ring/request "GET" "https://example.com/some/thing")
        absolute-path "/examples/123"]
    (is (= "https://example.com/examples/123"
          (hype/absolute-path->absolute-url request absolute-path)))))
