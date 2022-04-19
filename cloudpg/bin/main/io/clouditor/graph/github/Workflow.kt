package io.clouditor.graph.github

import com.fasterxml.jackson.annotation.JsonProperty

data class Workflow(var name: String, var jobs: Map<String, Job>, var on: On)

data class Job(
    var steps: List<Step>,
    @JsonProperty("runs-on") var runsOn: String?,
    var needs: Any?,
    var `if`: String?,
    var name: String?
)

data class Step(
    var name: String?,
    var run: String?,
    var uses: String?,
    var with: Map<String, String>?,
    var `if`: String?,
    var id: String?,
    var env: Map<String, String>?,
    @JsonProperty("working-directory") var workingDirectory: String?
)

data class On(
    var push: Map<String, List<String>>?,
    @JsonProperty("pull_request") var pullRequest: Map<String, List<String>>?,
    @JsonProperty("workflow_dispatch") var workflowDispatch: Any?
)
