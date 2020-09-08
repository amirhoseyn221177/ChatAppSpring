import React, { useState, Fragment, useEffect } from "react";
import Stomp from "stompjs";
import { withRouter } from "react-router-dom";
import { connect } from 'react-redux'
import Axios from "axios";

var stompClient = null;
var PrivateMessage = (props) => {
  const [user, setUser] = useState("");
  const [otherUser, setOtheruser] = useState("");
  const [broadCastMessage, setBroadCastMessage] = useState([]);
  const [value, setValue] = useState("");
  const [token, setToken] = useState("")
  const [file, setFile] = useState("")
  const [isItFile, setIsItFile] = useState(false)
  
  useEffect(() => {
    grabbingToken()
    //eslint-disable-next-line
  }, [])
  var grabbingToken = () => {
    let token = localStorage.getItem('token')
    setToken(token)
  }
  var connect = () => {
    console.log(21)
    console.log(token)
    stompClient = Stomp.client("ws://localhost:8080/ws");
    stompClient.connect({ Authorization: 'bearer ' + token }, onConnected, onError);

    //I controle these from the backend
    stompClient.heartbeat.outgoing = 0
    stompClient.heartbeat.incoming = 0
  };

  var onError = (e) => {
    console.error(e)
  }

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
    console.log(52)
    stompClient.subscribe(`/queue/user.${user}`, onMessageReceived, {
      "durable": false, "exclusive": false, "auto-delete": true, "x-dead-letter-exchange": "dead-letter-" + user,
      "x-message-ttl": 360000000,
    });
    stompClient.send(
      `/app/addPrivateUser/${user}`,
      { exchangeName: compareNamesAlphabetically(user, otherUser) },
      JSON.stringify({ sender: user, receiver: otherUser })
    );
  };

  var onMessageReceived = (payload) => {
    var message = JSON.parse(payload.body);
    console.log(message)
    if (message.contentType === "error") {
      diconnecting()
      console.log(message.contentType)
    }
    if(message.contentType==="media") setBroadCastMessage(prev=>[...prev, message.mediaContent])
    setBroadCastMessage((prev) => [...prev, message.textContent]);
  };

  var sendMessage = async () => {
    let AWSUrl = null;
    var chatMessage = {
      textContent: value,
      sender: user,
      receiver: otherUser,
      mediaContent: null,
      contentType: null
    };
    if (isItFile) {
      const resp = await Axios.get('/restchat/presignedurl', { headers: { "Authorization": 'bearer ' + token } });
      const data = await resp.data
      console.log(data)
      const AWSResp = await Axios.put(data.link, file[0])
      console.log(AWSResp)
      AWSUrl = AWSResp.config.url
      chatMessage.contentType= "media"
      chatMessage.mediaContent= AWSUrl
    } 

    var url = `/user/sendPrivateMessage/${user}`;
    stompClient.send(url, { wow: "sending" }, JSON.stringify(chatMessage));
  };

  var gettingFile = (e) => {
    let content = e.target.files
    setFile(content)
    setIsItFile(true)
  }

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
          <input type='file' name='image' accept="image/*" onChange={e => gettingFile(e)} />
          <button type="button" onClick={diconnecting}>disconnect</button>
        </form>
        <div>{broadCastMessage}</div>
      </div>
    </Fragment>
  );
};

const maptoState = state => {
  return {
    auth: state.auth.token
  }
}

export default withRouter(connect(maptoState)(PrivateMessage));
