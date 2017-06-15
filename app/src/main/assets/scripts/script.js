
var ContentView = (function () {
    var taskList
    var itemFactory

    function insertTasks(tasks) {
        clear()

        var taskWithDue = tasks.find(t => t.due != 0)
        if (taskWithDue)
        {
            var index = tasks.indexOf(taskWithDue)
            var tasksWithoutDue = tasks.splice(0, index)
            tasks = tasks.concat(tasksWithoutDue)
        }

        tasks.forEach(t => {
            insertTask(t)
        })
    }

    function insertTask(task, sorted) {
        var li = itemFactory.cloneNode(true)
        setLi(li, task)
        if (sorted) {
            var lis = Array.from(taskList.getElementsByTagName('li'));
            var liSucc;
            if (task.due == 0)
                taskList.appendChild(li);
            else if (lis.some(function (li) {
                liSucc = li;
                let date = new Date(li.dataset["due"]);
                return task.due <= date;
            }))
                taskList.insertBefore(li, liSucc);
            else
                taskList.appendChild(li);
        }
        else
            taskList.appendChild(li);
    }

    function setLi(li, task) {
        li.querySelector('.taskTitle').innerText = task.title
        li.querySelector('.taskNote').innerText = task.notes ? task.notes : null

        var due = new Date(task.due)
        var hasDue = task.due != 0
        if (hasDue) {
            li.querySelector('.taskDayOfWeek').innerText = getDayOfWeek(due);
            li.querySelector('.taskDateOnly').innerText = due.toLocaleDateString("de", { month: "2-digit", day: "2-digit" })
        }
        //li.dataset["key"] = task.key
        if (hasDue)
            li.dataset["due"] = due.toISOString()
        var taskSymbol = li.querySelector('.taskSymbol')
        if (hasDue) {
            var dateToday = new Date()
            var dayDiff = (due.getTime() - dateToday.getTime()) / (1000 * 3600 * 24)
            if (due <= dateToday) {
                if (dayDiff > -1 && dayDiff <= 0) {
                    taskSymbol.innerText = "H";
                    taskSymbol.style.backgroundColor = "#e6ef00";
                }
                else
                    taskSymbol.innerText = "!";
            }
            else if (dayDiff > 0 && dayDiff <= (7 - dateToday.getDay())) {
                taskSymbol.innerText = due.toLocaleDateString("de", { weekday: "short" });
                taskSymbol.style.backgroundColor = "#4CAF50";
            }
            else {
                taskSymbol.innerText = due.toLocaleDateString("de", { weekday: "short" });
                taskSymbol.style.backgroundColor = "#3F51B5";
            }
        }
        else {
            taskSymbol.style.backgroundColor = "lightgray";
            taskSymbol.innerText = "o";
        }
    }

    function getDayOfWeek(date) {
        var days = ['So', 'Mo', 'Di', 'Mi', 'Do', 'Fr', 'Sa'];
        return days[date.getDay()];
    }

    function clear() {
        taskList.innerHTML = '';
    }

    document.addEventListener("DOMContentLoaded", () => {
        taskList = document.getElementById("tasks");
        itemFactory = document.getElementById('taskTemplate').content.querySelector('li');
        Native.initialize()
    })

    return {
        insertTasks: insertTasks,
        insertTask: insertTask
    }
})()