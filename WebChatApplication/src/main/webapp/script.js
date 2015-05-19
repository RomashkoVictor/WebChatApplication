'use strict';

(function () {
    var username = "";
    var choisenMessage = null;
    var choiseEditMessage = false;

    window.addEventListener('DOMContentLoaded', run);
    window.addEventListener('resize', onResizeDocument);

    var newMessage = function (text, time) {
        return {
            username: username,
            text: text,
            time: time,
            edited: false,
            deleted: false,
            id: ""
        };
    }

    var appState = {
        mainUrl: 'chat',
        messages: [],
        token: '0'
    };

    function run(e) {
        var buttonSend = document.getElementById('send-button');
        var textArea = document.getElementById('Entered-Text');
        var name = document.getElementById('InputName');

        buttonSend.addEventListener('click', onButtonClick);
        textArea.addEventListener('keypress', onTextInput);
        name.addEventListener('focusout', onNameInput);
        document.getElementsByClassName('glyphicon')[0].addEventListener('click', onEdit);
        document.getElementsByClassName('glyphicon')[1].addEventListener('click', onRemove);
        editable(false);
        onResizeDocument();
        poll();
        //restore();
        //window.setInterval(restore, 500);
    }

    function onResizeDocument(e) {
        var allHeight = document.getElementsByTagName('html')[0].clientHeight;
        var inputHeight = document.getElementById('Entered-Text').clientHeight;
        var navbarHeight = document.getElementsByClassName('navbar')[0].clientHeight;
        var merges = 90;
        var height = allHeight - inputHeight - navbarHeight - merges;
        height = height.toString() + 'px';
        document.getElementsByClassName('my-table')[0].style.height = height;
    }

    function takeDate() {
        var date = new Date();
        var time = ('0' + date.getDate()).slice(-2) + '.' + ('0' + (date.getMonth() + 1)).slice(-2) + "<br>";
        time += ('0' + date.getHours()).slice(-2) + ':' + ('0' + date.getMinutes()).slice(-2);
        time += ':' + ('0' + date.getSeconds()).slice(-2);
        return time;
    }

    function onNameInput(e) {
        var name = document.getElementById('InputName');
        $('#InputName').popover('hide');
        if (choisenMessage != null) {
            choisenMessage.classList.remove('myMessage');
            choisenMessage = null;
            choiseEditMessage = false;
            editable(false);
        }
        if (!/\S/.test(name.value)) {
            name.value = '';
            username = '';
            // store(taskList);
            return;
        }
        username = name.value;
        // store(taskList);
    }

    function onTextInput(e) {
        var key = e.keyCode;
        if (key == 13) {
            if (e.shiftKey) {
                var textContainer = document.getElementById('Entered-Text');
                var caretPos = getCaretPosition(textContainer);
                textContainer.value = textContainer.value.slice(0, caretPos) + '\n' + textContainer.value.slice(caretPos);
                setCaretPosition(textContainer, caretPos + 1);
            }
            else {
                onButtonClick();
            }
            e.preventDefault();
        }
    }

    function getCaretPosition(textarea) {
        var caretPos = 0;
        if (document.selection) {
            var select = document.selection.createRange();
            select.moveStart('character', -textarea.value.length);
            caretPos = select.text.length;
        }
        else if (textarea.selectionStart || textarea.selectionStart == '0')
            caretPos = textarea.selectionStart;
        return caretPos;
    }

    function setCaretPosition(textarea, pos) {
        if (textarea.setSelectionRange) {
            textarea.focus();
            textarea.setSelectionRange(pos, pos);
        }
        else if (textarea.createTextRange) {
            var range = textarea.createTextRange();
            range.collapse(true);
            range.moveEnd('character', pos);
            range.moveStart('character', pos);
            range.select();
        }
    }

    function choiseMessage(e) {
        if (choiseEditMessage == true)
            return;
        var idMessage = this.getAttribute('id');
        for (var i = 0; i < appState.messages.length; i++)
            if (idMessage == appState.messages[i].id) {
                if (appState.messages[i].username != username)
                    return;
                if (appState.messages[i].deleted == true)
                    return;
            }
        if (choisenMessage == null) {
            choisenMessage = this;
            choisenMessage.classList.add('myMessage');
            editable(true)
        }
        else {
            choisenMessage.classList.remove('myMessage');
            if (choisenMessage != this) {
                choisenMessage = this;
                choisenMessage.classList.add('myMessage');
                editable(true);
            }
            else {
                choisenMessage = null;
                editable(false);
                choiseEditMessage = false;
            }
        }
    }

    function editable(obj) {
        if (obj == true) {
            document.getElementsByClassName('glyphicon')[0].style.display = 'inline-block';
            document.getElementsByClassName('glyphicon')[1].style.display = 'inline-block';
        }
        else {
            document.getElementsByClassName('glyphicon')[0].style.display = 'none';
            document.getElementsByClassName('glyphicon')[1].style.display = 'none';
        }
    }

    function addMessage(message) {
        var scrolling = document.getElementsByClassName('my-table')[0];
        var scrollIsEnd = false;
        var heightTable = scrolling.clientHeight;
        if (scrolling.scrollHeight - scrolling.scrollTop <= heightTable + 50)
            scrollIsEnd = true;
        var table = document.getElementById('talk');
        var row = table.insertRow(-1);
        createRowValues(row, message);
        appState.messages.push(message);
        if (scrollIsEnd == true)
            scrolling.scrollTop = scrolling.scrollHeight;
    }

    function createRowValues(row, message) {
        var tdTime = document.createElement('td');
        tdTime.classList.add('col-date');
        tdTime.innerHTML = message.time;
        row.appendChild(tdTime);

        var h4 = document.createElement('h4');
        h4.classList.add('list-group-item-heading');
        h4.innerHTML = message.username;

        var pDiv = document.createElement('div');
        pDiv.classList.add('wrap');
        pDiv.innerText = message.text;

        var tdDiv = document.createElement('div');
        tdDiv.classList.add('list-group-item');
        tdDiv.appendChild(h4);
        tdDiv.appendChild(pDiv);

        var tdMessage = document.createElement('td');
        tdMessage.classList.add('col-text');
        tdMessage.appendChild(tdDiv);
        row.appendChild(tdMessage);

        if (message.edited == true) {
            h4.innerHTML = message.username + ' ' + '<i class="glyphicon glyphicon-pencil iconEditedDeleted"></i>';
        }
        if (message.deleted == true) {
            h4.innerHTML = message.username + ' ' + '<i class="glyphicon glyphicon-trash iconEditedDeleted"></i>';
        }

        row.setAttribute('id', message.id);
        row.addEventListener('click', choiseMessage);
    }

    function connectedToServer(e) {
        var label = document.getElementById("ConnectedServer");
        if (e == true) {
            if (label.classList.contains('label-danger')) {
                label.classList.remove('label-danger');
                label.classList.add('label-success');
                label.textContent = "Connected";
            }
        }
        else {
            if (label.classList.contains('label-success')) {
                label.classList.remove('label-success');
                label.classList.add('label-danger');
                label.textContent = "Disconnected";
            }
        }
    }

    function onButtonClick(e) {
        var name = document.getElementById('InputName');
        while (username == null || username.length === 0) {
            $('#InputName').popover('show');
            name.focus();
            return;
        }
        var text = document.getElementById('Entered-Text');
        if (!/\S/.test(text.value)) {
            text.value = '';
            return;
        }
        var dateAndTime = takeDate();
        var message = newMessage(text.value, dateAndTime);
        if (choisenMessage == null || choiseEditMessage != true) {
            sendMessage(message);
        }
        else {//edit
            var idMessage = choisenMessage.getAttribute('id');
            for (var i = 0; i < appState.messages.length; i++)
                if (idMessage == appState.messages[i].id) {
                    appState.messages[i].text = text.value;
                    appState.messages[i].edited = true;
                    editMessage(appState.messages[i]);
                    choisenMessage.classList.remove('myMessage');
                    choisenMessage = null;
                    choiseEditMessage = false;

                    break;
                }

        }
        text.value = '';
    }

    function onEdit(e) {
        var idMessage = choisenMessage.getAttribute('id');
        for (var i = 0; i < appState.messages.length; i++)
            if (idMessage == appState.messages[i].id) {
                if (appState.messages[i].deleted == true) {
                    choisenMessage.classList.remove('myMessage');
                    editable(false);
                    return;
                }
                editable(false);
                choiseEditMessage = true;
                var text = document.getElementById("Entered-Text");
                text.value = choisenMessage.childNodes[1].childNodes[0].childNodes[1].innerText;
                break;
            }
    }

    function onRemove(e) {
        var idMessage = choisenMessage.getAttribute('id');
        for (var i = 0; i < appState.messages.length; i++)
            if (idMessage == appState.messages[i].id) {
                choisenMessage.classList.remove('myMessage');
                if (appState.messages[i].deleted == true) {
                    choisenMessage.classList.remove('myMessage');
                    editable(false);
                    return;
                }
                appState.messages[i].text = '';
                appState.messages[i].deleted = true;
                appState.messages[i].edited = true;
                editable(false);
                choisenMessage = null;
                deleteMessage(idMessage);
                break;
            }
    }

    function findAndReplace(message) {
        for (var i = 0; i < appState.messages.length; i++) {
            if (appState.messages[i].id == message.id) {
                appState.messages[i] = message;
                update(message);
                return;
            }
        }
        addMessage(message);
    }

    function update(message) {
        var mes = document.getElementById(message.id);
        if (message.edited == true) {
            mes.childNodes[1].childNodes[0].childNodes[0].innerHTML = message.username + ' ' + '<i class="glyphicon glyphicon-pencil iconEditedDeleted"></i>';
        }
        if (message.deleted == true) {
            mes.childNodes[1].childNodes[0].childNodes[0].innerHTML = message.username + ' ' + '<i class="glyphicon glyphicon-trash iconEditedDeleted"></i>';
        }
        mes.childNodes[1].childNodes[0].childNodes[1].innerText = message.text;
    }

    function updateAll(response) {
        for (var i = 0; i < response.length; i++) {
            findAndReplace(response[i]);
        }
    }

    function sendMessage(message) {
        post(appState.mainUrl, JSON.stringify(message), function () {
        });
    }

    function editMessage(message) {
        put(appState.mainUrl, JSON.stringify(message), function () {
        });
    }

    function deleteMessage(id) {
        del(appState.mainUrl + '?id=' + id, function () {
        });
    }

//    function restore(continueWith) {
//	var url = appState.mainUrl + '?token=' + appState.token;
//
//	get(url, function(responseText) {
//		console.assert(responseText != null);
//
//		var response = JSON.parse(responseText);
//
//		appState.token = response.token;
//		updateAll(response.messages);
//		//output(appState);
//
//		continueWith && continueWith();
//	});
//}

    function poll() {
        $.ajax({
            url: appState.mainUrl + '?token=' + appState.token,
            success: function (response) {
                // var response = JSON.parse(data);
                appState.token = response.token;
                updateAll(response.messages);
                connectedToServer(true);
            },
            error: function (e) {
                if (e.statusText == "timeout")
                    connectedToServer(true);
                else
                    connectedToServer(false);
            },
            dataType: "json",
            complete: poll,
            timeout: 30000
        });
    }

//    function get(url, continueWith, continueWithError) {
//	ajax('GET', url, null, continueWith, continueWithError);
//}

    function post(url, data, continueWith, continueWithError) {
        ajax('POST', url, data, continueWith, continueWithError);
    }

    function put(url, data, continueWith, continueWithError) {
        ajax('PUT', url, data, continueWith, continueWithError);
    }

    function del(url, continueWith, continueWithError) {
        ajax('DELETE', url, null, continueWith, continueWithError);
    }

    function defaultErrorHandler(message) {
        connectedToServer(false);
        if (message == 400)
            window.location.href="/chat/resources/jsp/error400.jsp";
        if (message == 500)
            window.location.href="/chat/resources/jsp/error500.jsp";
    }

    function isError(text) {
        if (text == "")
            return false;

        try {
            var obj = JSON.parse(text);
        } catch (ex) {
            return true;
        }

        return !!obj.error;
    }

    function ajax(method, url, data, continueWith, continueWithError) {
        var xhr = new XMLHttpRequest();

        continueWithError = defaultErrorHandler;
        xhr.open(method || 'GET', url, true);

        xhr.onload = function () {
            if (xhr.readyState !== 4)
                return;

            if (xhr.status != 200) {
                continueWithError(xhr.status);
                return;
            }
            else
                connectedToServer(true);

            if (isError(xhr.responseText)) {
                continueWithError(xhr.responseText);
                return;
            }

            continueWith(xhr.responseText);
        };

        xhr.ontimeout = function () {
            connectedToServer(false);
            continueWithError('Server timed out !');
        }

        xhr.onerror = function (e) {
            connectedToServer(false);
            continueWithError();
        };

        xhr.send(data);
    }
}

()
)