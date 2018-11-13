(ns mdparser-test.parser
  (:require [cljs.test :refer-macros [deftest testing is]]
            [mdparser.parser :as parser]))

(deftest test-operators
  (testing "testing basic operators"
    (is (= (parser/parse "x = y + z;")
           [:PROGRAM
            [:STATEMENT
              [:ASSIGN [:SYMBOL "x"] "=" [:add-sub [:SYMBOL "y"] "+" [:SYMBOL "z"]]]]]))
    (is (= (parser/parse "x = y - z;")
           [:PROGRAM
            [:STATEMENT
              [:ASSIGN [:SYMBOL "x"] "=" [:add-sub [:SYMBOL "y"] "-" [:SYMBOL "z"]]]]]))
    (is (= (parser/parse "x = y * z;")
           [:PROGRAM
            [:STATEMENT
              [:ASSIGN [:SYMBOL "x"] "=" [:mult-div [:SYMBOL "y"] "*" [:SYMBOL "z"]]]]]))
    (is (= (parser/parse "x = y / z;")
           [:PROGRAM
            [:STATEMENT
              [:ASSIGN [:SYMBOL "x"] "=" [:mult-div [:SYMBOL "y"] "/" [:SYMBOL "z"]]]]]))
    (is (= (parser/parse "x = y & z;")
           [:PROGRAM
            [:STATEMENT
              [:ASSIGN [:SYMBOL "x"] "=" [:bitwise [:SYMBOL "y"] "&" [:SYMBOL "z"]]]]]))
    (is (= (parser/parse "x = y | z;")
           [:PROGRAM
            [:STATEMENT
              [:ASSIGN [:SYMBOL "x"] "=" [:bitwise [:SYMBOL "y"] "|" [:SYMBOL "z"]]]]])))
  (testing "tesing conditional operators"
    (is (= (parser/parse "x = y == z;")
           [:PROGRAM
             [:STATEMENT
               [:ASSIGN [:SYMBOL "x"] "=" [:cond [:SYMBOL "y"] [:condop "=="] [:SYMBOL "z"]]]]]))
    (is (= (parser/parse "x = y != z;")
           [:PROGRAM
             [:STATEMENT
               [:ASSIGN [:SYMBOL "x"] "=" [:cond [:SYMBOL "y"] [:condop "!="] [:SYMBOL "z"]]]]]))
    (is (= (parser/parse "x = y > z;")
           [:PROGRAM
             [:STATEMENT
               [:ASSIGN [:SYMBOL "x"] "=" [:cond [:SYMBOL "y"] [:condop ">"] [:SYMBOL "z"]]]]]))
    (is (= (parser/parse "x = y < z;")
           [:PROGRAM
             [:STATEMENT
               [:ASSIGN [:SYMBOL "x"] "=" [:cond [:SYMBOL "y"] [:condop "<"] [:SYMBOL "z"]]]]]))
    (is (= (parser/parse "x = y >= z;")
           [:PROGRAM
             [:STATEMENT
               [:ASSIGN [:SYMBOL "x"] "=" [:cond [:SYMBOL "y"] [:condop ">="] [:SYMBOL "z"]]]]]))
    (is (= (parser/parse "x = y <= z;")
           [:PROGRAM
             [:STATEMENT
               [:ASSIGN [:SYMBOL "x"] "=" [:cond [:SYMBOL "y"] [:condop "<="] [:SYMBOL "z"]]]]]))
    (is (= (parser/parse "x = y && z;")
           [:PROGRAM
             [:STATEMENT
               [:ASSIGN [:SYMBOL "x"] "=" [:cond [:SYMBOL "y"] [:condop "&&"] [:SYMBOL "z"]]]]]))
    (is (= (parser/parse "x = y || z;")
           [:PROGRAM
             [:STATEMENT
               [:ASSIGN [:SYMBOL "x"] "=" [:cond [:SYMBOL "y"] [:condop "||"] [:SYMBOL "z"]]]]])))
  (testing "testing operator precedence"
    (is (= (parser/parse "x = y + z | w;")
           [:PROGRAM
             [:STATEMENT
               [:ASSIGN [:SYMBOL "x"] "=" [:bitwise [:add-sub [:SYMBOL "y"] "+" [:SYMBOL "z"]] "|" [:SYMBOL "w"]]]]]))
    (is (= (parser/parse "x = y | z + w;")
           [:PROGRAM
             [:STATEMENT
               [:ASSIGN [:SYMBOL "x"] "=" [:bitwise [:SYMBOL "y"] "|" [:add-sub [:SYMBOL "z"] "+" [:SYMBOL "w"]]]]]]))
    (is (= (parser/parse "x = y * z + w;")
           [:PROGRAM
            [:STATEMENT
              [:ASSIGN [:SYMBOL "x"] "=" [:add-sub [:mult-div [:SYMBOL "y"] "*" [:SYMBOL "z"]] "+" [:SYMBOL "w"]]]]]))
    (is (= (parser/parse "x = y + z * w;")
           [:PROGRAM
            [:STATEMENT
              [:ASSIGN [:SYMBOL "x"] "=" [:add-sub [:SYMBOL "y"] "+" [:mult-div [:SYMBOL "z"] "*" [:SYMBOL "w"]]]]]]))
    (is (= (parser/parse "x = (y + z) * w;")
           [:PROGRAM
            [:STATEMENT
              [:ASSIGN [:SYMBOL "x"] "=" [:mult-div [:add-sub [:SYMBOL "y"] "+" [:SYMBOL "z"]] "*" [:SYMBOL "w"]]]]]))
    (is (= (parser/parse "x = y < z + w;")
           [:PROGRAM
             [:STATEMENT
               [:ASSIGN [:SYMBOL "x"] "=" [:cond [:SYMBOL "y"] [:condop "<"] [:add-sub [:SYMBOL "z"] "+" [:SYMBOL "w"]]]]]]))
    (is (= (parser/parse "x = y + z > w;")
           [:PROGRAM
             [:STATEMENT
               [:ASSIGN [:SYMBOL "x"] "=" [:cond [:add-sub [:SYMBOL "y"] "+" [:SYMBOL "z"]] [:condop ">"] [:SYMBOL "w"]]]]]))
    (is (= (parser/parse "x = y + z > w * h;")
           [:PROGRAM
             [:STATEMENT
               [:ASSIGN [:SYMBOL "x"] "=" [:cond [:add-sub [:SYMBOL "y"] "+" [:SYMBOL "z"]] [:condop ">"] [:mult-div [:SYMBOL "w"] "*" [:SYMBOL "h"]]]]]]))))

(deftest test-assign-op
  (testing "assign ops"
    (is (= (parser/parse "x += y;")
           [:PROGRAM
             [:STATEMENT
               [:ASSIGN [:SYMBOL "x"] "+=" [:SYMBOL "y"]]]]))
    (is (= (parser/parse "x -= y;")
           [:PROGRAM
             [:STATEMENT
               [:ASSIGN [:SYMBOL "x"] "-=" [:SYMBOL "y"]]]]))
    (is (= (parser/parse "x *= y;")
           [:PROGRAM
             [:STATEMENT
               [:ASSIGN [:SYMBOL "x"] "*=" [:SYMBOL "y"]]]]))
    (is (= (parser/parse "x /= y;")
           [:PROGRAM
             [:STATEMENT
               [:ASSIGN [:SYMBOL "x"] "/=" [:SYMBOL "y"]]]]))
    (is (= (parser/parse "x %= y;")
           [:PROGRAM
             [:STATEMENT
               [:ASSIGN [:SYMBOL "x"] "%=" [:SYMBOL "y"]]]]))))

(deftest test-functions
  (testing "functons"
    (is (= (parser/parse "x = rand(y);")
           [:PROGRAM
             [:STATEMENT
               [:ASSIGN [:SYMBOL "x"] "=" [:funcall [:SYMBOL "rand"] [:SYMBOL "y"]]]]]))
    (is (= (parser/parse "x = pow(y, z);")
           [:PROGRAM
             [:STATEMENT
               [:ASSIGN [:SYMBOL "x"] "=" [:funcall [:SYMBOL "pow"] [:SYMBOL "y"] [:SYMBOL "z"]]]]]))
    (is (= (parser/parse "x = floor(y);")
           [:PROGRAM
             [:STATEMENT
               [:ASSIGN [:SYMBOL "x"] "=" [:funcall [:SYMBOL "floor"] [:SYMBOL "y"]]]]]))
    (is (= (parser/parse "x = max(y, z);")
           [:PROGRAM
             [:STATEMENT
               [:ASSIGN [:SYMBOL "x"] "=" [:funcall [:SYMBOL "max"] [:SYMBOL "y"] [:SYMBOL "z"]]]]]))))

(deftest test-if
  (testing "simple if"
    (is (= (parser/parse "x = if(x,y,z);")
           [:PROGRAM
             [:STATEMENT
               [:ASSIGN
                 [:SYMBOL "x"]
                 "="
                 [:if
                   [:SYMBOL "x"]
                   [:comma]
                   [:SYMBOL "y"]
                   [:comma]
                   [:SYMBOL "z"]]]]])))
  (testing "complex if"
    (is (= (parser/parse "x = if(x / 3,y = w + c; y,z = w + k; w - 8);")
           [:PROGRAM
             [:STATEMENT
               [:ASSIGN
                 [:SYMBOL "x"]
                 "="
                 [:if
                   [:mult-div [:SYMBOL "x"] "/" [:NUMBER [:INTEGER "3"]]]
                   [:comma]
                   [:ASSIGN [:SYMBOL "y"] "=" [:add-sub [:SYMBOL "w"] "+" [:SYMBOL "c"]]]
                   [:SYMBOL "y"]
                   [:comma]
                   [:ASSIGN [:SYMBOL "z"] "=" [:add-sub [:SYMBOL "w"] "+" [:SYMBOL "k"]]]
                   [:add-sub [:SYMBOL "w"] "-" [:NUMBER [:INTEGER "8"]]]]]]]))))

(deftest test-loops
  (testing "loop"
    (is (= (parser/parse "loop(5,x += 1);")
           [:PROGRAM
             [:STATEMENT
               [:loop
                 [:NUMBER [:INTEGER "5"]]
                 [:comma]
                 [:ASSIGN [:SYMBOL "x"] "+=" [:NUMBER [:INTEGER "1"]]]]]]))
    (is (= (parser/parse "loop(x,y += 1; z += y * y;);")
           [:PROGRAM
             [:STATEMENT
               [:loop
                 [:SYMBOL "x"]
                 [:comma]
                 [:ASSIGN [:SYMBOL "y"] "+=" [:NUMBER [:INTEGER "1"]]]
                 [:ASSIGN [:SYMBOL "z"] "+=" [:mult-div [:SYMBOL "y"] "*" [:SYMBOL "y"]]]]]]))))

(deftest test-numbers
  (testing "numbers"
    (testing "integers"
      (is (= (parser/parse "x = 1;")
             [:PROGRAM
               [:STATEMENT
                 [:ASSIGN [:SYMBOL "x"] "=" [:NUMBER [:INTEGER "1"]]]]]))
      (is (= (parser/parse "x = 123;")
             [:PROGRAM
               [:STATEMENT
                 [:ASSIGN [:SYMBOL "x"] "=" [:NUMBER [:INTEGER "123"]]]]]))
      (is (= (parser/parse "x = 001;")
             [:PROGRAM
               [:STATEMENT
                 [:ASSIGN [:SYMBOL "x"] "=" [:NUMBER [:INTEGER "001"]]]]])))
    (testing "decimals"
      (is (= (parser/parse "x = 1.0;")
             [:PROGRAM
               [:STATEMENT
                 [:ASSIGN [:SYMBOL "x"] "=" [:NUMBER [:DECIMAL "1" "." "0"]]]]]))
      (is (= (parser/parse "x = 12.345;")
             [:PROGRAM
               [:STATEMENT
                 [:ASSIGN [:SYMBOL "x"] "=" [:NUMBER [:DECIMAL "12" "." "345"]]]]]))
      (is (= (parser/parse "x = 1.;")
             [:PROGRAM
               [:STATEMENT
                 [:ASSIGN [:SYMBOL "x"] "=" [:NUMBER [:DECIMAL "1" "."]]]]]))
      (is (= (parser/parse "x = .1;")
             [:PROGRAM
               [:STATEMENT
                 [:ASSIGN [:SYMBOL "x"] "=" [:NUMBER [:DECIMAL "." "1"]]]]]))
      (is (= (parser/parse "x = 001.234;")
             [:PROGRAM
               [:STATEMENT
                 [:ASSIGN [:SYMBOL "x"] "=" [:NUMBER [:DECIMAL "001" "." "234"]]]]])))))

(deftest test-negative
  (testing "negative"
    (is (= (parser/parse "x = -1;")
           [:PROGRAM
             [:STATEMENT
               [:ASSIGN [:SYMBOL "x"] "=" [:NUMBER [:NEGATIVE] [:INTEGER "1"]]]]]))
    (is (= (parser/parse "x = --1;")
           [:PROGRAM
             [:STATEMENT
               [:ASSIGN [:SYMBOL "x"] "=" [:NUMBER [:NEGATIVE] [:NEGATIVE] [:INTEGER "1"]]]]]))
    (is (= (parser/parse "x = 1--1;")
           [:PROGRAM
             [:STATEMENT
               [:ASSIGN [:SYMBOL "x"] "=" [:add-sub [:NUMBER [:INTEGER "1"]] "-" [:NUMBER [:NEGATIVE] [:INTEGER "1"]]]]]]))
    (is (= (parser/parse "x = 1---1;")
           [:PROGRAM
             [:STATEMENT
               [:ASSIGN [:SYMBOL "x"] "=" [:add-sub [:NUMBER [:INTEGER "1"]] "-" [:NUMBER [:NEGATIVE] [:NEGATIVE] [:INTEGER "1"]]]]]]))
    (is (= (parser/parse "x = -y;")
           [:PROGRAM
             [:STATEMENT
               [:ASSIGN [:SYMBOL "x"] "=" [:SYMBOL [:NEGATIVE] "y"]]]]))
    (is (= (parser/parse "x = --y;")
           [:PROGRAM
             [:STATEMENT
               [:ASSIGN [:SYMBOL "x"] "=" [:SYMBOL [:NEGATIVE] [:NEGATIVE] "y"]]]]))
    (is (= (parser/parse "x = -if(x,y,z);")
           [:PROGRAM
             [:STATEMENT
               [:ASSIGN [:SYMBOL "x"] "=" [:if [:NEGATIVE] [:SYMBOL "x"] [:comma] [:SYMBOL "y"] [:comma] [:SYMBOL "z"]]]]]))
    (is (= (parser/parse "x = -(y * z);")
           [:PROGRAM
             [:STATEMENT
               [:ASSIGN [:SYMBOL "x"] "=" [:NEGATIVE] [:mult-div [:SYMBOL "y"] "*" [:SYMBOL "z"]]]]]))))

(deftest test-not
  (testing "not"
    (is (= (parser/parse "x = !y;")
           [:PROGRAM
             [:STATEMENT
               [:ASSIGN [:SYMBOL "x"] "=" [:SYMBOL [:NOT] "y"]]]]))))

(deftest test-buffers
  (testing "buffers"
    (is (= (parser/parse "x = gmegabuf(y);")
           [:PROGRAM
             [:STATEMENT
               [:ASSIGN [:SYMBOL "x"] "=" [:BUFFER "gmegabuf" [:SYMBOL "y"]]]]]))
    (is (= (parser/parse "x = megabuf(y+z);")
           [:PROGRAM
             [:STATEMENT
               [:ASSIGN [:SYMBOL "x"] "=" [:BUFFER "megabuf" [:add-sub [:SYMBOL "y"] "+" [:SYMBOL "z"]]]]]]))))

(deftest test-multi-statement
  (testing "multi line / multi on single line"
    (is (= (parser/parse "x=y;y=z;z=w;")
           [:PROGRAM
             [:STATEMENT [:ASSIGN [:SYMBOL "x"] "=" [:SYMBOL "y"]]]
             [:STATEMENT [:ASSIGN [:SYMBOL "y"] "=" [:SYMBOL "z"]]]
             [:STATEMENT [:ASSIGN [:SYMBOL "z"] "=" [:SYMBOL "w"]]]]))
    (is (= (parser/parse "x=y;
                          y=z;
                          z=w;")
           [:PROGRAM
             [:STATEMENT [:ASSIGN [:SYMBOL "x"] "=" [:SYMBOL "y"]]]
             [:STATEMENT [:ASSIGN [:SYMBOL "y"] "=" [:SYMBOL "z"]]]
             [:STATEMENT [:ASSIGN [:SYMBOL "z"] "=" [:SYMBOL "w"]]]]))
    (is (= (parser/parse "x=mid
                            varname + 3;")
           [:PROGRAM
             [:STATEMENT
               [:ASSIGN [:SYMBOL "x"] "=" [:add-sub [:SYMBOL "midvarname"] "+" [:NUMBER [:INTEGER "3"]]]]]]))))

(deftest test-memcpy
  (testing "memcpy"
    (is (= (parser/parse "memcpy(10, 5, 5);")
           [:PROGRAM
             [:STATEMENT
               [:memcpy [:NUMBER [:INTEGER "10"]] [:NUMBER [:INTEGER "5"]] [:NUMBER [:INTEGER "5"]]]]]))))
