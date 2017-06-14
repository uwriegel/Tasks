
function setTasksList(tasksListString, selectedTasklist) {
    var jason = decodeURIComponent(atob(tasksListString))
    var tasksList =  JSON.parse(jason)
    NavView.setTasksList(tasksList, selectedTasklist)
}

function setCalendarsList(calendarsListString) {
    var calendarsList = JSON.parse(calendarsListString)
    NavView.setCalendarLists(calendarsList)
}

function initializeAccount(accountString) {
    var jason = decodeURIComponent(atob(accountString))
    var account = JSON.parse(jason)
    NavView.initializeAccount(account)
}