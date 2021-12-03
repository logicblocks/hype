TODO
====

* Add more absolute <-> relative {path,URL} converters.
* Add support for more sophisticated template parameters.
* Provide switchable router backends.
  * Auto-detect based on hype.backend.* namespaces available on classpath
    * When to auto-detect? On every call could be expensive...`
  * If single hype.backend.* namespace available on classpath, don't
    auto-detect, just use.
  * Allow specifying by altering a var
  * Allow specifying by passing a backend instance
