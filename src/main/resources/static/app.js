document.getElementById('submit-user').addEventListener('click', target => {
    const name = document.getElementById('username').value;
    fetch('http://localhost:8081/addUser', {
        method: 'POST',
        headers: {
            "Content-Type": "application/json",
            "Access-Control-Allow-Origin": "*"
        },
        body: JSON.stringify({
            name
        })
    })
    .then(res => res.json())
    .then(res => console.log(res))
});