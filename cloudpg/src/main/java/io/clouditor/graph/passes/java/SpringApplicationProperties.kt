package io.clouditor.graph.passes.java

class SpringApplicationProperties(var spring: Spring)

class Spring(var datasource: DataSource)

class DataSource(var url: String, var username: String, var password: String)
