package com.virtualdogbert

import ast.virtualdogbert.CreatedAt

@CreatedAt(name = "timestamp")
class Calculator {
    int sum = 0

    def add(int value) {
        int v = sum + value
        sum = v
    }

    def subtract(int value) {
        sum -= value
    }
}

//def c = new Calculator()
//println c.add(5)