import React, { useState, Fragment } from "react";
import Stomp from "stompjs";
import Sockjs from "sockjs-client";

var stompClient = null;
var PrivateMessage = (props) => {
  const [user, setUser] = useState("");
  const [otherUser, setOtheruser] = useState("");
  const [broadCastMessage, setBroadCastMessage] = useState([]);
  const [value, setValue] = useState("");

  var connect = () => {
      console.log(user)
    var sockjs = new Sockjs("/ws");
    stompClient = Stomp.over(sockjs);
    stompClient.connect({ login: "sexpolo" }, onConnected);
  };

  var onConnected = () => {
    stompClient.subscribe(`/queue/${user}`, onMessageReceived);
    stompClient.send(
      "/app/addPrivateUser",
      {},
      JSON.stringify({ sender: user,receiver:otherUser })
    );
  };
  

  var onMessageReceived = (payload) => {
    var message = JSON.parse(payload.body);
    console.log("username="+user)
    console.log(message)
    if(message.receiver===user&&message.sender===otherUser){
      setBroadCastMessage((prev) => [...prev, message.content]);
    }
    // setBroadCastMessage((prev) => [...prev, message.content]);
  };

  var sendMessage = () => {
    var chatMessage = {
      content: value,
      sender: user,
      receiver: [otherUser],
      type: "TYPING",
    };
    console.log(user)
    console.log(otherUser)
    stompClient.send(
      `/user/sendPrivateMessage/${user}/${otherUser}`,
      {},
      JSON.stringify(chatMessage)
    );
    setBroadCastMessage((prev) => [...prev, chatMessage.content]);

  };

  return (
    <Fragment>
      <div>
        <input
          placeholder="username"
          value={user}
          onChange={(e) => setUser(e.target.value)}
        />
        <input
          placeholder="otherUser"
          value={otherUser}
          onChange={(e) => setOtheruser(e.target.value)}
        />

        <form>
          <h2>private message</h2>
          <input
            placeholder="type"
            value={value}
            onChange={(e) => setValue(e.target.value)}
          />
          <button type="button" onClick={sendMessage}>
            Send
          </button>
          <button type="button" onClick={connect}>
            connect
          </button>
        </form>
        <div>{broadCastMessage}</div>
      </div>
    </Fragment>
  );
};

export default PrivateMessage;
