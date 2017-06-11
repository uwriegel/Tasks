
function setTasksList(tasksListString) {




    var jason = decodeURIComponent(atob(tasksListString))
    var tasksList =  JSON.parse(jason)
    NavView.setTasksList(tasksList)
}