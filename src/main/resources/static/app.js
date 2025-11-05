const handleLoginAndRegister = (type) => {
    const username = document.getElementById(`${type}-username`).value;
    const password = document.getElementById(`${type}-password`).value;
    console.log(type);
    fetch(`/${type}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({username, password})
    })
    .then(res => {
        console.log(res.body);
        if(type == 'login' && res.ok) {
            window.location.href ='/chat.html';
        }
    })
};

document.getElementById('login-form').addEventListener("submit", e => {e.preventDefault(); handleLoginAndRegister('login')})
document.getElementById('register-form').addEventListener("submit", e => {e.preventDefault(); handleLoginAndRegister('register')})


const stompClient = new StompJs.Client({
    brokerURL: 'ws://localhost:8081/chat'
});

stompClient.onConnect = frame => {
    console.log('Connected ' + frame);
    stompClient.subscribe('/topic/welcome', msg => {
        console.log('asd')
        console.log(msg.body);
    })
    stompClient.subscribe('/topic/createChat', chat => {
        console.log(chat);
    })
};


stompClient.onWebSocketError = (error) => {
    console.error('Error with websocket', error);
};

stompClient.onStompError = (frame) => {
    console.error('Broker reported error: ' + frame.headers['message']);
    console.error('Additional details: ' + frame.body);
};

stompClient.activate();