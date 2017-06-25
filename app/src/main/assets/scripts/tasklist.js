
var ContentView = (function () {
    var taskList
    var itemFactory
    var dismissUndoFactory
    const DRAWER_TOUCH_MARGIN = 20

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
            var lis = Array.from(taskList.getElementsByTagName('li'))
            var liSucc
            if (task.due == 0)
                taskList.appendChild(li)
            else if (lis.some(function (li) {
                liSucc = li
                let date = new Date(li.dataset["due"])
                return task.due <= date
            }))
                taskList.insertBefore(li, liSucc)
            else
                taskList.appendChild(li)
        }
        else
            taskList.appendChild(li)
    }

    function insertCalendarItem(calendarItem) {
        var li = itemFactory.cloneNode(true)
        if (!setCalendarLi(li, calendarItem))
            return
        var lis = Array.from(taskList.getElementsByTagName('li'))
        var liSucc
        var date = new Date(calendarItem.due)
        if (lis.some(function (li) {
            liSucc = li
            let compareDate = new Date(li.dataset["due"])
            return date <= compareDate
        }))
            taskList.insertBefore(li, liSucc)
        else
            taskList.appendChild(li)
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

        li.dataset["id"] = task.id
        if (hasDue)
            li.dataset["due"] = due.toISOString()
        var taskSymbol = li.querySelector('.taskSymbol')
        if (hasDue) {
            var dateToday = new Date()
            var dayDiff = (due.getTime() - dateToday.getTime()) / (1000 * 3600 * 24)
            if (due <= dateToday) {
                if (dayDiff > -1 && dayDiff <= 0) {
                    taskSymbol.innerText = "H"
                    taskSymbol.style.backgroundColor = "#e6ef00"
                }
                else
                    taskSymbol.innerText = "!"
            }
            else if (dayDiff > 0 && dayDiff <= (7 - dateToday.getDay())) {
                taskSymbol.innerText = due.toLocaleDateString("de", { weekday: "short" })
                taskSymbol.style.backgroundColor = "#4CAF50"
            }
            else {
                taskSymbol.innerText = due.toLocaleDateString("de", { weekday: "short" })
                taskSymbol.style.backgroundColor = "#3F51B5"
            }
        }
        else {
            taskSymbol.style.backgroundColor = "lightgray"
            taskSymbol.innerText = "o"
        }
    }

    function setCalendarLi(li, calendarItem) {
        li.querySelector('.taskTitle').innerText = calendarItem.title
        var due = new Date(calendarItem.due)
        li.querySelector('.taskDayOfWeek').innerText = getDayOfWeek(due)
        li.querySelector('.taskDateOnly').innerText = due.toLocaleDateString("de", { month: "2-digit", day: "2-digit" })
        var dateToday = new Date()
        li.dataset["eventId"] = calendarItem.id
        li.dataset["due"] = due.toISOString()
        var taskSymbol = li.querySelector('.taskSymbol')
        var i = taskSymbol.querySelector("i")
        i.classList.remove("hidden")
        var dayDiff = (due.getTime() - dateToday.getTime()) / (1000 * 3600 * 24)
        if (due <= dateToday) {
            if (dayDiff > -1 && dayDiff <= 0) {
                taskSymbol.style.backgroundColor = "#e6ef00"
                taskSymbol.style.color = "#3F51B5"
            }
            else
                return false
        }
        else if (dayDiff > 0 && dayDiff <= (7 - dateToday.getDay()))
            taskSymbol.style.backgroundColor = "#4CAF50"
        else
            taskSymbol.style.backgroundColor = "#3F51B5"
        return true
    }

    function getDayOfWeek(date) {
        var days = ['So', 'Mo', 'Di', 'Mi', 'Do', 'Fr', 'Sa']
        return days[date.getDay()]
    }

    function clear() {
        taskList.innerHTML = ''
    }

    function addClick() {
        var inClick
        taskList.onclick = evt => {
            if (inClick)
                return
            var li = evt.target.closest('li')
            if (li.classList.contains('undoContainer'))
                return
            inClick = true
            Native.doHapticFeedback()
            var canvas = document.createElement("canvas")
            canvas.width = li.offsetWidth
            canvas.height = li.offsetHeight
            var context = canvas.getContext("2d")
            var lastindex = 1
            var dateNow = new Date().getTime()
            window.requestAnimationFrame(function resizeChecking() {
                var date = new Date().getTime()
                var index = Math.round((date - dateNow) / 40)
                if (index == lastindex) {
                    window.requestAnimationFrame(resizeChecking)
                    return
                }
                lastindex = index
                if (!drawCircle(index))
                    return
                window.requestAnimationFrame(resizeChecking)
            })
            var x = evt.clientX
            var y = evt.pageY - li.offsetTop + taskList.scrollTop
            var centerX = x
            var centerY = y
            var actionExecuted
            function drawCircle(index) {
                var alpha = index / 10
                if (!actionExecuted && alpha > 0.6) {
                    var key = li.dataset["id"]
                    if (key) {
//                        dialog = TaskDialog(key)
//                        actionExecuted = true
                    }
                    else {
                        Native.showEvent(li.dataset["eventId"])
                        actionExecuted = true
                    }
                }
                if (alpha > 1) {
                    inClick = false
                    li.style.background = ""
                    return false
                }
                var radius = (canvas.height / 2 - 6) + alpha * (canvas.width / 2 - (canvas.height / 2 - 6))
                context.clearRect(0, 0, canvas.width, canvas.height)
                context.beginPath()
                context.arc(centerX, centerY, radius, 0, 2 * Math.PI, false)
                context.fillStyle = '#efefff'
                context.globalAlpha = 1 - alpha
                context.fill()
                var url = canvas.toDataURL()
                li.style.background = `url(${url})`
                return true
            }
        }
    }

    function addDismissHandling() {
        taskList.addEventListener('touchstart', evt => {
            if (evt.touches.length != 1)
                return
            var initialX = evt.touches[0].clientX
            var initialY = evt.touches[0].clientY
            if (initialX <= DRAWER_TOUCH_MARGIN)
                return
            var moving
            var targetRow = evt.touches[0].target.closest('.taskRow')
            if (!targetRow)
                return
            var li = (targetRow.closest('li'))
            var diff
            var lastDiff
            taskList.addEventListener('touchmove', touchmove, true)
            taskList.addEventListener('touchend', touchend, true)
            function touchmove(evt) {
                if (diff)
                    lastDiff = diff
                diff = evt.touches[0].clientX - initialX
                if (!moving) {
                    moving = true
                    var diffY = Math.abs(evt.touches[0].clientY - initialY)
                    if (diff < 10 || Math.abs(diff) < diffY * 2) {
                        taskList.removeEventListener('touchmove', touchmove, true)
                        taskList.removeEventListener('touchend', touchend, true)
                        return
                    }
                    targetRow.style.backgroundColor = "white"
                    li.style.backgroundColor = "darkcyan"
                }
                if (diff >= 0)
                    targetRow.style.left = diff + 'px'
                evt.preventDefault()
                evt.stopPropagation()
            }
            function touchend(evt) {
                if (!moving)
                    return
                taskList.removeEventListener('touchmove', touchmove, true)
                taskList.removeEventListener('touchend', touchend, true)
                targetRow.style.transition = 'left 0.3s'
                var dismiss = diff - lastDiff > 15 || (diff - lastDiff > 0 && diff > (window.innerWidth / 2))
                var left = dismiss ? window.innerWidth : 0
                if (dismiss)
                    li.style.height = `${li.offsetHeight}px`
                targetRow.style.left = `${left}px`
                targetRow.addEventListener("transitionend", function transitionend(evt) {
                    setTimeout(() => {
                        targetRow.removeEventListener("transitionend", transitionend)
                        if (dismiss) {
                            var dismissUndo = (dismissUndoFactory.cloneNode(true))
                            li.removeChild(targetRow)
                            li.classList.add("undoContainer")
                            li.appendChild(dismissUndo)
                            var undoTimer
                            dismissUndo.onclick = () => {
                                Native.deleteTask(li.dataset["id"], false)
                                clearTimeout(undoTimer);
                                li.removeChild(dismissUndo);
                                li.classList.remove("undoContainer");
                                li.appendChild(targetRow);
                                targetRow.style.transition = 'left 0.3s';
                                targetRow.addEventListener("transitionend", function transitionend(evt) {
                                    targetRow.style.transition = '';
                                });
                                setTimeout(function () {
                                    targetRow.style.left = '0px';
                                }, 20);
                            }

                            Native.deleteTask(li.dataset["id"], true)

                            undoTimer = setTimeout(() => {
                                try {
                                    //yield Google.closeTask(taskId);
                                    li.style.height = '0px'
                                }
                                catch (err) {
                                    var ge = err
                                    alert(ge.toString())
                                }
                            }, 3000)
                            li.addEventListener("transitionend", function transitionend(evt) {
                                var transitionEvent = evt
                                if (transitionEvent.propertyName == 'height') {
                                    li.removeEventListener("transitionend", transitionend)
                                    li.parentNode.removeChild(li)
                                }
                            })
                        }
                        else {
                            targetRow.style.transition = ''
                            targetRow.style.backgroundColor = ""
                            li.style.backgroundColor = ""
                        }
                        evt.preventDefault()
                        evt.stopPropagation()
                    }, 10)
                })
            }
        }, true)
    }

    document.addEventListener("DOMContentLoaded", () => {
        taskList = document.getElementById("tasks")
        itemFactory = document.getElementById('taskTemplate').content.querySelector('li')
        dismissUndoFactory = document.getElementById('dismissUndoTemplate').content.querySelector('span')
        addDismissHandling()
        addClick()
        Native.initialize()
    })

    return {
        insertTasks: insertTasks,
        insertTask: insertTask
    }
})()