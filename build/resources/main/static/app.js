const userMsg = document.createElement('span');
const botMsg = document.createElement('span');

let username = '';
function createChat() {
    username = document.getElementById("username").value;
    if(username.length == 0) {return;}
    stompClient.activate();

}
document.getElementById('sendMessage').addEventListener('click', e => {
    const messageContent = document.getElementById('msg-box').value;
    stompClient.publish({destination: '/app/chat', body: JSON.stringify({content: messageContent, sender: username})});
});

const stompClient = new StompJs.Client({
    brokerURL: 'ws://localhost:8081/chat'
});
stompClient.onConnect = frame => {
    console.log('Connected ' + frame);
    document.getElementsByClassName('container')[0].style.display = 'flex';
    document.getElementById('username-form').style.display = 'none';
    
    stompClient.subscribe('/topic/message', val => {
        const message = JSON.parse(val.body).content;
        const msgel = document.createElement('span');
        msgel.innerText = "BOT: " +message;
        document.getElementById('messages').appendChild(msgel);
    })
};
stompClient.onWebSocketError = (error) => {
    console.error('Error with websocket', error);
};

stompClient.onStompError = (frame) => {
    console.error('Broker reported error: ' + frame.headers['message']);
    console.error('Additional details: ' + frame.body);
};