
var NavView = (function () {
    var taskslistUl
    var calendarslistUl
    var taskListFactory
    //var itemFactory

    function setTasksList(taskslist, selectedTasklist) {
        taskslistUl.innerHTML = ''
        taskslist.forEach(item => {
            var li = taskListFactory.cloneNode(true)
            li.dataset["id"] = item.id

            if (item.id == selectedTasklist)
                li.classList.add("selectedList");

            var name = li.querySelector('.taskListName')
            name.innerText = item.name
            li.onclick = () => {
                var lis = li.parentElement.querySelectorAll('li')
                for (let i = 0; i < lis.length; ++i)
                    lis[i].classList.remove('selectedList')
                li.classList.add('selectedList');
                var span = li.querySelector('.taskListName')
                var taskListString = JSON.stringify({
                    id: li.dataset["id"],
                    name: span.innerText
                });
                Native.doHapticFeedback()
                Native.selectTasklist(taskListString)
            }
            taskslistUl.appendChild(li)
        })
    }

    document.addEventListener("DOMContentLoaded", () => {
        taskslistUl = document.getElementById("taskslist");
        calendarslistUl = document.getElementById("calendarslist");
        taskListFactory = document.getElementById('taskListTemplate').content.querySelector('li');

      //  itemFactory = document.getElementById('taskTemplate').content.querySelector('li');
        Native.initialize()
    })

    return {
        setTasksList: setTasksList
    }
})()