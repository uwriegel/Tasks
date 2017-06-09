
function setTasks(tasksString) {
    //Native.affe(tasksString)

    var jason = decodeURIComponent(atob(tasksString))
    var tasks =  JSON.parse(jason)
    ContentView.insertTasks(tasks)
}