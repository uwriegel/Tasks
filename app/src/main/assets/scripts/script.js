
var ContentView = (function () {
    var taskList
    var itemFactory

    function insertTasks(items) {
        clear()

        items.tasks.forEach(t => {
            insertTask(t)
        })

        items.calendarItems.forEach(c => {
            insertCalendarItem(c)
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

    function insertCalendarItem(calendarItem) {
        var li = itemFactory.cloneNode(true);
        if (!setCalendarLi(li, calendarItem))
            return;
        var lis = Array.from(taskList.getElementsByTagName('li'));
        var liSucc;
        var date = new Date(calendarItem.due);
        if (lis.some(function (li) {
            liSucc = li;
            let compareDate = new Date(li.dataset["due"]);
            return date <= compareDate;
        }))
            taskList.insertBefore(li, liSucc);
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

    function setCalendarLi(li, calendarItem) {
        li.querySelector('.taskTitle').innerText = calendarItem.title;
        var due = new Date(calendarItem.due)
        li.querySelector('.taskDayOfWeek').innerText = getDayOfWeek(due);
        li.querySelector('.taskDateOnly').innerText = due.toLocaleDateString("de", { month: "2-digit", day: "2-digit" });
        var dateToday = new Date();
        li.dataset["eventId"] = calendarItem.Id;
        li.dataset["due"] = due.toISOString();
        var taskSymbol = li.querySelector('.taskSymbol');
        var i = taskSymbol.querySelector("i");
        i.classList.remove("hidden");
        var dayDiff = (due.getTime() - dateToday.getTime()) / (1000 * 3600 * 24);
        if (due <= dateToday) {
            if (dayDiff > -1 && dayDiff <= 0) {
                taskSymbol.style.backgroundColor = "#e6ef00";
                taskSymbol.style.color = "#3F51B5";
            }
            else
                return false;
        }
        else if (dayDiff > 0 && dayDiff <= (7 - dateToday.getDay()))
            taskSymbol.style.backgroundColor = "#4CAF50";
        else
            taskSymbol.style.backgroundColor = "#3F51B5";
        return true;
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