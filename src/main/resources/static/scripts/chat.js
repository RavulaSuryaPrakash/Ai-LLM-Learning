function appendMessage(message, sender) {
    var messageElement = document.createElement('div');
    messageElement.className = 'message ' + (sender === 'user' ? 'user-message' : 'bot-message');

    var avatarElement = document.createElement('img');
    avatarElement.src = sender === 'user' ? '/images/avatar.svg' : '/images/bot.svg';
    avatarElement.className = 'avatar';

    var textElement = document.createElement('span');
    textElement.innerHTML = message.replace(/\n/g, '<br/>');

    messageElement.appendChild(avatarElement);
    messageElement.appendChild(textElement);

    if (sender === 'bot') {
        var copyButton = document.createElement('button');
        copyButton.className = 'btn btn-link';
        copyButton.innerHTML = '<i class="fas fa-copy"></i>';
        copyButton.addEventListener('click', function () {
            navigator.clipboard.writeText(message);
        });

        messageElement.appendChild(copyButton);
    }

    document.getElementById('chatBox').appendChild(messageElement);
}

$(document).ready(function () {
    $("#submitBtn").click(function () {
        let questionValue = $("#questionInput").val();
        if (!questionValue) {
            swal("Warning", "Please enter your question", "warning");
            return;
        }

        appendMessage(questionValue, 'user');
        document.getElementById('loadingSpinner').style.display = 'block';

        let model = getSelectedModel();
        let askUrl = '/' + model + '/ask';

        $.ajax({
            type: "POST",
            url: askUrl,
            data: JSON.stringify({ question: $("#questionInput").val() }),
            contentType: "application/json; charset=utf-8",
            dataType: "text",
            success: function (data) {
                document.getElementById('loadingSpinner').style.display = 'none';
                appendMessage(data, 'bot');
            },
            error: function (xhr, status, error) {
                document.getElementById('loadingSpinner').style.display = 'none';
                console.error('Error:', error);
                appendMessage("Sorry, I encountered an error. Please try again.", 'bot');
            }
        });
    });

    $("#clearBtn").click(function () {
        $("#questionInput").val('');
    });

    document.getElementById('submitBtn').disabled = true;
    document.getElementById('clearBtn').disabled = true;
});
