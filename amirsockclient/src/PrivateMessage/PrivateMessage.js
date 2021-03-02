import React, { useState, Fragment, useEffect } from "react";
import { withRouter } from "react-router-dom";
import { connect } from 'react-redux'


var PrivateMessage = (props) => {
  const [user, setUser] = useState(props.match.params.username);
  const [otherUser, setOtheruser] = useState(props.match.params.otheruser);
  // const [broadCastMessage, setBroadCastMessage] = useState([]);
  const [value, setValue] = useState("");
  // const [file, setFile] = useState("")
  // const [isItFile, setIsItFile] = useState(false)
  const [socket, setSocket] = useState(null)
  useEffect(() => {
    let socket = new WebSocket(`ws://10.0.0.8:8080/ws/${user}`)
    setSocket(socket)
    //eslint-disable-next-line
  }, [])







    if (socket !== null) {
      socket.onmessage = (e) => {
        console.log(e)

      }
      socket.onopen = () => {
        var chatMessage = {
          textContent: "we are connected and authorized",
          sender: user,
          receiver:otherUser,
          mediaContent: null,
          contentType: null,
          token: "bearer " + localStorage.getItem("token")
        };

        socket.send(JSON.stringify(chatMessage))
      }
    }else{
      console.log("socket is null")
    }




  // var onError = (e) => {
  //   console.error(e)
  // }

  var SendingMessages = () => {
      var chatMessage = {
      textContent: value,
      sender: user,
      receiver: otherUser,
      mediaContent: null,
      contentType: null,
      token: "bearer " + localStorage.getItem("token")
    };
    socket.send(JSON.stringify(chatMessage))
  }

  // var diconnecting = () => {
  //   stompClient.disconnect(() => {
  //     stompClient.unsubscribe(`/queue/user.${user}`);
  //   });
  // };

  // var compareNamesAlphabetically = (name, name1) => {
  //   let fullName;
  //   if (name > name1) {
  //     fullName = name + "_" + name1;
  //   } else {
  //     fullName = name1 + "_" + name;
  //   }
  //   return fullName;
  // }

  // var onConnected = () => {
  //   stompClient.subscribe(`/queue/user.${user}`, onMessageReceived, {
  //     "durable": false, "exclusive": false, "auto-delete": true, "x-dead-letter-exchange": "dead-letter-" + user,
  //     "x-message-ttl": 360000000,
  //   });
  //   stompClient.send(
  //     `/app/addPrivateUser/${user}`,
  //     { exchangeName: compareNamesAlphabetically(user, otherUser) },
  //     JSON.stringify({ sender: user, receiver: otherUser })
  //   );
  // };

  // var onMessageReceived = (payload) => {
  //   var message = JSON.parse(payload.body);
  //   console.log(message)
  //   if (message.contentType === "error") {
  //     diconnecting()
  //     console.log(message.contentType)
  //   }
  //   if(message.contentType==="media") setBroadCastMessage(prev=>[...prev, message.mediaContent])
  //   setBroadCastMessage((prev) => [...prev, message.textContent]);
  // };

  // var sendMessage = async () => {
  //   let AWSUrl = null;
  //   var chatMessage = {
  //     textContent: value,
  //     sender: user,
  //     receiver: otherUser,
  //     mediaContent: null,
  //     contentType: null
  //   };
  //   if (isItFile) {
  //    AWSUrl= uploadingToS3()
  //     chatMessage.contentType= "media"
  //     chatMessage.mediaContent= AWSUrl
  //   } 

  //   var url = `/user/sendPrivateMessage/${user}`;
  //   stompClient.send(url, { wow: "sending" }, JSON.stringify(chatMessage));
  // };

  // var uploadingToS3=async()=>{
  //   const resp = await Axios.get('/restchat/presignedurl', { headers: { "Authorization": 'bearer ' + token } });
  //   const data = await resp.data
  //   console.log(data)
  //   const AWSResp = await Axios.put(data.link, file[0])
  //   console.log(AWSResp)
  //   return AWSResp.config.url

  // }

  // var gettingFile = (e) => {
  //   let content = e.target.files
  //   let fileSize=content[0].size
  //   console.log(fileSize)
  //   if(fileSize>5*Math.pow(10,9)){
  //     console.log("its too big my guys")
  //   }else{
  //     setFile(content)
  //     setIsItFile(true)
  //   }

  // }


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
          <button type="button" onClick={SendingMessages}>
            Send
          </button>

          {/* <input type='file' name='image' accept="image/*" onChange={e => gettingFile(e)} /> */}
          {/* <button type="button" onClick={diconnecting}>disconnect</button> */}
        </form>
        {/* <div>{broadCastMessage}</div> */}
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
