
var ContentView = (function () {
    var taskList
    var itemFactory

    document.addEventListener("DOMContentLoaded", () => {
        taskList = document.getElementById("tasks");
        itemFactory = document.getElementById('taskTemplate').content.querySelector('li');
        Native.initialize()
    })
})()