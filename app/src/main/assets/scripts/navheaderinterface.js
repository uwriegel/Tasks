
function setTasksList(tasksListString, selectedTasklist) {
    var jason = decodeURIComponent(atob(tasksListString))
    var tasksList =  JSON.parse(jason)
    NavView.setTasksList(tasksList, selectedTasklist)
}

function setCalendarsList(calendarsListString) {
    var calendarsList = JSON.parse(calendarsListString)
    NavView.setCalendarLists(calendarsList)
}