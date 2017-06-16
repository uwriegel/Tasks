
var NavView = (function () {
    var taskslistUl
    var calendarslistUl
    var taskListFactory
    var calendarListFactory
    var accountDisplayName
    var accountName
    var icon
    var noAvatar
    var marker

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

    function setCalendarLists(calendarLists) {
        calendarslistUl.innerHTML = ''
        var lis = calendarLists.map(calendarList => {
            var li = calendarListFactory.cloneNode(true);
            li.dataset["id"] = calendarList.id;
            var name = li.querySelector('.calendarName');
            name.innerText = calendarList.name;
            var account = li.querySelector('.calendarAccount');
            account.innerText = calendarList.account;
            if (calendarList.isSelected)
                li.classList.add("calendarSelected");
            li.onclick = evt => {
                if (!li.classList.contains("calendarSelected"))
                    li.classList.add("calendarSelected");
                else
                    li.classList.remove("calendarSelected");
                let lis = Array.from(calendarslistUl.querySelectorAll('li.calendarSelected'));
                let calendarIds = lis.map(li => {
                    return li.dataset['id']
                })
                Native.doHapticFeedback()
                Native.selectCalendarsList(JSON.stringify(calendarIds))
            };
            return li;
        }).forEach(li => {
            calendarslistUl.appendChild(li);
        });
    }

    function initializeAccount(account) {
        marker.classList.remove("opened")
        accountName.innerText = account.name
        accountDisplayName.innerText = account.displayName
        icon.src = account.photoUrl ? account.photoUrl : noAvatar
    }

    document.addEventListener("DOMContentLoaded", () => {
        taskslistUl = document.getElementById("taskslist");
        calendarslistUl = document.getElementById("calendarslist");
        taskListFactory = document.getElementById('taskListTemplate').content.querySelector('li');
        calendarListFactory = document.getElementById('calendarListTemplate').content.querySelector('li');
        accountDisplayName = document.getElementById("accountDisplayName")
        accountName = document.getElementById("accountName")
        icon = document.getElementById("icon")
        var header = document.getElementById("drawerHeader")
        marker = document.getElementById("marker")
        header.onclick = evt => {
            marker.classList.add("opened")
            Native.doHapticFeedback()
            Native.chooseAccount()
        }

        var queries = extractQuery(location.search.substring(1))
        accountName.innerText = queries.name
        accountDisplayName.innerText = queries.displayName
        noAvatar = icon.src
        if (queries.photo)
            icon.src = queries.photo

        Native.initialize()
    })

    return {
        setTasksList: setTasksList,
        setCalendarLists: setCalendarLists,
        initializeAccount: initializeAccount
    }
})()

