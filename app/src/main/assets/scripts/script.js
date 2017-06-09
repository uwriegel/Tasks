
var ContentView = (function () {
    var taskList
    var itemFactory

    function insertTasks(tasks) {
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
            task.due.setHours(12);
            task.due.setMinutes(0);
            task.due.setUTCSeconds(0, 0);
            if (lis.some(function (li) {
                liSucc = li;
                let date = new Date(li.dataset["due"]);
                date.setHours(12);
                date.setMinutes(0);
                date.setUTCSeconds(0, 0);
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
        var hasDue = due.getFullYear() < 3000
        if (hasDue) {
            li.querySelector('.taskDayOfWeek').innerText = getDayOfWeek(due);
            li.querySelector('.taskDateOnly').innerText = due.toLocaleDateString("de", { month: "2-digit", day: "2-digit" })
        }
        //li.dataset["key"] = task.key
        if (hasDue)
            li.dataset["due"] = due.toISOString()
        var taskSymbol = li.querySelector('.taskSymbol')
        if (hasDue) {
            due.setHours(12)
            due.setMinutes(0)
            due.setUTCSeconds(0, 0)
            var dateToday = new Date()
            dateToday.setHours(12)
            dateToday.setMinutes(0)
            dateToday.setUTCSeconds(0, 0)
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