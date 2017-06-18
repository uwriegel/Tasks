
function setTasks(itemsString) {
    var jason = decodeURIComponent(atob(itemsString))
    var items =  JSON.parse(jason)
    ContentView.insertTasks(items)
}