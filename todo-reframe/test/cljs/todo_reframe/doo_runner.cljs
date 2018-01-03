(ns todo-reframe.doo-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [todo-reframe.core-test]))

(doo-tests 'todo-reframe.core-test)

