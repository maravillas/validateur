## Changes between Validateur 1.2.0 and 1.3.0

### clojurewerkz.support 0.7.0

[ClojureWerkz Support](https://github.com/clojurewerkz/support) upgraded to `v0.7.0`.

### Clojure 1.4

Validateur now depends on `org.clojure/clojure` version `1.4.0`. It is still compatible with Clojure 1.3 and if your `project.clj` depends
on 1.3, it will be used, but 1.4 is the default now.

We encourage all users to upgrade to 1.4, it is a drop-in replacement for the majority of projects out there.


## Changes between Validateur 1.1.0 and 1.2.0

### Doc strings

Built-in validation functions now have doc strings.

### clojurewerkz.support 0.6.0

[ClojureWerkz Support](https://github.com/clojurewerkz/support) upgraded to `v0.6.0`.


## Changes between Validateur 1.0.0 and 1.1.0

### Nested attributes support

`presence-of` and other DSL functions now support nested attributes (think `clojure.core/get-in`),
for example:

``` clojure
(validation-set
  (presence-of :email)
  (presence-of [:address :street])
  (presence-of [:card :cvc]))
```


### clojurewerkz.support dependency

Validateur now depends on [ClojureWerkz Support](https://github.com/clojurewerkz/support).


### Leiningen 2

Validateur now uses [Leiningen 2](https://github.com/technomancy/leiningen/wiki/Upgrading).
