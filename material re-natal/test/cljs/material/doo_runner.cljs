(ns material.doo-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [material.core-test]))

(doo-tests 'material.core-test)

