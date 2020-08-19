import React, { useState, Fragment } from "react";
import Stomp from "stompjs";
import { withRouter } from "react-router-dom";

var stompClient = null;
var PrivateMessage = (props) => {
  const [user, setUser] = useState("");
  const [otherUser, setOtheruser] = useState("");
  const [broadCastMessage, setBroadCastMessage] = useState([]);
  const [value, setValue] = useState("");

  var connect = () => {
    stompClient = Stomp.client("ws://localhost:8080/ws");
    stompClient.connect({}, onConnected);

    //I controle these from the backend
    // stompClient.heartbeat.outgoing=20000
    // stompClient.heartbeat.incoming=0
  };

  var diconnecting = () => {
    stompClient.disconnect(() => {
      stompClient.unsubscribe(`/queue/user.${user}`);
    });
  };

  var compareNamesAlphabetically = (name, name1) => {
    let fullName;
    if (name > name1) {
      fullName = name + "_" + name1;
    } else {
      fullName = name1 + "_" + name;
    }
    return fullName;
  }

  var onConnected = () => {
    console.log(38)
    stompClient.subscribe(`/queue/user.${user}`, onMessageReceived, {
      "durable": false, "exclusive": false, "auto-delete": true, "x-dead-letter-exchange": "dead-letter-" + user,
      "x-message-ttl":3600000,
    });
    stompClient.send(
      `/app/addPrivateUser/${user}`,
      { exchangeName:compareNamesAlphabetically(user,otherUser) },
      JSON.stringify({ sender: user, receiver: otherUser })
    );
  };

  var onMessageReceived = (payload) => {
    var message = JSON.parse(payload.body);
    console.log(message)
    setBroadCastMessage((prev) => [...prev, message.textContent]);
  };

  var sendMessage = () => {
    var chatMessage = {
      textContent: value,
      sender: user,
      receiver: otherUser,
    };
    var url = `/user/sendPrivateMessage/${user}`;
    stompClient.send(url, { wow: "sending" }, JSON.stringify(chatMessage));
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
          <button type="button" onClick={diconnecting}>disconnect</button>
        </form>
        <div>{broadCastMessage}</div>
      </div>
    </Fragment>
  );
};

export default withRouter(PrivateMessage);
