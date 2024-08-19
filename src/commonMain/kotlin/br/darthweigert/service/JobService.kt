package br.darthweigert.service

import korlibs.io.async.launchImmediately
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job

class JobService(private val context: CoroutineScope) {

    enum class Task {
        NONE,
        TRIANGULATE,
        PATH_FIND,
        FUNNEL
    }

    var currentJob: Job? = null
        private set

    var currentTask: Task = Task.NONE
        private set

    fun launch(task: Task, callback: suspend () -> Unit) {
        val newJob = context.launchImmediately(callback)
        currentTask = task
        currentJob = newJob
        newJob.invokeOnCompletion {
            currentTask = Task.NONE
            currentJob = null
        }
    }
}
