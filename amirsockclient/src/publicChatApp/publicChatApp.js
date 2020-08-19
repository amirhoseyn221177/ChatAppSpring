import React, { useState, Fragment } from "react";
import Stomp from "stompjs";
import { withRouter } from "react-router-dom";
var stompClient = null;

const PublicMessage = (props) => {
  const [user, setUser] = useState("");
  const [broadCastMessage, setBroadCastMessage] = useState([]);
  const [grouChatName, setGroupChatName] = useState("");
  const [value, setValue] = useState("");
  const [file, setFile] = useState(null);
  const [contentType, setContentType] = useState("text")

  var connect = (username) => {
    if (username) {
      stompClient = Stomp.client("ws://localhost:8080/ws");
      stompClient.connect(
        { groupName: grouChatName, username: user },
        onConnected,
        onError
      );
    }
  };

  var onConnected = () => {
    stompClient.subscribe(`/queue/user.${user}`, onMessageReceived, {
      "durable": false, "exclusive": false, "auto-delete": true, "x-dead-letter-exchange": "dead-letter-" + user,
      "x-message-ttl": 3600000,
    });
    stompClient.send(
      "/app/addUser",
      { user: user },
      JSON.stringify({ sender: user, groupChat: grouChatName })
    );
  };

  var onError = (e) => {
    console.error(e);
  };

  var sendMessage = () => {
    let chatMassege = {
      sender: user,
      textContent: value,
      groupChat: grouChatName,
      contentType: contentType,
      mediaContent:file
    };
    stompClient.send(
      "/app/sendMessage",
      {},
      JSON.stringify(chatMassege)
    );
  };

  var diconnecting = () => {
    stompClient.disconnect(() => {
      stompClient.unsubscribe(`/topic/public/${grouChatName}`);
    });
  };

  var onMessageReceived = (payload) => {
    var message = JSON.parse(payload.body);
    setBroadCastMessage((prev) => [...prev, message.textContent]);
  };

  var gettingFiles = (e) => {
    setContentType("binary")
    let content = e.target.files;
    var reader = new FileReader();
    reader.onload = () => {
      var data = reader.result;
      setFile(data)
      //Converting base64 to byte array
      // var parts=data.split(";base64,")
      // parts[0].replace("data:","")
      // var data_64=parts[1]
      // var binary_String =window.atob(data_64)
      // var len =binary_String.length
      // var bytes=new Uint8Array(len)
      // for(let i=0 ; i<len;i++){
      //   bytes[i]=binary_String.charCodeAt(i)
    };
    reader.readAsDataURL(content[0]);

  };
  return (
    <Fragment>
      <div>
        <h2>messages</h2>
      </div>
      <input
        value={user}
        onChange={(e) => setUser(e.target.value)}
        placeholder="username"
      />
      <form>
        <input
          type="text"
          value={value}
          onChange={(e) => setValue(e.target.value)}
        />
        <input
          type="text"
          value={grouChatName}
          onChange={(e) => setGroupChatName(e.target.value)}
          placeholder="groupChatName"
        />
        <button type="button" onClick={sendMessage}>
          Send
        </button>
        <button type="button" onClick={connect}>
          connect
        </button>
        <button type="button" onClick={diconnecting}>
          disconnect
        </button>
      </form>
      <form>
        <input type="file" name="file" onChange={(e) => gettingFiles(e)} />
      </form>
      <div>
        {broadCastMessage.map((x) => (
          <p key={Math.random() * 120000}>{x}</p>
        ))}
      </div>
    </Fragment>
  );
};

export default withRouter(PublicMessage);
