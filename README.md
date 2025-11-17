# Chat-bot

## Installation
Clone the repo. 
```
docker build -t chatbot-task .
docker-compose up --build
```

## Base URL
`http://localhost:8081/api/v1`

## API documentation

### Endpoints

#### GET /getConfig
Returns a json with the current chatbot flow
Response:
```json
{
    "name": "Chat Bot",
    "start_block_id": "greeting",
    "blocks": [
        {"id": "greeting", "type": "send_message", "message": "Hello, what are you looking for today?", "nextBlock": "wait_response_1"},
        {"id": "wait_response_1", "type": "wait_response", "nextBlock": "recognize_intent_1"},
        {
            "id": "recognize_intent_1",
            "type": "recognize_intent",
            "intents": ["weather", "time"],
            "branches": {
                "weather": "weather_response",
                "time": "time_response"
            }
        },
        {"id": "weather_response", "type": "send_message", "nextBlock": "ask_again", "message": "Light rain, 20 degrees"},
        {"id": "time_response", "type": "send_message", "nextBlock": "ask_again"},
        {"id": "end", "type": "end"}
    ]
}
```

#### POST /createConfig
```bash
curl -X POST http://localhost:8081/createConfig \
-H "Content-Type: application/json" \
-d '{
  "start_block_id": "start",
  "blocks": [
    {"id": "start", "type": "send_message", "message": "hi", "nextBlock": "wait_response_1"}
  ]
}'
```
Response:
```json
{
    "id":"690e0f857a78f648d3427d9f",
    "start_block_id":"start",
    "blocks":[
        {
            "id":"start",
            "type":"send_message",
            "message":"hi",
            "nextBlock":"wait_response_1",
            "intents":null,
            "branches":null
        }
    ]
}
```

#### WebSocket endpoint `ws://localhost:8081/chat`
**Clien**t gets message with `/app/chat`, with argument message containtaing `{"content": 'message-text', "sender": 'user', "date": ''}`\
**Server** sends message with `/topic/message`
#### Example communcation using stomp.js
```javascript
const stompClient = new StompJs.Client({
    brokerURL: 'ws://localhost:8081/chat'
});

stompClient.onConnect = frame => {
    console.log('Connected ' + frame);
    
    stompClient.subscribe('/topic/message', val => {
        const message = JSON.parse(val.body).content;
    });
};

stompClient.publish({destination: '/app/chat', body: JSON.stringify({content: 'message-text', sender: 'user'})});
stompClient.activate();
```
